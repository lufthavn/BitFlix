package peers;

import java.io.IOException;

import models.Peer;

public interface IPeerConnector {
	public void connect(Peer peer) throws IOException;
	public void connectToPeers() throws IOException;
	public int currentConnected();
}
