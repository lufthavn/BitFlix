package bencoding;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Decoder {
	
	private static final byte DICTIONARY_DELIMITER = 0x64; // d
	private static final byte LIST_DELIMITER = 0x6C; // l
	private static final byte INTEGER_DELIMITER = 0x69; // i
	private static final byte END_DELIMITER = 0x65; // e
	private static final byte COLON = 0x3A; // :
	private static final byte MINUSSIGN = 0x2D; // -
	
	private BufferedInputStream stream;
	public Decoder(String file)
	{
		try {
			this.stream = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<BElement> decode()
	{		
		ArrayList<BElement> elements = new ArrayList<BElement>();
		try {
				int next = stream.read();
				
				String torrent = "";
				while(next != -1)
				{
					byte nextByte = (byte)next;
					BElement element = decodeElement(nextByte);
					elements.add(element);
					next = stream.read();
				}
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return elements;
	}

	private byte getNextByte(){
		try{
			return (byte) stream.read();
		}catch(IOException e){
			e.printStackTrace();
		}
		return (Byte) null;
	}
	
	private BElement decodeElement(byte nextByte) {
		BElement toReturn = null;
		switch(nextByte){
		case DICTIONARY_DELIMITER:
			toReturn = decodeDictionary();
			break;
		case LIST_DELIMITER:
			toReturn = decodeList();
			break;
		case INTEGER_DELIMITER:
			toReturn = decodeInteger();
			break;
		default:
			toReturn = decodeString(nextByte);
			break;
		}
		return toReturn;
	}

	private BElement decodeElement()
	{
		return decodeElement(getNextByte());
	}
	
	private BDictionary decodeDictionary()
	{
		BDictionary dictionary = new BDictionary();
		byte nextByte = getNextByte();
		while(nextByte != END_DELIMITER)
		{
			String key = decodeUtf8String(nextByte);
			BElement element = decodeElement();
			dictionary.put(key, element);
			nextByte = getNextByte();
		}
		return dictionary;
	}
	
	private BList decodeList() {
		BList toReturn = new BList();
		byte nextByte = getNextByte();
		while(nextByte != END_DELIMITER)
		{
			BElement element = decodeElement(nextByte);
			toReturn.add(element);
			nextByte = getNextByte();
		}
		
		return toReturn;
	}	
	
	private BInteger decodeInteger() {
		byte nextByte = getNextByte();
		boolean isNegative = nextByte == MINUSSIGN;
		long value = 0;
		if(isNegative){
			nextByte = getNextByte();
		}
		
		while(nextByte != END_DELIMITER)
		{
			int digit = nextByte - '0';
			value *= 10;
			value += digit;
			nextByte = getNextByte();
		}
		
		if(isNegative){
			value = value * -1;
		}
		
		return new BInteger(value);
	}
	
	private BString decodeString(byte next){
		byte[] bytes = getStringBytes(next);
		return new BString(bytes);
	}
	
	private String decodeRawString()
	{
		return decodeUtf8String(getNextByte());
	}
	
	private String decodeUtf8String(byte next)
	{
		byte[] bytes = getStringBytes(next);
		String toReturn = "";
		try {
			toReturn = new String(bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toReturn;
		
	}
	
	private byte[] getStringBytes(byte next)
	{
		String lengthValue = "";
		byte nextByte = next;
		while(nextByte != COLON)
		{
			lengthValue += (char)nextByte;
			nextByte = getNextByte();
		}
		
		int length = Integer.parseInt(lengthValue);
		byte[] bytes = new byte[length];
		try{
			for(int i = 0; i < length; i++)
			{
				bytes[i] = getNextByte();
			}
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		return bytes;
	}
	

}
