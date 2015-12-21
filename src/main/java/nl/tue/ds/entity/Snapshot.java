package nl.tue.ds.entity;

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Distributed Snapshot associated with the node
 *
 * @see Node
 */
public final class Snapshot implements Serializable {

    /**
     * Sequential number, current snapshot ID to be taken
     */
    private int id;

    /**
     * Current amount of money at the bank
     */
    private int localBalance;

    /**
     * All money transfers from incoming channels upon receiving the marker
     */
    private int moneyInTransfer;

    /**
     * Locks operations over the channels
     */
    private static final ReadWriteLock channelLock = new ReentrantReadWriteLock();

    /**
     * Incoming nodes to be recorded for distributed snapshot
     * holds only node ids from where the marker has not arrived yet
     * if collection is empty -> all markers are received
     * <p>
     * Map<NodeId>
     */
    private final @NotNull Set<Integer> unrecordedNodes = new HashSet<>();

    public void startSnapshotRecording(int nodeId, int balance, Map<Integer, String> nodes) {
        id++;
        localBalance = balance;
        moneyInTransfer = 0;
        unrecordedNodes.addAll(nodes.entrySet().parallelStream().filter(n -> n.getKey() != nodeId).map(Map.Entry::getKey).collect(Collectors.toList()));
    }

    public int getId() {
        return id;
    }

    public int getLocalBalance() {
        return localBalance;
    }

    public int getMoneyInTransfer() {
        return moneyInTransfer;
    }

    /**
     * Increments the money-in-transfer upon receiving the marker from that node
     *
     * @param nodeId sender of the money transfer
     * @param amount of the money transfer
     */
    public void incrementMoneyInTransfer(int nodeId, int amount) {
        channelLock.writeLock().lock();
        try {
            if (unrecordedNodes.contains(nodeId)) {
                moneyInTransfer += amount;
            }
        } finally {
            channelLock.writeLock().unlock();
        }
    }

    public void markRecorded(int nodeId) {
        channelLock.writeLock().lock();
        try {
            unrecordedNodes.remove(nodeId);
        } finally {
            channelLock.writeLock().unlock();
        }
    }

    public boolean isRecording() {
        channelLock.writeLock().lock();
        try {
            return unrecordedNodes.size() != 0;
        } finally {
            channelLock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Snapshot) {
            Snapshot object = (Snapshot) o;

            return Objects.equals(id, object.id) &&
                    Objects.equals(localBalance, object.localBalance) &&
                    Objects.equals(moneyInTransfer, object.moneyInTransfer);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, localBalance, moneyInTransfer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("localBalance", localBalance)
                .add("moneyInTransfer", moneyInTransfer)
                .add("unrecordedNodes", Arrays.toString(unrecordedNodes.toArray()))
                .toString();
    }
}
