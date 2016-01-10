package nl.tue.ds;

/**
 * Holds bank transfer properties defined in "service.configuration"
 * <p>
 * NOTE: Properties must maintain formula [ MIN_AMOUNT < MAX_AMOUNT < INITIAL_BALANCE ] !
 *
 * @see ServiceConfiguration
 */
public interface BankTransfer {

    /**
     * Initial amount at bank account
     */
    int INITIAL_BALANCE = ServiceConfiguration.getBankInitialAmount();

    /**
     * Minimal amount to transfer
     */
    int MIN_AMOUNT = ServiceConfiguration.getBankTransferMinAmount();

    /**
     * Maximal amount to transfer
     */
    int MAX_AMOUNT = ServiceConfiguration.getBankTransferMaxAmount();

    /**
     * Frequency of money transfers
     */
    int TIMEOUT_FREQUENCY = ServiceConfiguration.getBankTransferTimeoutFrequency();

    /**
     * Unit of money transfers
     */
    String TIMEOUT_UNIT = ServiceConfiguration.getBankTransferTimeoutUnit();
}
