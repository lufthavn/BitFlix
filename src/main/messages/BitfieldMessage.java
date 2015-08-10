package messages;

import java.nio.ByteBuffer;

public class BitfieldMessage extends Message {

	private byte[] bitField;

	public BitfieldMessage(byte[] bits){
		this.bitField = bits;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.BITFIELD;
	}
	
	public byte[] getBitField(){
		return bitField;
	}

	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer.allocate(5 + bitField.length)
				.putInt(bitField.length + 1) //length
				.put((byte) MessageType.BITFIELD.getValue())
				.put(bitField);
	}

}
