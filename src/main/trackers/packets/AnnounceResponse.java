package trackers.packets;

import java.net.URI;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import peers.Peer;
import trackers.Action;

public class AnnounceResponse implements ITrackerResponse{

	private Action action;
	private int actualTransactionId;
	private int interval;
	private int leechers;
	private int seeders;
	private ArrayList<Peer> peers;
	private byte[] data;
	private ErrorResponse errorResponse;
	
	public AnnounceResponse(byte[] data)  {
		this.data = data;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		try{
			action = Action.fromValue(buffer.getInt());
			if(action == Action.ERROR){
				errorResponse = new ErrorResponse(data);
			}else{
				actualTransactionId = buffer.getInt();
				interval = buffer.getInt();
				leechers = buffer.getInt();
				seeders = buffer.getInt();
				peers = parsePeers(buffer);
			}
		}catch(BufferUnderflowException e){
			throw new IllegalArgumentException("The data received from tracker is malformed.");
		}
	}

	private ArrayList<Peer> parsePeers(ByteBuffer buffer) throws BufferUnderflowException {
		
		ArrayList<Peer> list = new ArrayList<Peer>();
		
		while(buffer.remaining() != 0){	
			int ip = buffer.getInt();
			int port = buffer.getShort() & 0xffff;
			Peer peer = new Peer(ip, port);
			list.add(peer);
		}
		return list;
		
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
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * @return the leechers
	 */
	public int getLeechers() {
		return leechers;
	}

	/**
	 * @return the seeders
	 */
	public int getSeeders() {
		return seeders;
	}

	/**
	 * @return the peers
	 */
	public ArrayList<Peer> getPeers() {
		return peers;
	}

	public byte[] getData() {
		return data;
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
		return action == Action.ANNOUNCE && transactionIdsMatch(expectedTransactionId);
	}

}
