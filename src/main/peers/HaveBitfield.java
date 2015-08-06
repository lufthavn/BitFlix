package peers;

import java.util.Arrays;
import java.util.BitSet;

public class HaveBitfield {
	private byte[] bits;
	private int trailLength;
	private long pieceAmount;

	/**
	 * @param pieceAmount the amount of pieces in torrent this bitfield keeps track of.
	 */
	public HaveBitfield(long pieceAmount){
		this.pieceAmount = pieceAmount;
		int bitfieldLength = (int) Math.ceil((double)pieceAmount / Byte.SIZE);
		this.trailLength  = (int) (pieceAmount % Byte.SIZE);
		bits = new byte[bitfieldLength];
		
	}
	
	public HaveBitfield(byte[] bitField, long expectedPieceAmount) {
		this.pieceAmount = (bitField.length * Byte.SIZE);
		int trail = (int) (pieceAmount % expectedPieceAmount);
		this.pieceAmount -= trail;
		this.trailLength  = (int) (pieceAmount % Byte.SIZE);
		this.bits = bitField;
	}

	/**
	 * @param index the index of the piece
	 */
	public void setHasPiece(int index){
		
		int byteIndex = index / Byte.SIZE;
		int bitIndex = index % Byte.SIZE;
		
		/*
		 * initial:  01000000
		 * 128 >> 7: 00000001
		 * or:       01000001
		 */
		
		bits[byteIndex] |= 128 >> bitIndex;
	}
	
	/**
	 * @param index the index of the piece
	 * @return
	 */
	public boolean hasPiece(int index){
		int byteIndex = index / Byte.SIZE;
		int bitIndex = index % Byte.SIZE;
		int shift = bits[byteIndex] << bitIndex;
		int and = shift & 128;
		
		return and == 128;
	}
	
	public double percentComplete(){
		long setBits = BitSet.valueOf(bits).cardinality();

		return  setBits * 100 / (double)pieceAmount;
	}

	public boolean isFinished() {
		
		byte lastByteValue = (byte) (-1 << Byte.SIZE - trailLength);
		
		for(int i = 0; i < bits.length; i++){
			byte b = bits[i];
			
			if(b != (byte)-1){
				if(i == bits.length - 1 && b != lastByteValue){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @return the bitfield, represented as a byte array. 
	 */
	public byte[] getBytes(){
		return bits;
	}
}
