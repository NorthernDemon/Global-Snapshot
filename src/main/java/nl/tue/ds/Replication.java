package nl.tue.ds;

import nl.tue.ds.entity.Item;
import nl.tue.ds.entity.Node;

/**
 * Configures replication within the ring and quorums for read/write access
 * <p>
 * Must maintain the formula [ W + R > N ] to avoid read/write conflicts
 *
 * @see Item
 * @see Node
 * @see ServiceConfiguration
 */
public interface Replication {

    /**
     * Write quorum
     */
    int W = ServiceConfiguration.getReplicationW();

    /**
     * Read quorum
     */
    int R = ServiceConfiguration.getReplicationR();

    /**
     * Count of successor nodes used for replication, including itself
     */
    int N = ServiceConfiguration.getReplicationN();
}
