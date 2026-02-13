package enigma.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "processing",
        indexes = {
                @Index(name = "idx_processing_machine_id", columnList = "machine_id"),
                @Index(name = "idx_processing_session_id", columnList = "session_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_processing_id", columnNames = "id")
        }
)
public class ProcessRecordEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "machine_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_processing_machine_id")
    )
    private MachineEntity machine;

    @Column(name = "session_id", nullable = false, columnDefinition = "TEXT")
    private String sessionId;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String outputText;

    @Column(name = "time", nullable = false)
    private long durationNanos;

    public ProcessRecordEntity() {
    }

    public ProcessRecordEntity(UUID id,
                               MachineEntity machine,
                               String sessionId,
                               String code,
                               String inputText,
                               String outputText,
                               long durationNanos) {
        this.id = id;
        this.machine = machine;
        this.sessionId = sessionId;
        this.code = code;
        this.inputText = inputText;
        this.outputText = outputText;
        this.durationNanos = durationNanos;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MachineEntity getMachine() {
        return machine;
    }

    public void setMachine(MachineEntity machine) {
        this.machine = machine;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
