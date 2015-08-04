package peers;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import util.BitfieldHelper;
import models.Peer;
import files.Piece;
import files.TorrentFile;

public class PieceHandler {

	private static final int BLOCK_SIZE = 16384;
	
	private final TorrentFile file;
	private int blockSize;
	private final int pieceAmount;
	private final Map<Peer, Piece> pieces;
	private final byte[] haveBitfield;

	public PieceHandler(TorrentFile file) {
		this(file, BLOCK_SIZE);
	}
	
	public PieceHandler(TorrentFile file, int blockSize){
		this.file = file;
		pieces = new HashMap<Peer, Piece>();
		this.blockSize = blockSize;
		this.pieceAmount = file.getPieces().length;
		int bitfieldSize = (int) Math.ceil((double)pieceAmount / 8);
		this.haveBitfield = new byte[bitfieldSize];
	}
	
	public int getBlockSize(){
		return blockSize;
	}

	/**
	 * assigns this peer to the next missing piece
	 * @param peer
	 */
	public void assign(Peer peer) {
		int index = nextPieceIndex();
		assign(peer, index);
	}
	
	
	
	/**
	 * assigns this peer to the piece with the specified index
	 * @param peer
	 * @param index
	 */
	public void assign(Peer peer, int index) {
		byte[] pieceHash = file.getPieces()[index];
		int maxLength = file.getPieceLength();
		boolean isLastPiece = index == pieceAmount - 1; 
		int pieceLength;
		if(isLastPiece){
			//the length of the last piece is most likely irregular.
			pieceLength = (int) (file.getLength() % maxLength);
		}else{
			pieceLength = maxLength;
		}
		Piece piece = new Piece(pieceHash, pieceLength, blockSize, index);
		pieces.put(peer, piece);
	}
	
	public int nextPieceIndex(){
		int remainder = pieceAmount % 8;
		
		BitSet set = BitSet.valueOf(haveBitfield);
		int nextBitIndex = pieceAmount + remainder - 1;
		boolean done = false;
		while(!done){
			
			nextBitIndex = set.previousClearBit(nextBitIndex);
			if(nextBitIndex < remainder){
				return -1;
			}
			
			int pieceIndex = pieceAmount - nextBitIndex + remainder - 1;
			if(isAssigned(pieceIndex)){
				nextBitIndex--;
			}else{
				done = true;
				nextBitIndex = pieceIndex;
			}
			
		}
		return nextBitIndex;
	}
	
	public void unassign(Peer peer){
		pieces.remove(peer);
	}
	
	public Piece finishPiece(Peer peer){
		Piece piece = pieces.remove(peer);
		BitfieldHelper.setBit(haveBitfield, piece.getIndex());
		return piece;
	}

	public int remainingBlocks(Peer peer) {
		Piece piece = pieces.get(peer);
		return piece.remainingBlocks();
	}

	/**
	 * @param peer
	 * @return the piece this peer is assigned to, or null, if this peer isn't assigned to any piece.
	 */
	public Piece getPiece(Peer peer){
		return pieces.get(peer);
	}
	
	public int getPieceAmount(){
		return pieceAmount;
	}
	
	public boolean isAssigned(Peer peer){
		return pieces.containsKey(peer);
	}
	
	public boolean isAssigned(int pieceIndex){
		for(Piece piece : pieces.values()){
			if(piece.getIndex() == pieceIndex){
				return true;
			}
		}
		return false;
	}
}
