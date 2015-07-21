package messages;

public class KeepAlive extends Message{

	@Override
	public MessageType getType() {
		return MessageType.KEEPALIVE;
	}

	
}
