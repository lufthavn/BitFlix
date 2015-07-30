package util;

public class BitfieldHelper {

	public static boolean isAtIndex(byte[] bitfield, int index){
		int byteIndex = index / 8;
		int bitIndex = index % 8;
		int shift = bitfield[byteIndex] << bitIndex;
		int and = shift & 128;
		
		return and == 128;
		
	}
	
	public static void setBit(byte[] bitfield, int index){
		int byteIndex = index / 8;
		int bitIndex = index % 8;
		
		/*
		 * initial:  01000000
		 * 128 >> 7: 00000001
		 * or:       01000001
		 */
		
		bitfield[byteIndex] |= 128 >> bitIndex;
	}
}
