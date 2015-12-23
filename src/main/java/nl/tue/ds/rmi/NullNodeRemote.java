package nl.tue.ds.rmi;

import nl.tue.ds.entity.Node;
import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Used to simulate crashed node or in case of network errors
 */
public final class NullNodeRemote extends UnicastRemoteObject implements NodeServer {

    @NotNull
    private final Node node;

    public NullNodeRemote(@NotNull Node node) throws RemoteException {
        this.node = node;
    }

    @NotNull
    @Override
    public Node getNode() throws RemoteException {
        return node;
    }

    @Override
    public void addNode(int id, @NotNull String host) throws RemoteException {
    }

    @Override
    public void transferMoney(int recipientNodeId, int amount) throws RemoteException {
    }

    @Override
    public boolean acceptMoney(int senderNodeId, int amount) throws RemoteException {
        return false;
    }

    @Override
    public void receiveMarker(int nodeId) throws RemoteException {
    }
}
