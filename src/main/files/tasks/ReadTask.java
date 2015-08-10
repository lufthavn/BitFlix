package files.tasks;

import peers.Peer;

public class ReadTask extends Task {

	private int pieceIndex;
	private int begin;
	private int length;
	private Peer peer;
	

	public ReadTask(int pieceIndex, int begin, int length, Peer peer) {
		super(TaskType.READ);
		this.pieceIndex = pieceIndex;
		this.begin = begin;
		this.length = length;
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
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return the peer
	 */
	public Peer getPeer() {
		return peer;
	}

}
