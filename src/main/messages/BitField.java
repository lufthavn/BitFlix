package messages;

public class BitField extends Message {

	private byte[] bitField;

	public BitField(byte[] bitField){
		this.bitField = bitField;
	}
	
	@Override
	public MessageType getType(){
		return MessageType.BITFIELD;
	}
	
	public byte[] getBitField(){
		return bitField;
	}

}
