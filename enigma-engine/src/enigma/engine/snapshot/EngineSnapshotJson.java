package enigma.engine.snapshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enigma.engine.exception.EngineException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * JSON persistence helper for saving and loading {@link EngineSnapshot}.
 *
 * <p>Responsible ONLY for filesystem & JSON, not for touching the running engine.
 * EngineImpl calls this class when it wants to save/load its state.</p>
 */
public final class EngineSnapshotJson {

    /** Default file suffix for snapshot files. */
    private static final String SNAPSHOT_SUFFIX = ".enigma.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private EngineSnapshotJson() {
        // utility class
    }

    /**
     * Save the given snapshot to a JSON file.
     *
     * @param snapshot fully-populated engine snapshot
     * @param basePath full path without extension (e.g. "C:\\tmp\\my-machine")
     * @throws EngineException if path is invalid or write fails
     */
    public static void save(EngineSnapshot snapshot, String basePath) {
        if (snapshot == null) {
            throw new EngineException("Snapshot is not available");
        }
        if (basePath == null || basePath.isBlank()) {
            throw new EngineException("Path is empty");
        }

        String fullPath = basePath + SNAPSHOT_SUFFIX;
        File target = new File(fullPath);

        File parent = target.getParentFile();
        if (parent == null || !parent.exists()) {
            throw new EngineException(
                String.format("Folder '%s' does not exist", basePath));
        }

        try (FileWriter writer = new FileWriter(target)) {
            GSON.toJson(snapshot, writer);
        } catch (IOException e) {
            throw new EngineException(
                String.format("Unable to write to file '%s': %s", fullPath, e.getMessage()), e);
        }
    }

    /**
     * Load an {@link EngineSnapshot} from a JSON file.
     *
     * @param basePath full path without extension (e.g. "C:\\tmp\\my-machine")
     * @return deserialized snapshot
     * @throws EngineException if file is missing or JSON is invalid
     */
    public static EngineSnapshot load(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            throw new EngineException("Path is empty");
        }

        String fullPath = basePath + SNAPSHOT_SUFFIX;
        File source = new File(fullPath);
        if (!source.isFile()) {
            throw new EngineException(
                String.format("File '%s' does not exist", fullPath));
        }

        try (FileReader reader = new FileReader(source)) {
            EngineSnapshot snapshot = GSON.fromJson(reader, EngineSnapshot.class);
            if (snapshot == null || snapshot.spec() == null) {
                throw new EngineException(
                    String.format("Snapshot from '%s' is invalid or empty", fullPath));
            }
            return snapshot;
        } catch (IOException e) {
            throw new EngineException(
                String.format("Unable to read file '%s': %s", fullPath, e.getMessage()), e);
        }
    }
}
