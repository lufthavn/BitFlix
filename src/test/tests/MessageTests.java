package tests;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import messages.BitField;
import messages.Have;
import messages.Message;
import messages.MessageType;
import messages.Piece;
import messages.Request;

import org.junit.Test;

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
		
		Have m = (Have)Message.fromBytes(buffer);
		assertEquals(MessageType.HAVE, m.getType());
		assertEquals(1337, m.getIndex());
	}

	@Test
	public void canParseBitField(){
		ByteBuffer buffer = ByteBuffer.allocate(10);
		buffer.putInt(6);
		buffer.put((byte) 5);
		buffer.put("abcde".getBytes());
		
		BitField m = (BitField) Message.fromBytes(buffer);
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
		
		Request m = (Request)Message.fromBytes(buffer);
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
		
		Piece m = (Piece) Message.fromBytes(buffer);
		assertEquals(MessageType.PIECE, m.getType());
		assertArrayEquals("abcdefghijklmnopqrstuvwxyz".getBytes(), m.getBlock());
	}
}
