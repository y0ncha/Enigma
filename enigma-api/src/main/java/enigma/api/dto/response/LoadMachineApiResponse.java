package enigma.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoadMachineApiResponse(
        boolean success,
        String name,
        String error
) {
}
