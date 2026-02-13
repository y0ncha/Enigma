package enigma.api.dto.response;

public record RotorSelectionWithNotchResponse(
        int rotorNumber,
        String rotorPosition,
        int notchDistance
) {
}
