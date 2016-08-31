package com.livio.sdl.ext;

public class MyIpAddress {
	private String ipAddress, tcpPort;
	int connType;

	public MyIpAddress(String ipAddress, String tcpPort, int connType) {
		this.ipAddress = ipAddress;
		this.tcpPort = tcpPort;
		this.connType = connType;
	}

	public MyIpAddress(String ipAddress, int tcpPort, int connType) {
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
