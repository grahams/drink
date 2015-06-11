
// $Id: BasicAcctMgr.java,v 1.9 2000/12/11 16:15:35 grahams Exp $
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: Authenticates users against the CSH POP3 server and stores
//              user information in a simply hashtable which is filled from
//              and stored to a simple text file.
// Revisions:
//              $Log: BasicAcctMgr.java,v $
//              Revision 1.9  2000/12/11 16:15:35  grahams
//              Unmeaningful log message.
//
//              Revision 1.8  2000/12/09 21:54:30  grahams
//              fixed retardation in authentication.
//              exceptions now cause isValidUser to return false
//
//              Revision 1.7  2000/12/09 04:07:23  grahams
//              Turned AcctMgr into an Interface (finally), and
//              edited BasicAcctMgr to implement it.
//
//              Revision 1.6  2000/12/09 03:30:00  grahams
//              Fixed a ton of silly-ass bugs
//
//              Revision 1.5  2000/12/08 21:04:08  grahams
//              Now authenticating against IMAP vs. POP3, and cleaned up
//              SundayServer a bit
//
//              Revision 1.4  2000/12/06 17:13:37  grahams
//              Fixed a bug in the POP3 auth where it wouldn't quit the 
//              POP3 connection, leaving a mailbox lock lying around for 
//              awhile until the server timed out.
//
//              Also prettied some of the output in FingerServer
//
//              Revision 1.3  2000/12/06 09:05:05  grahams
//              Added CVS headers for all files, and improved basic admin 
//              functionality
//
//


import java.net.*;
import java.io.*;
import java.util.*;

public class BasicAcctMgr implements AcctMgr {
    private static String filename   = new String( "accounts" );
    private static String hostname   = new String( "mail.csh.rit.edu" );

    private DrinkLogger log;
    private Hashtable hash;     // This hash stores the user/pass/balance
                                // matches

    public BasicAcctMgr( DrinkLogger inLog ) {

        log = inLog;

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            hash = new Hashtable();
    
            while( in.ready() ) {
                StringTokenizer s = new StringTokenizer( in.readLine() );
                if( s.countTokens() == 3 ) {
                    String user = s.nextToken();
                    int balance = new Integer(s.nextToken()).intValue();
                    boolean admin = new Boolean(s.nextToken()).booleanValue(); 
    
                    hash.put( user, new DrinkUser(user, "", balance, admin) );
                }
            }

            in.close();
        }
        catch( FileNotFoundException ex ) {
            log.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }
        catch( IOException ex ) {
            log.println( "BasicAcctMgr: Error: " + ex.getMessage() );
        }
    }

    // This function currently authenticates against the CSH IMAP server 
    // This should get an enema quickly. :)
    public boolean isValidUser( String username, String password ) {
        boolean validUser = true;

        try {
            Socket s = new Socket( hostname, 143 );
            PrintWriter out = new PrintWriter( s.getOutputStream(), true );
            BufferedReader in = new BufferedReader(
                                new InputStreamReader( s.getInputStream() ) );
            String line;

            line = in.readLine();
            if( !(line.startsWith("* OK")) ) {
                validUser = false;
            }            

            if( password == null ) {
                validUser = false;
            }

            if( password.compareTo("") == 0) {
                validUser = false;
            }

            out.println( "a242 LOGIN " + username + " \"" + password + "\"" );

            while(true) {
                line = in.readLine();
                if( line.startsWith("a242") ) {
                    break;
                }
            }

            if( !(line.startsWith("a242 OK")) ) {
                validUser = false;
            }

            out.println( "a243 LOGOUT"  );

            out.close();
            in.close();
            s.close();
        }
        catch( UnknownHostException e) {
            log.println("BasicAcctMgr: Error: Invalid Hostname: " + hostname);
            validUser = false;
        }
        catch( IOException e ) {
            log.println( "BasicAcctMgr: Error: " + e );
            validUser = false;
        }
        catch( Exception e ) {
            log.println( "BasicAcctMgr: Error: " + e.getMessage() );
            validUser = false;
        }

        if( validUser && !(hash.containsKey(username)) ) {
            addUser( username, "" );
            return validUser;
        }
        else {
            return validUser;
        }
    }

    // This function returns true if the user has used drink before.
    public boolean isValidUser( String username ) {
        if( hash.containsKey(username) ) {
            return true;
        }
        else {
            return false;
        }
    }

    // Balance Maintenance
    public int getUserBalance( String username ) {
        if( hash.containsKey( username ) ){
            DrinkUser user = (DrinkUser) hash.get(username);
            
            return user.getBalance();
        }
        else {
            return 0;
        }
    }

    public synchronized boolean decreaseUserBalance( String username, 
                                                     int amount ) {
        if( hash.containsKey( username ) ) {
            DrinkUser user = (DrinkUser) hash.get(username);
            
            if( user.getBalance() < amount ) {
                return false;
            }
            else {
                user.setBalance( user.getBalance() - amount );

                saveConfig();

                return true;
            }
        }
        else {
            return false;
        }
    }

    public synchronized boolean increaseUserBalance( String username, 
                                                     int amount ) {
        if( hash.containsKey( username ) ) {
            DrinkUser user = (DrinkUser) hash.get(username);
            
            user.setBalance( user.getBalance() + amount );

            saveConfig();

            return true;
        }
        else {
            return false;
        }
    }

    // User Maintenance
    public boolean addUser( String username, String password ) {
        DrinkUser newUser = new DrinkUser( username, password, 0, false );
        
        if( hash.containsKey( username ) ) {
            return false; 
        }
        else {
            hash.put( username, newUser );

            saveConfig();
            return true;
        }
    }

    public boolean removeUser( String username ) {
        if( hash.containsKey( username ) ) {

            hash.remove( username );
            saveConfig();

            return true;
        }
        else {
            return false;
        }
        
    }

    public boolean isUserAdmin( String username ) {
        if( hash.containsKey( username ) ) {
            DrinkUser user = (DrinkUser) hash.get(username);
            
            return user.getAdmin();
        }
        else {
            return false;
        }
    }

    public boolean setUserAdmin( String username, boolean admin ) {
        if( hash.containsKey( username ) ) {
            DrinkUser user = (DrinkUser) hash.get(username);
            
            user.setAdmin(admin);
            saveConfig();

            return true;
        }
        else {
            return false;
        }
    }

    private synchronized void saveConfig() {
        log.println( "BasicAcctMgr: Saving user info to " + filename );
        try {
            PrintWriter out = new PrintWriter( new FileWriter(filename) );
            Enumeration keys = hash.keys();

            while( keys.hasMoreElements() ) {
                DrinkUser user = (DrinkUser) hash.get( 
                                        (String) keys.nextElement() );
                out.println( user.getUsername() + " " 
                             + new Integer(user.getBalance()) + " "
                             + new Boolean(user.getAdmin()) ); 
            }
            out.close();
        }
        catch( Exception e ) {
            log.println( "BasicAcctMgr: Error writing user info: " +
                                                        e.getMessage() );
        }
    }
}
