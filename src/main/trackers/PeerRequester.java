package trackers;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import peers.Peer;

public class PeerRequester implements Runnable {

	private Queue<Peer> peers;
	private TrackerPool trackerPool;
	private boolean isRunning;

	public PeerRequester(Queue<Peer> peers) {
		this(peers, new TrackerPool());
	}
	
	public PeerRequester(Queue<Peer> peers, TrackerPool trackerPool) {
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
	
	public void stop(){
		this.isRunning = false;
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
