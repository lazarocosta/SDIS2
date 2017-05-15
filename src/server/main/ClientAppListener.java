package server.main;

import java.rmi.RemoteException;

import server.protocol.*;

public class ClientAppListener implements ClientInterface{

	@Override
	public void backup(String protocolVersion, String filePath, int replicationDeg) throws RemoteException {
		if (protocolVersion.equals("1.0") || Peer.protocolVersion.equals(protocolVersion))
			new Backup(protocolVersion, filePath, replicationDeg);
	}

	@Override
	public void restore(String protocolVersion, String filePath) throws RemoteException {
		if (protocolVersion.equals("1.0") || Peer.protocolVersion.equals(protocolVersion))
			new Restore(protocolVersion, filePath);
	}

	@Override
	public void delete(String protocolVersion, String filePath) throws RemoteException {
		if (protocolVersion.equals("1.0") || Peer.protocolVersion.equals(protocolVersion))
			new DeleteProtocol(protocolVersion, filePath);
	}

	@Override
	public void reclaim(String protocolVersion, int diskSpace) throws RemoteException {
		if (protocolVersion.equals("1.0") || Peer.protocolVersion.equals(protocolVersion))
			new Reclaim(protocolVersion, diskSpace);
	}

	@Override
	public String state() throws RemoteException {
		return new CheckState().getState();
	}

}
