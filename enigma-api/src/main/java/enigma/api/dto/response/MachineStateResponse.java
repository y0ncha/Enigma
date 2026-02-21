package enigma.api.dto.response;

public record MachineStateResponse(
        int rotorsDefined,
        int reflectorsDefined,
        int stringsProcessed,
        String originalCode,
        String currentCode
) {
}
