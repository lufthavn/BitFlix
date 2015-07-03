package trackers.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import trackers.Action;
import trackers.Tracker;

public class AnnounceRequest implements ITrackerRequest {


	private Tracker tracker;
	private long connection_id;
	private Action action;
	private int transaction_id;
	private byte[] peer_id;
	private long downloaded;
	private long left;
	private long uploaded;
	private int event;
	private int ip;
	private int key;
	private int num_want;
	private short port;
	
	public AnnounceRequest(Tracker tracker, long connection_id, int transaction_id, 
			byte[] peer_id, int event, int ip, int key,
			int num_want, short port) {
		
		if(peer_id.length != 20){
			throw new IllegalArgumentException("The peer id must be 20 bytes long");
		}
		this.tracker = tracker;
		this.connection_id = connection_id;
		this.action = Action.ANNOUNCE;
		this.transaction_id = transaction_id;
		this.peer_id = peer_id;
		this.event = event;
		this.ip = ip;
		this.key = key;
		this.num_want = num_want;
		this.port = port;
	}
	
	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the info_hash
	 */
	public byte[] getInfo_hash() {
		return tracker.getInfoHash();
	}

	/**
	 * @return the downloaded
	 */
	public long getDownloaded() {
		return downloaded;
	}

	/**
	 * @param downloaded the downloaded to set
	 */
	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	/**
	 * @return the left
	 */
	public long getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(long left) {
		this.left = left;
	}

	/**
	 * @return the uploaded
	 */
	public long getUploaded() {
		return uploaded;
	}

	/**
	 * @param uploaded the uploaded to set
	 */
	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	/**
	 * @return the event
	 */
	public int getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(int event) {
		this.event = event;
	}

	/**
	 * @return the ip
	 */
	public long getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(int ip) {
		this.ip = ip;
	}

	/**
	 * @return the key
	 */
	public long getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(int key) {
		this.key = key;
	}

	/**
	 * @return the num_want
	 */
	public int getNum_want() {
		return num_want;
	}

	/**
	 * @param num_want the num_want to set
	 */
	public void setNum_want(int num_want) {
		this.num_want = num_want;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(short port) {
		this.port = port;
	}

	/**
	 * @return the connection_id
	 */
	public long getConnection_id() {
		return connection_id;
	}

	/**
	 * @return the transaction_id
	 */
	public int getTransaction_id() {
		return transaction_id;
	}

	/**
	 * @return the peer_id
	 */
	public byte[] getPeer_id() {
		return peer_id;
	}
	
	@Override
	public byte[] getData()
	{
		ByteBuffer buffer = ByteBuffer.allocate(98);
		
		buffer.putLong(this.connection_id);
		
		buffer.putInt(this.action.getValue());
		
		buffer.putInt(this.transaction_id);
		
		buffer.put(this.tracker.getInfoHash());
		
		buffer.put(this.peer_id);
		
		buffer.putLong(this.downloaded);
		
		buffer.putLong(this.left);
		
		buffer.putLong(this.uploaded);
		
		buffer.putInt(this.event);
		
		buffer.putInt(this.ip);
		
		buffer.putInt(this.key);
		
		buffer.putInt(this.num_want);
		
		buffer.putShort(this.port);
		
		return buffer.array();
	}

	@Override
	public DatagramPacket getPacket() {
		byte[] data = getData();
		DatagramPacket packet = new DatagramPacket(data, data.length, tracker.getHost(), tracker.getTrackerUri().getPort());
		return packet;
	}
	
}
