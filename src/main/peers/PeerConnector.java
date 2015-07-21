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

import messages.Message;
import models.Peer;

public class PeerConnector implements IPeerConnector {
	
	private Selector selector;
	private byte[] infohash;

	public PeerConnector(byte[] infohash) throws IOException {
		selector  = Selector.open();
		this.infohash = infohash;
	}

	@Override
	public boolean addPeer(Peer peer) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(68);
		buffer.put((byte) 0x13);
		buffer.put("BitTorrent protocol".getBytes());
		buffer.put(new byte[8]);
		buffer.put(infohash);
		buffer.put("abcdefghijklmnopqrst".getBytes());
		buffer.position(0);
		
		
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(peer.getAddress(), peer.getPort()));
		long timeOfStart = System.currentTimeMillis();
		while(!channel.finishConnect()){
			if(System.currentTimeMillis() - timeOfStart >= 5000){
				System.out.println("Handshake timed out.");
				channel.close();
				return false;
			}
		}
		channel.write(buffer);
		
		ByteBuffer rec = ByteBuffer.allocate(68);
		int totalRead = 0;
		while(totalRead < 68){
			int read = 0;
			 read = channel.read(rec);
			if(read < 0){
				channel.close();
				return false;
			}else{
				totalRead += read;
			}
			
		}
		
		if(rec.get(0) != 19){
			return false;
		}
		
		String peerId = new String(Arrays.copyOfRange(rec.array(), 48, 68));
		peer.setPeerId(peerId);
		channel.register(selector, SelectionKey.OP_READ, peer);
		return true;
	}
	
	public void connectToPeers() throws IOException{
		selector.select(5000);
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = keys.iterator();
		
		while(iterator.hasNext()){
			SelectionKey key = iterator.next();
			SocketChannel channel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(4);
			if(key.isReadable()){
				boolean isvalid = true;
				int read = 0; 
				while(read < 4 && isvalid){
					int r = channel.read(buffer);
					if (r >= 0){
						read += r;
					}else{
						isvalid = false;
					}
				}
				if(isvalid){
					int length = buffer.getInt(0);
					byte[] receivedData = buffer.array();
					receivedData = Arrays.copyOfRange(receivedData, 0, read);
					buffer = ByteBuffer.allocate(length + 4);
					buffer.put(receivedData);
					while(read < length && isvalid){
						int r = channel.read(buffer);
						if (r >= 0){
							read += r;
						}else{
							isvalid = false;
						}
					}
					
					if(isvalid){
						Message m = Message.fromBytes(buffer);
						if(m != null)
							System.out.println("Peer sent a " + m.getType().toString() + " request.");
					}else{
						channel.close();
						key.cancel();
					}
				}else{
					channel.close();
					key.cancel();
				}
				
			}
		}
	}

}
