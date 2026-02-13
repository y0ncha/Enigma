package enigma.api.dto.response;

import java.util.List;

public record EnigmaCodeStructureResponse(
        List<RotorSelectionWithNotchResponse> rotors,
        String reflector,
        List<PlugConnectionResponse> plugs
) {
}
