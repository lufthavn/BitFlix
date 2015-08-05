package trackers;

import java.io.IOException;
import trackers.packets.ITrackerRequest;
import trackers.packets.ITrackerResponse;

public interface ITrackerSocket {

	void send(ITrackerRequest request)
			throws IOException;

	ITrackerResponse receive(int length) throws IOException;
	
	int getPort();
}