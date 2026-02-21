package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"rotorNumber", "rotorPosition", "notchDistance"})
public record RotorSelectionWithNotchResponse(
        int rotorNumber,
        String rotorPosition,
        int notchDistance
) {
}
