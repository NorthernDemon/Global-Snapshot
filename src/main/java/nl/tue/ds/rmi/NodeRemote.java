package nl.tue.ds.rmi;

import nl.tue.ds.entity.Node;
import nl.tue.ds.entity.Snapshot;
import nl.tue.ds.util.RemoteUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     * Locks operations over the item
     */
    private static final ReadWriteLock itemTransferLock = new ReentrantReadWriteLock();

    /**
     * Locks operations over the item
     */
    private static final ReadWriteLock itemAcceptLock = new ReentrantReadWriteLock();

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
    public void transferMoney(int recipientNodeId, int amount) throws RemoteException {
        itemTransferLock.writeLock().lock();
        try {
            boolean isWithdraw = node.getItem().decrementBalance(amount);
            if (isWithdraw) {
                logger.trace("Transferring amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                boolean isAccepted = RemoteUtil.getRemoteNode(recipientNodeId, node.getNodes().get(recipientNodeId)).acceptMoney(node.getId(), amount);
                if (isAccepted) {
                    logger.trace("Transferred amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                } else {
                    node.getItem().restoreBalance();
                    logger.trace("NOT Transferred amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                }
            } else {
                logger.trace("NOT Withdraw money amount=" + amount);
            }
        } finally {
            itemTransferLock.writeLock().unlock();
        }
    }

    @Override
    public boolean acceptMoney(int senderNodeId, int amount) throws RemoteException {
        itemAcceptLock.writeLock().lock();
        try {
            logger.trace("Accepting money amount=" + amount + " from senderNodeId=" + senderNodeId);
            node.getSnapshot().incrementMoneyInTransfer(senderNodeId, amount);
            node.getItem().incrementBalance(amount);
            logger.trace("Accepted, new balance=" + node.getItem().getBalance());
            return true;
        } finally {
            itemAcceptLock.writeLock().unlock();
        }
    }

    @Override
    public void receiveMarker(int nodeId) throws RemoteException {
        markerLock.writeLock().lock();
        itemAcceptLock.writeLock().lock();
        itemTransferLock.writeLock().lock();
        try {
            logger.debug("Received marker from nodeId=" + nodeId);
            @NotNull Snapshot snapshot = node.getSnapshot();
            if (!snapshot.isRecording()) {
                node.startSnapshotRecording();
                logger.debug("Broadcasting marker to neighbours");
                ExecutorService executorService = Executors.newFixedThreadPool(node.getNodes().size() - 1);
                node.getNodes().entrySet().parallelStream().filter(n -> n.getKey() != node.getId()).forEach(entry -> {
                    executorService.execute(() -> {
                        try {
                            RemoteUtil.getRemoteNode(entry.getKey(), entry.getValue()).receiveMarker(node.getId());
                            logger.debug("Marker sent to nodeId=" + entry.getKey());
                        } catch (RemoteException e) {
                            logger.error("Failed to sent marker to nodeId=" + entry.getKey(), e);
                        }
                    });
                });
            }
            snapshot.stopRecording(nodeId);
            if (!snapshot.isRecording()) {
                logger.debug("Received all markers for snapshot on nodeId=" + nodeId);
                node.stopSnapshotRecording();
            }
        } finally {
            markerLock.writeLock().unlock();
            itemAcceptLock.writeLock().unlock();
            itemTransferLock.writeLock().unlock();
        }
    }
}
