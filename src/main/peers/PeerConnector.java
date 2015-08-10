package peers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import files.Block;
import files.IPieceTaskBuffer;
import files.Piece;
import files.TorrentFile;
import files.tasks.ReadResult;
import files.tasks.ReadTask;
import files.tasks.Result;
import files.tasks.WriteResult;
import files.tasks.WriteTask;
import messages.BitfieldMessage;
import messages.ChokeMessage;
import messages.HaveMessage;
import messages.InterestMessage;
import messages.KeepAliveMessage;
import messages.Message;
import messages.PieceMessage;
import messages.RequestMessage;

public class PeerConnector implements IPeerConnector {
	
	
	private Selector selector;
	private byte[] infohash;
	private TorrentFile file;
	
	private PieceHandler pieceHandler;
	private MessageHandler messageHandler;
	
	private final IPieceTaskBuffer pieceQueue;

	public PeerConnector(TorrentFile file, IPieceTaskBuffer pieceQueue) throws IOException {
		selector  = Selector.open();
		this.file = file;
		this.infohash = file.getInfoHash();
		pieceHandler = new PieceHandler(file);
		messageHandler = new MessageHandler();
		this.pieceQueue = pieceQueue;
	}

	@Override
	public void connect(Peer peer) throws IOException {
		try{
			SocketChannel channel = SocketChannel.open();
			channel.socket().setSoTimeout(10000);
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(peer.getAddress(), peer.getPort()));
			
			channel.register(selector, SelectionKey.OP_CONNECT, peer);
		}catch(Exception e){
			//TODO: logging
		}
	}
	
	@Override
	public void connectToPeers() throws IOException{
		selector.select(5000);
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = keys.iterator();
		
		for(Result r : pieceQueue.getCompletedTasks()){
			switch(r.getType()){
			case READ:
				ReadResult result = (ReadResult)r;
				PieceMessage message = new PieceMessage(result.getPieceIndex(), result.getBegin(), result.getBlock());
				result.getPeer().addMessageToQueue(message);
				break;
			case WRITE:
				Piece p = ((WriteResult)r).getPiece();
				pieceHandler.finishPiece(p);
				addMessageToAllPeers(new HaveMessage(p.getIndex()));
				System.out.println("piece successfully written to hard drive. Progress: " + pieceHandler.getHaveBitField().percentComplete() + "%.");
				break;
			default:
				break;
			}

		}
		
		while(iterator.hasNext()){
			SelectionKey key = iterator.next();
			SocketChannel channel = (SocketChannel) key.channel();
			Peer peer = (Peer)key.attachment();
			
			if(key.isValid() && key.isConnectable()){
				finishTCPConnect(key);
			}
			
			if(System.currentTimeMillis() - peer.getTimeOfLastMessage() >= 30000){
				if(key.isValid()){
					sendKeepAlive(key);
					if(key.isValid()){
						System.out.println("Successfully written keepalive. Current amount of peers: " + selector.keys().size());
					}
				}
			}
			
			if(!peer.isChokingThis()){
				int index = pieceHandler.nextPieceIndex();
				if(peer.hasPiece(index) && !pieceHandler.isAssigned(peer)){
					pieceHandler.assign(peer);
					Piece p = pieceHandler.getPiece(peer);
					int begin = p.indexOfNextBlock();
					
					Message m = new RequestMessage(index, begin, p.nextBlockSize());
					peer.addMessageToQueue(m);
				}
				
			}
			
			if(peer.hasMessages()){
				while(peer.hasMessages()){
					Message message = peer.getNextMessage();
					sendMessage(key, message, peer);
				}
			}
			
			if(key.isValid() && peer.readyForHandshake()){
				sendHandshake(key);
				peer.handshakeSent(true);
			}
			
			//only reads from here...
			if(!key.isValid() || !key.isReadable()){
				continue;
			}
			
			if(!peer.isConnected()){
				finishHandshake(key);
				if(key.isValid()){
					peer.addMessageToQueue(new BitfieldMessage(pieceHandler.getHaveBitField().getBytes()));
					System.out.println("connected to peer with id: " + peer.getPeerId());
				}
			}else{
				Message m = receiveMessage(key);
				if(m != null){
					handleMessage(m, peer);
					if(key.isValid()){
						System.out.println("Peer sent a " + m.getType().toString() + " request.");
					}
				}
			}
		}
	}

	/**
	 * Attempts to connect to the socket. If the connection failed, then this method removes the passed key from the selector.
	 * If the connection succeeds, then it reregisters the key, with SelectionKey.OP_WRITE, so it can start the BitTorrent handshake
	 * @param key the SelectionKey of the socket to connect to
	 */
	private void finishTCPConnect(SelectionKey key){
		SocketChannel channel = (SocketChannel) key.channel();
		try{
			if(channel.finishConnect()){
				key.interestOps(SelectionKey.OP_WRITE);
			}
		}catch(IOException e){
			removeConnection(key);
		}
	}
	
	/**
	 * sends the data for a BitTorrent handshake to a peer. If the connection fails, then this method removes it from the selector.
	 * if the write is successful, then this method reregisters the key for SelectionKey.OP_READ | SelectionKey.OP_WRITE, so it can receive 
	 * the other end of the handshake.
	 * @param key
	 */
	private void sendHandshake(SelectionKey key){
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(68);
		buffer.put((byte) 0x13);
		buffer.put("BitTorrent protocol".getBytes());
		buffer.put(new byte[8]);
		buffer.put(infohash);
		buffer.put("abcdefghijklmnopqrst".getBytes());
		buffer.position(0);
		
		try{
			channel.write(buffer);
		}catch(IOException e){
			removeConnection(key);
			return;
		}
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}
	
	/**
	 * receives the other end of the sent handshake. Validates the response, and removes the key from the selector, if the response is invalid,
	 * or if the connection fails.
	 * if the handshake succeeds, then it updates the attached peer with the received information, and sets its connection status to true.
	 * @param key
	 */
	private void finishHandshake(SelectionKey key){
		SocketChannel channel = (SocketChannel) key.channel();
		Peer peer = (Peer) key.attachment();
		
		byte[] handshake = readFromSocket(channel, 68);
		
		if(handshake == null || handshake[0] != 19){
			removeConnection(key);
			return;
		}
		
		String peerId = new String(Arrays.copyOfRange(handshake, 48, 68));
		peer.setPeerId(peerId);
		peer.setConnected(true);
	}
	
	/**
	 * Sends a KEEPALIVE message to a peer, and updates its timestamp. Removes this key if the connection fails.
	 * @param key the key of the socket to send to
	 */
	private void sendKeepAlive(SelectionKey key){
		SocketChannel channel = (SocketChannel) key.channel();
		Peer peer = (Peer) key.attachment();
		
		ByteBuffer buffer = new KeepAliveMessage().getBytes();
		buffer.flip();
		while(buffer.hasRemaining()){
			try{
				channel.write(buffer);
			}catch(IOException e){
				removeConnection(key);
				return;
			}
		}
		peer.setTimeOfLastMessage(System.currentTimeMillis());
	}
	
	private void sendMessage(SelectionKey key, Message message, Peer peer) {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = message.getBytes();
		buffer.flip();
		while(buffer.hasRemaining()){
			try{
				channel.write(buffer);
			}catch(IOException e){
				removeConnection(key);
				return;
			}
		}
		peer.setTimeOfLastMessage(System.currentTimeMillis());
	}
	
	/**
	 * Receives a message from a peer. Removes this key, if the connection fails.
	 * @param key the key for this peer's socket
	 * @return the message, if the connection succeeds, null if it fails.
	 */
	private Message receiveMessage(SelectionKey key){
		SocketChannel channel = (SocketChannel) key.channel();
		Peer peer = (Peer) key.attachment();
		
		//get the length of the message
		if(messageHandler.ready(peer)){
			int length = readLength(channel);
			if(length < 0){
				removeConnection(key);
				return null;
			}
			messageHandler.newMessage(peer, length);
		}
		
		//read the actual message
		boolean success = readFromSocket(channel, messageHandler.bufferForPeer(peer));
		if(!success){
			removeConnection(key);
			return null;
		}
		if(messageHandler.messageComplete(peer)){
			return messageHandler.messageForPeer(peer);
		}
		return null;
	}
	
	private void removeConnection(SelectionKey key) {
		System.out.println("removing peer with address " + ((Peer)key.attachment()).getAddress());
		pieceHandler.unassign((Peer)key.attachment());
		try {
			key.channel().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		key.cancel();
	}

	private int readLength(SocketChannel channel)  {
		byte[] bytes = readFromSocket(channel, 4);
		if(bytes == null){
			return -1;
		}
		return ByteBuffer.wrap(bytes).getInt(0);
	}
	
	/**
	 * Reads the specified amount of bytes from the channel. 
	 * This method blocks until all the bytes have been read. Is therefore inefficient for large messages, and should only be used 
	 * for very small ones.
	 * @param channel the channel to read from.
	 * @param length the amount of bytes to read from this channel.
	 * @return the read bytes
	 */
	private byte[] readFromSocket(SocketChannel channel, int length){
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int read = 0;
		boolean isValid = true;
		while(read < length && isValid){
			try{
				int r = channel.read(buffer);
				if (r >= 0){
					read += r;
				}else{
					isValid = false;
				}
			}catch(IOException e){
				//TODO: logging
				isValid = false;
			}
		}
		if(isValid){
			return buffer.array();
		}else{
			return null;
		}
	}
	
	/**
	 * @param channel
	 * @param buffer
	 * @return true if the read was successful, false if not. If the read was unsuccessful, this connection should be terminated.
	 */
	private boolean readFromSocket(SocketChannel channel, ByteBuffer buffer){
		int read = 0;
		boolean isValid = true;
		try{
			read = channel.read(buffer);
			isValid = read >= 0;
		}catch(IOException e){
			//TODO: logging
			isValid = false;
		}
		
		return isValid;
	}
	
	private void handleMessage(Message message, Peer peer){
		switch(message.getType()){
		case BITFIELD:{
				BitfieldMessage bitfield = (BitfieldMessage)message;
				byte[] bits = bitfield.getBitField();
				
				peer.setHaveBitfield(new HaveBitfield(bits, this.file.getPieces().length));
	
				double percentage =  peer.getHaveBitField().percentComplete();
				if(percentage >= 95){
					//yo, this peer is gooood.
					InterestMessage i = new InterestMessage(2);
					peer.addMessageToQueue(i);
					peer.setInterested(true);
				}else{
					peer.setInterested(false);
				}
				System.out.println("A peer sent a BITFIELD request, and has " + percentage + "% of the pieces");
			}
			break;
		case CANCEL:
			break;
		case CHOKE:
			pieceHandler.unassign(peer);
			peer.setChokingThis(true);
			break;
		case HAVE:
			HaveMessage have = (HaveMessage)message;
			if(peer.getHaveBitField() == null){
				peer.initializeHaveBitfield(file);
			}
			peer.setHasPiece(have.getIndex());
			
			HaveBitfield bitfield = peer.getHaveBitField();

			double percentage =  bitfield.percentComplete();
			if(percentage >= 97){
				//yo, this peer is gooood.
				InterestMessage i = new InterestMessage(2);
				peer.addMessageToQueue(i);
				peer.setInterested(true);
			}else{
				InterestMessage i = new InterestMessage(2);
				peer.setInterested(false);
				peer.addMessageToQueue(i);
			}
			
			break;
		case INTERESTED:
			peer.setInterestedInThis(true);
			peer.setChoking(false);
			peer.addMessageToQueue(new ChokeMessage(1));
			break;
		case KEEPALIVE:
			break;
		case PIECE:
			Piece piece = pieceHandler.getPiece(peer);
			//if the peer sends a block for a piece it isn't assigned to, or a block to a piece that is complete, it should be removed.
			if(piece == null || piece.remainingBlocks() == 0){
				pieceHandler.unassign(peer);
				return;
			}
			PieceMessage data = (PieceMessage)message;
			Block block = new Block(data.getIndex(), data.getBegin(), data.getBlock());
			piece.addBlock(block);
			System.out.println(piece.indexOfNextBlock() + " : " + piece.nextBlockSize());
			if(piece.indexOfNextBlock() >= 0){
				RequestMessage r = new RequestMessage(piece.getIndex(), piece.indexOfNextBlock(), piece.nextBlockSize());
				peer.addMessageToQueue(r);
			}else{
				boolean success = piece.checkHash();
				if(success){
					pieceQueue.addTask(new WriteTask(piece));
				}else{
					pieceHandler.unassign(peer);
				}
			}

			break;
		case PORT:
			break;
		case REQUEST:
			RequestMessage m = (RequestMessage)message;
			if(pieceHandler.getHaveBitField().hasPiece(m.getIndex())){
				pieceQueue.addTask(new ReadTask(m.getIndex(), m.getBegin(), m.getLength(), peer));
			}
			//TODO: disconnect if piece is unavailable
			break;
		case UNCHOKE:{
			peer.setChokingThis(false);
			ChokeMessage c = new ChokeMessage(1);
			peer.addMessageToQueue(c);
			break;
		}
		case UNINTERESTED:{
			peer.setChoking(true);
			peer.setInterestedInThis(false);
			ChokeMessage c = new ChokeMessage(0);
			peer.addMessageToQueue(c);
			break;
		}
		default:
			break;
		
		}
	}

	/**
	 * adds a message to send to all the peers this client is currently connected to (completed the handshake)
	 * @param message
	 */
	private void addMessageToAllPeers(Message message) {
		for(SelectionKey key : selector.keys()){
			Peer peer = (Peer) key.attachment();
			if(peer.isConnected()){
				peer.addMessageToQueue(message);
			}
		}
		
	}

	@Override
	public int currentConnected() {
		return selector.keys().size();
	}
}
