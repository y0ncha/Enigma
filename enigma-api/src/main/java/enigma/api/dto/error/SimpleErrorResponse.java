package enigma.api.dto.error;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"error"})
public record SimpleErrorResponse(
        String error
) {
}
