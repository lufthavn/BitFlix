package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import util.Hashing;
import files.Block;
import files.Piece;

public class PieceTests {

	@Test
	public void canAddBlocksToPiece() {
		String expected = "abcdefghijklmnopqrstuvwxyz";
		byte[] hash = Hashing.Sha1Hash(expected);
		Piece piece = new Piece(hash, 26, 13);
		Block b1 = mock(Block.class);
		Block b2 = mock(Block.class);
		when(b1.getBytes()).thenReturn("abcdefghijklm".getBytes());
		when(b2.getBytes()).thenReturn("nopqrstuvwxyz".getBytes());
		piece.addBlock(0, b1);
		piece.addBlock(1, b2);
		
		assertTrue(piece.checkHash());
	}

}
