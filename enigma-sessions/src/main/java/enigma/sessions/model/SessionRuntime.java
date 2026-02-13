package enigma.sessions.model;

import enigma.engine.Engine;

import java.time.Instant;
import java.util.UUID;

public class SessionRuntime {

    private final UUID sessionId;
    private final UUID machineId;
    private final String machineName;
    private final String xmlPath;
    private final Engine engine;
    private final Instant openedAt;
    private volatile SessionStatus status;
    private volatile Instant closedAt;
    private final Object lock = new Object();

    public SessionRuntime(UUID sessionId,
                          UUID machineId,
                          String machineName,
                          String xmlPath,
                          Engine engine,
                          Instant openedAt) {
        this.sessionId = sessionId;
        this.machineId = machineId;
        this.machineName = machineName;
        this.xmlPath = xmlPath;
        this.engine = engine;
        this.openedAt = openedAt;
        this.status = SessionStatus.OPEN;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public UUID machineId() {
        return machineId;
    }

    public String machineName() {
        return machineName;
    }

    public String xmlPath() {
        return xmlPath;
    }

    public Engine engine() {
        return engine;
    }

    public Instant openedAt() {
        return openedAt;
    }

    public SessionStatus status() {
        return status;
    }

    public Instant closedAt() {
        return closedAt;
    }

    public Object lock() {
        return lock;
    }

    public void markClosed(Instant closedAt) {
        this.status = SessionStatus.CLOSED;
        this.closedAt = closedAt;
    }
}
