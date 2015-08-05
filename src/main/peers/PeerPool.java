package peers;

import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
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
					}else{
						break;
					}
				}
			}
		}
		connector.connectToPeers();
	}
}
