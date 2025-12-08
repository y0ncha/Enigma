package test.enigma.loader;

import enigma.loader.exception.EnigmaLoadingException;
import enigma.loader.Loader;
import enigma.loader.LoaderXml;
import enigma.shared.spec.MachineSpec;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * LoaderTester — structured manual test suite for {@link LoaderXml}.
 *
 * <p>Reworked to match the style of other testers in the project: a main method
 * that runs several boolean tests, collects results, and prints a short summary.
 * Tests include both successful loads and files that are expected to fail validation.</p>
 */
public class LoaderTester {

    // Relative to the project root — actual files are under the enigma-loader test resources directory
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("========================================");
        System.out.println(" LoaderXml Test Suite");
        System.out.println("========================================\n");

        boolean t1 = testLoadShouldSucceed("ex1-sanity-small.xml");
        passed += t1 ? 1 : 0; failed += t1 ? 0 : 1;

        boolean t2 = testLoadShouldSucceed("ex1-sanity-paper-enigma.xml");
        passed += t2 ? 1 : 0; failed += t2 ? 0 : 1;

        boolean t3 = testLoadShouldFail("ex1-error-3.xml");
        passed += t3 ? 1 : 0; failed += t3 ? 0 : 1;

        boolean t4 = testLoadShouldFail("ex1-error-5.xml");
        passed += t4 ? 1 : 0; failed += t4 ? 0 : 1;

        boolean t5 = testLoadShouldFail("ex1-error-8.xml");
        passed += t5 ? 1 : 0; failed += t5 ? 0 : 1;

        // Summary
        System.out.println("\n=========== SUMMARY ===========");
        System.out.println("Total tests : " + (passed + failed));
        System.out.println("Passed      : " + passed);
        System.out.println("Failed      : " + failed);
        if (failed == 0) {
            System.out.println("Status      : ALL GOOD ✔✔✔");
        } else {
            System.out.println("Status      : CHECK FAILED CASES ✘");
        }
        System.out.println("================================");
    }

    private static boolean testLoadShouldSucceed(String filename) {
        System.out.println("========== Test: Load should succeed - " + filename + " ==========");
        Loader loader = new LoaderXml();
        Path path = Paths.get(XML_BASE_DIR, filename);
        try {
            MachineSpec spec = loader.loadSpecs(path.toAbsolutePath().toString());
            System.out.println("Result: PASSED ✔ (Loaded successfully)");
            System.out.println("Spec summary: \n" + spec);
            System.out.println();
            return true;
        } catch (Exception e) {
            System.out.println("Result: FAILED ✘ (Unexpected exception: " + e.getMessage() + ")");
            e.printStackTrace(System.out);
            System.out.println();
            return false;
        }
    }

    private static boolean testLoadShouldFail(String filename) {
        System.out.println("========== Test: Load should fail - " + filename + " ==========");
        Loader loader = new LoaderXml();
        Path path = Paths.get(XML_BASE_DIR, filename);
        try {
            loader.loadSpecs(path.toAbsolutePath().toString());
            System.out.println("Result: FAILED ✘ (Expected failure but load succeeded)");
            System.out.println();
            return false;
        } catch (EnigmaLoadingException e) {
            System.out.println("Result: PASSED ✔ (Loading failed as expected)");
            System.out.println("Message: " + e.getMessage());
            System.out.println();
            return true;
        } catch (Exception e) {
            System.out.println("Result: FAILED ✘ (Unexpected exception type: " + e.getClass().getSimpleName() + ")");
            e.printStackTrace(System.out);
            System.out.println();
            return false;
        }
    }
}