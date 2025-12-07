package enigma.shared.dto.record;

public record MessageRecord(
        String originalText,
        String processedText,
        long durationNanos
) {
    @Override
    public String toString() {
        return "Input=\"" + originalText + "\"" +
                ", Output=\"" + processedText + "\"" +
                ", Duration=" + durationNanos + "ns";
    }
}