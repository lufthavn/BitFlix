package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import messages.BitfieldMessage;
import messages.HaveMessage;
import messages.Message;
import messages.MessageType;
import messages.PieceMessage;
import messages.RequestMessage;

import org.junit.Test;

import peers.Peer;
import files.TorrentFile;
import util.BitfieldHelper;

public class MessageTests {

	@Test
	public void canParseChoke() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(1);
		buffer.put((byte) 0);
		
		Message m = Message.fromBytes(buffer);
		assertEquals(MessageType.CHOKE, m.getType());
	}
	
	@Test
	public void canParseUnChoke() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(1);
		buffer.put((byte) 1);
		
		Message m = Message.fromBytes(buffer);
		assertEquals(MessageType.UNCHOKE, m.getType());
	}
	
	@Test
	public void canParseHave(){
		ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.putInt(1);
		buffer.put((byte) 4);
		buffer.putInt(1337);
		
		HaveMessage m = (HaveMessage)Message.fromBytes(buffer);
		assertEquals(MessageType.HAVE, m.getType());
		assertEquals(1337, m.getIndex());
	}

	@Test
	public void canParseBitField(){
		ByteBuffer buffer = ByteBuffer.allocate(10);
		buffer.putInt(6);
		buffer.put((byte) 5);
		buffer.put("abcde".getBytes());
		
		BitfieldMessage m = (BitfieldMessage) Message.fromBytes(buffer);
		assertEquals(MessageType.BITFIELD, m.getType());
		assertArrayEquals("abcde".getBytes(), m.getBitField());
	}
	
	@Test
	public void canParseRequest(){
		ByteBuffer buffer = ByteBuffer.allocate(17);
		buffer.putInt(1);
		buffer.put((byte) 6);
		buffer.putInt(1337);
		buffer.putInt(5);
		buffer.putInt(16000);
		
		RequestMessage m = (RequestMessage)Message.fromBytes(buffer);
		assertEquals(MessageType.REQUEST, m.getType());
		assertEquals(1337, m.getIndex());
		assertEquals(5, m.getBegin());
		assertEquals(16000, m.getLength());
	}
	
	@Test
	public void canParsePiece(){
		ByteBuffer buffer = ByteBuffer.allocate(39);
		buffer.putInt(35);
		buffer.put((byte) 7);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.put("abcdefghijklmnopqrstuvwxyz".getBytes());
		
		PieceMessage m = (PieceMessage) Message.fromBytes(buffer);
		assertEquals(MessageType.PIECE, m.getType());
		assertArrayEquals("abcdefghijklmnopqrstuvwxyz".getBytes(), m.getBlock());
	}
	
	@Test
	public void canInterpretBitfield(){
		int i = 1333333337; //01001111 01111001 00001101 01011001
		byte[] bitfield = ByteBuffer.allocate(4).putInt(i).array();
		boolean isAvailable = BitfieldHelper.isAtIndex(bitfield, 9); // bit at index 9 is 1, so should return true.
		assertTrue(isAvailable);
	}
	
	@Test
	public void canSetBitField(){
		TorrentFile file = mock(TorrentFile.class);
		when(file.getLength()).thenReturn((long) 225);
		Peer peer = new Peer("192.30.252.128", 34);
		peer.initializeHaveBitfield(file);
		byte[] bitfield = peer.getHaveBitField();
		assertEquals(29, bitfield.length);
	}
	
	@Test
	public void canSetHasPiece(){
		TorrentFile file = mock(TorrentFile.class);
		when(file.getLength()).thenReturn((long) 225);
		Peer peer = new Peer("192.30.252.128", 34);
		peer.initializeHaveBitfield(file); //29 empty bytes
		peer.setHasPiece(7); //10000000 00000000
		peer.setHasPiece(12);//00000000 00001000
		assertEquals(1, peer.getHaveBitField()[0]);
		assertEquals(8, peer.getHaveBitField()[1]);
	}
}
