// $Id: FingerServer.java,v 1.14 2000/12/12 19:59:34 grahams Exp $
// Author: Bill kuker
// Revisions:
//      $Log: FingerServer.java,v $
//      Revision 1.14  2000/12/12 19:59:34  grahams
//      Denastified the column view a bit by doing
//      some simple padding.  it is cheesy, but it
//      makes it look better
//
//      Revision 1.13  2000/12/09 05:28:12  grahams
//      Removed temp printing for now.
//
//      Revision 1.12  2000/12/09 05:16:01  grahams
//      Fixed the finger server:
//
//      Revision 1.11  2000/12/09 03:50:25  kukester
//      Abstracted drinkmachine class.
//
//      Revision 1.10  2000/12/08 23:50:48  kukester
//
//      Fixed Finger errors.
//      (Name length)
//
//      Revision 1.9  2000/12/06 17:13:37  grahams
//      Fixed a bug in the POP3 auth where it wouldn't quit the POP3 connection,
//      leaving a mailbox lock lying around for awhile until the server timed out.
//
//      Also prettied some of the output in FingerServer
//
//      Revision 1.8  2000/12/06 09:42:46  grahams
//      cleaned up "Listening" message
//
//      Revision 1.7  2000/12/06 09:37:40  grahams
//      prettied up uptime needlessly
//
//      Revision 1.6  2000/12/06 08:00:57  kukester
//      fixed year bug (was 70 too high!)
//
//      Revision 1.5  2000/12/06 07:55:03  kukester
//      Added uptime output.
//
//      Revision 1.4  2000/12/06 07:34:45  kukester
//      Added standard system.err's and made FingerServer
//      recover from naughty exceptions.
//
//      Revision 1.3  2000/12/06 06:52:15  kukester
//      Added some basic comments.
//
//      Revision 1.2  2000/12/06 06:29:01  kukester
//      Added headers
//
//


import java.net.*;
import java.io.*;
import java.util.*;

public class FingerServer extends Thread{

    ServerSocket         listen;
    DrinkMachine drink;
    AcctMgr acct;
    DrinkLogger log;

    //
    // Ctor
    //
    //  makes a new finger Server thread. Pass in the
    //  drinkMachine and acctMgr on the system.
    //
        public FingerServer(DrinkMachine _drink, AcctMgr _acct, 
                            DrinkLogger _log ) {
        int port = 79;
        drink = _drink;
        acct = _acct;
        log = _log;
             try { 
                 listen = new ServerSocket(port);
                 log.println("FingerServer: Listening on port: " 
                                                                    + port);
             } catch(Exception e) {
                 log.println(e.getMessage());
             }
        }


    private String slotStat( int slot ) {
        String slotname;
        String slotcontents = (String) drink.getDrinkNames().elementAt(slot);

        slotname = new String( slot + "\t" + slotcontents );

        for( int i = 0; i < (17 - slotcontents.length()); i++ ) {
            slotname = slotname + " ";
        }
        slotname = slotname 
                   + new Integer(drink.getPrice(slot)).toString() + "\t" 
                   + new Integer(drink.drinksLeft(slot)).toString() + "\t"
                   + new Integer(drink.drinksDropped(slot)).toString();

        return slotname;
    }

    private void printInfo( PrintWriter out ){
        Vector names = drink.getDrinkNames();
        out.println( "" );
        out.println( "Welcome to the CSH drink server." );
        out.println( "" );
        out.println( "Drink currently contains:" );

        int i = 0;
        while( names.removeElement("-"));
        for (i = 0; i < names.size() - 2 ; i++)     
            out.print( names.elementAt(i) + ", " );
        out.print( names.elementAt( i+0 ) + " & " );
        out.println(names.elementAt( i+1 ) + "." );

        out.println( "" );
        out.println( "For general stats finger @drink.csh.rit.edu," );
        out.println( "for stats on a specific slot finger" );
        out.println( "[name]@drink.csh.rit.edu or by slot number" );
        out.println( "[number]@drink.csh.rit.edu." );
        out.println( "" );
    }



    private void printStat( PrintWriter out ){
        Vector names = drink.getDrinkNames();
        Date d = new Date( drink.uptime() );
                out.println( "" );

        int year, month, date, hours, minutes, seconds;

        year = (d.getYear() - 70);
        month = d.getMonth();
        date = d.getDate();
        hours = d.getHours();
        minutes = d.getMinutes();
        seconds = d.getSeconds();

        out.print( "Drink's uptime: " );
        if( year > 0 ) {
            out.print( year + " year" );
            if( year != 1 ) {
                out.print( "s" );
            }
            out.print( ", " );
        }
        if( month > 0 ) {
            out.print( month + " month" );
            if( month != 1 ) {
                out.print( "s" );
            }
            out.print( ", " );
        }
        if( date > 0 ) {
            out.print( date + " day" );
            if( date != 1 ) {
                out.print( "s" );
            }
            out.print( ", " );
        } 
        if( hours > 0 ) {
            out.print( hours + " hour" );
            if( hours != 1 ) {
                out.print( "s" );
            }
            out.print( ", " );
        }
        if( minutes > 0 ) {
            out.print( minutes + " minute" );
            if( minutes != 1 ) {
                out.print( "s" );
            }
            out.print( ", " );
        }
        out.print( seconds + " second" );
        if( seconds != 1 ) {
            out.print( "s" );
        }
        out.println( "." );
                    
        out.println( "" );
                
                out.println( "Slot\tName             Price\tCans\tDropped"  );
                out.println( "" );
                for (int slot = 0; slot < names.size(); slot++)
                       out.println( slotStat(slot) );
                out.println("");
    }

    private void printSlot( PrintWriter out, int slot ){
                out.println( slotStat( slot ) );
    }

    private void printUser( PrintWriter out, String user ) {
        out.println( "" );
        out.println( "Drink info on " + user + ".");
        out.println( "Balance: " + acct.getUserBalance( user ) ); 
        if ( acct.isUserAdmin( user ) )
            out.println( user + " is an admin." );
        out.println( "" );      
    }

    private void printErr( PrintWriter out, String command ){
        out.println("");
        out.println( command + " is not a user known to drink.");
        out.println("");
    }

        public void run(){
          while(true){
             try {
                 while(true) {
                        Socket clnt = listen.accept();
                        log.println("Finger: Somebody connected ... ");
                        PrintWriter out = new PrintWriter
                         (clnt.getOutputStream (), true);
            BufferedReader in = new BufferedReader
                ( new InputStreamReader( clnt.getInputStream ()) );
            String comand;
            String command = in.readLine();

            Vector names = drink.getDrinkNames();
            boolean found = false;

            if ( command.equals( "info" ) ) {
                printInfo( out );
            }
            else if ( acct.isValidUser( command ) ) {
                printUser(out, command);
            }
            else if( command.compareTo( "" ) != 0 ) { 
                out.println( "" );
                out.println( "Slot\tName             Price\tCans\tDropped"  );
                out.println( "" );

                for( int i = 0; i < names.size(); i++ ) {
                    String slot = ((String)names.elementAt(i)).toLowerCase();
                    String request = command.toLowerCase();
                    if( slot.indexOf(request) != -1 ) {
                        printSlot( out, i );
                        found = true;
                    }
                }
            }
            else if ( command.equals( "" ) ) {
                printStat(out);
            }
            else {
                printErr(out, command);
            }

                        clnt.close();
                 }
             } catch(Exception e) {
                    log.println("FingerServer: Unexpected " + e);
                    e.printStackTrace();
             }
      }
        }
     }


