// $Id: FakeDrinkMachine.java,v 1.2 2000/12/12 04:51:57 kukester Exp $
// Author: Bill kuker
// Revisions:
//	$Log: FakeDrinkMachine.java,v $
//	Revision 1.2  2000/12/12 04:51:57  kukester
//	Added slot editing through getslot and setSlot in Drinkmachine
//	and the Slot class.
//	
//	Revision 1.1  2000/12/08 20:51:53  kukester
//	Added FakeDrinkMachine, a drink Machine that does nothig
//	but debug output.
//	
//	Fixed DrinkMachine with default ctor for the fake one
//	and fixed a synchronization issue in drop().
//	
//

import java.util.*;
import java.io.*;


public class FakeDrinkMachine implements DrinkMachine{
	private Vector DrinkButtonListeners;
	private int numslots;
	private Hashtable config;
	private String configFile;
	private ButtonLoop buttonloop;
	private Object bus;

	//
	// ButtonLoop
	//
	//	A private class to do the callbacks for
	//	the buttons.
	//
	private class ButtonLoop extends Thread{
		private Vector dbls;
		private Hashtable config;

		public ButtonLoop(Vector _dbls, Hashtable _config){
			dbls = _dbls;
			config = _config;
			this.setDaemon( true );
		}
		public void run(){

		}
	}//buttonloop


	//
	// Constructor
	//
	//	Takes a String for a config file, throws an IOException
	//	if the config file is crapped up.
	//
	public FakeDrinkMachine(String _configFile)
	throws IOException
	{
		configFile = _configFile;

		String configFileVersion;
		String busName;
		DrinkButtonListeners = new Vector();
		config = new Hashtable();

		try{
			BufferedReader in = new BufferedReader(new FileReader(configFile));
			while (in.ready()){
					StringTokenizer s = new StringTokenizer( in.readLine() );
					if ( s.countTokens() == 2 ){
						String key = s.nextToken();
						String val = s.nextToken();
						config.put(key, val);
					}
			}

			configFileVersion = (String)config.get("version");
			busName = (String)config.get("bus");
			numslots = Integer.parseInt( (String)config.get("slots") );
	

		} catch ( NullPointerException e ) {
			System.err.println( "DrinkMachine: A required config value is missing" );
			throw new IOException("Configuration error");
		} catch (Exception e) {
			System.err.println("DrinkMachine: " + e);
			throw new IOException("Configuration error");
		}

		bus = new Object();

		//now start the button listener thread
		buttonloop = new ButtonLoop(DrinkButtonListeners, config);
		//buttonloop.start();

	} // constructor

	//
	// shutdown()
	public void shutdown( boolean reboot ){
		synchronized( bus ){
			System.exit( 0 );
		}
	}


	//
	// uptime()
	public long uptime(){
		return 0;
	}	

	//
	// saveConfig
	//
	private void saveConfig( String file ){
		System.out.println( "DrinkMachine: Saving config to " + file );
		try{
			FileWriter out = new FileWriter( file );
			Enumeration keys = config.keys();
			while(keys.hasMoreElements()){
				String ck = (String)keys.nextElement();
				out.write( ck + "         " + (String)config.get(ck) + "\r\n" );
			}
			out.close();
		} catch ( Exception e ) {
			System.err.println("DrinkMachine: Error writing config file: " + e );
		}
	}

	//
	// getDrinkNames
	//
	//	Return a vector of stings, in order by
	//	slot number
	//
	public Vector getDrinkNames(){
		Vector names = new Vector();
		for (int i = 0; i < numslots; i++)
			names.addElement( config.get( i + "name" ));
		return names;
	}

	//
        // getSlot
        //
        //      returns a slot object for more advanced uses.
        //
        public Slot getSlot( int slot ){
                if ( slot < 0 || slot >= numslots ){
                        System.err.println( "Invalid slot " + slot );
                        return null;
                }
                Slot s = new Slot();
                s.setName( (String)config.get( slot + "name" ) );
                s.setCans( drinksLeft( slot ));
                s.setDropped( drinksDropped( slot ));
                s.setPrice( getPrice( slot ) );
                s.setEnabled( isEnabled( slot ) );
                return s;
        }

