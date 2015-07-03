package trackers.packets;

import java.net.DatagramPacket;
import java.net.URI;
import java.net.UnknownHostException;

public interface ITrackerRequest {
	
	DatagramPacket getPacket();

	byte[] getData();
	
}
