package messages;

import java.nio.ByteBuffer;

import peers.HaveBitfield;

public class BitfieldMessage extends Message {

	private byte[] bitField;

	public BitfieldMessage(HaveBitfield haveBitfield){
		this.bitField = haveBitfield.getBytes();
	}
	
	@Override
	public MessageType getType(){
		return MessageType.BITFIELD;
	}
	
	public HaveBitfield getBitField(){
		return new HaveBitfield(bitField);
	}

	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer.allocate(5 + bitField.length)
				.putInt(bitField.length + 1) //length
				.put((byte) MessageType.BITFIELD.getValue())
				.put(bitField);
	}

}
