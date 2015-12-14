package nl.tue.ds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads property file with service configuration
 */
public abstract class ServiceConfiguration {

    private static final Logger logger = LogManager.getLogger();

    public static final String CONFIGURATION_FILE = "service.properties";

    private static int rmiPort;

    private static int bankInitialAmount;

    private static int bankTransferMinAmount;

    private static int bankTransferMaxAmount;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(CONFIGURATION_FILE));
            rmiPort = Integer.parseInt(properties.getProperty("rmi-port"));
            bankInitialAmount = Integer.parseInt(properties.getProperty("bank-initial-amount"));
            bankTransferMinAmount = Integer.parseInt(properties.getProperty("bank-transfer-min-amount"));
            bankTransferMaxAmount = Integer.parseInt(properties.getProperty("bank-transfer-max-amount"));
        } catch (IOException e) {
            logger.error("Failed to load service configuration!", e);
        }
    }

    public static int getRmiPort() {
        return rmiPort;
    }

    public static int getBankInitialAmount() {
        return bankInitialAmount;
    }

    public static int getBankTransferMinAmount() {
        return bankTransferMinAmount;
    }

    public static int getBankTransferMaxAmount() {
        return bankTransferMaxAmount;
    }
}
