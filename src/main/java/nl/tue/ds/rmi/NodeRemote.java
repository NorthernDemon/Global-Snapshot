package nl.tue.ds.rmi;

import nl.tue.ds.entity.Item;
import nl.tue.ds.entity.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides an access to remote node via RMI
 * <p>
 * Uses read/write locks for manipulation with internal data structure of the node in case of multiple requests
 * <p>
 * Read Lock: multiple readers can enter, if not locked for writing
 * Write Lock: only one writer can enter, if not locked for reading
 *
 * @see Item
 * @see Node
 * @see java.util.concurrent.locks.ReadWriteLock
 * @see java.util.concurrent.locks.ReentrantReadWriteLock
 */
public final class NodeRemote extends UnicastRemoteObject {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Locks nodes TreeMap operations of the node
     */
    private static final ReadWriteLock nodesLock = new ReentrantReadWriteLock();

    @NotNull
    private final Node node;

    public NodeRemote(@NotNull Node node) throws RemoteException {
        this.node = node;
    }

    @NotNull
    public Node getNode() throws RemoteException {
        logger.debug("Get node=" + node);
        return node;
    }

    @NotNull
    public Map<Integer, String> getNodes() throws RemoteException {
        nodesLock.readLock().lock();
        try {
            logger.debug("Get nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
            return node.getNodes();
        } finally {
            nodesLock.readLock().unlock();
        }
    }

    public void addNode(int id, @NotNull String host) throws RemoteException {
        nodesLock.writeLock().lock();
        try {
            logger.debug("Add id=" + id + ", host=" + host);
            node.putNode(id, host);
            logger.debug("Current nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        } finally {
            nodesLock.writeLock().unlock();
        }
    }

    public void removeNode(int id) throws RemoteException {
        nodesLock.writeLock().lock();
        try {
            logger.debug("Remove id=" + id);
            node.removeNode(id);
            logger.debug("Current nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        } finally {
            nodesLock.writeLock().unlock();
        }
    }
}
