package enigma.engine.components.loader;

public class LoadXml implements Loader {
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
