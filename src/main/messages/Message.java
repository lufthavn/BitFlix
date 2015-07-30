package messages;

import java.nio.ByteBuffer;

public abstract class Message {
	
	public abstract MessageType getType();
	public abstract ByteBuffer getBytes();
	
	public static Message fromBytes(byte[] bytes){
		return fromBytes(ByteBuffer.wrap(bytes));
	}
	
	public static Message fromBytes(ByteBuffer buffer){
		Message message;
		buffer.position(0);
		int length = buffer.getInt();
		if(length == 0){
			message = new KeepAlive();
		}else{
			int id = (byte)buffer.get();
			
			switch(id){
				case 0:
				case 1:
					message = new ChokeStatus(id);
				break;
				case 2:
				case 3:
					message = new InterestStatus(id);
				break;
				case 4:
					message = new Have(buffer.getInt());
				break;
				case 5:{
					int bitFieldOffset = buffer.position() - 4;
					
					int bitFieldLength = length - bitFieldOffset;
					byte[] bitField = new byte[bitFieldLength];
					buffer.get(bitField, 0, bitFieldLength);
					
					message = new Bitfield(bitField);
				}
				break;
				case 6:{
					int index = buffer.getInt();
					int begin = buffer.getInt();
					int blockLength = buffer.getInt();
					message = new Request(index, begin, blockLength);
				}
				break;
				case 7:{
					int index = buffer.getInt();
					int begin = buffer.getInt();
					int blockOffset = buffer.position() - 4;
					
					int blockLength = length - blockOffset;
					byte[] block = new byte[blockLength];
					buffer.get(block, 0, blockLength);
					
					message = new Piece(index, begin, block);
				}
				break;
				case 8:{
					int index = buffer.getInt();
					int begin = buffer.getInt();
					int blockLength = buffer.getInt();
					
					message = new Cancel(index, begin, blockLength);
				}
				break;
				case 9:
					message = new Port(buffer.getInt());
				break;
				default:
					message = null;
				break;
			}
		}
		
		return message;
	}

}
