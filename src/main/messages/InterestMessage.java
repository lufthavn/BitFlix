package messages;

import java.nio.ByteBuffer;

public class InterestMessage extends Message {
	
	private int id;

	public InterestMessage(int id){
		if(id != 2 && id != 3){
			throw new IllegalArgumentException("An interest status' id must either 2 or 3");
		}
		this.id = id;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.fromValue(id);
	}

	@Override
	public ByteBuffer getBytes() {
		
		return ByteBuffer.allocate(5)
				.putInt(1) //length
				.put((byte) id);
	}
}
