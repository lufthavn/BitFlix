package peers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import files.TorrentFile;

public class PeerPool {

	private TorrentFile torrent;
	private Queue<Peer> waitingPeers;
	private IPeerConnector connector;
	private int limit;
	private long timeOfLastPeerCheck;
	
	public PeerPool(TorrentFile torrent, Queue<Peer> peers, IPeerConnector connector, int maxConnectedPeers) throws IOException{
		this.torrent = torrent;
		this.connector = connector;
		this.limit = maxConnectedPeers;
		this.waitingPeers = peers;
	}
	
	public void addPeer(Peer peer) throws IOException{
		waitingPeers.add(peer);
	}
	
	public void addManyPeers(Collection<Peer> peers){
		waitingPeers.addAll(peers);
	}
	
	
	public void connectToPeers() throws IOException{
		if(System.currentTimeMillis() - timeOfLastPeerCheck >= 15000){
			int currentConnected = connector.currentConnected();
			if(currentConnected < limit){
				int amount = limit - currentConnected;
				while(amount > 0){
					Peer peer = waitingPeers.poll();
					if(peer != null){
						connector.connect(peer);
						amount--;
					}
				}
			}
		}
		connector.connectToPeers();
	}
}
