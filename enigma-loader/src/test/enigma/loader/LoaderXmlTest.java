package test.enigma.loader;

import enigma.loader.EnigmaLoadingException;
import enigma.loader.Loader;
import enigma.loader.LoaderXml;
import enigma.shared.spec.MachineSpec;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manual test harness for {@link LoaderXml}.
 *
 * <p>Runs the loader against several XML files and prints results to stdout.
 * This is not a unit test but a manual verification tool.</p>
 *
 * @since 1.0
 */
public class LoaderXmlTest {

    // Relative to the project root — actual files are under enigma-engine/src/resources/xml/
    private static final String XML_BASE_DIR = "enigma-engine/src/resources/xml";

    /**
     * Entry point for the test harness.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        Loader loader = new LoaderXml(3);

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
                MachineSpec spec = loader.loadSpecs(absolutePath);
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