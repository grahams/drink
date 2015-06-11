// $Id: DrinkMachine.java,v 1.9 2000/12/12 04:51:57 kukester Exp $
// Author: Bill kuker
// Revisions:
//	
//	$Log: DrinkMachine.java,v $
//	Revision 1.9  2000/12/12 04:51:57  kukester
//	Added slot editing through getslot and setSlot in Drinkmachine
//	and the Slot class.
//	
//	Revision 1.8  2000/12/09 03:50:22  kukester
//	Abstracted drinkmachine class.
//	


import java.util.*;


public interface DrinkMachine{

	//
	// shutdown
	//
	//	Shut down server. Value passed in is true = reboot
	//	false = no reboot.
	public void shutdown( boolean reboot );

	//
	// uptime
	//	returns long uptime millis
	public long uptime();

	//
	// getDrinkNames
	//
	//	Return a vector of stings, in order by
	//	slot number
	//
	public Vector getDrinkNames();


	//
        // getSlot
        //
        //      returns a slot object for more advanced uses.
        //
        public Slot getSlot( int slot );

	//
	// setSlot
	//
	//	sets a slots values to those contained in the slot object.
	//
	public void setSlot( int slot, Slot s );

	//
        // isEnabled
        //
        //      returns true false as to wether or not a slot is enabled.
        //
        public boolean isEnabled( int slot );

	//
	//Drop a drink
	//
	//	return true if it works false otherwise
	//
	public boolean drop(int slot);

	//
	//  getPrice
	//	
	//	Returns the price of a drink in CSH Wonderwubbas.
	//	defaults to 50 wubbas if the price is not in the
	//	config file.
	//
	public int getPrice(int slot);


	//
	// lightOn
	//
	//	Turns the given slot's light on.
	//
	public void lightOn(int slot);

	//
	// lightOff
	//
	//	Turns the given slot's light off.
	//
	public void lightOff(int slot);

	//
	// drinksLeft
	//
	//	Returns the nuber of drinks left in a slot.
	//
	public int drinksLeft(int slot);

	//
	// drinksDropped
	//
	//	Return the number of drinks dropped by a slot
	//
	public int drinksDropped(int slot);

	//
	// temp
	//
	//	Returns a vector of Floats, one for each temp sensor
	//	on the given slot's list. Note Float not float.
	//
	public Vector slotTemp(int slot);

	//
	//  slotTempAvg
	//
	//	Returns a float (not Float) for the average
	//	slot temp, -127 if there is no data.
	//	
	public float slotTempAvg( int slot );

	//
        // addDrinkButtonListener
        //
        //      Add a listener to the list of objects requesting
        //      callbacks from the buttons.
        //
	public void addDrinkButtonListener(DrinkButtonListener dbl);

	//
	// delDrinkButtonListener
	//
	//	remove a listener from the list of objects requesting 
	//	callbacks from the buttons.
	//
	public void delDrinkButtonListener(DrinkButtonListener dbl);

}

