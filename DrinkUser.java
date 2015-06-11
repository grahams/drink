// $Id: DrinkUser.java,v 1.3 2000/12/06 09:05:06 grahams Exp $
// Author:      Sean M. Graham <grahams@csh.rit.edu>
// Description: A container for user information
// Revisions:
//               $Log: DrinkUser.java,v $
//               Revision 1.3  2000/12/06 09:05:06  grahams
//               Added CVS headers for all files, and improved basic admin functionality
//
//


import java.io.*;

public class DrinkUser implements Serializable {
    private String username;
    private String password;
    private int balance;
    private boolean admin;

    public DrinkUser( String inUsername, String inPassword, 
                       int inBalance, boolean inAdmin) {
        username = inUsername;
        password = inPassword;
        balance = inBalance;
        admin = inAdmin; 
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getBalance() {
        return balance;
    }
    public boolean getAdmin() {
        return admin;
    }

    public void setUsername( String inUsername ) {
        username = inUsername;
    }
    public void setPassword( String inPassword ) {
        password = inPassword;
    }
    public void setBalance( int inBalance ) {
        balance = inBalance;
    }
    public void setAdmin( boolean inAdmin ) {
        admin = inAdmin;
    }

    public int hashCode() {
        return username.hashCode();
    }
}
