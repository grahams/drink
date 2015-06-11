// $Id: SundayServer.java,v 1.16 2000/12/12 17:40:09 grahams Exp $
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: Implements the Drink Protocol as defined by Joe
//              Sunday (sunday@csh.rit.edu).  This protocol is much
//              like SMTP.
// Revisions: 
//              $Log: SundayServer.java,v $
//              Revision 1.16  2000/12/12  17:40:09  grahams
//              Removed the null from the end of each line sent from STAT
//
//              Revision 1.15  2000/12/11 16:15:36  grahams
//              Unmeaningful log message.
//
//              Revision 1.14  2000/12/09 21:54:31  grahams
//              fixed retardation in authentication.
//              exceptions now cause isValidUser to return false
//
//              Revision 1.13  2000/12/09 05:23:30  grahams
//              shutdown -r kills the client connection before it reboots now.
//
//              Revision 1.12  2000/12/09 05:16:02  grahams
//              Fixed the finger server:
//
//              Revision 1.11  2000/12/09 03:50:26  kukester
//              Abstracted drinkmachine class.
//
//              Revision 1.10  2000/12/09 03:30:00  grahams
//              Fixed a ton of silly-ass bugs
//
//              Revision 1.9  2000/12/08 21:04:08  grahams
//              Now authenticating against IMAP vs. POP3, and cleaned up
//              SundayServer a bit
//
//              Revision 1.8  2000/12/08 19:51:05  grahams
//              Fixed an inconsistent error message
//
//              Revision 1.7  2000/12/07 15:19:18  grahams
//              Cleaned up some of the status messages (to correspond with 
//              the Protocol documentation I just wrote), and also 
//              implemented the delay argument to the DROP command
//
//              Revision 1.6  2000/12/07 02:14:23  grahams
//              Added a socket timeout for clients, and server
//              is shutdownable (but that's a undocumented feature)
//
//              Revision 1.5  2000/12/06 09:05:06  grahams
//              Added CVS headers for all files, and improved basic 
//              admin functionality
//
//              Revision 1.4  2000/12/06 08:39:40  grahams
//              Added some rudimentary administrative features to the protocol
//
//              Revision 1.3  2000/12/06 07:12:35  grahams
//              un dos-ified all these files
//
//              Revision 1.2  2000/12/06 06:59:05  grahams
//              commented like a madman
//

import java.io.*;
import java.net.*;
import java.util.*;
import com.dalsemi.system.TINIOS;

/**
 * A class that implements Joe Sunday's Drink Protocol for the TINI-board
 * based Drink Server (although, in theory it is abstracted enough to be
 * used on anything that has a DrinkMachine class).
 * 
 * @see <a href="http://www.csh.rit.edu/projects/drink/Sunday.html">
 *       Sunday Protocol Spec</a>
 */
public class SundayServer extends Thread{
    private static final int MAXCLIENTS = 3;
    private static String banner = "Welcome to Drink, now go home.";

    // The listen socket
    private static ServerSocket listen = null;
    private static int clientCount = 0;

    private AcctMgr acctMgr;
    private DrinkMachine drink;
    private DrinkLogger log;

    /** 
     * Constructor.  Creates the listen socket
     *
     * @param inDrink   Reference to the Systemwide DrinKMachine object 
     * @param inAcctMgr Reference to the Systemwide AcctMgr object
     * @param inLog     Reference to the Systemwide DrinkLogger object
     * @param port      The port the server should listen on
     */
    public SundayServer(DrinkMachine inDrink, AcctMgr inAcctMgr, 
                        DrinkLogger inLog, int port) {
        acctMgr = inAcctMgr;
        log = inLog;

        try {
            drink = inDrink;
            listen = new ServerSocket(port);
        }
        catch ( IOException e ) {
            log.println( "SundayServer: Could not listen on port: " + port );
            System.exit(1);
        }

        // Simple debug logging
        log.println( "SundayServer: Listening on port: " + port );
            
    }

