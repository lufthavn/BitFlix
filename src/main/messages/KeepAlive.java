package messages;

import java.nio.ByteBuffer;

public class KeepAlive extends Message{

	@Override
	public MessageType getType() {
		return MessageType.KEEPALIVE;
	}

	public ByteBuffer getBytes(){
		return (ByteBuffer) ByteBuffer.allocate(4).putInt(0).position(0);
	}
}
