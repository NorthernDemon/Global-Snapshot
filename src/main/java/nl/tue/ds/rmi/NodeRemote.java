package nl.tue.ds.rmi;

import nl.tue.ds.entity.Node;
import nl.tue.ds.entity.Snapshot;
import nl.tue.ds.util.RemoteUtil;
import nl.tue.ds.util.StorageUtil;
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
 * @see Node
 * @see java.util.concurrent.locks.ReadWriteLock
 * @see java.util.concurrent.locks.ReentrantReadWriteLock
 */
public final class NodeRemote extends UnicastRemoteObject implements NodeServer {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Locks operations over the nodes
     */
    private static final ReadWriteLock nodesLock = new ReentrantReadWriteLock();

    /**
     * Locks operations over the items
     */
    private static final ReadWriteLock itemsLock = new ReentrantReadWriteLock();

    /**
     * Locks operations over the marker
     */
    private static final ReadWriteLock markerLock = new ReentrantReadWriteLock();

    @NotNull
    private final Node node;

    public NodeRemote(@NotNull Node node) throws RemoteException {
        this.node = node;
    }

    @NotNull
    @Override
    public Node getNode() throws RemoteException {
        logger.debug("Get node=" + node);
        return node;
    }

    @Override
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

    @Override
    public boolean transferMoney(int senderNodeId, int amount) throws RemoteException {
        itemsLock.writeLock().lock();
        try {
            logger.debug("Accepting money amount=" + amount + ", from senderNodeId=" + senderNodeId);
            node.getItem().incrementBalance(amount);
            node.getSnapshot().incrementMoneyInTransfer(senderNodeId, amount);
            logger.debug("Accepted, new balance=" + node.getItem().getBalance());
            return true;
        } finally {
            itemsLock.writeLock().unlock();
        }
    }

    @Override
    public void sendMarker(int nodeId) throws RemoteException {
        markerLock.writeLock().lock();
        try {
            logger.debug("Received marker from nodeId=" + nodeId);
            @NotNull Snapshot snapshot = node.getSnapshot();
            if (!snapshot.isRecording()) {
                node.startSnapshotRecording();
                logger.info("Broadcasting marker to neighbours...");
                for (Map.Entry<Integer, String> entry : node.getNodes().entrySet()) {
                    if (entry.getKey() != node.getId()) {
                        RemoteUtil.getRemoteNode(new Node(entry.getKey(), entry.getValue())).sendMarker(node.getId());
                        logger.info("Marker sent to nodeId=" + entry.getKey());
                    }
                }
            }
            snapshot.markRecorded(nodeId);
            if (!snapshot.isRecording()) {
                StorageUtil.write(node);
            }
        } finally {
            markerLock.writeLock().unlock();
        }
    }
}