    /** 
     * The run method accepts client connections.  
     *
     * If MAXCLIENTS has not been reached, then it create a new
     * SundayClientThread, otherwise it prints an error message and
     * disconnect the client.  
     */
    public void run() {
        while(true) {
            Socket client = null;  // The socket to accept client connections

            try {
                client = listen.accept();

                if ( addClient() ) {
                    new SundayClientThread(drink,acctMgr,log, 
                                           client,this).start();
                }
                else {
                    PrintWriter out;        // The socket writer
                    out = new PrintWriter( client.getOutputStream(), true );

                    out.println( "ERR Maximum user count reached." );
                    out.close();
                    client.close();
                    client = null;
                }
            }
            catch( IOException e ) {
                log.println( "SundayServer: Accept Failed." );
                System.exit(1);
            }

        }
    }

    /** 
     * If MAXCLIENTS has not been reached, then increment clientCount and
     * return true, otherwise return false.
     */
    public synchronized boolean addClient() {
        if( clientCount < MAXCLIENTS ) {
            clientCount += 1;
            return true;
        }
        else {
            return false;
        }
    }
    
    /** 
     * If clientCount is not zero, then decrement clientCount and
     * return true, otherwise return false.
     */
    public synchronized boolean removeClient() {
        if( clientCount <= 0 ) {
            return false;
        }
        else {
            clientCount -= 1;
            System.gc();    // Manually invoke the Garbage Collector
            return true;
        }
    }

    /**
     * This inner class represents an individual client connection to the
     * server.  This thread persists while the client connection does.
     */
    public class SundayClientThread extends Thread {
        // Readers and Writers for the socket connection.
        private PrintWriter out;        // The socket writer
        private BufferedReader in;      // The socket reader

        // The account service object and the drink hardware object.
        private DrinkMachine drink;
        private AcctMgr acctMgr;
        private DrinkLogger log;
        private SundayServer server;

        // The socket on which the client resides.
        private Socket client;

        /** 
         * Constructor.  
         *
         * @param inDrink   Reference to the Systemwide DrinKMachine object 
         * @param inAcctMgr Reference to the Systemwide AcctMgr object
         * @param inLog     Reference to the Systemwide DrinkLogger object
         * @param inClient  Reference to the client socket connection
         * @param inServer  Reference to the SundayServer object
         */
        public SundayClientThread( DrinkMachine inDrink, AcctMgr inAcctMgr, 
                                   DrinkLogger inLog, Socket inClient,  
                                   SundayServer inServer ) {
            acctMgr = inAcctMgr;
            drink = inDrink;
            client = inClient;
            log = inLog;
            server = inServer;
        }

