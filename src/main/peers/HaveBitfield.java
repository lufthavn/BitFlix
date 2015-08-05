package peers;

import java.util.BitSet;

public class HaveBitfield {
	private byte[] bits;
	private int trailLength;
	private long length;

	/**
	 * @param length the amunt of pieces in torrent this bitfield keeps track of.
	 */
	public HaveBitfield(long length){
		this.length = length;
		int bitfieldLength = (int) Math.ceil((double)length / 8);
		bits = new byte[bitfieldLength];
		this.trailLength  = (int) (length % Byte.SIZE);
	}
	
	public HaveBitfield(byte[] bitField) {
		this.length = bitField.length;
		this.bits = bitField;
		this.trailLength  = (int) (length % Byte.SIZE);
	}

	/**
	 * @param index the index of the piece
	 */
	public void setHasPiece(int index){
		
		int byteIndex = index / 8;
		int bitIndex = index % 8;
		
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
		int byteIndex = index / 8;
		int bitIndex = index % 8;
		int shift = bits[byteIndex] << bitIndex;
		int and = shift & 128;
		
		return and == 128;
	}
	
	public double percentComplete(){
		long totalBits = (length * 8) - trailLength;
		long setBits = BitSet.valueOf(bits).cardinality();

		return  setBits * 100 / (double)totalBits;
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
