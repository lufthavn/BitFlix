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
import java.util.Iterator;
import java.util.Set;

import files.TorrentFile;
import models.Peer;

public class PeerPool {

	private TorrentFile torrent;
	private IPeerConnector connector;
	
	public PeerPool(TorrentFile torrent, IPeerConnector connector) throws IOException{
		this.torrent = torrent;
		this.connector = connector;
		
	}
	
	public void connect(Peer peer) throws IOException{
		boolean success = connector.addPeer(peer);
		if(success){
			System.out.println("added peer with id: " + peer.getPeerId());
		}
		
	}
	
	public void connectToPeers() throws IOException{
		connector.connectToPeers();
	}
}
