package trackers.packets;

import java.nio.ByteBuffer;

import trackers.Action;

public class ErrorResponse implements ITrackerResponse {
	private byte[] data;
	private Action action;
	private int transactionId;
	private String message;

	public ErrorResponse(byte[] data){
		this.data = data;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		action = Action.fromValue(buffer.getInt());
		if(action != Action.ERROR){
			throw new IllegalArgumentException("The action for an error must be 3: error.");
		}
		transactionId = buffer.getInt();
		
		byte[] messageBytes = new byte[buffer.remaining()]; //the remaining bytes make up the message from the tracker
		buffer.get(messageBytes);
		message = new String(messageBytes);
	}
	
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the transactionId
	 */
	public int getTransactionId() {
		return transactionId;
	}

	/**
	 * @return the message from the tracker
	 */
	public String getMessage() {
		return message;
	}
}
