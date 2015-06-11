// $Id: DrinkServer.java,v 1.3 2000/12/09 03:50:25 kukester Exp $
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: This is a simple wrapper that spawns the various
//              protocol-server threads, and initializes the system-wide
//              DrinkMachine and AcctMgr objects.
// Revisions:
//              $Log: DrinkServer.java,v $
//              Revision 1.3  2000/12/09 03:50:25  kukester
//              Abstracted drinkmachine class.
//
//              Revision 1.2  2000/12/06 09:05:06  grahams
//              Added CVS headers for all files, and improved basic admin functionality
//
//


import java.io.*;
import java.util.*;
import java.text.*;

public class DrinkServer {
    private static AcctMgr acctMgr;
    private static DrinkMachine drink;
    private static DrinkLogger log;

    public static void main( String args[] ) {
        try {
            drink = new TINIDrinkMachine("configfile");
        }
        catch( IOException e ) {
            System.err.println( "DrinkMachine: Error reading config file" );
            System.exit(1);
        }

        log = new DrinkLogger();
        acctMgr = new BasicAcctMgr(log);

        new SundayServer( drink, acctMgr, log, 4242 ).start();
        new FingerServer( drink, acctMgr, log ).start();
        System.out.println( "DrinkServer: All Daemons Spawned" );
        log.println( "DrinkServer: All Daemons Spawned" );
    }
}
