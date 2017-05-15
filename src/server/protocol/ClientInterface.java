package server.protocol;

import java.rmi.*;

public interface ClientInterface extends Remote
{
    public void backup(String protocolVersion, String filePath, int replicationDeg) throws RemoteException;
    public void restore(String protocolVersion, String filePath) throws RemoteException;
    public void delete(String protocolVersion, String filePath) throws RemoteException;
    public void reclaim(String protocolVersion, int diskSpace) throws RemoteException;
    public String state() throws RemoteException;
} 