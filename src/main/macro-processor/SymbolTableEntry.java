class SymbolTableEntry {
    final String name;
    private short value;
    private Type type = Type.UNDEFINED;

    public SymbolTableEntry(String name) {
        this.name = name;
        value = 0;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public boolean isUndefined() {
        return type == Type.UNDEFINED;
    }

    public void setDefined() {
        type = Type.DEFINED;
    }

    public void setMultiplyDefined() {
        type = Type.MultiplyDefined;
    }

    enum Type {
        DEFINED,
        UNDEFINED, // U
        MultiplyDefined, // MTDF
        Invalid
    }
}
