package enigma.sessions.model;

import java.util.List;
import java.util.UUID;

public record HistoryView(
        String scope,
        UUID sessionId,
        String machineName,
        List<ConfigEventView> configurationEvents,
        List<ProcessRecordView> processRecords
) {
}
