package messages;

import java.nio.ByteBuffer;

public class PortMessage extends Message {

	private int port;
	
	public PortMessage(int port){
		this.port = port;
	}


	@Override
	public MessageType getType() {
		return MessageType.PORT;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}


	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer.allocate(7)
				.putInt(3)
				.put((byte) MessageType.PORT.getValue())
				.putShort((short) port);
	}
	
}
