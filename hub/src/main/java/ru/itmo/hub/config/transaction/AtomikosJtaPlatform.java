package ru.itmo.hub.config.transaction;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import lombok.NonNull;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

public class AtomikosJtaPlatform extends AbstractJtaPlatform {
    static TransactionManager transactionManager;
    static UserTransaction transaction;

    @Override
    @NonNull
    protected TransactionManager locateTransactionManager() {
        return transactionManager;
    }

    @Override
    @NonNull
    protected UserTransaction locateUserTransaction() {
        return transaction;
    }
}
