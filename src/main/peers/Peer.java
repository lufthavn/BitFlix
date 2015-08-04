package peers;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import messages.Message;
import util.BitfieldHelper;
import files.TorrentFile;

public class Peer {

	private InetAddress address;
	private int port;
	private String peerId;
	private byte[] haveBitfield = null;
	private long timeOfLastMessage;

	private boolean handshakeSent;
	private boolean isConnected;
	
	private boolean isChoking;
	private boolean isChokingThis;
	
	private boolean isInterested;
	private boolean isInterestedInThis;
	
	private Queue<Message> messageQueue;
	
	/**
	 * Creates this peer from the specified connection info.
	 * @param ip the IP address of this peer, in textual form.
	 * @param port this peers port
	 */
	public Peer(String ip, int port) {
		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = port;
		
		isConnected = false;
		isChoking = true;
		isChokingThis = true;
		isInterested = false;
		isInterestedInThis = false;
		
		messageQueue = new LinkedList<Message>();
	}
	
	/**
	 * Creates this peer from the specified connection info.
	 * @param ip the IP address of this peer, in numeric form.
	 * @param port this peers port
	 */
	public Peer(int ip, int port)
	{
		byte[] bytes = BigInteger.valueOf(ip).toByteArray();
		try {
			address = InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = port;
		
		//TODO: duplicated code
		isConnected = false;
		isChoking = true;
		isChokingThis = true;
		isInterested = false;
		isInterestedInThis = false;
		
		messageQueue = new LinkedList<Message>();
	}

	/**
	 * adds a message to this peers to-be-written message queue.
	 * @param message
	 */
	public void addMessageToQueue(Message message){
		messageQueue.add(message);
	}
	
	/**
	 * @return the next message in this peers message queue, null if the queue is empty.
	 */
	public Message getNextMessage(){
		return messageQueue.poll();
	}
	
	/**
	 * @return true if this peer has awaiting messages, false if not.
	 */
	public boolean hasMessages(){
		return !messageQueue.isEmpty();
	}
	
	/**
	 * creates a new empty HAVE bitfield, used to track which pieces a peer has to offer.
	 * @param file the metainfo of the torrent this peer participates in.
	 */
	public void initializeHaveBitfield(TorrentFile file) {
		initializeHaveBitfield(file.getLength());
	}
	
	/**
	 * creates a new empty HAVE bitfield, used to track which pieces a peer has to offer.
	 * @param length the total length of the torrent this peer participates in.
	 */
	public void initializeHaveBitfield(long length) {
		int arrayLength = (int) Math.ceil((double)length / 8);
		haveBitfield = new byte[arrayLength];
	}
	
	
	/**
	 * Sets the specified bitfield as this peers HAVE bitfield. Should be used when a peer sends a <a href="https://wiki.theory.org/BitTorrentSpecification#bitfield:_.3Clen.3D0001.2BX.3E.3Cid.3D5.3E.3Cbitfield.3E">BITFIELD</a> message.
	 * @param bitfield the bitfield received from a peer.
	 */
	public void setHaveBitfield(byte[] bitfield){
		haveBitfield = bitfield;
	}
	
	/**
	 * @return this peers HAVE bitfield, used to track a peers piece availability. Returns null if the bitfield hasn't been initialized.
	 */
	public byte[] getHaveBitField() {
		return haveBitfield;
	}

	/**
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	
	public String getPeerId(){
		return peerId;
	}
	
	public void handshakeSent(boolean sent){
		handshakeSent = sent;
	}
	
	/**
	 * @return true if this client is not yet connected to this peer, nor has initied the handshake with this peer.
	 */
	public boolean readyForHandshake(){
		return !isConnected && !handshakeSent;
	}
	
	/**
	 * @param connected true if this client is currently connected to this peer, and has completed the handshake
	 */
	public void setConnected(boolean connected){
		isConnected = connected;
	}
	
	/**
	 * @return true if this client is currently connected to this peer, and has completed the handshake, false if not.
	 */
	public boolean isConnected(){
		return isConnected;
	}
	
	/**
	 * @return true if this client is choking this peer, false if not
	 */
	public boolean isChoking() {
		return isChoking;
	}

	/**
	 * @param isChoking set whether this client is choking this peer
	 */
	public void setChoking(boolean isChoking) {
		this.isChoking = isChoking;
	}

	/**
	 * @return true if this peer is choking this client, false if not
	 */
	public boolean isChokingThis() {
		return isChokingThis;
	}

	/**
	 *@param isChoking set whether this peer is choking this client
	 */
	public void setChokingThis(boolean isChokingThis) {
		this.isChokingThis = isChokingThis;
	}

	/**
	 * @return true if this client is interested this peer, false if not
	 */
	public boolean isInterested() {
		return isInterested;
	}

	/**
	 * @param isInterested set whether this client in interested in this peer
	 */
	public void setInterested(boolean isInterested) {
		this.isInterested = isInterested;
	}

	/**
	 * @return true if this peer is interested in this client, false if not
	 */
	public boolean isInterestedInThis() {
		return isInterestedInThis;
	}

	/**
	 * @param isInterestedInThis set whether this peer in interested in this client
	 */
	public void setInterestedInThis(boolean isInterestedInThis) {
		this.isInterestedInThis = isInterestedInThis;
	}

	
	/**
	 * the system time when a message was last sent to this peer
	 * @param time the difference, measured in milliseconds, between January 1st 1970, and the time of the last sent message.
	 */
	public void setTimeOfLastMessage(long time){
		this.timeOfLastMessage = time;
	}
	
	/**
	 * @return the system time when a message was last sent to this peer
	 */
	public long getTimeOfLastMessage(){
		return timeOfLastMessage;
	}

	/**
	 * specifies that this peer has the piece at the given index
	 * @param index the index of the piece
	 */
	public void setHasPiece(int index) {
		BitfieldHelper.setBit(haveBitfield, index);
	}
	
	public boolean hasPiece(int index){
		if(haveBitfield == null){
			return false;
		}
		return BitfieldHelper.isAtIndex(haveBitfield, index);
	}
}
