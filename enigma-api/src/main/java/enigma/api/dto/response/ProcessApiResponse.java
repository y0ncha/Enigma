package enigma.api.dto.response;

public record ProcessApiResponse(
        String output,
        String currentRotorsPositionCompact
) {
}
