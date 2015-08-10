package files.tasks;

import peers.Peer;

public class ReadResult extends Result {

	private int pieceIndex;
	private int begin;
	private byte[] block;
	private Peer peer;


	public ReadResult(int pieceIndex, int begin, byte[] block, Peer peer) {
		super(TaskType.READ);
		this.pieceIndex = pieceIndex;
		this.begin = begin;
		this.block = block;
		this.peer = peer;
	}

	/**
	 * @return the pieceIndex
	 */
	public int getPieceIndex() {
		return pieceIndex;
	}

	/**
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * @return the block
	 */
	public byte[] getBlock() {
		return block;
	}

	/**
	 * @return the peer
	 */
	public Peer getPeer() {
		return peer;
	}
}
