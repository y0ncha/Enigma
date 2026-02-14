package enigma.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "machines_reflectors",
        indexes = {
                @Index(name = "idx_machines_reflectors_machine_id", columnList = "machine_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_machines_reflectors_machine_id_reflector_id",
                        columnNames = {"machine_id", "reflector_id"}
                )
        }
)
public class MachineReflectorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "machine_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_machines_reflectors_machine_id")
    )
    private MachineEntity machine;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reflector_id", nullable = false, columnDefinition = "reflector_id_enum")
    private ReflectorId reflectorId;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String output;

    public MachineReflectorEntity() {
    }

    public MachineReflectorEntity(UUID id,
                                  MachineEntity machine,
                                  ReflectorId reflectorId,
                                  String input,
                                  String output) {
        this.id = id;
        this.machine = machine;
        this.reflectorId = reflectorId;
        this.input = input;
        this.output = output;
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

    public ReflectorId getReflectorId() {
        return reflectorId;
    }

    public void setReflectorId(ReflectorId reflectorId) {
        this.reflectorId = reflectorId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
