package enigma.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "process_records")
public class ProcessRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "machine_name", nullable = false)
    private String machineName;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output_text", nullable = false, columnDefinition = "TEXT")
    private String outputText;

    @Column(name = "duration_nanos", nullable = false)
    private long durationNanos;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessRecordEntity() {
    }

    public ProcessRecordEntity(UUID sessionId,
                               String machineName,
                               String inputText,
                               String outputText,
                               long durationNanos,
                               Instant processedAt) {
        this.sessionId = sessionId;
        this.machineName = machineName;
        this.inputText = inputText;
        this.outputText = outputText;
        this.durationNanos = durationNanos;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public void setDurationNanos(long durationNanos) {
        this.durationNanos = durationNanos;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
