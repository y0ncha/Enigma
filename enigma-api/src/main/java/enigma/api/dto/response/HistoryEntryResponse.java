package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"input", "output", "duration"})
public record HistoryEntryResponse(
        String input,
        String output,
        long duration
) {
}
