package program;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import peers.PeerConnector;
import peers.PeerPool;
import models.Peer;
import trackers.PeerRequester;
import trackers.Tracker;
import trackers.TrackerConnection;
import trackers.TrackerPool;
import trackers.TrackerSocket;
import bencoding.Decoder;
import files.TorrentFile;

public class Program {

	public Program() {
	}
	
	public static void main(String[] args) throws InterruptedException, IOException
	{
		//Decoder decoder = new Decoder("path/to/torrent/file.torrent");
		Decoder decoder = new Decoder("C:/Users/Tobias/Desktop/avengers.torrent");
		TorrentFile file = new TorrentFile(decoder);
		TrackerSocket socket = null;
		try {
			socket = new TrackerSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TrackerPool pool = new TrackerPool();
		System.out.println("Initial tracker size: " + file.getTrackerList().size());
		//TODO: Implement HTTP trackers, and create specialized subclasses and a Tracker interface for both HTTP and UDP trackers.
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
		
		List<Peer> peers = new ArrayList<Peer>();
		PeerRequester peerRequester = new PeerRequester(peers, pool);
		Thread peerThread = new Thread( peerRequester, "PeerRequesterThread");
		peerThread.setDaemon(false);
		peerThread.start();
		Thread.sleep(30000);
		peerRequester.stop();
		peerThread.join();
		PeerPool p = new PeerPool(file, new PeerConnector(file.getInfoHash()));
		
		for(int i = 0; i < 50; i ++){
			Peer peer = peers.get(i);
			try{
				p.connect(peer);
			}catch(SocketException e){
				System.out.println("Cant connect: " + e.getMessage());
			}catch(ClosedChannelException e){
				System.out.println("Timed out, closed channel. Message: " + e.getMessage());
			}catch(IOException e){
				System.out.println("Connection error: " + e.getMessage());
			}
		}
		for(;;){
			try{
				p.connectToPeers();	
			}catch(SocketException e){
				System.out.println("Cant connect: " + e.getMessage());
			}catch(ClosedChannelException e){
				System.out.println("Timed out, closed channel. Message: " + e.getMessage());
			}catch(IOException e){
				System.out.println("Connection error: " + e.getMessage());
			}
			Thread.sleep(50);
		}
//		System.out.println("All done.");
//		System.exit(0);
	}
	
}
