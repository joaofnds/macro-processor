import java.util.Objects;

class OpCode {
    String mnemonic;
    byte opcode;
    Type type;

    public OpCode(String mnemonic, byte opcode, Type type) {
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpCode opCode = (OpCode) o;
        return opcode == opCode.opcode && mnemonic.equals(opCode.mnemonic) && type == opCode.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mnemonic, opcode, type);
    }

    enum Type {
        RegisterRegister("RR"),
        RegisterIndex("RX"),
        RegisterImmediate("RI");

        private final String crypticName;

        Type(String crypticName) {
            this.crypticName = crypticName;
        }

        @Override
        public String toString() {
            return crypticName;
        }
    }
}
