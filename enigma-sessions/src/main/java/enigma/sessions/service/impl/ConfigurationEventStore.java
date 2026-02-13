package enigma.sessions.service.impl;

import enigma.sessions.model.ConfigEventView;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ConfigurationEventStore {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final List<ConfigEventView> events = new CopyOnWriteArrayList<>();

    public void add(UUID sessionId, String machineName, String action, String payload, Instant createdAt) {
        events.add(new ConfigEventView(
                idSequence.incrementAndGet(),
                sessionId,
                machineName,
                action,
                payload,
                createdAt
        ));
    }

    public List<ConfigEventView> bySession(UUID sessionId) {
        return events.stream()
                .filter(event -> event.sessionId().equals(sessionId))
                .sorted(Comparator.comparing(ConfigEventView::createdAt)
                        .thenComparing(ConfigEventView::id))
                .toList();
    }

    public List<ConfigEventView> byMachineName(String machineName) {
        return events.stream()
                .filter(event -> event.machineName().equals(machineName))
                .sorted(Comparator.comparing(ConfigEventView::createdAt)
                        .thenComparing(ConfigEventView::id))
                .toList();
    }
}
