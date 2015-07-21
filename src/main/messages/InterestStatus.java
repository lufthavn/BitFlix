package messages;

public class InterestStatus extends Message {
	
	private int id;

	public InterestStatus(int id){
		if(id != 2 || id != 3){
			throw new IllegalArgumentException("An interest status' id must either 0 or 1");
		}
		this.id = id;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.fromValue(id);
	}
}
