package trackers;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import peers.Peer;

public class Tracker {
	
	private URI tracker_uri;
	private InetAddress host;
	private byte[] info_hash;
	private ArrayList<Peer> peers;
	
	
	public Tracker(URI uri, byte[] info_hash) throws UnknownHostException  {
		this.tracker_uri = uri;
		this.host = InetAddress.getByName(uri.getHost());
		if(info_hash.length != 20){
			throw new IllegalArgumentException("Info hash must be 20 bytes long");
		}
		this.info_hash = info_hash;
		peers = new ArrayList<Peer>();
	}
	
	public URI getTrackerUri() {
		return tracker_uri;
	}
	
	public InetAddress getHost(){
		return host;
	}
	
	public byte[] getInfoHash() {
		return info_hash;
	}
	
	public void addPeer(Peer peer)
	{
		peers.add(peer);
	}
	
	public void addAllPeers(List<Peer> peers)
	{
		peers.addAll(peers);
	}
	
	public ArrayList<Peer> getPeers()
	{
		return peers;
	}
}
