package nl.tue.ds.util;

import nl.tue.ds.entity.Node;
import nl.tue.ds.rmi.NodeServer;
import nl.tue.ds.rmi.NullNodeRemote;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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
            try {
                return new NullNodeRemote(new Node());
            } catch (RemoteException re) {
                logger.error("Failed to get Null Node Pattern", re);
                throw new RuntimeException("RMI failed miserably", re);
            }
        }
    }

    /**
     * Gets node given nodeId
     *
     * @param currentNode current node
     * @return currentNode if nodeId is the same, remote node otherwise
     */
    public static Node getRandomNode(@NotNull Node currentNode) throws RemoteException {
        List<Integer> keysAsArray = new ArrayList<>(currentNode.getNodes().keySet());
        int nodeId = keysAsArray.get(new Random().nextInt(currentNode.getNodes().size()));
        if (nodeId == currentNode.getId()) {
            return null;
        } else {
            return getRemoteNode(new Node(nodeId, currentNode.getNodes().get(nodeId))).getNode();
        }
    }
}
