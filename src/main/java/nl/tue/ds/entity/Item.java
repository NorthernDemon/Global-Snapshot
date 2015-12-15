package nl.tue.ds.entity;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Items represent an instance of distributed snapshot and associated to nodes by one-to-one relation
 *
 * @see Node
 */
public final class Item implements Serializable {

    /**
     * Sequential number, current snapshot ID to be taken
     */
    private int snapshotID;

    /**
     * Current amount of money at the bank
     */
    private int balance;

    /**
     * All money transfers from incoming channels upon receiving the marker
     */
    private int moneyInTransfer;

    public Item() {
    }

    public Item(int snapshotID, int balance, int moneyInTransfer) {
        this.snapshotID = snapshotID;
        this.balance = balance;
        this.moneyInTransfer = moneyInTransfer;
    }

    /**
     * Initiate new snapshot
     */
    public void createNewItem() {
        snapshotID++;
        moneyInTransfer = 0;
    }

    public int getSnapshotID() {
        return snapshotID;
    }

    public int getBalance() {
        return balance;
    }

    public void incrementBalance(int balance) {
        this.balance += balance;
    }

    public void decrementBalance(int balance) {
        this.balance -= balance;
    }

    public int getMoneyInTransfer() {
        return moneyInTransfer;
    }

    public void incrementMoneyInTransfer(int moneyInTransfer) {
        this.moneyInTransfer += moneyInTransfer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Item) {
            Item object = (Item) o;

            return Objects.equals(snapshotID, object.snapshotID) &&
                    Objects.equals(balance, object.balance);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotID, balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("snapshotID", snapshotID)
                .add("balance", balance)
                .add("moneyInTransfer", moneyInTransfer)
                .toString();
    }
}
