package nl.tue.ds.util;

import nl.tue.ds.entity.Item;
import nl.tue.ds.entity.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Convenient class to work with Node's internal list of snapshots
 * <p>
 * Maintains CSV file (under STORAGE_FOLDER directory) in format:
 * {snapshotID},{local balance},{sum of all incoming transfers upon receiving the marker}
 */
public abstract class StorageUtil {

    private static final Logger logger = LogManager.getLogger();

    private static final String SEPARATOR = ",";

    private static final String STORAGE_FOLDER = "storage";

    /**
     * Creates/Updates list of nodes items into CSV file
     *
     * @param node to write
     */
    public static void write(@NotNull Node node) {
        try (PrintWriter writer = new PrintWriter(getFileName(node.getId()), "UTF-8")) {
            Item item = node.getItem();
            writer.write(item.getSnapshotID() + SEPARATOR + item.getBalance() + SEPARATOR + item.getMoneyInTransfer() + System.getProperty("line.separator"));
            logger.debug("Storage wrote an item=" + item);
        } catch (Exception e) {
            logger.error("Failed to write items from node=" + node, e);
        }
    }

    /**
     * Creates storage folder to keep node's CSV files in
     */
    public static void init() {
        try {
            Path path = Paths.get(STORAGE_FOLDER);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (Exception e) {
            logger.error("Failed to create storage directory", e);
        }
    }

    /**
     * Removes node's CSV file
     *
     * @param nodeId of the node
     */
    public static void removeFile(int nodeId) {
        try {
            Path path = Paths.get(getFileName(nodeId));
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            logger.error("Failed to remove file for nodeId=" + nodeId, e);
        }
    }

    @NotNull
    private static String getFileName(int nodeId) {
        return STORAGE_FOLDER + "/Node-" + nodeId + ".csv";
    }
}
