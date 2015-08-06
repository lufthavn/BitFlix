package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import peers.Peer;
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
	
	@Test
	public void canCalculateNextPiece(){
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
		
		int n0 = handler.nextPieceIndex();
		handler.assign(peer, 0);
		
		int n1 = handler.nextPieceIndex();
		Piece piece = handler.finishPiece(peer);
		handler.assign(peer, 1);
		
		int n2 = handler.nextPieceIndex();
		handler.finishPiece(peer);
		
		handler.assign(peer, 2);
		handler.finishPiece(peer);
		
		handler.assign(peer, 3);
		handler.finishPiece(peer);
		
		int n3 = handler.nextPieceIndex();
		
		assertEquals(0, piece.getIndex());
		assertEquals(0, n0);
		assertEquals(1, n1);
		assertEquals(2, n2);
		assertEquals(-1, n3);
	}
	
	@Test
	public void canSelfAssignPieces(){
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
		
		int n0 = handler.nextPieceIndex();
		handler.assign(peer);
		
		int n1 = handler.nextPieceIndex();
		Piece piece = handler.finishPiece(peer);
		handler.assign(peer);
		
		int n2 = handler.nextPieceIndex();
		handler.finishPiece(peer);
		
		handler.assign(peer);
		handler.finishPiece(peer);
		
		handler.assign(peer);
		handler.finishPiece(peer);
		
		int n3 = handler.nextPieceIndex();
		
		assertEquals(0, piece.getIndex());
		assertEquals(0, n0);
		assertEquals(1, n1);
		assertEquals(2, n2);
		assertEquals(-1, n3);
	}
	
	@Test
	public void canFinishTorrent() throws URISyntaxException{
		URL res = this.getClass().getResource("../resources/BigBuckBunny.torrent");
		String torrent = new URI(res.toExternalForm()).getPath();
		TorrentFile file = new TorrentFile(torrent);
		
		Peer peer = mock(Peer.class);
		PieceHandler handler = new PieceHandler(file);
		
		for(int i = 0; i < handler.getPieceAmount(); i++){
			handler.assign(peer);
			handler.finishPiece(peer);
		}
		assertTrue(handler.isFinished());
		double percent = handler.getHaveBitField().percentComplete();
		assertEquals(100, (int)percent);
	}
}
