package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import models.Peer;

import org.junit.Test;

import peers.PieceHandler;
import util.Hashing;
import files.Block;
import files.Piece;
import files.TorrentFile;

public class PieceTests {

	@Test
	public void canAddBlocksToPiece() {
		String expected = "abcdefghijklmnopqrstuvwxyz";
		byte[] hash = Hashing.Sha1Hash(expected);
		Piece piece = new Piece(hash, 26, 13, 0);
		Block b1 = mock(Block.class);
		Block b2 = mock(Block.class);
		when(b1.getBytes()).thenReturn("abcdefghijklm".getBytes());
		when(b2.getBytes()).thenReturn("nopqrstuvwxyz".getBytes());
		piece.addBlock(0, b1);
		piece.addBlock(1, b2);
		
		assertTrue(piece.checkHash());
	}
	
	@Test
	public void canAddUnevenBlocksToPiece() {
		String expected = "abcdefghijklmnopqrstuvwxyz";
		byte[] hash = Hashing.Sha1Hash(expected);
		Piece piece = new Piece(hash, 26, 20, 0);
		Block b1 = mock(Block.class);
		Block b2 = mock(Block.class);
		when(b1.getBytes()).thenReturn("abcdefghijklmnopqrst".getBytes());
		when(b2.getBytes()).thenReturn("uvwxyz".getBytes());
		piece.addBlock(0, b1);
		piece.addBlock(1, b2);
		
		assertTrue(piece.checkHash());
	}
	
	@Test
	public void canGetNextBlockSize(){
		String expected = "abcdefghijklmnopqrstuvwxyz";
		byte[] hash = Hashing.Sha1Hash(expected);
		Piece piece = new Piece(hash, 100, 30, 0);
		Block b = mock(Block.class);
		when(b.getBytes()).thenReturn("abcdefghijklmnopqrstuvwxyz1111".getBytes());
		
		
		int s1 = piece.nextBlockSize();
		piece.addBlock(0, b);
		piece.addBlock(1, b);
		piece.addBlock(2, b);
		int s2 = piece.nextBlockSize();
		
		assertEquals(30, s1);
		assertEquals(10, s2);
	}
	
	@Test
	public void canAssignPeerToPiece(){
		TorrentFile file = mock(TorrentFile.class);
		when(file.getLength()).thenReturn((long) 400);
		when(file.getPieceLength()).thenReturn(100);
		byte[][] pieces =  new byte[4][];
		pieces[0] = new byte[20];
		pieces[1] = new byte[20];
		pieces[2] = new byte[20];
		pieces[3] = new byte[20];
		when(file.getPieces()).thenReturn(pieces);
		Peer peer = mock(Peer.class);
		
		PieceHandler handler = new PieceHandler(file, 30);
		handler.assign(peer);
		int remaining = handler.remainingBlocks(peer);
		assertEquals(4, remaining);
	}
}
