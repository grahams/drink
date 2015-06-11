
// $Id$
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: A basic logger for the drink software.  Far more useful than
//              writing to the console (especially on TINI hardware, since
//              System.out.println is so expensive).
// Revisions:
//              $Log$
//


import java.io.*;
import java.util.*;

public class DrinkLogger {
    private static String filename   = new String( "log" );

    public DrinkLogger() {
        PrintWriter out = null;

        try {
            out = new PrintWriter( new FileWriter(filename, true) );
        }
        catch( FileNotFoundException ex ) {
            System.err.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }
        catch( IOException ex ) {
            System.err.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }

        out.println( "=====================================================" );
        out.close();
    }

    public synchronized void println( String message ) {
        PrintWriter out = null;

        try {
            out = new PrintWriter( new FileWriter(filename, true) );
        }
        catch( FileNotFoundException ex ) {
            System.err.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }
        catch( IOException ex ) {
            System.err.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }

        out.println( new Date() + ": " + message );
        out.close();
    }
}
