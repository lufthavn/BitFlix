package messages;

public class ChokeStatus extends Message {

	private int id;

	public ChokeStatus(int id){
		if(id != 0 && id != 1){
			throw new IllegalArgumentException("A choke status' id must either 0 or 1");
		}
		this.id = id;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.fromValue(id);
	}

}
