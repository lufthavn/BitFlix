package peers;

import java.util.HashMap;
import java.util.Map;

import files.Piece;
import files.TorrentFile;

public class PieceHandler {

	private static final int BLOCK_SIZE = 16384;
	
	private final TorrentFile file;
	private int blockSize;
	private final int pieceAmount;
	private final Map<Peer, Piece> pieces;
	private final HaveBitfield haveBitfield;

	public PieceHandler(TorrentFile file) {
		this(file, BLOCK_SIZE);
	}
	
	public PieceHandler(TorrentFile file, int blockSize){
		this.file = file;
		pieces = new HashMap<Peer, Piece>();
		this.blockSize = blockSize;
		this.pieceAmount = file.getPieces().length;
		this.haveBitfield = new HaveBitfield(pieceAmount);
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
		for(int i = 0; i < pieceAmount; i++){
			if(!haveBitfield.hasPiece(i) && !isAssigned(i)){
				return i;
			}
		}
		return -1;
	}
	
	public void unassign(Peer peer){
		pieces.remove(peer);
	}
	
	public Piece finishPiece(Peer peer){
		Piece piece = pieces.remove(peer);
		haveBitfield.setHasPiece(piece.getIndex());
		return piece;
	}
	
	public void finishPiece(Piece piece){
		pieces.values().remove(piece);
		haveBitfield.setHasPiece(piece.getIndex());
	}

	public int remainingBlocks(Peer peer) {
		Piece piece = pieces.get(peer);
		return piece.remainingBlocks();
	} 

	public boolean isFinished(){
		return haveBitfield.isFinished();
	}
	
	public HaveBitfield getHaveBitField(){
		return haveBitfield;
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
