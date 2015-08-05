package trackers.packets;

import java.net.DatagramPacket;

public interface ITrackerRequest {
	
	DatagramPacket getPacket();

	byte[] getData();
	
}
