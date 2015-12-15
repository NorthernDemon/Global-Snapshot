package nl.tue.ds.util;

import nl.tue.ds.BankTransfer;
import nl.tue.ds.entity.Node;
import nl.tue.ds.rmi.NodeServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;

/**
 * Convenient class to deal with RMI for nodes
 */
public abstract class RemoteUtil {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Get reference to remote node
     *
     * @param node remote node
     * @return reference to remote object
     */
    @NotNull
    public static NodeServer getRemoteNode(@NotNull Node node) {
        try {
            return (NodeServer) Naming.lookup("rmi://" + node.getHost() + "/NodeRemote" + node.getId());
        } catch (Exception e) {
            logger.error("Failed to get remote interface for id=" + node.getId(), e);
            throw new RuntimeException("RMI failed miserably", e);
        }
    }

    /**
     * Gets node given nodeId
     *
     * @param currentNode current node
     * @param nodes       set of nodes
     * @return currentNode if nodeId is the same, remote node otherwise
     */
    private static Node getRandomNode(@NotNull Node currentNode, @NotNull Map<Integer, String> nodes) throws RemoteException {
        int amount = new Random().nextInt((BankTransfer.MAX_AMOUNT - BankTransfer.MIN_AMOUNT + 1) + BankTransfer.MIN_AMOUNT);
        int nodeId = new Random().nextInt(nodes.size());
        if (nodeId == currentNode.getId()) {
            return null;
        } else {
            return getRemoteNode(new Node(nodeId, nodes.get(nodeId))).getNode();
        }
    }
}
