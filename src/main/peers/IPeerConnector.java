package peers;

import java.io.IOException;

public interface IPeerConnector {
	public void connect(Peer peer) throws IOException;
	public void connectToPeers() throws IOException;
	public int currentConnected();
}
