package enigma.machine.component.machine;

import enigma.machine.component.code.Code;

/**
 * Represents the core Enigma machine interface.
 * The machine encrypts and decrypts characters using the configured
 * rotors, reflector, and plugboard (if present).
 */
public interface Machine {

    /**
     * Sets the code configuration for the machine.
     * This includes the rotors, their positions, and the reflector.
     *
     * @param code the code configuration to apply
     */
    void setCode(Code code);

    /**
     * Processes a single character through the Enigma machine.
     * This involves stepping the rotors, passing the signal through
     * the rotors forward, reflecting, and passing back through the rotors.
     *
     * @param input the character to encrypt/decrypt
     * @return the resulting encrypted/decrypted character
     */
    char process(char input);
}
