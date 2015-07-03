package trackers.packets;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import trackers.Action;

public class ConnectionResponse implements ITrackerResponse{

	private Action action;
	private int actualTransactionId;
	private long connection_id;
	private ErrorResponse errorResponse;
	
	public ConnectionResponse(byte[] data) {
		try{
			ByteBuffer buffer = ByteBuffer.wrap(data);
			this.action = Action.fromValue(buffer.getInt());
			if(action == Action.ERROR){
				errorResponse = new ErrorResponse(data);
			}else{
				this.actualTransactionId = buffer.getInt();
				this.connection_id = buffer.getLong();
			}
		}catch(BufferUnderflowException e){
			throw new IllegalArgumentException("The data received from tracker is malformed.");
		}
	}

	
	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the transaction id returned by the tracker
	 */
	public int getTransaction_id() {
		return actualTransactionId;
	}

	/**
	 * @return the connection_id
	 */
	public long getConnection_id() {
		return connection_id;
	}


	/**
	 * @return the errorResponse, if tracker returned an error Action, null if not.
	 */
	public ErrorResponse getErrorResponse() {
		return errorResponse;
	}

	/**
	 * @return true if tracker response contains expected transaction id, false if not.
	 */
	public boolean transactionIdsMatch(int expectedTransactionId){
		return actualTransactionId == expectedTransactionId;
	}
	
	/**
	 * @return true if the response contains the expected Action and Transaction ID, false if not. 
	 */
	public boolean isValid(int expectedTransactionId){
		return action == Action.CONNECT && transactionIdsMatch(expectedTransactionId);
	}

}
