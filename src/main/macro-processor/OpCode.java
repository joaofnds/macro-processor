class OpCode {
    String mnemonic;
    byte opcode;
    Type type;

    public OpCode(String mnemonic, byte opcode, Type type) {
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.type = type;
    }

    enum Type {
        RegisterRegister, // RR
        RegisterIndex, // RX
    }
}
