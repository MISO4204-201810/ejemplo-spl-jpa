/*
 * JPQL.java
 *
 * Copyright (c) 2008-2012 Edward Rayl. All rights reserved.
 *
 * JPQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPQL.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

import static cc.jpa.JPAUtil.getManagedClassTypes;
import static cc.jpa.JPAUtil.getProvider;
import static cc.jpa.JPQLCommon.printCause;
import static cc.jpa.JPQLCommon.printClasses;
import static cc.jpa.JPQLCommon.printDescription;
import static cc.jpa.JPQLIO.println;
import static cc.jpa.JPQLIO.print;
import static cc.jpa.JPQLIO.initIO;
import static cc.jpa.JPQLIO.readLine;
import static cc.jpa.JPQLIO.closeIO;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.ManagedType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JPQL Command Processor
 * 
 * JPQL supports EclipseLink, Hibernate, and OpenJPA. JPQLe is also available
 * with specific support for EclipseLink.
 * 
 * JPQL provides a command line execution environment for JPQL queries and
 * updates. It also provides support for SQL commands, and commands often found
 * in SQL command line tools. Type help at the jpql&gt; command line to get a
 * list of all commands.
 * 
 * @author Edward Rayl
 */

public class JPQL
{
    private static String puName = "EarthlingsPU";
    private static final String JPQL_VERSION = "1.0";
    private static Set<ManagedType<?>> managedClasses;
    private static ToStringStyle style = JPQLStyle.getInstance();
    private static Stack<String> queryBuffer = new Stack<String>();
    private static boolean multiline = true;

    /**
     * Processes command-line arguments and instantiates an entity manager.
     * Enters a command-processing loop to accept queries or commands.
     * Delegates to {@ #parseCommand parseCommand} to handle each command
     * when completed.
     *
     * @param args
     * Persistence unit name and -showsql are the two allowed parameters. If no
     * persistence unit is specified, then the internally defined name is used.
     * If the -showsql parameter is used, then SQL tracing is turned on.
     */
    public static void main(String[] args)
    {
        initIO();
        EntityManagerFactory emf = null;
        EntityManager em = null;
        String query = null;
        boolean showSQL = false;

        println("JPQL Generic Command Processor - Version " + JPQL_VERSION);
        println("Copyright Edward Rayl 2008-2012. All rights reserved.");
        println();

        switch (args.length)
        {
        case 0:
            break;
        case 1:
            if (args[0].equalsIgnoreCase("-showsql"))
                showSQL = true;
            else
                puName = args[0];
            break;
        case 2:
            if (args[0].equalsIgnoreCase("-showsql"))
            {
                showSQL = true;
                puName = args[1];
            }
            else if (args[1].equalsIgnoreCase("-showsql"))
            {
                showSQL = true;
                puName = args[0];
            }
            else
            {
                println("\tUse the optional parameter -showsql to show SQL output for JPQL");
            }
            break;

        default:
            println("JPQL accepts these optional parameters:");
            println("   [persistence unit name] [-showsql]");
            println("   Default persistence unit is " + puName + " and -showsql is false.");
            println("   -showsql gives detailed SQL logging for entity collection fields");
            println("   The default persistence unit can be changed in the source by assigning the variable: puName.");
            System.exit(0);
        }
        println("Connection using " + puName + " ... ");

        // Change the default LOGGING_LEVEL so that we don't get extraneous
        // information messages to our command line
        Map<String, String> properties = null;
        if (showSQL)
        {
            properties = new HashMap<String, String>();
            properties.put("eclipselink.logging.level", "SEVERE");
            properties.put("eclipselink.logging.level.sql", "FINE");
            properties.put("eclipselink.logging.timestamp", "OFF");
            properties.put("eclipselink.logging.session", "OFF");
            properties.put("eclipselink.logging.connection", "OFF");
            properties.put("openjpa.Log", "DefaultLevel=WARN, Runtime= WARN, Tool=WARN, SQL=TRACE");
            properties.put("hibernate.show_sql", "true");
            properties.put("hibernate.format_sql", "true");
            properties.put("hibernate.use_sql_comments", "true");
        }

        try
        {
            if (showSQL)
                emf = Persistence.createEntityManagerFactory(puName, properties);
            else
                emf = Persistence.createEntityManagerFactory(puName);
            em = emf.createEntityManager();
            managedClasses = getManagedClassTypes(emf);
            println("Persistence provider: " + getProvider(em));
            println();
        }
        catch (final PersistenceException pe)
        {
            Throwable cause = pe.getCause();
            while (cause != null)
            {
                if (cause instanceof SQLNonTransientConnectionException)
                {
                    printDatabaseException((SQLNonTransientConnectionException) cause);
                    System.exit(1);
                }
                cause = cause.getCause();
            }
            final String message = pe.getMessage();
            println(message);
            if (message.contains("No Persistence provider"))
            {
                println("Please run JPQL again followed with the name of the persistence unit.");
                println("The persistence unit name is typically found in META-INF/persistence.xml.");
            }
            System.exit(2);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            System.exit(2);
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
                            loop = parseCommand(em, query);
                            query = null;
                            prompt = "jpql> ";
                        }
                        else
                            prompt = "    > ";
                    else
                    {
                        loop = parseCommand(em, queryLine);
                        query = null;
                        prompt = "jpql> ";
                    }
                }
            }
            while (loop);
            closeIO();
            if (em != null)
                em.close();
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
    private static boolean parseCommand(EntityManager em, String query)
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
                    final List<?> results = em.createQuery(query).getResultList();
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
                    final List<?> results = em.createNativeQuery(query).getResultList();
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
                    final EntityTransaction et = em.getTransaction();
                    try
                    {
                        et.begin();
                        final int count = em.createQuery(query).executeUpdate();
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
    private static void printDatabaseException(SQLException se)
    {
        println("Database Error");
        println("ErrorCode: " + se.getErrorCode());
        println("DatabaseErrorCode: " + se.getErrorCode());
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
        println("Starting JPQL:");
        println("Run JPQL followed with the name of the persistence unit");
        println("\tto use a peristence unit other than " + puName);
        println("Use the optional parameter -showsql to show SQL output for JPQL");
        println();
        println("Running JPQL:");
        println("Type a JPQL command");
        println(" or type SQL followed by a SQL command");
        println(" or type LIST to print the last command");
        println(" or type HISTORY to print the command history");
        println(" or type CLEAR to clear command history");
        println(" or type DESCRIBE <class name> to print just the fields of the class");
        println(" or type DESCRIBE ALL <class name> to print all members and annotations");
        println(" or type SHOW ENTITIES to show all entities ordered by entity name");
        println(" or type SHOW ENTITIES PACKAGE to show all entities and their packages");
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
        else if (results instanceof Object[])
        {
            for (final Object o : (Object[]) results)
                printResults(o);
            println();
        }
        else
            println(ReflectionToStringBuilder.toString(results, style));
    }
}
