package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {

	private Hashing() {
	}
	
	public static byte[] Sha1Hash(String input)
	{
		return Sha1Hash(input.getBytes());
	}
	
	public static byte[] Sha1Hash(byte[] input)
	{
		MessageDigest cript;
		byte[] arr = new byte[0];
		try {
			cript = MessageDigest.getInstance("SHA-1");
			cript.update(input);
			arr = cript.digest();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arr;
	}
}
