package trackers.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import trackers.Action;
import trackers.Tracker;

public class ConnectionRequest implements ITrackerRequest {

	private static final long DEFAULT_CONNECTION_ID = 0x41727101980L;
	
	private Action action;
	private int transaction_id;
	private Tracker tracker;
	
	public ConnectionRequest(Tracker tracker, int transaction_id) {
		this.tracker = tracker;
		this.action = Action.CONNECT;
		this.transaction_id = transaction_id;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the transaction_id
	 */
	public long getTransaction_id() {
		return transaction_id;
	}

	@Override
	public byte[] getData()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteBuffer buffer;
		
		try {
			buffer = ByteBuffer.allocate(Long.BYTES);
			buffer.putLong(DEFAULT_CONNECTION_ID);
			bos.write(buffer.array());
			
			buffer = ByteBuffer.allocate(Integer.BYTES);
			buffer.putInt(this.action.getValue());
			bos.write(buffer.array());
			
			buffer = ByteBuffer.allocate(Integer.BYTES);
			buffer.putInt(this.transaction_id);
			bos.write(buffer.array());
			
			return bos.toByteArray();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new byte[0];
		}
		
	}

	@Override
	public DatagramPacket getPacket(){
		
		byte[] data = getData();
		DatagramPacket packet = new DatagramPacket(data, data.length, tracker.getHost(), tracker.getTrackerUri().getPort());
		return packet;
	}
	
}
