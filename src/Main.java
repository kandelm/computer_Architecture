import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Computer computer = new Computer();

        computer.dataMemory.populateWithRandomValues();

        computer.runProgram("programs/program_1.txt");

    }
}
