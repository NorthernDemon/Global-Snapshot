package nl.tue.ds.entity;

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

/**
 * Nodes are put in the ring in acceding order (with the most greatest id followed by the most lowest id, forming a ring)
 * Nodes store items, such that (NodeId >= itemKey) and replicas of N predecessor's node items
 *
 * @see Item
 */
public final class Node implements Serializable {

    /**
     * Positive integer to determine position in the ring
     */
    private final int id;

    /**
     * IP address of the server node
     */
    @NotNull
    private final String host;

    /**
     * Own items, for which the node is responsible for
     * <p>
     * Map<ItemKey, Item>
     */
    @NotNull
    private final Map<Integer, Item> items = new TreeMap<>();

    /**
     * All known nodes in the graph, including itself
     * <p>
     * Map<NodeId, Host>
     */
    @NotNull
    private final Map<Integer, String> nodes = new HashMap<>();

    public Node(int id, @NotNull String host) {
        this.id = id;
        this.host = host;
        nodes.put(id, host);
    }

    public Node() {
        this(0, "");
    }

    public Node(@NotNull Node node) {
        this(node.id, node.host);
    }

    public void putNodes(@NotNull Map<Integer, String> nodes) {
        this.nodes.putAll(nodes);
    }

    public void putNode(int id, @NotNull String host) {
        nodes.put(id, host);
    }

    public void removeNode(int id) {
        nodes.remove(id);
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    public Map<Integer, Item> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public Map<Integer, String> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Node) {
            Node object = (Node) o;

            return Objects.equals(id, object.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("host", host)
                .add("items", Arrays.toString(items.keySet().toArray()))
                .add("nodes", Arrays.toString(nodes.entrySet().toArray()))
                .toString();
    }
}
