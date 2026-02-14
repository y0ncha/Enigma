package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "name", "error"})
public record LoadMachineApiResponse(
        boolean success,
        String name,
        String error
) {
}
