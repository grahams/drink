public class Slot{

	private String name;
	private int cans;
	private int dropped;
	private int price;
	private boolean enabled;

	public Slot(){
		name = new String();
		cans = 0;
		dropped = 0;
		price = 0;
		enabled = false;
	}

	public String getName(){
		return name;
	}

	public void setName( String _name ){
		name = _name;
	}

	public int getCans(){
		return cans;
	}
	
	public void setCans( int _cans ){
		cans = _cans;
	}

	public int getDropped(){
		return dropped;
	}
	
	public void setDropped( int _dropped ){
		dropped = _dropped;
	}

	public int getPrice(){
		return price;
	}

	public void setPrice(int _price){
		price = _price;
	}

	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled( boolean _enabled ){
		enabled = _enabled;
	}
}
