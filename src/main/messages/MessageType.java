package messages;

import trackers.Action;

public enum MessageType {
	CHOKE(0), UNCHOKE(1), INTERESTED(2), UNINTERESTED(3), HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), CANCEL(8), PORT(9), KEEPALIVE(10);
	
	private final int value;
	
	private MessageType(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public static MessageType fromValue(int value)
	{
		switch(value){
			case 0:
			return CHOKE;
			case 1:
			return UNCHOKE;
			case 2:
			return INTERESTED;
			case 3:
			return UNINTERESTED;
			case 4:
			return HAVE;
			case 5:
			return BITFIELD;
			case 6:
			return REQUEST;
			case 7:
			return PIECE;
			case 8:
			return CANCEL;
			case 9:
			return PORT;
			case 10:
			return KEEPALIVE;
		}
		return null;
	}
}
