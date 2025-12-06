package enigma.engine.components.dto;

/**
 * Represents machine state information.
 */
public class MachineDataDTO {
    private final int availableRotorsCount;
    private final int availableReflectorsCount;
    private final int processedMessagesCount;
    private final CodeConfigurationDTO originalCodeConfiguration;
    private final CodeConfigurationDTO currentCodeConfiguration;

    public MachineDataDTO(
            int availableRotorsCount,
            int availableReflectorsCount,
            int processedMessagesCount,
            CodeConfigurationDTO originalCodeConfiguration,
            CodeConfigurationDTO currentCodeConfiguration) {
        this.availableRotorsCount = availableRotorsCount;
        this.availableReflectorsCount = availableReflectorsCount;
        this.processedMessagesCount = processedMessagesCount;
        this.originalCodeConfiguration = originalCodeConfiguration;
        this.currentCodeConfiguration = currentCodeConfiguration;
    }

    public int getAvailableRotorsCount() {
        return availableRotorsCount;
    }

    public int getAvailableReflectorsCount() {
        return availableReflectorsCount;
    }

    public int getProcessedMessagesCount() {
        return processedMessagesCount;
    }

    public CodeConfigurationDTO getOriginalCodeConfiguration() {
        return originalCodeConfiguration;
    }

    public CodeConfigurationDTO getCurrentCodeConfiguration() {
        return currentCodeConfiguration;
    }
}
