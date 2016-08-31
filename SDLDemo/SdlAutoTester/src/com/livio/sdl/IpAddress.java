package com.livio.sdl;

/**
 * Represents an IP address and port number as 2 strings - one string for the
 * address itself and another string for the associated TCP port number.
 *
 * @author Mike Burke
 *
 */
public class IpAddress {

	private String ipAddress, tcpPort;
	int connType;

	public IpAddress(String ipAddress, String tcpPort, int connType) {
		this.ipAddress = ipAddress;
		this.tcpPort = tcpPort;
		this.connType = connType;
	}

	public IpAddress(String ipAddress, int tcpPort, int connType) {
		this(ipAddress, String.valueOf(tcpPort), connType);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getTcpPort() {
		return tcpPort;
	}

	public int getConnType() {
		return connType;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(ipAddress).append(":")
				.append(tcpPort).toString();
	}

}
