package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "totalRotors",
        "totalReflectors",
        "totalProcessedMessages",
        "originalCode",
        "currentRotorsPosition",
        "originalCodeCompact",
        "currentRotorsPositionCompact"
})
public record ConfigStatusApiResponse(
        int totalRotors,
        int totalReflectors,
        int totalProcessedMessages,
        EnigmaCodeStructureResponse originalCode,
        EnigmaCodeStructureResponse currentRotorsPosition,
        String originalCodeCompact,
        String currentRotorsPositionCompact
) {
}
