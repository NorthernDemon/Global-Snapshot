package nl.tue.ds;

import nl.tue.ds.entity.Node;

/**
 * Represents different states the node can be in
 *
 * @see Node
 */
public enum NodeState {

    /**
     * Node is currently operational
     */
    CONNECTED,

    /**
     * Node is currently NOT operational
     */
    DISCONNECTED
}
