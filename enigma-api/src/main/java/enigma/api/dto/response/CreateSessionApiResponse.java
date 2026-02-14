package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"sessionID"})
public record CreateSessionApiResponse(
        String sessionID
) {
}
