package peers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import files.Block;
import files.Piece;
import files.TorrentFile;
import messages.Bitfield;
import messages.ChokeStatus;
import messages.Have;
import messages.InterestStatus;
import messages.KeepAlive;
import messages.Message;
import messages.Request;
import models.Peer;

public class PeerConnector implements IPeerConnector {
	
	
	private Selector selector;
	private byte[] infohash;
	private TorrentFile file;
	
	private PieceHandler handler;
	
	private final BlockingQueue<Piece> pieceQueue;

	public PeerConnector(TorrentFile file, BlockingQueue<Piece> pieceQueue) throws IOException {
		selector  = Selector.open();
		this.file = file;
		this.infohash = file.getInfoHash();
		handler = new PieceHandler(file);
		this.pieceQueue = pieceQueue;
	}

	@Override
	public void connect(Peer peer) throws IOException {
		try{
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(peer.getAddress(), peer.getPort()));
			
			channel.register(selector, SelectionKey.OP_CONNECT, peer);
		}catch(Exception e){
			//TODO: logging
		}
	}
	
	public void connectToPeers() throws IOException{
		selector.select(5000);
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = keys.iterator();
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
				int index = handler.nextPieceIndex();
				if(peer.hasPiece(index) && !handler.isAssigned(index) && !handler.isAssigned(peer)){
					handler.assign(peer);
					Piece p = handler.getPiece(peer);
					int begin = p.indexOfNextBlock();
					
					Message m = new Request(index, begin, p.nextBlockSize());
					peer.addMessageToQueue(m);
				}
				
//				int lastPieceIndex = handler.getPieceAmount() - 1;
//				if(!handler.isAssigned(peer) && peer.hasPiece(lastPieceIndex) && !handler.isAssigned(lastPieceIndex)){
//					handler.assign(peer, lastPieceIndex);
//					Piece p = handler.getPiece(peer);
//					int begin = p.indexOfNextBlock();
//					int bs = p.nextBlockSize();
//					
//					Message m = new Request(lastPieceIndex, begin, bs);
//					peer.addMessageToQueue(m);
//				}
				
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
		
		ByteBuffer buffer = new KeepAlive().getBytes();
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
		
		//get the length of the message
		int length = readLength(channel);
		if(length < 0){
			removeConnection(key);
			return null;
		}
		
		//read the actual message
		byte[] message = readFromSocket(channel, length);
		if(message == null){
			removeConnection(key);
			return null;
		}
		
		//parse the received bytes to a Message object
		ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
		buffer.putInt(length);
		buffer.put(message);
		Message m = Message.fromBytes(buffer);
		return m;
	}
	
	private void removeConnection(SelectionKey key) {
		System.out.println("removing peer with address " + ((Peer)key.attachment()).getAddress());
		handler.unassign((Peer)key.attachment());
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
	
	private void handleMessage(Message message, Peer peer){
		switch(message.getType()){
		case BITFIELD:{
				Bitfield bitfield = (Bitfield)message;
				peer.setHaveBitfield(bitfield.getBitField());
				int remainder = file.getPieces().length % 8;
				long totalBits = (bitfield.getBitField().length * 8) - remainder;
				long setBits = BitSet.valueOf(bitfield.getBitField()).cardinality();
	
				double percentage =  setBits * 100 / totalBits;
				if(percentage >= 97){
					//yo, this peer is gooood.
					InterestStatus i = new InterestStatus(2);
					peer.addMessageToQueue(i);
					peer.setInterested(true);
				}else{
					InterestStatus i = new InterestStatus(2);
					peer.setInterested(false);
					peer.addMessageToQueue(i);
				}
				System.out.println("A peer sent a BITFIELD request, and has " + percentage + "% of the pieces");
			}
			break;
		case CANCEL:
			break;
		case CHOKE:
			peer.setChokingThis(true);
			break;
		case HAVE:
			Have have = (Have)message;
			if(peer.getHaveBitField() == null){
				peer.initializeHaveBitfield(file);
			}
			peer.setHasPiece(have.getIndex());
			
			byte[] bitfield = peer.getHaveBitField();
			int remainder = file.getPieces().length % 8;
			long totalBits = (bitfield.length * 8) - remainder;
			long setBits = BitSet.valueOf(bitfield).cardinality();

			double percentage =  setBits * 100 / totalBits;
			if(percentage >= 97){
				//yo, this peer is gooood.
				InterestStatus i = new InterestStatus(2);
				peer.addMessageToQueue(i);
				peer.setInterested(true);
			}else{
				InterestStatus i = new InterestStatus(2);
				peer.setInterested(false);
				peer.addMessageToQueue(i);
			}
			
			break;
		case INTERESTED:
			break;
		case KEEPALIVE:
			break;
		case PIECE:
			Piece piece = handler.getPiece(peer);
			messages.Piece data = (messages.Piece)message;
			Block block = new Block(data.getBlock());
			piece.addBlock(data.getBegin() / handler.getBlockSize(), block);
			System.out.println(piece.indexOfNextBlock() + " : " + piece.nextBlockSize());
			if(piece.indexOfNextBlock() >= 0){
				Request r = new Request(piece.getIndex(), piece.indexOfNextBlock(), piece.nextBlockSize());
				peer.addMessageToQueue(r);
			}else{
				boolean success = piece.checkHash();
				if(success){
					Piece p = handler.finishPiece(peer);
					try {
						pieceQueue.put(p);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					handler.unassign(peer);
				}
				System.out.println("piece checked. Hash success: " + success);
			}

			break;
		case PORT:
			break;
		case REQUEST:
			break;
		case UNCHOKE:
			peer.setChokingThis(false);
			ChokeStatus c = new ChokeStatus(1);
			peer.addMessageToQueue(c);
			break;
		case UNINTERESTED:
			break;
		default:
			break;
		
		}
	}

	@Override
	public int currentConnected() {
		return selector.keys().size();
	}
}
