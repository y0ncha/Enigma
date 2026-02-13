package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
