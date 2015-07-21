package messages;

public class Have extends Message{
	
	private int index;

	public Have(int index){
		this.index = index;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.HAVE;
	} 
	
	public int getIndex(){
		return this.index;
	}

}
