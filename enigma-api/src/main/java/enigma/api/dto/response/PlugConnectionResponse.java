package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"plug1", "plug2"})
public record PlugConnectionResponse(
        String plug1,
        String plug2
) {
}
