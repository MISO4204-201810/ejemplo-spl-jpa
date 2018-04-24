/*
 * JPQLe.java
 *
 * Copyright (c) 2008-2012 Edward Rayl. All rights reserved.
 *
 * JPQLe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPQLe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPQLe.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

import static cc.jpa.JPAUtil.getManagedClassTypes;
import static cc.jpa.JPQLCommon.printCause;
import static cc.jpa.JPQLCommon.printClasses;
import static cc.jpa.JPQLCommon.printDescription;
import static cc.jpa.JPQLIO.println;
import static cc.jpa.JPQLIO.print;
import static cc.jpa.JPQLIO.initIO;
import static cc.jpa.JPQLIO.readLine;
import static cc.jpa.JPQLIO.closeIO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.ManagedType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.persistence.Version;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.ProfilerType;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.server.ServerSession;

/**
 * JPQL Command Processor 
 * 
 * JPQLe provides a command line execution environment for JPQL queries and
 * updates specific to EclipseLink. It also provides support for SQL commands, and commands often
 * found in SQL command line tools. Type help at the jpql&gt; command line to get a list of all
 * commands.
 * 
 * @author Edward Rayl
 */

public class JPQLe
{
    private static final int LINE_SIZE = 80;
    private static final int TAB_SIZE = "\t".length();
    private static final String JPQL_VERSION = "1.0";
    private static Set<ManagedType<?>> managedClasses;
    private static String puName;
    private static ToStringStyle style = JPQLStyle.getInstance();
    private static boolean multiline = true;

