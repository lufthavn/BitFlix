package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import models.Peer;

import org.junit.Test;

import trackers.Action;
import trackers.ITrackerSocket;
import trackers.Tracker;
import trackers.TrackerConnection;
import trackers.TrackerConnection.ITransactionIdGenerator;
import trackers.TrackerPool;
import trackers.packets.AnnounceRequest;
import trackers.packets.AnnounceResponse;
import trackers.packets.ConnectionResponse;

public class TrackerTests {

	@Test
	public void CanSerializeAnnounceRequest() throws UnknownHostException, URISyntaxException
	{
		byte[] hash = new byte[]{0x05, (byte) 0x9b, (byte) 0x8b, (byte) 0x88, 0x0f, (byte) 0x84, 0x41, 0x50, (byte) 0x9e, (byte) 0xc8, (byte) 0xa6, 0x5b, 0x50, (byte) 0xb4, (byte) 0xc6, (byte) 0xae, 0x74, (byte) 0xeb, (byte) 0xea, 0x76};
		byte[] peer_id = new byte[]{(byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b, (byte) 0x9b};
		
		Tracker t = mock(Tracker.class);
		when(t.getInfoHash()).thenReturn(hash);
		
		AnnounceRequest request = new AnnounceRequest(t, 4, 4, peer_id, 0, 1308622847, 12, 30, (short) 6553);
		
		ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(4);//connection id
		buffer.putInt(1);//action
		buffer.putInt(4);//transaction id
		buffer.put(hash);//info hash
		buffer.put(peer_id);//peer id
		buffer.putLong(0);//downloaded
		buffer.putLong(0);//left
		buffer.putLong(0);//uploaded
		buffer.putInt(0);//event
		buffer.putInt(1308622847);//ip
		buffer.putInt(12);//key
		buffer.putInt(30);//num want
		buffer.putShort((short) 6553);//port
		
		assertArrayEquals(buffer.array(), request.getData());
	}
	
	@Test
	public void CanParseAnnounceResponse() {
		
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.putInt(1);//action
		buffer.putInt(32);//transaction id
		buffer.putInt(60);//interval
		buffer.putInt(3000);//leechers
		buffer.putInt(4000);//seeders
		
		AnnounceResponse response = new AnnounceResponse(buffer.array());
		assertEquals(Action.ANNOUNCE, response.getAction());
		assertEquals(32, response.getTransaction_id());
		assertEquals(60, response.getInterval());
		assertEquals(3000, response.getLeechers());
		assertEquals(4000, response.getSeeders());
	}
	
	@Test
	public void CanParseAnnounceResponsePeers()
	{
		ByteBuffer buffer = ByteBuffer.allocate(38);
		buffer.putInt(1);//action
		buffer.putInt(32);//transaction id
		buffer.putInt(60);//interval
		buffer.putInt(3000);//leechers
		buffer.putInt(4000);//seeders
		
		buffer.putInt(1297020500);
		buffer.putShort((short) 6889);
		
		buffer.putInt(1308622847);
		buffer.putShort((short) 65535);
		
		buffer.putInt(1297023060);
		buffer.putShort((short) 1);
		
		AnnounceResponse response = new AnnounceResponse(buffer.array());
		assertEquals(3, response.getPeers().size());
		assertEquals("77.78.246.84", response.getPeers().get(0).getAddress().getHostAddress());
		assertEquals(6889, response.getPeers().get(0).getPort());
	}
	
	@Test
	public void responseCanHandleErrors(){
		ByteBuffer buffer = ByteBuffer.allocate(11);
		buffer.putInt(3);
		buffer.putInt(32);
		buffer.put("ABC".getBytes());
		byte[] data = buffer.array();
		
		AnnounceResponse response = new AnnounceResponse(buffer.array());
		assertFalse(response.isValid(32));
		assertNotNull(response.getErrorResponse());
		assertTrue("Expected: ABC Actual: " +  response.getErrorResponse().getMessage(), response.getErrorResponse().getMessage().equals("ABC"));
	}
	
	@Test
	public void trackerConnectionCanConnect() throws IOException
	{
		ITrackerSocket socket = mock(ITrackerSocket.class);
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putInt(0);//action
		buffer.putInt(32);//tran. id
		buffer.putLong(69L);//con. id
		byte[] connect = buffer.array();
		
		buffer = ByteBuffer.allocate(38);
		buffer.putInt(1);//action
		buffer.putInt(32);//transaction id
		buffer.putInt(60);//interval
		buffer.putInt(3000);//leechers
		buffer.putInt(4000);//seeders
		buffer.putInt(1297020500);
		buffer.putShort((short) 6889);
		buffer.putInt(1308622847);
		buffer.putShort((short) 65535);
		buffer.putInt(1297023060);
		buffer.putShort((short) 1);
		byte[] announce = buffer.array();
		when(socket.receive(any(int.class))).thenReturn(new ConnectionResponse(connect), new AnnounceResponse(announce));
		
		byte[] hash = new byte[]{0x05, (byte) 0x9b, (byte) 0x8b, (byte) 0x88, 0x0f, (byte) 0x84, 0x41, 0x50, (byte) 0x9e, (byte) 0xc8, (byte) 0xa6, 0x5b, 0x50, (byte) 0xb4, (byte) 0xc6, (byte) 0xae, 0x74, (byte) 0xeb, (byte) 0xea, 0x76};
		Tracker tracker = mock(Tracker.class);
		when(tracker.getInfoHash()).thenReturn(hash);		
		
		ITransactionIdGenerator generator = mock(ITransactionIdGenerator.class);
		when(generator.generate()).thenReturn(32);
		
		TrackerConnection connection = new TrackerConnection(tracker, socket, generator);
		
		boolean canConnect = connection.connect();
		List<Peer> peers = connection.announce();
		assertTrue("Unable to connect", canConnect);
		assertEquals(3, peers.size());
	}
	
	@Test
	public void trackerConnectionCanHandleTimeout() throws IOException
	{
		ITrackerSocket socket = mock(ITrackerSocket.class);

		
		when(socket.receive(any(int.class))).thenThrow(new SocketTimeoutException());
		
		byte[] hash = new byte[]{0x05, (byte) 0x9b, (byte) 0x8b, (byte) 0x88, 0x0f, (byte) 0x84, 0x41, 0x50, (byte) 0x9e, (byte) 0xc8, (byte) 0xa6, 0x5b, 0x50, (byte) 0xb4, (byte) 0xc6, (byte) 0xae, 0x74, (byte) 0xeb, (byte) 0xea, 0x76};
		Tracker tracker = mock(Tracker.class);
		when(tracker.getInfoHash()).thenReturn(hash);
		
		TrackerConnection connection = new TrackerConnection(tracker, socket);
		
		boolean canConnect = connection.connect();
		assertFalse("Test failed: ", canConnect);
	}
	
	
	
	@Test
	public void TrackerPoolCanConnect() throws UnknownHostException, IOException
	{
		ArrayList<Peer> peers = new ArrayList<Peer>();
		peers.add(new Peer(1297020500, 6889));
		peers.add(new Peer(1308622847, 65535));
		peers.add(new Peer(1297023060, 1));
		
		TrackerConnection connection1 = mock(TrackerConnection.class);
		when(connection1.getIntervalElapsed()).thenReturn(new Date());
		when(connection1.requestPeers()).thenReturn(peers);
		
		TrackerConnection connection2 = mock(TrackerConnection.class);
		when(connection2.getIntervalElapsed()).thenReturn(new Date());
		when(connection2.requestPeers()).thenReturn(null);
		
		TrackerConnection connection3 = mock(TrackerConnection.class);
		when(connection3.getIntervalElapsed()).thenReturn(new Date());
		when(connection3.requestPeers()).thenReturn(peers);
		
		TrackerPool pool = new TrackerPool();
		pool.add(connection1);
		pool.add(connection2);
		pool.add(connection3);
		
		ArrayList<Peer> receivedPeers = new ArrayList<>(pool.requestPeers());
		assertEquals(6, receivedPeers.size());
		assertEquals("77.78.246.84", receivedPeers.get(0).getAddress().getHostAddress());
	}
	
	@Test
	public void trackerPoolRemovesInvalidTrackers() throws IOException
	{
		ITrackerSocket socket = mock(ITrackerSocket.class);
		ITrackerSocket badSocket = mock(ITrackerSocket.class);
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putInt(0);//action
		buffer.putInt(32);//tran. id
		buffer.putLong(69L);//con. id
		byte[] connect = buffer.array();
		
		buffer = ByteBuffer.allocate(38);
		buffer.putInt(1);//action
		buffer.putInt(32);//transaction id
		buffer.putInt(60);//interval
		buffer.putInt(3000);//leechers
		buffer.putInt(4000);//seeders
		buffer.putInt(1297020500);
		buffer.putShort((short) 6889);
		buffer.putInt(1308622847);
		buffer.putShort((short) 65535);
		buffer.putInt(1297023060);
		buffer.putShort((short) 1);
		byte[] announce = buffer.array();
		when(socket.receive(any(int.class))).thenReturn(new ConnectionResponse(connect), new AnnounceResponse(announce)).thenReturn(new ConnectionResponse(connect), new AnnounceResponse(announce));
		when(badSocket.receive(any(int.class))).thenThrow(new SocketTimeoutException());
		
		byte[] hash = new byte[]{0x05, (byte) 0x9b, (byte) 0x8b, (byte) 0x88, 0x0f, (byte) 0x84, 0x41, 0x50, (byte) 0x9e, (byte) 0xc8, (byte) 0xa6, 0x5b, 0x50, (byte) 0xb4, (byte) 0xc6, (byte) 0xae, 0x74, (byte) 0xeb, (byte) 0xea, 0x76};
		Tracker tracker = mock(Tracker.class);
		when(tracker.getInfoHash()).thenReturn(hash);
		
		ITransactionIdGenerator generator = mock(ITransactionIdGenerator.class);
		when(generator.generate()).thenReturn(32);
		
		TrackerConnection connection1 = new TrackerConnection(tracker, socket, generator);
		TrackerConnection connection2 = new TrackerConnection(tracker, socket, generator);
		TrackerConnection connection3 = new TrackerConnection(tracker, badSocket, generator);
		
		TrackerPool pool = new TrackerPool();
		pool.add(connection1);
		pool.add(connection2);
		pool.add(connection3);
		
		for(int i = 0; i < 5; i++){
			pool.requestPeers();
		}
		
		assertEquals(2, pool.size());
	}

}
