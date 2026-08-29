package com.financemanager.persistence;

import com.financemanager.exception.DataAccessException;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public final class FileDataStore {
    private final Path dataFile;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private AppState state;

    public FileDataStore(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
            this.dataFile = dataDirectory.resolve("finance-manager.dat");
            this.state = load();
        } catch (IOException ex) {
            throw new DataAccessException("Could not initialize application storage.", ex);
        }
    }

    public <T> T read(Function<AppState, T> operation) {
        lock.readLock().lock();
        try {
            return operation.apply(state);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T write(Function<AppState, T> operation) {
        lock.writeLock().lock();
        try {
            T result = operation.apply(state);
            persist();
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private AppState load() {
        if (!Files.exists(dataFile)) {
            return new AppState();
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(dataFile))) {
            Object object = input.readObject();
            if (!(object instanceof AppState loaded)) {
                throw new IOException("Unexpected data format.");
            }
            return loaded;
        } catch (IOException | ClassNotFoundException ex) {
            Path backup = dataFile.resolveSibling("finance-manager-corrupt-" + System.currentTimeMillis() + ".dat");
            try {
                Files.move(dataFile, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // The original exception is more useful to the caller.
            }
            throw new DataAccessException("Stored finance data could not be read. A backup was preserved.", ex);
        }
    }

    private void persist() {
        Path temp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
        try (ObjectOutputStream output = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(temp, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)))) {
            output.writeObject(state);
            output.flush();
        } catch (IOException ex) {
            throw new DataAccessException("Could not write finance data.", ex);
        }

        try {
            Files.move(temp, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            try {
                Files.move(temp, dataFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException inner) {
                throw new DataAccessException("Could not finalize finance data update.", inner);
            }
        } catch (IOException ex) {
            throw new DataAccessException("Could not finalize finance data update.", ex);
        }
    }
}
