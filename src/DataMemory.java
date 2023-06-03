public class DataMemory {
    byte[] memory;

    public DataMemory() {
        this.memory = new byte[2048];
    }

    public void populateWithRandomValues() {
        for (int i = 0; i < memory.length; i++) {
            byte randomValue = (byte) (Math.random() * 256 - 128);
            memory[i] = randomValue;
        }
    }
}