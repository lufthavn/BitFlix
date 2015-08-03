package messages;

import java.nio.ByteBuffer;

public class HaveMessage extends Message{
	
	private int index;

	public HaveMessage(int index){
		this.index = index;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.HAVE;
	} 
	
	public int getIndex(){
		return this.index;
	}

	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer
				.allocate(9)
				.putInt(5) //length
				.put((byte) MessageType.HAVE.getValue())
				.putInt(index);
	}

}
