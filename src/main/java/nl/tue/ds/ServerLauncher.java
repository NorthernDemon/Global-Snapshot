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
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * Thread pool scheduler of N threads for money transfer and snapshot taking
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Description: method name,node host,node id,existing node host,existing node id
     * Example: join,localhost,10,none,0
     * Example: join,localhost,15,localhost,10
     * Example: join,localhost,20,localhost,15
     * Example: join,localhost,25,localhost,20
     * Example: join,localhost,20,localhost,25
     * Example: view
     * Example: cut
     */
    public static void main(String[] args) {
        logger.info("You can change service configuration parameters in " + ServiceConfiguration.CONFIGURATION_FILE);
        logger.info("Service configuration: RMI port=" + RMI_PORT);
        logger.info("Service configuration: BankTransfer MIN_AMOUNT=" + BankTransfer.MIN_AMOUNT + ", MAX_AMOUNT=" + BankTransfer.MAX_AMOUNT + ", INITIAL_BALANCE=" + BankTransfer.INITIAL_BALANCE);
        logger.info("Service configuration: BankTransfer TIMEOUT_FREQUENCY=" + BankTransfer.TIMEOUT_FREQUENCY + ", TIMEOUT_UNIT=" + BankTransfer.TIMEOUT_UNIT);
        if (BankTransfer.MIN_AMOUNT >= BankTransfer.MAX_AMOUNT || BankTransfer.MAX_AMOUNT >= BankTransfer.INITIAL_BALANCE) {
            logger.warn("Bank transfer properties must maintain formula [ MIN_AMOUNT < MAX_AMOUNT < INITIAL_BALANCE ] !");
            return;
        }
        logger.info("Type in: method name,node host,node id,existing node host,existing node id");
        logger.info("Example: create,localhost,10");
        logger.info("Example: join,localhost,15,localhost,10");
        logger.info("Example: join,localhost,20,localhost,15");
        logger.info("Example: join,localhost,25,localhost,20");
        logger.info("Example: join,localhost,30,localhost,25");
        logger.info("Example: view");
        logger.info("Example: cut");
        StorageUtil.init();
        NetworkUtil.printMachineIPv4();
        logger.info("Bank is ready for request >");
        InputUtil.readInput(ServerLauncher.class.getName());
    }

    /**
     * Signals current node to create the graph
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
        logger.info("NodeId=" + nodeId + " is the first bank in the graph");
        node = register(nodeId, nodeHost);
        logger.info("NodeId=" + nodeId + " is connected as first node=" + node);
        nodeState = NodeState.CONNECTED;
        startMoneyTransferring();
    }

    /**
     * Signals current node to join the graph:
     * - accumulate the graph structure of all available banks from the existing node
     * - start randomly sending/accepting money transfers
     * <p>
     * Existing node MUST be operational!
     *
     * @param nodeHost         host for new current node
     * @param nodeId           id for new current node
     * @param existingNodeHost of node in the graph to fetch data from
     * @param existingNodeId   of node in the graph to fetch data from
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
        Node existingNode = RemoteUtil.getRemoteNode(existingNodeId, existingNodeHost).getNode();
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
        startMoneyTransferring();
    }

    /**
     * View the graph topology aka all the banks in connected component
     */
    public static void view() throws RemoteException {
        if (nodeState != NodeState.CONNECTED) {
            logger.warn("Must be CONNECTED to view topology! Current nodeState=" + nodeState);
            return;
        }
        logger.info("Viewing topology from node=" + node);
        node.getNodes().entrySet().forEach(n -> {
            try {
                RemoteUtil.getRemoteNode(n.getKey(), n.getValue()).getNode();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Initiate distributed snapshot to all known nodes (all nodes are interconnected as a digraph)
     */
    public static void cut() throws RemoteException {
        if (nodeState != NodeState.CONNECTED) {
            logger.warn("Must be CONNECTED to initiate the distributed snapshot! Current nodeState=" + nodeState);
            return;
        }
        logger.info("Starting distributed snapshot from node=" + node);
        RemoteUtil.getRemoteNode(node).sendMarker(node.getId());
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
     * Signals current node to leave the graph
     */
    private static void leave() throws Exception {
        logger.info("NodeId=" + node.getId() + " is disconnecting from the graph...");
        Naming.unbind("rmi://" + node.getHost() + "/NodeRemote" + node.getId());
        StorageUtil.removeFile(node.getId());
        logger.info("NodeId=" + node.getId() + " disconnected");
        node = null;
        nodeState = NodeState.DISCONNECTED;
    }

    /**
     * Announce JOIN operation to the nodes in the graph
     */
    private static void announceJoin() throws RemoteException {
        logger.debug("Announcing join to nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        node.getNodes().entrySet().parallelStream().filter(n -> n.getKey() != node.getId()).forEach(n -> {
            try {
                RemoteUtil.getRemoteNode(n.getKey(), n.getValue()).addNode(node.getId(), node.getHost());
                logger.trace("Announced join to nodeId=" + n.getKey());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void startMoneyTransferring() {
        executor.scheduleAtFixedRate((Runnable) () -> {
            try {
                if (node.getNodes().size() > 1) {
                    Node randomNode = getRandomNode(ServerLauncher.node);
                    if (randomNode != null) {
                        @NotNull Item item = node.getItem();
                        int randomAmount = new Random().nextInt(BankTransfer.MAX_AMOUNT) + BankTransfer.MIN_AMOUNT;
                        boolean isDecremented = item.decrementBalance(randomAmount);
                        if (isDecremented) {
                            logger.trace("Transferring money amount=" + randomAmount + ", to nodeId=" + randomNode.getId());
                            boolean isTransferred = RemoteUtil.getRemoteNode(randomNode).transferMoney(node.getId(), randomAmount);
                            if (!isTransferred) {
                                item.incrementBalance(randomAmount);
                                logger.trace("NOT Transferred, restore balance=" + node.getItem().getBalance());
                            } else {
                                logger.trace("Transferred, new balance=" + node.getItem().getBalance());
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                logger.error("Failed to fetch random node!", e);
            }
        }, 0, BankTransfer.TIMEOUT_FREQUENCY, TimeUnit.valueOf(BankTransfer.TIMEOUT_UNIT));
    }

    /**
     * Gets node given nodeId
     *
     * @param currentNode current node
     * @return currentNode if nodeId is the same, remote node otherwise
     */
    public static Node getRandomNode(@NotNull Node currentNode) {
        List<Integer> keysAsArray = new ArrayList<>(currentNode.getNodes().keySet());
        int nodeId = keysAsArray.get(new Random().nextInt(currentNode.getNodes().size()));
        if (nodeId == currentNode.getId()) {
            return null;
        } else {
            return new Node(nodeId, currentNode.getNodes().get(nodeId));
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
