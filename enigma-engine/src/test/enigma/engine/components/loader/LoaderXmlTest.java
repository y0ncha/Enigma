package test.enigma.engine.components.loader;

import enigma.engine.components.loader.EnigmaLoadingException;
import enigma.engine.components.loader.Loader;
import enigma.engine.components.loader.LoaderXml;
import enigma.engine.components.model.MachineSpec;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoaderXmlTest {

    // Relative to the project root — actual files are under enigma-engine/src/resources/xml/
    private static final String XML_BASE_DIR = "enigma-engine/src/resources/xml";

    public static void main(String[] args) {
        Loader loader = new LoaderXml();

        String[] files = {
                "ex1-sanity-small.xml",
                "ex1-sanity-paper-enigma.xml",
                "ex1-error-3.xml",
                "ex1-error-5.xml",
                "ex1-error-8.xml"
        };

        for (String fileName : files) {
            Path path = Paths.get(XML_BASE_DIR, fileName);
            System.out.println("\n==============================");
            System.out.println("Testing file: " + path.toAbsolutePath());
            System.out.println("==============================");

            try {
                // Pass an absolute filesystem path string to the loader
                String absolutePath = path.toAbsolutePath().toString();
                MachineSpec spec = loader.loadMachine(absolutePath);
                System.out.println("Load succeeded.");
                System.out.println(spec);

            }
            catch (EnigmaLoadingException e) {
                System.out.println("EnigmaLoadingException:");
                System.out.println("  " + e.getMessage());
            }
            catch (Exception e) {
                System.out.println("Unexpected exception:");
                e.printStackTrace(System.out);
            }
        }
    }
}