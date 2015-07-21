package messages;

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
	
}
