
// $Id: AcctMgr.java,v 1.4 2000/12/09 04:07:23 grahams Exp $
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: Abstract class to define an interface for providing 
//              user authentication
// Revisions:
//              $Log: AcctMgr.java,v $
//              Revision 1.4  2000/12/09 04:07:23  grahams
//              Turned AcctMgr into an Interface (finally), and
//              edited BasicAcctMgr to implement it.
//
//              Revision 1.3  2000/12/06 09:05:05  grahams
//              Added CVS headers for all files, and improved basic 
//              admin functionality
//
//


import java.io.*;

public interface AcctMgr {

    // Authentication
    public boolean isValidUser( String username, String password );
    public boolean isValidUser( String username );

    // Balance Maintenance
    public int getUserBalance( String username ); 
    public abstract boolean decreaseUserBalance( String username, int amount );
    public abstract boolean increaseUserBalance( String username, int amount );

    // User Maintenance
    public boolean addUser( String username, String password );
    public boolean removeUser( String username );
    public boolean isUserAdmin( String username );
    public boolean setUserAdmin( String username, boolean admin );
}
