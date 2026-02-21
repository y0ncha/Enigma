package enigma.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(
        name = "machines",
        indexes = {
                @Index(name = "idx_machines_name", columnList = "name", unique = true)
        }
)
public class MachineEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "rotors_count", nullable = false)
    private Integer rotorsCount;

    @Column(name = "abc", nullable = false, columnDefinition = "TEXT")
    private String abc;

    public MachineEntity() {
    }

    public MachineEntity(UUID id, String name, Integer rotorsCount, String abc) {
        this.id = id;
        this.name = name;
        this.rotorsCount = rotorsCount;
        this.abc = abc;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRotorsCount() {
        return rotorsCount;
    }

    public void setRotorsCount(Integer rotorsCount) {
        this.rotorsCount = rotorsCount;
    }

    public String getAbc() {
        return abc;
    }

    public void setAbc(String abc) {
        this.abc = abc;
    }

}
