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
}
