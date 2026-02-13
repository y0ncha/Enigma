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
        name = "machines_rotors",
        indexes = {
                @Index(name = "idx_machines_rotors_machine_id", columnList = "machine_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_machines_rotors_machine_id_rotor_id",
                        columnNames = {"machine_id", "rotor_id"}
                )
        }
)
public class MachineRotorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "machine_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_machines_rotors_machine_id")
    )
    private MachineEntity machine;

    @Column(name = "rotor_id", nullable = false)
    private Integer rotorId;

    @Column(name = "notch")
    private Integer notch;

    @Column(name = "wiring_right", nullable = false, columnDefinition = "TEXT")
    private String wiringRight;

    @Column(name = "wiring_left", nullable = false, columnDefinition = "TEXT")
    private String wiringLeft;

    public MachineRotorEntity() {
    }

    public MachineRotorEntity(UUID id,
                              MachineEntity machine,
                              Integer rotorId,
                              Integer notch,
                              String wiringRight,
                              String wiringLeft) {
        this.id = id;
        this.machine = machine;
        this.rotorId = rotorId;
        this.notch = notch;
        this.wiringRight = wiringRight;
        this.wiringLeft = wiringLeft;
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

    public Integer getRotorId() {
        return rotorId;
    }

    public void setRotorId(Integer rotorId) {
        this.rotorId = rotorId;
    }

    public Integer getNotch() {
        return notch;
    }

    public void setNotch(Integer notch) {
        this.notch = notch;
    }

    public String getWiringRight() {
        return wiringRight;
    }

    public void setWiringRight(String wiringRight) {
        this.wiringRight = wiringRight;
    }

    public String getWiringLeft() {
        return wiringLeft;
    }

    public void setWiringLeft(String wiringLeft) {
        this.wiringLeft = wiringLeft;
    }
}
