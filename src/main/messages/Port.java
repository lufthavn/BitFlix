package messages;

import java.nio.ByteBuffer;

public class Port extends Message {

	private int port;
	
	public Port(int port){
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
