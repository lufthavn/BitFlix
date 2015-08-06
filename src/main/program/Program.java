package program;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import peers.IPieceQueue;
import peers.Peer;
import peers.PeerConnector;
import peers.PeerPool;
import peers.PieceQueue;
import trackers.PeerRequester;
import trackers.Tracker;
import trackers.TrackerConnection;
import trackers.TrackerPool;
import trackers.TrackerSocket;
import files.Piece;
import files.TorrentFile;
import files.TorrentFileWriter;

public class Program {

	public Program() {
	}
	
	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException
	{
		//Decoder decoder = new Decoder("path/to/torrent/file.torrent");
		TorrentFile file = new TorrentFile(args[0]);
		TrackerSocket socket = null;
		try {
			socket = new TrackerSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TrackerPool pool = new TrackerPool();
		System.out.println("Initial tracker size: " + file.getTrackerList().size());
		//TODO: Implement HTTP trackers, and create specialized subclasses and an ITracker interface for both HTTP and UDP trackers.
		//For the moment, just fetch the URLs with UDP as protocol.
		for(String trackerURL : file.getTrackerList()){
			Tracker tracker = null;
			try {
				URI uri = new URI(trackerURL);
				if(uri.getScheme().equals("udp")){
					tracker = new Tracker(uri, file.getInfoHash());
					TrackerConnection connection = new TrackerConnection(tracker, socket);
					pool.add(connection);
				}
			} catch (URISyntaxException | SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				System.err.println("unknown host: " + e.getMessage() + ". Removing from pool.");
				pool.remove(tracker);
			}
		}
		
		Queue<Peer> peers = new ConcurrentLinkedQueue<Peer>();
		PeerRequester peerRequester = new PeerRequester(peers, pool);
		Thread peerThread = new Thread(peerRequester, "PeerRequesterThread");
		peerThread.setDaemon(true);
		peerThread.start();
		
		IPieceQueue pieceQueue = new PieceQueue();
		TorrentFileWriter writer = new TorrentFileWriter(args[1], file, pieceQueue);
		Thread writerThread = new Thread(writer, "WriterThread");
		writerThread.setDaemon(true);
		writerThread.start();
		
		PeerPool p = new PeerPool(file, peers, new PeerConnector(file, pieceQueue), 10);
		
		for(;;){
			try{
				p.connectToPeers();	
			}catch(SocketException e){
				System.out.println("Cant connect: " + e.getMessage());
			}catch(ClosedChannelException e){
				System.out.println("Timed out, closed channel. Message: " + e.getMessage());
			}catch(IOException e){
				System.out.println("Connection error: " + e.getMessage());
				e.printStackTrace();
			}
		}
//		System.out.println("All done.");
//		System.exit(0);
	}
	
}