    /**
     * Processes command-line arguments and instantiates an entity manager.
     * Enters a command-processing loop to accept queries or commands.
     * Delegates to {@ #parseCommand parseCommand} to handle each command
     * when completed.
     *
     * @param args
     *            Persistence unit name and -showsql are the two allowed parameters. If no
     *            persistence unit is specified, JPQLe will search for all available persistence
     *            units. If only one is found, it will be used. If several are found, JPQLe will
     *            quit, explaining that one persistence unit must chosen. If the -showsql parameter
     *            is used, then SQL tracing is turned on.
     */
    @SuppressWarnings("deprecation")
	public static void main(String[] args)
    {
        initIO();
        EntityManagerFactory emf = null;
        EntityManagerFactoryImpl emfi;
        // JpaEntityManagerFactory emfi = null;
        JpaEntityManager jem = null;
        ServerSession session = null;
        String query = null;
        final Stack<String> queryBuffer = new Stack<String>();
        boolean showSQL = false;
        boolean queryMonitor = false;
        boolean performanceProfiler = false;

        println("JPQLe Command Processor - Version " + JPQL_VERSION);
        println("Copyright Edward Rayl 2008-2012. All rights reserved.");
        println(Version.getProduct() + ": " + Version.getVersion());
        switch (args.length)
        {
        case 0:
            // Scan for all PUs
            puName = scanForPU();
            break;
        case 1:
            if (args[0].equalsIgnoreCase("-showsql"))
            {
                showSQL = true;
                puName = scanForPU();
            }
            else if (args[0].equalsIgnoreCase("-performanceprofiler"))
            {
                performanceProfiler = true;
                puName = scanForPU();
            }
            else if (args[0].equalsIgnoreCase("-querymonitor"))
            {
                queryMonitor = true;
                puName = scanForPU();
            }
            else
                puName = args[0];
            break;
        case 2:
            if (args[0].equalsIgnoreCase("-showsql"))
            {
                showSQL = true;
                puName = args[1];
            }

            else if (args[0].equalsIgnoreCase("-performanceprofiler"))
            {
                performanceProfiler = true;
                puName = args[1];
            }
            else if (args[0].equalsIgnoreCase("-querymonitor"))
            {
                queryMonitor = true;
                puName = args[1];
            }
            else if (args[1].equalsIgnoreCase("-showsql"))
            {
                showSQL = true;
                puName = args[0];
            }
            else if (args[0].equalsIgnoreCase("-performanceprofiler"))
            {
                performanceProfiler = true;
                puName = args[0];
            }
            else if (args[0].equalsIgnoreCase("-querymonitor"))
            {
                queryMonitor = true;
                puName = args[0];
            }
            break;
        default:
            println("Usage:");
            println("jpql [persistence unit name] [-showsql | -performanceprofiler | -querymonitor]");
            println("   -showsql               detailed SQL logging for entity collection fields");
            println("   -performanceprofiler   show performance statistics for every executed query");
            println("   -querymonitor          monitor query executions and cache hits");
            println("   Only one of the optional parameters above can be used at a time");
            System.exit(0);
        }

        final Map<String, String> properties = new HashMap<String, String>();
        if (showSQL)
        {
            // Show SQL detailed logging for entity collection fields
            properties.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.SEVERE_LABEL);
            properties.put(PersistenceUnitProperties.CATEGORY_LOGGING_LEVEL_ + SessionLog.SQL, SessionLog.FINE_LABEL);
            properties.put(PersistenceUnitProperties.LOGGING_TIMESTAMP, SessionLog.OFF_LABEL);
            properties.put(PersistenceUnitProperties.LOGGING_SESSION, SessionLog.OFF_LABEL);
            properties.put(PersistenceUnitProperties.LOGGING_CONNECTION, SessionLog.OFF_LABEL);
        }
        // Profiling options:
        // See
        // http://wiki.eclipse.org/Optimizing_the_EclipseLink_Application_(ELUG)
        // and http://wiki.eclipse.org/Using_EclipseLink_JPA_Extensions_(ELUG)
        else if (performanceProfiler)
            // Performance Profiler
            properties.put(PersistenceUnitProperties.PROFILER, ProfilerType.PerformanceProfiler);
        else if (queryMonitor)
            // Query Monitor
            properties.put(PersistenceUnitProperties.PROFILER, ProfilerType.QueryMonitor);
        else
            // Change the default LOGGING_LEVEL so that we don't get extraneous
            // information messages to our command line
            properties.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.OFF_LABEL);

        try
        {
            emf = Persistence.createEntityManagerFactory(puName, properties);
            emfi = JpaHelper.getEntityManagerFactory((EntityManagerFactoryImpl) emf);
            jem = (JpaEntityManager) emfi.createEntityManager();
            managedClasses = getManagedClassTypes(emfi);
            session = emfi.getServerSession();
            println();
            println("Connected using " + puName);
            println();
        }
        catch (final PersistenceException pe)
        {
            printCause(pe);
            System.exit(1);
        }
        catch (final DatabaseException de)
        {
            printDatabaseException(de);
            System.exit(2);
        }
        catch (final IllegalArgumentException iae)
        {
            printCause(iae);
            System.exit(3);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            System.exit(4);
        }
        try
        {
            boolean loop = true;
            boolean activeMultiline = false;
            String prompt = "jpql> ";
            do
            {
                print(prompt);
                final String queryLine = readLine();
                if (queryLine.length() != 0)
                {
                    if (query == null)
                        query = queryLine;
                    else
                        query += " " + queryLine;
                    if (multiline && (queryLine.toLowerCase().startsWith("insert")
                        || queryLine.toLowerCase().startsWith("sql ")
                        || queryLine.toLowerCase().startsWith("select")))
                    {
                        activeMultiline = true;
                    }
                    if (activeMultiline)
                        if (queryLine.endsWith(";"))
                        {
                            activeMultiline = false;
                            loop = parseCommand(jem, session, query, queryBuffer);
                            query = null;
                            prompt = "jpql> ";
                        }
                        else
                            prompt = "    > ";
                    else
                    {
                        loop = parseCommand(jem, session, query, queryBuffer);
                        query = null;
                        prompt = "jpql> ";
                    }
                }
            }
            while (loop);
            closeIO();
            if (jem != null)
                jem.close();
            if (emf != null)
                emf.close();
            println("Disconnected from " + puName);
        }
        catch (final NullPointerException npe)
        {
            npe.printStackTrace();
        }
        catch (final Exception e)
        {
            println("Query: " + query);
            if (e.getMessage() != null)
                println(e.getMessage());
            else
                printCause(e);
            println();
        }
    }

    /**
     * Primary command processor. Recognizes commands and carries them out,
     * or processes JPQL queries and shows results. Refuses to process
     * JPQL inserts.
     */
    @SuppressWarnings("unchecked")
    private static boolean parseCommand(JpaEntityManager jem, ServerSession session, String query, Stack<String> queryBuffer)
    {
        String className = null;

        // Ignore semicolons at the end of the query
        if (query.endsWith(";"))
            query = query.substring(0, query.length() - 1).trim();

        // Help
        if (query.equalsIgnoreCase("help"))
        {
            printHelp();
            return true;
        }
        // Prevent attempts to insert
        else if (query.toLowerCase().startsWith("insert") || 
                 query.toLowerCase().startsWith("sql insert"))
        {
            println("You cannot do an insert from either JPQL or as a native SQL query");
            println();
        }
        // List last query
        else if (query.equalsIgnoreCase("list"))
        {
            if (queryBuffer.size() > 0)
                println(queryBuffer.peek());
            else
                println("Buffer is empty");
        }
        // History - list all queries
        else if (query.toLowerCase().startsWith("hist"))
        {
            if (queryBuffer.size() > 0)
                for (final String s : queryBuffer)
                    println(s);
            else
                println("Buffer is empty");
        }
        // Clear history
        else if (query.toLowerCase().startsWith("clea"))
        {
            queryBuffer.clear();
        }
        // Set Multiline
        else if (query.toLowerCase().startsWith("mult"))
        {
            final String[] arguments = query.split("\\s");
            switch (arguments.length)
            {
            case 1:
                println("No arguments specified");
                break;
            case 2:
                if (arguments[1].equalsIgnoreCase("on") || 
                    arguments[1].equalsIgnoreCase("true"))
                {
                    multiline = true;
                    println("Multiline on");
                }
                else if (arguments[1].equalsIgnoreCase("off") || 
                         arguments[1].equalsIgnoreCase("false"))
                {
                    multiline = false;
                    println("Multiline off");
                }
                else
                    println("Wrong argument specified");
                break;
            default:
                println("Wrong arguments specified");
            }

        }
        else if (query.length() != 0 && 
                (query.equalsIgnoreCase("quit") || 
                 query.equalsIgnoreCase("exit")))
            return false;
        else
            try
            {
                // JPQL select
                if (query.toLowerCase().startsWith("sele"))
                {
                    queryBuffer.add(query);
                    final List<?> results = jem.createQuery(query).getResultList();
                    final DatabaseQuery dbQuery = ((JpaQuery<?>) jem.createQuery(query)).getDatabaseQuery();
                    dbQuery.prepareCall(session, new DatabaseRecord());
                    // getSQLStrings() does not have a generic version
                    for (final String sqlQuery : (List<String>) dbQuery.getSQLStrings())
                        println(wrapString(sqlQuery));
                    println();
                    if (results.size() > 0)
                    {
                        for (final Object o : results)
                            printResults(o);
                        println(results.size() + " results returned");
                    }
                    else
                        println("0 results returned");
                    println();
                }
                // SQL select
                else if (query.toLowerCase().startsWith("sql"))
                {
                    queryBuffer.add(query);
                    query = query.substring(3).trim();
                    final List<?> results = jem.createNativeQuery(query).getResultList();
                    if (results.size() > 0)
                    {
                        for (final Object o : results)
                            printResults(o);
                        println(results.size() + " results returned");
                    }
                    else
                        println("0 results returned");
                    println();

                }
                // Describe class
                else if (query.toLowerCase().startsWith("desc"))
                {
                    final String[] arguments = query.split("\\s");
                    switch (arguments.length)
                    {
                    case 1:
                        println("No arguments specified");
                        break;
                    case 2:
                        className = arguments[1];
                        printDescription(className, false, managedClasses);
                        break;
                    case 3:
                        className = arguments[2];
                        if (arguments[1].matches("(ALL|all)"))
                            printDescription(className, true, managedClasses);
                        else
                            println("Incorrect argument specified");
                        break;
                    default:
                        println("Wrong number of arguments specified");
                    }
                }
                // Show named queries
                else if (query.toLowerCase().startsWith("show quer"))
                {
                    final TreeSet<String> sorted = new TreeSet<String>(session.getQueries().keySet());

                    final String[] arguments = query.split("\\s");
                    switch (arguments.length)
                    {
                    case 2:
                        className = "";
                        break;
                    case 4:
                        if (arguments[2].matches("(FOR|for)"))
                            className = arguments[3];
                        break;
                    default:
                        println("Wrong number of arguments specified");
                    }

                    if (className != null)
                    {
                        for (final String key : sorted)
                        {
                            if (key.indexOf(className) >= 0)
                            {
                                println(key + " = " + session.getQuery(key).getEJBQLString());
                                final DatabaseQuery dbQuery = ((JpaQuery<?>) jem.createNamedQuery(key)).getDatabaseQuery();
                                dbQuery.prepareCall(session, new DatabaseRecord());
                                println(wrapString(dbQuery.getSQLString()));
                                println();
                            }
                        }
                    }
                }
                // Show classes
                else if (query.toLowerCase().startsWith("show ent"))
                {
                    final String[] arguments = query.split("\\s");
                    switch (arguments.length)
                    {
                    case 2:
                        printClasses(false, managedClasses);
                        break;
                    case 3:
                        if (arguments[2].matches("(PACK.*|pack.*)"))
                            printClasses(true, managedClasses);
                        else
                            println("Wrong arguments specified");
                        break;
                    default:
                        println("Wrong number of arguments specified");
                    }

                }
                // Perform an update, delete, or other JPQL command
                else
                {
                    queryBuffer.add(query);
                    final EntityTransaction et = jem.getTransaction();
                    try
                    {
                        et.begin();
                        final int count = jem.createQuery(query).executeUpdate();
                        et.commit();
                        switch (count)
                        {
                        case 0:
                            println("No entities affected");
                            break;
                        case 1:
                            println("One entity affected");
                            break;
                        default:
                            println(count + " entities affected");
                        }
                    }
                    finally
                    {
                        if (et != null && et.isActive())
                        {
                            et.rollback();
                            println();
                        }
                    }
                }
            }
            catch (final ClassNotFoundException cnfe)
            {
                println("Class " + className + " not found");
            }
            catch (final NumberFormatException nfe)
            {
                if (nfe.getMessage() != null)
                    println(nfe.getMessage());
                else
                    printCause(nfe);
            }
            catch (final IllegalArgumentException iae)
            {
                printCause(iae);
            }
            catch (final QueryException qe)
            {
                println(qe.getMessage());
            }
            catch (final DatabaseException de)
            {
                printDatabaseException(de);
            }
            catch (final PersistenceException de)
            {
                printCause(de);
            }
            catch (final Exception e)
            {
                if (e.getMessage() != null)
                    println(e.getMessage());
                else
                    printCause(e);
                println("JPQL received an unexpected or internal provider exception");
            }
        return true;
    }

    /**
     * Helper to produce a user-friendly representation of a database exception.
     */
    private static void printDatabaseException(DatabaseException se)
    {
        println("Database Error");
        println("ErrorCode: " + se.getErrorCode());
        println("DatabaseErrorCode: " + se.getDatabaseErrorCode());
        final String message = se.getMessage();

        final int start = message.indexOf("Call: ");
        final int end = message.indexOf("\n", start);
        if (start > -1 && end > start)
        {
            final String call = message.substring(start, end);
            println(call);
        }
        else
            println(message);
        // Add JDBC 4.0 cause when known
        final Throwable cause = se.getCause();
        if (cause != null)
        {
            println("Cause: " + cause);
        }
        println();
    }

    /**
     * Prints a help statement with a list of all recognized commands.
     */
    private static void printHelp()
    {
        println("Type a JPQL command");
        println(" or type SQL followed by a SQL command");
        println(" or type LIST to print the last command");
        println(" or type HISTORY to print the command history");
        println(" or type CLEAR to clear command history");
        println(" or type DESCRIBE <class name> to print just the fields of the class");
        println(" or type DESCRIBE ALL <class name> to print all members and annotations");
        println(" or type SHOW ENTITIES to show all entities ordered by entity name");
        println(" or type SHOW ENTITIES PACKAGE to show all entities and their packages");
        println(" or type SHOW QUERIES to show all named queries ordered by entity name");
        println(" or type SHOW QUERIES FOR 'xxx' to show all named queries containing 'xxx'");
        println(" or type MULTILINE ON (or TRUE) to use multiline mode*");
        println(" or type MULTILINE OFF (or FALSE) to use single line mode*");
        println("         Default mode is multi-line mode.");
        println(" or type QUIT or EXIT to exit.");
        println("All commands are case insensitive and require only the first four letters");
        println();
        println("* When MULTILINE is on (by default at startup), JPQL will wait for a");
        println("  semicolon character before processing any queries or commands.");
        println("  In single-line mode, it will process one line at a time, ignoring");
        println("  any ending semicolons, so all queries must be expressed on a single line.");
        println();
        println("  It is not ever necessary to follow LIST, HISTORY, CLEAR, DESCRIBE [ALL],");
        println("  SHOW ENTITIES [PACKAGE], MULTILINE ON | OFF, or QUIT commands with a semicolon");
        println();
    }

    /**
     * Produces formatted query results.
     */
    private static void printResults(Object results)
    {
        if (results == null)
            println("NULL");
        else if (results instanceof String)
            println("String:" + results + " ");
        else if (results instanceof Number || results instanceof Date)
            println(results.getClass().getSimpleName() + ":" + results + " ");
        else if (results instanceof NonSynchronizedVector)
        {
            for (final Object o : (NonSynchronizedVector) results)
                printResults(o);
            println();
        }
        else if (results instanceof Object[])
        {
            for (final Object o : (Object[]) results)
                printResults(o);
            println();
        }
        else
            println(ReflectionToStringBuilder.toString(results, style));
    }

    /**
     * Scans for known persistence units using an EclipseLink-proprietary API.
     * If it finds only one unit, and it declares EclipseLink as its provider,
     * returns the name of that unit. Otherwise, writes an informative 
     * message and exits the process.
     */
    private static String scanForPU()
    {
        String puName = null;
    
        final Set<org.eclipse.persistence.jpa.Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();
        final ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        for (final org.eclipse.persistence.jpa.Archive archive : archives)
        {
            final List<SEPersistenceUnitInfo> infos = PersistenceUnitProcessor.getPersistenceUnits(archive, threadLoader);
            if (infos.size() > 1)
            {
                println("More than one persistence unit was found.  Persistence units:");
                for (final SEPersistenceUnitInfo info : infos)
                {
                    println(info.getPersistenceUnitName());
                }
                println("Please run JPQLe again followed with the name of the persistence unit.");
                println("The persistence unit name is typically found in META-INF/persistence.xml.");
                System.exit(0);
            }
            else if (infos.get(0).getPersistenceProviderClassName() != null
                && !infos.get(0).getPersistenceProviderClassName().equals("org.eclipse.persistence.jpa.PersistenceProvider"))
            {
                println();
                println("The persistence provider is incorrectly specified.");
                println("Please make sure the following element is in the persistence.xml:");
                println("<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>");
                println("   or use the generic version of this tool, JPQL.");
                System.exit(0);
            }
            else
                puName = infos.get(0).getPersistenceUnitName();
        }
        return puName;
    }

    /**
     * Performs some formatting for purposes of producing diagnostic SQL
     * output.
     */
    private static String wrapString(String sqlQuery)
    {
        final String[] parts = sqlQuery.split(" ");
        String result = "\tSQL: ";
        int currentColumn = 1;
        for (final String piece : parts)
        {
            if (piece.equals("FROM") || piece.equals("GROUP") || piece.equals("HAVING") || piece.equals("ORDER")
                || piece.equals("WHERE"))
            {
                if (!result.endsWith("\t"))
                    result += "\n\t";
                result += piece + " ";
                currentColumn = piece.length() + TAB_SIZE;
            }
            else if (currentColumn >= LINE_SIZE)
            {
                result += piece + "\n\t\t";
                currentColumn = TAB_SIZE;
            }
            else
            {

                result += piece + " ";
                currentColumn += piece.length() + 1;
            }
        }
        return result;
    }
}
