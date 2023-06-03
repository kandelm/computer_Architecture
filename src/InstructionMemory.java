public class InstructionMemory {
    short[] memory;
    int memoryPointer;

    public InstructionMemory() {
        this.memory = new short[1024];
        this.memoryPointer = 0;
    }
}
