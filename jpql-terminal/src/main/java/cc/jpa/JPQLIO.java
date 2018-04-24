/*
 * JPQLIO.java
 *
 * Copyright (c) 2012 Edward Rayl. All rights reserved.
 *
 * JPQLIO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPQLIO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPQL.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.jpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * JPQL input/output 
 * 
 * Isolates input and output streams for the JPQL tool, defaulting to
 * standard I/O but also making it easy to redirect, multiplex, etc.
 *
 * In this variant, we check a system property cc.jpa.JPQL.logFilename
 * and if a log filename is set we use the {@link Duplex} class to 
 * duplicate output to the system output stream and to the file.
 * 
 * @author Edward Rayl
 */
public class JPQLIO
{
    private static Duplex out = new Duplex(System.out, null);
    static BufferedReader in = null;

    /**
     * Set our input reader to a buffered reader over standard input.
     * Set our output to a {@link Duplex} instance, either simply passing
     * through to standard output or echoing content to a log file if 
     * configured.
     */
    static void initIO()
    {
        in = new BufferedReader(new InputStreamReader(System.in));
        String logFilename = System.getProperty("cc.jpa.JPQL.logFilename");
        if (logFilename != null && logFilename.length() != 0)
        try
        {
            FileWriter log = new FileWriter(logFilename);
            out = new Duplex(System.out, new PrintWriter(log));
            System.out.println("Logging all output to " + logFilename);
        }
        catch (IOException ex)
        {
            println("Couldn't open log file for writing: " + logFilename);
        }
    }

    /**
     * Closes input and output channels.
     */
    static void closeIO() throws IOException
    {
        in.close();
        out.close();
    }

    /**
     * Pass through to our configured output channel.
     */
    static void print(String s)
    {
        out.print(s);
    }

    /**
     * Pass through to our configured output channel.
     */
    static void println()
    {
        out.println();
    }

    /**
     * Pass through to our configured output channel.
     */
    static void println(String s)
    {
        out.println(s);
    }

    /**
     * Read a text line from our configured input channel, and trim whitespace.
     */
    static String readLine() throws IOException
    {
        return in.readLine().trim();
    }
}
