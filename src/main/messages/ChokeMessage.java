package messages;

import java.nio.ByteBuffer;

public class ChokeMessage extends Message {

	private int id;

	public ChokeMessage(int id){
		if(id != 0 && id != 1){
			throw new IllegalArgumentException("A choke status' id must either 0 or 1");
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
