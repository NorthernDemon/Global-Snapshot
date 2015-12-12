package nl.tue.ds;

import nl.tue.ds.entity.Node;

/**
 * Represents different states the node can be in
 *
 * @see Node
 */
public enum NodeState {

    /**
     * Node is currently in the ring and operational
     */
    CONNECTED,

    /**
     * Node is currently NOT in the ring, therefore NOT operational
     */
    DISCONNECTED,

    /**
     * Node is currently in the ring, but NOT operational
     */
    CRASHED,
}
