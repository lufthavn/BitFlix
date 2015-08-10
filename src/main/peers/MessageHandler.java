package peers;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import messages.Message;

public class MessageHandler {
	
	private Map<Peer, ByteBuffer> peers;

	public MessageHandler(){
		this.peers = new HashMap<Peer, ByteBuffer>();
	}

	/**
	 * @param peer
	 * @param length the length represented by the first four bytes in the recieved message.
	 */
	public void newMessage(Peer peer, int length) {
		//allocate a bytebuffer for the message, with the size incremented to also fit the passed length argument.
		peers.put(peer, ByteBuffer.allocate(length + Integer.BYTES).putInt(length));
	}

	public ByteBuffer bufferForPeer(Peer peer) {
		return peers.get(peer);
	}

	/**
	 * returns a Message object, parsed from this peers buffer. Throws an IllegalStateException if the buffer isn't full.
	 * @param peer the peer whose message to fetch.
	 * @return the message
	 */
	public Message messageForPeer(Peer peer) {
		ByteBuffer buffer = peers.get(peer);
		if(buffer.remaining() > 0){
			throw new IllegalStateException("The requested message is not finished. There are " + buffer.remaining() + " bytes left in this buffer.");
		}
		return Message.fromBytes(buffer);
	}
	
	/**
	 * @param peer the peer
	 * @return true if the current message for this peer is complete, and ready for parsing, false if it is not.
	 */
	public boolean messageComplete(Peer peer){
		return !peers.get(peer).hasRemaining();
	}
	
	/**
	 * @param peer
	 * @return true if this peer is ready to be assigned to a new message, if it hasn't been assigned yet, or if the current message is complete.
	 */
	public boolean ready(Peer peer){
		return !peers.containsKey(peer) || messageComplete(peer);
	}
	
	/**
	 * returns a Message object, parsed from this peers buffer, and resets the buffer with the specified length. Throws an IllegalStateException if the current buffer isn't full.
	 * @param peer the peer whose message to fetch.
	 * @param length the length of the new buffer.
	 * @return the message
	 */
	public Message resetBuffer(Peer peer, int length){
		Message m = messageForPeer(peer);
		newMessage(peer, length);
		return m;
	}

}
