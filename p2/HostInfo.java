public class HostInfo {

	public int hostId;
	public String hostName;
	public String ipAddress;
	public int portNo;
	public Boolean active;

	public String stringValue() {
		String s = hostId + "," + hostName + "," + ipAddress + "," + portNo;
		return s;
	}

	public Boolean equals(HostInfo dest) {
		return ipAddress.equals(dest.ipAddress) && portNo == dest.portNo;
	}

	public HostInfo(String hostString) {
		String []parts = hostString.split(",");
		hostId = Integer.parseInt(parts[0]);
		hostName = parts[1];
		ipAddress = parts[2];
		portNo = Integer.parseInt(parts[3]);
	}

	public HostInfo(int _hostId, String _hostName, String _ipAddress, int _portNo) {
		hostId = _hostId;
		hostName = _hostName;
		ipAddress = _ipAddress;
		portNo = _portNo;
		active = true;
	}

	public HostInfo(int _hostId, String _hostName, String _ipAddress, int _portNo, Boolean _active) {
		hostId = _hostId;
		hostName = _hostName;
		ipAddress = _ipAddress;
		portNo = _portNo;
		active = _active;
	}
}