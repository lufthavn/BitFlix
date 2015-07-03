package trackers;

import java.io.IOException;
import java.util.List;

import models.Peer;

public class PeerRequester implements Runnable {

	private List<Peer> peers;
	private TrackerPool trackerPool;
	private boolean isRunning;

	public PeerRequester(List<Peer> peers) {
		this(peers, new TrackerPool());
	}
	
	public PeerRequester(List<Peer> peers, TrackerPool trackerPool) {
		this.peers = peers;
		this.trackerPool = trackerPool;
		this.isRunning = true;
	}
	
	public void addTracker(TrackerConnection connection){
		trackerPool.add(connection);
	}
	
	public void addTrackers(List<TrackerConnection> connections){
		trackerPool.addAll(connections);
	}

	@Override
	public void run() {
		while(isRunning){
			List<Peer> requestedPeers = null;
			try {
				requestedPeers = trackerPool.requestPeers();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			peers.addAll(requestedPeers);
		}

	}

}
