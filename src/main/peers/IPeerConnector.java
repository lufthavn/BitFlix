package peers;

import java.io.IOException;

import models.Peer;

public interface IPeerConnector {
	public boolean addPeer(Peer peer) throws IOException;
	public void connectToPeers() throws IOException;
}
