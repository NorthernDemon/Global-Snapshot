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
    public boolean transferMoney(int senderNodeId, int amount) throws RemoteException {
        return false;
    }
}
