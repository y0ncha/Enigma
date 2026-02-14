package enigma.loader;

import enigma.loader.exception.EnigmaLoadingException;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoaderXmlTest {

    @Test
    void loadSpecs_whenXmlMissingRequiredField_returnsSchemaValidationMessage() throws URISyntaxException {
        LoaderXml loader = new LoaderXml();
        Path xmlPath = Path.of(Objects.requireNonNull(
                getClass().getResource("/ex3-error-missing-abc.xml")).toURI());

        EnigmaLoadingException exception = assertThrows(
                EnigmaLoadingException.class,
                () -> loader.loadSpecs(xmlPath.toString())
        );

        assertTrue(exception.getMessage().contains("XML does not comply with XSD schema"));
        assertTrue(exception.getMessage().contains("ABC"));
    }
}
