package enigma.machine.component.machine;

import enigma.machine.component.code.Code;

/**
 * Represents the core Enigma machine interface.
 * <p>
 * The Machine is responsible for encrypting/decrypting individual characters
 * using a configured code (rotors, reflector, and initial positions).
 * </p>
 */
public interface Machine {

    /**
     * Sets the code configuration for the machine.
     * <p>
     * The code includes the rotors, reflector, and initial rotor positions
     * that define how characters are transformed.
     * </p>
     *
     * @param code the code configuration to use for encryption/decryption
     */
    void setCode(Code code);

    /**
     * Processes a single character through the Enigma machine.
     * <p>
     * The character passes through the keyboard, rotors (forward), reflector,
     * rotors (backward), and back through the keyboard to produce the output.
     * Each call advances the rotors according to the stepping mechanism.
     * </p>
     *
     * @param input the character to encrypt/decrypt
     * @return the encrypted/decrypted character
     */
    char process(char input);
}
