package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"output", "currentRotorsPositionCompact"})
public record ProcessApiResponse(
        String output,
        String currentRotorsPositionCompact
) {
}
