package nl.tue.ds;

import nl.tue.ds.entity.Node;
import nl.tue.ds.rmi.NodeRemote;
import nl.tue.ds.util.InputUtil;
import nl.tue.ds.util.NetworkUtil;
import nl.tue.ds.util.RemoteUtil;
import nl.tue.ds.util.StorageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simulates server node (bank) in the graph for distributed snapshot
 *
 * @see Node
 * @see ServiceConfiguration
 */
public final class ServerLauncher {

    private static final Logger logger = LogManager.getLogger();

    private static final int RMI_PORT = ServiceConfiguration.getRmiPort();

    @Nullable
    private static Node node;

    private static NodeState nodeState = NodeState.DISCONNECTED;

    /**
     * Description: method name,node host,node id,existing node host, existing node id
     * Example: join,localhost,10,none,0
     * Example: join,localhost,15,localhost,10
     * Example: join,localhost,20,localhost,15
     * Example: join,localhost,25,localhost,20
     * Example: join,localhost,20,localhost,25
     * Example: crash
     * Example: recover,localhost,20
     * Example: leave
     */
    public static void main(String[] args) {
        logger.info("You can change service configuration parameters in " + ServiceConfiguration.CONFIGURATION_FILE);
        logger.info("Service configuration: RMI port=" + RMI_PORT);
        logger.info("Service configuration: BankTransfer MIN_AMOUNT=" + BankTransfer.MIN_AMOUNT + ", MAX_AMOUNT=" + BankTransfer.MAX_AMOUNT + ", BankTransfer=" + BankTransfer.INITIAL_BALANCE);
        if (BankTransfer.MIN_AMOUNT >= BankTransfer.MAX_AMOUNT || BankTransfer.MAX_AMOUNT >= BankTransfer.INITIAL_BALANCE) {
            logger.warn("Bank transfer properties must maintain formula [ MIN_AMOUNT < MAX_AMOUNT < INITIAL_BALANCE ] !");
            return;
        }
        logger.info("Type in: method name,node host,node id,existing node host, existing node id");
        logger.info("Example: create,localhost,10");
        logger.info("Example: join,localhost,15,localhost,10");
        logger.info("Example: join,localhost,20,localhost,15");
        logger.info("Example: join,localhost,25,localhost,20");
        logger.info("Example: join,localhost,30,localhost,25");
        logger.info("Example: crash");
        logger.info("Example: recover,localhost,20");
        logger.info("Example: leave");
        logger.info("Example: view");
        StorageUtil.init();
        NetworkUtil.printMachineIPv4();
        logger.info("Server Bank is ready for request >");
        InputUtil.readInput(ServerLauncher.class.getName());
    }

    /**
     * Signals current node to create the ring
     *
     * @param nodeHost host for new current node
     * @param nodeId   id for new current node
     */
    public static void create(@NotNull String nodeHost, int nodeId) throws Exception {
        if (nodeState != NodeState.DISCONNECTED) {
            logger.warn("Must be DISCONNECTED to create! Current nodeState=" + nodeState);
            return;
        }
        if (nodeId <= 0) {
            logger.warn("Node id must be positive integer [ nodeID > 0 ] !");
            return;
        }
        startRMIRegistry();
        logger.info("NodeId=" + nodeId + " is the first node in the ring");
        node = register(nodeId, nodeHost);
        logger.info("NodeId=" + nodeId + " is connected as first node=" + node);
        nodeState = NodeState.CONNECTED;
    }

    /**
     * Signals current node to join the ring and take items that fall into it's responsibility from the successor node
     * <p>
     * Existing node MUST be operational!
     *
     * @param nodeHost         host for new current node
     * @param nodeId           id for new current node
     * @param existingNodeHost of node in the ring to fetch data from
     * @param existingNodeId   of node in the ring to fetch data from
     */
    public static void join(@NotNull String nodeHost, int nodeId, @NotNull String existingNodeHost, int existingNodeId) throws Exception {
        if (nodeState != NodeState.DISCONNECTED) {
            logger.warn("Must be DISCONNECTED to join! Current nodeState=" + nodeState);
            return;
        }
        if (nodeId <= 0) {
            logger.warn("Node id must be positive integer [ nodeID > 0 ] !");
            return;
        }
        startRMIRegistry();
        logger.info("NodeId=" + nodeId + " connects to existing nodeId=" + existingNodeId);
        Node existingNode = RemoteUtil.getRemoteNode(new Node(existingNodeId, existingNodeHost)).getNode();
        if (existingNode.getNodes().isEmpty()) {
            logger.warn("Existing node must be operational!");
            return;
        }
        if (existingNode.getNodes().containsKey(nodeId)) {
            logger.warn("Cannot join as nodeId=" + nodeId + " already taken!");
            return;
        }
        node = register(nodeId, nodeHost);
        node.putNodes(existingNode.getNodes());
        announceJoin();
        logger.info("NodeId=" + nodeId + " connected as node=" + node + " from existingNode=" + existingNode);
        nodeState = NodeState.CONNECTED;
    }

    /**
     */
    public static void cut() throws RemoteException {
        if (nodeState != NodeState.CONNECTED) {
            logger.warn("Must be CONNECTED to initiate the distributed snapshot! Current nodeState=" + nodeState);
            return;
        }
        List<Node> nodes = new LinkedList<>();
        for (Map.Entry<Integer, String> entry : RemoteUtil.getRemoteNode(node).getNodes().entrySet()) {
            nodes.add(RemoteUtil.getRemoteNode(new Node(entry.getKey(), entry.getValue())).getNode());
        }
        logger.info("Viewing topology from node=" + node);
        for (Node node : nodes) {
            logger.info(node);
        }
    }

    /**
     * Registers RMI for new node, initializes node object
     *
     * @param id   of the new node
     * @param host of the new node
     */
    @NotNull
    private static Node register(int id, @NotNull String host) throws Exception {
        System.setProperty("java.rmi.server.hostname", host);
        Node node = new Node(id, host);
        Naming.bind("rmi://" + node.getHost() + "/NodeRemote" + node.getId(), new NodeRemote(node));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Auto-leaving process initiated...");
                try {
                    if (nodeState == NodeState.CONNECTED) {
                        leave();
                    }
                } catch (Exception e) {
                    logger.error("Failed to leave node", e);
                }
            }
        });
        return node;
    }

    /**
     * Signals current node to leave the ring and pass all it's items to the successor node
     */
    private static void leave() throws Exception {
        logger.info("NodeId=" + node.getId() + " is disconnecting from the ring...");
        Naming.unbind("rmi://" + node.getHost() + "/NodeRemote" + node.getId());
        StorageUtil.removeFile(node.getId());
        logger.info("NodeId=" + node.getId() + " disconnected");
        node = null;
        nodeState = NodeState.DISCONNECTED;
    }

    /**
     * Announce JOIN operation to the nodes in the ring
     */
    private static void announceJoin() throws RemoteException {
        logger.debug("Announcing join to nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        for (Map.Entry<Integer, String> entry : node.getNodes().entrySet()) {
            if (entry.getKey() != node.getId()) {
                RemoteUtil.getRemoteNode(new Node(entry.getKey(), entry.getValue())).addNode(node.getId(), node.getHost());
                logger.trace("Announced join to nodeId=" + entry.getKey());
            }
        }
    }

    /**
     * Starts RMI registry on default port if not started already
     */
    private static void startRMIRegistry() {
        try {
            LocateRegistry.createRegistry(RMI_PORT);
        } catch (RemoteException e) {
            // already started
        }
    }
}
