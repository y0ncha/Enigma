package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"rotors", "reflector", "plugs"})
public record EnigmaCodeStructureResponse(
        List<RotorSelectionWithNotchResponse> rotors,
        String reflector,
        List<PlugConnectionResponse> plugs
) {
}
