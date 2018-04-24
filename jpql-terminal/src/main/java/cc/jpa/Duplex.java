/*
Copyright 2011 Will Provost.
All rights reserved by Capstone Courseware, LLC.
*/

package cc.jpa;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
Stand-in for System.out that passes output through to one or two destinations.
One is a PrintStream and this is expected to be System.out itself, or
possibly System.err; the other is a PrintWriter and it can be anything,
but most often will be a FileWriter to keep a record of application output.
The class only supports print() and println() methods accepting String
parameters, and println() with no arguments to print a blank line.

@author Will Provost
*/
public class Duplex
    //implements AutoCloseable -- Java 7 only
{
    private static String endLine = System.getProperty ("line.separator");
    
    private PrintStream console;
    private PrintWriter log;

    /**
    Build the object with one or two delegates; either one can be null.
    (Both can be null, too! perhaps to squelch all output.)
    */
    public Duplex (PrintStream console, PrintWriter log)
    {
        this.console = console;
        this.log = log;
    }
    
    /**
    Prints the given string to either or both destinations, as provided.
    */
    public void print (String output)
    {
        if (console != null)
            console.print (output);
        if (log != null)
            log.print (output);
    }
    
    /**
    Prints the given string to either or both destinations, as provided.
    */
    public void println (String output)
    {
        print (output);
        print (endLine);
    }
    
    /**
    Prints a blank line.
    */
    public void println ()
    {
        println ("");
    }
    
    /**
    */
    public void close ()
    {
        if (console != null)
            console.close ();
        if (log != null)
            log.close ();
    }
}
