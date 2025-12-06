package enigma.engine.components.loader;

public class LoadXml implements Loader {
    // These fields should be populated during XML loading
    private int availableRotorsCount;
    private int availableReflectorsCount;

    @Override
    public int getAvailableRotorsCount() {
        return availableRotorsCount;
    }

    @Override
    public int getAvailableReflectorsCount() {
        return availableReflectorsCount;
    }
}
