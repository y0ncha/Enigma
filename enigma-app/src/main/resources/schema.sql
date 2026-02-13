DO $$
BEGIN
    CREATE TYPE reflector_id_enum AS ENUM ('I', 'II', 'III', 'IV', 'V');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

CREATE TABLE IF NOT EXISTS machines (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    rotors_count INTEGER NOT NULL,
    abc TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS machines_rotors (
    id UUID PRIMARY KEY,
    machine_id UUID NOT NULL,
    rotor_id INTEGER NOT NULL,
    notch INTEGER NULL,
    wiring_right TEXT NOT NULL,
    wiring_left TEXT NOT NULL,
    CONSTRAINT fk_machines_rotors_machine_id
        FOREIGN KEY (machine_id) REFERENCES machines(id),
    CONSTRAINT uk_machines_rotors_machine_id_rotor_id
        UNIQUE (machine_id, rotor_id)
);

CREATE INDEX IF NOT EXISTS idx_machines_rotors_machine_id
    ON machines_rotors(machine_id);

CREATE TABLE IF NOT EXISTS machines_reflectors (
    id UUID PRIMARY KEY,
    machine_id UUID NOT NULL,
    reflector_id reflector_id_enum NOT NULL,
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    CONSTRAINT fk_machines_reflectors_machine_id
        FOREIGN KEY (machine_id) REFERENCES machines(id),
    CONSTRAINT uk_machines_reflectors_machine_id_reflector_id
        UNIQUE (machine_id, reflector_id)
);

CREATE INDEX IF NOT EXISTS idx_machines_reflectors_machine_id
    ON machines_reflectors(machine_id);

CREATE TABLE IF NOT EXISTS processing (
    id UUID PRIMARY KEY,
    machine_id UUID NOT NULL,
    session_id TEXT NOT NULL,
    code TEXT NULL,
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    time BIGINT NOT NULL,
    CONSTRAINT fk_processing_machine_id
        FOREIGN KEY (machine_id) REFERENCES machines(id)
);

CREATE INDEX IF NOT EXISTS idx_processing_machine_id
    ON processing(machine_id);

CREATE INDEX IF NOT EXISTS idx_processing_session_id
    ON processing(session_id);
