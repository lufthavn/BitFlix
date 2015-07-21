package models;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer {

	private InetAddress address;
	private int port;
	private String peerId;
	
	public Peer(String ip, int port) {
		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = port;
	}
	
	public Peer(int ip, int port)
	{
		byte[] bytes = BigInteger.valueOf(ip).toByteArray();
		
		try {
			address = InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = port;
	}

	/**
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	
	public String getPeerId(){
		return peerId;
	}

}
