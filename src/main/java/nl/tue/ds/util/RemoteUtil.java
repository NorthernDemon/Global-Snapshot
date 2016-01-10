package nl.tue.ds.util;

import nl.tue.ds.entity.Node;
import nl.tue.ds.rmi.NodeServer;
import nl.tue.ds.rmi.NullNodeRemote;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.rmi.Naming;
import java.rmi.RemoteException;

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
        return getRemoteNode(node.getId(), node.getHost());
    }

    /**
     * Get reference to remote node
     *
     * @param id   of the node
     * @param host of the node
     * @return reference to remote object
     */
    @NotNull
    public static NodeServer getRemoteNode(int id, @NotNull String host) {
        try {
            return (NodeServer) Naming.lookup("rmi://" + host + "/NodeRemote" + id);
        } catch (Exception e) {
            logger.error("Failed to get remote interface for id=" + id, e);
            try {
                return new NullNodeRemote(new Node());
            } catch (RemoteException re) {
                logger.error("Failed to get Null Node Pattern", re);
                throw new RuntimeException("RMI failed miserably", re);
            }
        }
    }
}