	//
        // setSlot
        //
        //      sets a slots values to those contained in the slot object.
        //              
        public void setSlot( int slot, Slot s ){
                config.put( slot + "name", s.getName() );
                config.put( slot + "cans", Integer.toString( s.getCans() ) );
                config.put( slot + "dropped", Integer.toString( s.getDropped() ) );
                config.put( slot + "price", Integer.toString( s.getPrice() ) );
                if ( s.isEnabled() ){
                        config.put( slot + "enabled", "true" );
                } else {
                        config.put( slot + "enabled", "false" );
                }       
        } 


        //
        // isEnabled
        //
        //      returns true false as to wether or not a slot is enabled.
        //
        public boolean isEnabled( int slot ){
                if ( slot < 0 || slot >= numslots ){
                        System.err.println( "Invalid slot " + slot );
                        return false;
                }
                String s = (String)config.get( slot + "enabled" );
                if ( s == null ){
                        return true;
                } else if ( s.equals("true") ){
                        return true;
                } else {
                        return false;
                }
        }




	//
	//Drop a drink
	//
	//	return true if it works false otherwise
	//
	public boolean drop(int slot){
		boolean ret;

		if ( slot < 0 || slot >= numslots ){
			System.err.println( "DrinkMachine: Invalid slot " + slot );
			return false;
		}

		//get the address of the device
		String addie = (String)config.get( slot + "drop" );
		System.err.println( "DrinkMachine: Drop on slot " + slot + " address " + addie);

		int cans = Integer.parseInt( (String)config.get( slot + "cans" ) );
		
		if (cans < 1){
			System.err.println( "DrinkMachine: Slot " + slot + " empty." );
			return false;
		}
		synchronized (bus){
			ret = toggle( addie );
			
			//Pause appropriatly
			try{
				Thread.sleep( 1000 );
			} catch ( InterruptedException e ) {
				 System.err.println( "DrinkMachine: Exception in drop (sleep()): " + e );
			}

			if (ret){
				//Kill the motor
				ret = toggle( addie );
				//deduct a can
				cans--;
				config.put( slot + "cans", Integer.toString( cans ) );
			
				//add a dropped can
				int dropped = Integer.parseInt( (String)config.get( slot + "dropped" ) );
	        		dropped++;
	        		config.put( slot + "dropped", Integer.toString( dropped ) );
			}
		

		        saveConfig( configFile );
			return ret;
		}

	} //drop


	//
	//  getPrice
	//	
	//	Returns the price of a drink in CSH Wonderwubbas.
	//	defaults to 50 wubbas if the price is not in the
	//	config file.
	//
	public int getPrice(int slot){
		try {
			return Integer.parseInt( (String)config.get( slot + "price" ) );
		} catch ( Exception e ) {
			return 50;
		}
	}



	//
	// lightOn
	//
	//	Turns the given slot's light on.
	//
	public void lightOn(int slot){
		if ( slot < 0 || slot >= numslots )
                        return;
		//get the address of the device
                String addie = (String)config.get( slot + "light" ); 

	}

	//
	// lightOff
	//
	//	Turns the given slot's light off.
	//
	public void lightOff(int slot){
		if ( slot < 0 || slot >= numslots )
                        return;
		//get the address of the device
                String addie = (String)config.get( slot + "drop" ); 
	}

	//
	// drinksLeft
	//
	//	Returns the nuber of drinks left in a slot.
	//
	public int drinksLeft(int slot){
		if ( slot < 0 || slot >= numslots )
                        return 0;
                else{
			return Integer.parseInt( (String)config.get( slot + "cans" ) );
                }
	}

	//
	// drinksDropped
	//
	//	Return the number of drinks dropped by a slot
	//
	public int drinksDropped(int slot){
		if ( slot < 0 || slot >= numslots )
                        return 0; 
                else{
			return Integer.parseInt( (String)config.get( slot + "dropped" ) );
                }
	}

