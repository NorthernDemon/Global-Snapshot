package nl.tue.ds.rmi;

import nl.tue.ds.entity.Node;
import org.jetbrains.annotations.NotNull;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Interface to be used by SERVER for accessing the remote node via RMI
 */
public interface NodeServer extends Remote {

    @NotNull Node getNode() throws RemoteException;

    @NotNull Map<Integer, String> getNodes() throws RemoteException;

    void addNode(int id, @NotNull String host) throws RemoteException;
}
