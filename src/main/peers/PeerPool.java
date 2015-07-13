package peers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import files.TorrentFile;
import models.Peer;

public class PeerPool {

	private TorrentFile torrent;
	
	public PeerPool(TorrentFile torrent){
		this.torrent = torrent;
	}
	
	public void connect(Peer peer) throws IOException{
		
		ByteBuffer buffer = ByteBuffer.allocate(68);
		buffer.put((byte) 0x13);
		buffer.put("BitTorrent protocol".getBytes());
		buffer.put(new byte[8]);
		buffer.put(torrent.getInfoHash());
		buffer.put("abcdefghijklmnopqrst".getBytes());
		
		Socket socket = new Socket(peer.getAddress(), peer.getPort());
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		byte[] data = buffer.array();
		for(int i = 0; i < data.length; i++){
			socket.getOutputStream().write(data[i]);
		}
		buffer = ByteBuffer.allocate(1000);
		int i = in.read();
		while(i == 0){
			i = in.read();
		}
		byte[] rec = buffer.array();
		System.out.println("Done. response: " + i);
	}
}