	//
	// temp
	//
	//	Returns a vector of Floats, one for each temp sensor
	//	on the given slot's list. Note Float not float.
	//
	public Vector slotTemp(int slot){
		Vector temps = new Vector();
		if ( slot < 0 || slot >= numslots )
                        return temps;
                else{
			Vector addies = new Vector();

			String a = new String();
			for (int i = 0; a != null; i++ ){
				a = (String)config.get( slot + "temp" + i );
				if ( a != null )
					addies.addElement( a );
			}

			//Drop out if no thermometers to
			//escape 800ms delay disaster!$#!@^
			if ( addies.size() == 0 ){
				return temps;
			}

			for (int i = 0; i < addies.size(); i++){
				calcTemp( (String)addies.elementAt(i) );
			}

			try{
				Thread.sleep(800);
			} catch (Exception e) {}

			for (int i = 0; i < addies.size(); i++){
				try{
					Float f = getTemp( (String)addies.elementAt(i) );
					if ( f != null )
					temps.addElement( f );
				} catch (Exception e) {
					System.err.println("DrinkMachine: ST, " + e);
				}
			}
                }
		return temps;
	}

	//
	//  slotTempAvg
	//
	//	Returns a float (not Float) for the average
	//	slot temp, -127 if there is no data.
	//	
	public float slotTempAvg( int slot ){
		Vector v = slotTemp( slot );
		float sum = 0;
		int n = 0;
		for ( int i = 0; i < v.size(); i++ ){
			try{
				sum+= ( (Float)v.elementAt(i) ).floatValue();
				n++;
			} catch (Exception e) {
				System.err.println("DrinkMachine:  avgTemp " + e);
			}
		}
		if ( n == 0 )
			return -127;
		else
			return sum / v.size();
	}	

	//
        // addDrinkButtonListener
        //
        //      Add a listener to the list of objects requesting
        //      callbacks from the buttons.
        //
	public void addDrinkButtonListener(DrinkButtonListener dbl){
		DrinkButtonListeners.addElement(dbl);
	}

	//
	// delDrinkButtonListener
	//
	//	remove a listener from the list of objects requesting 
	//	callbacks from the buttons.
	//
	public void delDrinkButtonListener(DrinkButtonListener dbl){
		DrinkButtonListeners.removeElement(dbl);
	}


	//
	// getBus
	//
	//	Returns the one wire bus for synchronizing and
	//	using.
	//
//	public TINIExternalAdapter getBus(){
//		return bus;
//	}

	//
	// getConfig
	//
	//	Return the config
	//
	public Hashtable getConfig(){
		return config;
	}

	//
	// setConfig
	//
	//	installs the new config Hashtable
	//	does not install it if there is an error.
	//
	public boolean setConfig( Hashtable newconfig ){
		String configFileVersion;
		String busName;

		try {
			configFileVersion = (String)newconfig.get("version");
                        busName = (String)newconfig.get("bus");
                        numslots = Integer.parseInt( (String)newconfig.get("slots") );
                                        
			config = newconfig;
			return true;
                                                
                } catch ( Exception e ) {
                        System.err.println( "DrinkMachine: A required config value is missing, reverting." );
			configFileVersion = (String)config.get("version");
                        busName = (String)config.get("bus");
                        numslots = Integer.parseInt( (String)config.get("slots") );
			return false;
                }


	}

	//
	// Toggle
	//
	//	toggles the one wire device with the given address
	//	on or off.
	//
	private boolean toggle( String addie ){
		System.err.println( " Fake: Toggled " + addie );
		return true;
	}//toggle

	private void calcTemp( String addie ) {
		System.err.println( "Fake: calcT'd " + addie );
	}//calcTemp

	private Float getTemp( String addie ) throws Exception {
		try{
			synchronized (bus) {
				System.err.println( "Fake: Got temp from " + addie );
			}
                } catch ( Exception e ) {
                        System.err.println( "DrinkMachine: Gettemp: Unexpected exception " + e );
                } 
		return null;
	}//getTemp
}

