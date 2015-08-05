package trackers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import trackers.packets.AnnounceResponse;
import trackers.packets.ConnectionResponse;
import trackers.packets.ErrorResponse;
import trackers.packets.ITrackerRequest;
import trackers.packets.ITrackerResponse;

public class TrackerSocket implements ITrackerSocket {

	private static final int DEFAULT_TIMEOUT = 15000;
	
	private DatagramSocket socket;

	public TrackerSocket() throws SocketException
	{
		this.socket = new DatagramSocket();
	}
	
	public TrackerSocket(int port) throws SocketException
	{
		this.socket = new DatagramSocket(port);
	}
	
	/* (non-Javadoc)
	 * @see trackers.ITrackerConnection#send(trackers.packets.TrackerRequest)
	 */
	@Override
	public void send(ITrackerRequest request) throws IOException
	{
		DatagramPacket packet = request.getPacket();
		socket.send(packet);
	}
	
	/* (non-Javadoc)
	 * @see trackers.ITrackerConnection#receive(int)
	 */
	@Override
	public ITrackerResponse receive(int length) throws IOException
	{
		socket.setSoTimeout(DEFAULT_TIMEOUT);
		byte[] data = new byte[length];
		DatagramPacket responsePacket = new DatagramPacket(data, data.length);
		socket.receive(responsePacket);
		
		byte[] receivedData = Arrays.copyOfRange(responsePacket.getData(), 0, responsePacket.getLength());
		Action action = determineAction(receivedData);
		ITrackerResponse response;
		switch(action){
			case CONNECT:
				response = new ConnectionResponse(receivedData);
				break;
			case ANNOUNCE:
				response = new AnnounceResponse(receivedData);
				break;
			case ERROR:
				response = new ErrorResponse(receivedData);
				break;
			default:
				response = null;
				break;
		}
		return response;
	}

	private Action determineAction(byte[] data){
		int i = ByteBuffer.wrap(data).getInt();
		return Action.fromValue(i);
	}
	
	@Override
	public int getPort() {
		return socket.getLocalPort();
	}

}