        /** 
         * The run method loops, processing user input and requests.  
         */
        public void run() {
            try {
                out = new PrintWriter( client.getOutputStream(), true );
                in = new BufferedReader( new InputStreamReader(
                                        client.getInputStream() ) );

                client.setSoTimeout( 120000 );
                String username = null;
                String password = null;
                boolean validUser = false;

                out.println( "OK " + banner );

                // while the user is babbling at us....
                while(true) {
                    String selection = in.readLine();

                    // if the selection is null, the user has gone walkabout
                    // on us.
                    if( selection == null ) {
                        break;
                    }

                    if( selection.toLowerCase().startsWith( "user" ) ) {
                        username = selection.substring(5);
                        out.println( "OK Password required." );

                        // Makes sure that you can't login, then change
                        // your username to steal from someone else's acct.
                        password = null;
                        validUser = false;
                    }
                    else if( selection.toLowerCase().startsWith( "pass" ) ) {
                        // make sure the 'user' command has been issued
                        if( username != null ) {
                            
                            if( selection.compareTo("pass") == 0 ) {
                                password = "";
                            }
                            else {
                                password = selection.substring(5);
                            }

                            if( ( username.compareTo("") != 0 ) &&
                                ( password.compareTo("") != 0 ) ) {
                                if(acctMgr.isValidUser(username, password)) {
                                    out.println( "OK Credits: " +
                                        acctMgr.getUserBalance(username) );
                                    validUser = true;
                                }
                                else {
                                    log.println( "SundayServer: "
                                                        + "Invalid User." );
                                    out.println( "ERR Invalid username or " + 
                                                    "password." );
                                    validUser = false;
                                }
                            }
                            else {
                                log.println( "SundayServer: "
                                                    + "Invalid User." );
                                out.println( "ERR Invalid username or " + 
                                                "password." );
                                validUser = false;
                            }
                        }
                        else {
                            out.println( 
                                "ERR USER command needs to be issued first." );
                        }
                    }
                    else if( selection.toLowerCase().startsWith( "stat" ) ) {
                        getStats(); 
                    }
                    else if( selection.toLowerCase().startsWith( "drop" ) ) {
                        if( validUser ) { 
                            int slot = -1;
                            int delay = 0;

                            StringTokenizer s = new StringTokenizer(
                                                    selection.substring(5));
                            if( s.countTokens() != 0 ) {
                                if( s.countTokens() == 1 ) {
                                    slot = 
                                        new Integer(s.nextToken()).intValue();
                                    delay = 0;
                                }
                                else if( s.countTokens() == 2 ) {
                                    slot = 
                                        new Integer(s.nextToken()).intValue();
                                    delay = 
                                        new Integer(s.nextToken()).intValue();
                                }

                                if( delay > 60 ) {
                                    delay = 60;
                                }

                                dropDrink( username, slot, delay );
                                break;
                            }
                            else {
                                out.println( "ERR Invalid syntax." );
                            }
                        }
                        else {
                            out.println( "ERR You need to login." );
                        }
                    }
                    else if( selection.toLowerCase().startsWith("edituser") ) {
                        if( validUser && acctMgr.isUserAdmin( username ) ) {
                            if(selection.toLowerCase().compareTo(
                                                        "edituser") != 0 ) {
                                editUser( selection );
                            }
                            else {
                                out.println( "OK Admin user." );
                            }
                        }
                        else {
                            out.println( "ERR Access denied." );
                        }
                    }
                    else if( selection.toLowerCase().startsWith("editslot") ) {
                        if( validUser && acctMgr.isUserAdmin( username ) ) {
                            if(selection.toLowerCase().compareTo(
                                                        "editslot") != 0 ) {
                                editSlot( selection );
                            }
                            else {
                                out.println( "ERR Syntax error." );
                            }
                        }
                        else {
                            out.println( "ERR Access denied." );
                        }
                    }
                    else if( selection.toLowerCase().startsWith("shutdown") ) {
                        if( validUser && acctMgr.isUserAdmin( username ) ) {
                            if(selection.toLowerCase().compareTo(
                                                    "shutdown") != 0 ) {
                                String cmd = selection.substring(9);
                                if( cmd.toLowerCase().startsWith("-r") ) {
                                    out.println( "OK Rebooting." );
                                    out.close();
                                    in.close();
                                    client.close();

                                    server.removeClient();

                                    try {
                                        Thread.sleep( 5000 );
                                    }
                                    catch( InterruptedException e ) {}
                                    drink.shutdown(true);
                                }
                                else {
                                    out.println( "ERR Invalid option." );
                                }
                            }
                            else {
                                out.println("OK Shutting down server.");
                                drink.shutdown(false);
                            }
                        }
                        else {
                            out.println( "ERR Access denied." );
                        }
                    }
                    else if( selection.toLowerCase().startsWith( "quit" ) ) {
                        break;
                    }
                }

                out.close();
                in.close();
                client.close();
                server.removeClient();
            }
            catch( InterruptedIOException e ) {
                log.println( 
                            "SundayServer: Disconnecting sleeping client." );
                out.println( "ERR Timeout, disconnecting." );
                try {
                    server.removeClient();
                    out.close();
                    in.close();
                    client.close();
                }
                catch( IOException ex ) {
                    log.println( "SundayServer: Client respazzed." );
                }
            }
            catch( IOException e ) {
                log.println( "SundayServer: Client spazzed." );
                try {
                    out.close();
                    in.close();
                    client.close();
                    server.removeClient();
                }
                catch( IOException ex ) {
                    log.println( "SundayServer: Client respazzed." );
                }
            }
            
            client = null;
        }


