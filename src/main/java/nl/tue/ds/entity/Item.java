package nl.tue.ds.entity;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Items represent an entity hold by the bank (balance in our case) and associated to nodes by one-to-one relation
 *
 * @see Node
 */
public final class Item implements Serializable {

    /**
     * Current amount of money at the bank
     */
    private int balance;

    public Item(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void incrementBalance(int amount) {
        balance += amount;
    }

    /**
     * Checks if current balance is over or equal the amount to be deducted
     * if it is -> deducts the money, if not -> balance stay untouched
     *
     * @param amount to be deducted
     * @return whether operation succeed or not
     */
    public boolean decrementBalance(int amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Item) {
            Item object = (Item) o;

            return Objects.equals(balance, object.balance);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("balance", balance)
                .toString();
    }
}
