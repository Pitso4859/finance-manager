package com.financemanager.patterns.observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class AuditLogObserver implements TransactionObserver {
    private final Path auditFile;

    public AuditLogObserver(Path dataDirectory) {
        this.auditFile = dataDirectory.resolve("audit.log");
    }

    @Override
    public void onTransactionEvent(TransactionEvent event) {
        String txId = event.transaction() == null ? "unknown" : event.transaction().getId();
        String line = "%s | %s | transaction=%s%n".formatted(event.occurredAt(), event.type(), txId);
        try {
            Files.writeString(auditFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not write audit log.", ex);
        }
    }
}
