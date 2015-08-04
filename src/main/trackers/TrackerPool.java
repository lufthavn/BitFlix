package trackers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import peers.Peer;

public class TrackerPool extends ArrayList<TrackerConnection> {
	
	private Date ealiestReannounceDeadline;
	private final ReentrantLock lock = new ReentrantLock(true);
	private final Condition waitInterval = lock.newCondition();

	/**
	 * Requests peers from the trackers contained in the pool. Calling this method can block the current thread. 
	 * To avoid busy waiting when reannouncing, then suspends the thread, until the shortest interval has elapsed. 
	 * This class is therefore intended to be used in a daemon worker thread.
	 * @return The peers returned from the tracker.
	 * @throws IOException
	 */
	public List<Peer> requestPeers() throws IOException
	{
		waitForDeadline();

		List<Peer> peers = new ArrayList<>();
		Iterator<TrackerConnection> iterator = this.iterator();
		
		while(iterator.hasNext()){
			TrackerConnection connection = iterator.next();
			try{
				if(connection.isStale()){
					iterator.remove();
				}else{
					List<Peer> receivedPeers = connection.requestPeers();
					if(receivedPeers != null){
						peers.addAll(receivedPeers);
					}
				}
				
			}catch(UnknownHostException e){
				System.out.println("Unknown host: " + e.getMessage());
				iterator.remove();
			}
		}
		this.sort(new TrackerConnection.TrackerConnectionComparator());
		ealiestReannounceDeadline = this.get(0).getIntervalElapsed();
		return peers;
	}
	
	public void waitForDeadline(){
		if(ealiestReannounceDeadline != null){
			try {
				lock.lock();
				System.out.println("waiting for the shortest interval to pass. Time: " + ealiestReannounceDeadline.toString() + " seconds. ");
				waitInterval.awaitUntil(ealiestReannounceDeadline);
				System.out.println("Reannouncing...");
				lock.unlock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public synchronized boolean add(TrackerConnection connection){
		lock.lock();
		waitInterval.signalAll();
		lock.unlock();
		return super.add(connection);
	}

}
