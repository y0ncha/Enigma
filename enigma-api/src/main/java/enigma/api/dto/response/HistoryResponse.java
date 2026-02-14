package enigma.api.dto.response;

import java.util.List;
import java.util.UUID;

public record HistoryResponse(
        String scope,
        UUID sessionId,
        String machineName,
        List<ConfigEventResponse> configurationEvents,
        List<ProcessRecordResponse> processRecords
) {
}
