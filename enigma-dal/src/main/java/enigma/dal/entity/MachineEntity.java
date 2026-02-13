package enigma.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "machines")
public class MachineEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "rotors_count", nullable = false)
    private Integer rotorsCount;

    @Column(name = "reflectors_count", nullable = false)
    private Integer reflectorsCount;

    @Column(name = "xml_path", nullable = false)
    private String xmlPath;

    @Column(name = "loaded_at", nullable = false)
    private Instant loadedAt;

    public MachineEntity() {
    }

    public MachineEntity(UUID id, String name, String xmlPath, Instant loadedAt) {
        this(id, name, 0, 0, xmlPath, loadedAt);
    }

    public MachineEntity(UUID id, String name, Integer rotorsCount, Integer reflectorsCount, String xmlPath, Instant loadedAt) {
        this.id = id;
        this.name = name;
        this.rotorsCount = rotorsCount;
        this.reflectorsCount = reflectorsCount;
        this.xmlPath = xmlPath;
        this.loadedAt = loadedAt;
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

    public Integer getReflectorsCount() {
        return reflectorsCount;
    }

    public void setReflectorsCount(Integer reflectorsCount) {
        this.reflectorsCount = reflectorsCount;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public Instant getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(Instant loadedAt) {
        this.loadedAt = loadedAt;
    }
}
