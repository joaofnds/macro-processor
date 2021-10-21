class OpCodeNotFoundError extends Error {
    public OpCodeNotFoundError(Instruction instruction) {
        super("couldn't find OpCode for instruction: " + instruction);
    }
}
