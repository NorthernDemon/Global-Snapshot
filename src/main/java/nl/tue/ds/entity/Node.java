package nl.tue.ds.entity;

import com.google.common.base.MoreObjects;
import nl.tue.ds.BankTransfer;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

/**
 * Nodes are represented as banks in distributed environment and are interconnected in peer-to-peer fashion
 * <p>
 * Nodes form a digraph, consisting of one strongly connected component
 */
public final class Node implements Serializable {

    /**
     * Positive integer to determine position in the graph
     */
    private final int id;

    /**
     * IP address of the server node
     */
    @NotNull
    private final String host;

    /**
     * Current state of the bank, used to compute distributed snapshot
     */
    @NotNull
    private final Item item;

    /**
     * All known nodes in the graph, including itself
     * <p>
     * Map<NodeId, Host>
     */
    @NotNull
    private final Map<Integer, String> nodes = new HashMap<>();

    public Node() {
        this(0, "");
    }

    public Node(int id, @NotNull String host) {
        this.id = id;
        this.host = host;
        item = new Item(1, BankTransfer.INITIAL_BALANCE, 0);
        nodes.put(id, host);
    }

    public int getId() {
        return id;
    }

    @NotNull
    public Item getItem() {
        return item;
    }

    public void putNodes(@NotNull Map<Integer, String> nodes) {
        this.nodes.putAll(nodes);
    }

    public void putNode(int id, @NotNull String host) {
        nodes.put(id, host);
    }

    @NotNull
    public String getHost() {
        return host;
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
                .add("item", item)
                .add("nodes", Arrays.toString(nodes.entrySet().toArray()))
                .toString();
    }
}
