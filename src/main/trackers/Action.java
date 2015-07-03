package trackers;

public enum Action {
	CONNECT(0), ANNOUNCE(1), SCRAPE(2), ERROR(3);
	
	private final int value;
	private Action(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public static Action fromValue(int value)
	{
		switch(value){
			case 0:
			return CONNECT;
			case 1:
			return ANNOUNCE;
			case 2:
			return SCRAPE;
			case 3:
			return ERROR;
		}
		return null;
	}
}