        /**
         * Attempt to drop a drink, checking both user balance and slot
         * quantity first.  Note, this method does not authenticate the
         * user, that should be checked outside of this method.
         *
         * @param username  The username to charge for the drink
         * @param slot      The slot to drop
         * @param delay     The delay (in seconds) to wait before dropping
         */
        private void dropDrink( String username, int slot, int delay ) 
                                                       throws IOException {
            int numSlots = drink.getDrinkNames().size();
                    
            // Is it a valid slot number
            if( (slot >= 0) && (slot < numSlots) ) {
                // Does the user have enough cash to afford this drink?
                if( acctMgr.decreaseUserBalance(username, 
                                                    drink.getPrice(slot))) {
                    // Are there enough drinks in the slot?
                    if( drink.drinksLeft(slot) > 0 ) {
                        try {
                            Thread.sleep( delay * 1000 );
                        }
                        catch( InterruptedException e ) {
                            log.println( 
                                 "SundayServer: Drop Delay Interrupted." );
                        }
                        // Drop the drink.
                        if( drink.drop(slot) ) {
                            out.println( "OK Credits remaining: " + 
                                         acctMgr.getUserBalance( username ) );
                        }
                        else {
                            //acctMgr.increaseUserBalance(username, 
                            //                            drink.getPrice(slot)); 
                            out.println( "ERR Drop failed, contact an admin." );
                        }
                    }
                    else {
                            //acctMgr.increaseUserBalance(username, 
                            //                            drink.getPrice(slot)); 
                            out.println( "ERR Slot empty." );
                    }
                }
                else {
                        out.println( "ERR User is poor." );
                }
            }
            else {
                    out.println( "ERR Invalid slot number." );
            }
        }

        /**
         * Print the contents and statistics of the drink machine out to the
         * socket.
         */
        private void getStats() {
            Vector names = drink.getDrinkNames();

            // Iterate over the slots and gather their info
            for( int i = 0; i < names.size(); i++ ) {
                out.println( i + " " + (String) names.elementAt(i) + " " 
                                            + drink.getPrice(i) + " " +
                                            drink.drinksLeft(i) + " " +
                                            drink.drinksDropped(i) );

                // Some of the clients expect this null.
                out.flush();
            }        

            out.println( "OK " + names.size() + " Slots retrieved" );
            out.flush();
        }

        /**
         * Edit a users account information. 
         *
         * @param command   The entire command that was issued by the user,
         *                  including the initial 'edituser ' which is
         *                  truncated by this method.
         */
        private void editUser( String command ) {
            String args = command.substring( 9 );
            String username = null;
            int wubbas = 0;
            boolean admin = false;
        
            StringTokenizer s = new StringTokenizer( args );
        
            if( s.countTokens() == 3 ) {
                username = s.nextToken();
                wubbas = new Integer( s.nextToken() ).intValue();
                admin = new Boolean( s.nextToken() ).booleanValue();

                if( acctMgr.isValidUser(username) ) {
                    acctMgr.increaseUserBalance( username, wubbas );
                    acctMgr.setUserAdmin( username, admin );
                    out.println( "OK Changes saved." );
                }
                else {
                    out.println( "ERR Invalid user." );
                }
            }
            else if( s.countTokens() == 2 ) {
                username = s.nextToken();
                wubbas = new Integer( s.nextToken() ).intValue();

                if( acctMgr.isValidUser(username) ) {
                    acctMgr.increaseUserBalance( username, wubbas );
                    out.println( "OK Changes saved." );
                }
                else {
                    out.println( "ERR Invalid user." );
                }
            }
             
        }

        /**
         * Edit a slots information. 
         *
         * @param command   The entire command that was issued by the user,
         *                  including the initial 'editslot ' which is
         *                  truncated by this method.
         */
        private void editSlot( String command ) {
            String args = command.substring( 9 );
            Slot target = null;
            int slot = 0;
            String name = null;
            int cost = 0;
            int quantity = 0;
            int num_dropped = 0;
            boolean enabled = true;
        
            StringTokenizer s = new StringTokenizer( args );
        
            if( s.countTokens() == 6 ) {
                slot        = new Integer( s.nextToken() ).intValue();
                name        = s.nextToken();
                cost        = new Integer( s.nextToken() ).intValue();
                quantity    = new Integer( s.nextToken() ).intValue();
                num_dropped = new Integer( s.nextToken() ).intValue();
                enabled     = new Boolean( s.nextToken() ).booleanValue();

                target = drink.getSlot(slot);
                if( target != null ) {
                    target.setName( name );
                    target.setCans( quantity );
                    target.setPrice( cost );
                    target.setDropped( num_dropped );
                    target.setEnabled( enabled );

                    drink.setSlot( slot, target );

                    out.println( "OK Changes saved." );
                }
                else {
                    out.println( "ERR Invalid slot." ); 
                }
            }
            else {
                out.println( "ERR Syntax Error" );
            }
        }
    }
}
