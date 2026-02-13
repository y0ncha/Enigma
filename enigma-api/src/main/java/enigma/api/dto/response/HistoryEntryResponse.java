package enigma.api.dto.response;

public record HistoryEntryResponse(
        String input,
        String output,
        long duration
) {
}
