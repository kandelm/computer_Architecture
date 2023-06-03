import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Computer {
    Processor cpu;
    DataMemory dataMemory;
    InstructionMemory instructionMemory;
    Register[] gpRegisters;
    boolean[] statusRegister;
    short programCounter;
    ArrayList<Integer> instructionModify;

    public Computer() {
        this.cpu = new Processor();
        this.dataMemory = new DataMemory();
        this.instructionMemory = new InstructionMemory();
        this.gpRegisters = new Register[64];
        this.programCounter = 0;
        this.statusRegister = new boolean[8];
        this.instructionModify = new ArrayList<Integer>();

        for (int i = 0; i < 64; i++)
            gpRegisters[i] = new Register();

    }

    public void runProgram(String path) throws IOException {
        readInstructionsIntoMemory(path);

        int clockCycle = 0;

        short instructionFetched = -1;
        short instrucionToDecode = -1;

        int pc = programCounter;

        // PIPELINE
        System.out.println("--------------- Program Started ---------------");

        for (int i = pc; i < instructionMemory.memoryPointer + 2; i++) {

            System.out.println("Current Clock Cycle: " + ++clockCycle);
            System.out.println("Program Counter: " + programCounter);
            System.out.println("----------------------------");

            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            StringBuilder sb3 = new StringBuilder();
            StringBuilder sb4 = new StringBuilder();

            // pipeline
            if (i < instructionMemory.memoryPointer) {
                sb2.append("Instruction being fetched: Instruction " + (i - pc + 1));
                instructionFetched = fetch();
            }
            if (i > pc + 1) {
                sb4.append("Instruction being executed: Instruction " + (i - pc - 1) + "\n");
                execute();
                if (cpu.opcode == 4 || cpu.opcode == 7) {
                    sb.append("Register Change: Program Counter = " + programCounter + "\n");
                    sb.append("Data Memory Changes: ");
                } else if (cpu.opcode == 10) {
                    sb.append("Register Change: Register(" + cpu.operatingRegister + ") = "
                            + gpRegisters[cpu.operatingRegister].data + "\n");
                    sb.append("Data Memory Changes: ");

                } else if (cpu.opcode == 11) {
                    sb.append("Register Change: \n");
                    sb.append("Data Memory Changes: DMEM[" + cpu.operand2 + "] = "
                            + cpu.operand1);

                } else {
                    sb.append("Register Change: Register(" + cpu.operatingRegister + ") = "
                            + gpRegisters[cpu.operatingRegister].data + "\n");
                    sb.append("Data Memory Changes: ");
                }

            } else {
                sb.append("Register Change: \n");
                sb.append("Data Memory Changes: ");
            }

            // decode
            if (i > pc && i < instructionMemory.memoryPointer + 1) {
                sb3.append("Instruction being decoded: Instruction " + (i - pc));
                decode(instrucionToDecode);
            }

            instrucionToDecode = instructionFetched;

            System.out.println(sb2.toString());
            System.out.println(sb3.toString());
            System.out.print(sb4.toString());

            System.out.println("----------------------------");
            System.out.println(sb.toString());

            // print register and data memory that changed
            System.out.println("----------------------------\n");

            // if (i == instructionMemory.memoryPointer + 1) {
            // printRegsiters();
            // printMemoryContent();
            // }

        }
        System.out.println("--------------- Program Ended ---------------");

    }

    public void readInstructionsIntoMemory(String path) throws IOException {
        InputStream in = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String instruction;
        while ((instruction = br.readLine()) != null) {
            short resultInstruction = getInstructionBits(instruction);
            instructionMemory.memory[instructionMemory.memoryPointer++] = resultInstruction;
        }

        br.close();
    }

    public short fetch() {
        return instructionMemory.memory[programCounter++];
    }

    public void decode(short instr) {

        String tmpInstr = instr + "" + instructionModify.remove(0);
        int instruction = Integer.parseInt(tmpInstr);

        byte opcode = (byte) ((instruction & 0b1111000000000000) >> (12));
        byte argument1 = (byte) ((instruction & 0b0000111111000000) >> (6));
        byte argument2 = (byte) ((instruction & 0b0000000000111111));

        cpu.opcode = opcode;
        cpu.operatingRegister = argument1;

        cpu.operand1 = gpRegisters[argument1].data;

        if (opcode == 3 || opcode == 4 || opcode == 8 || opcode == 9 || opcode == 10 || opcode == 11)
            cpu.operand2 = argument2;
        else
            cpu.operand2 = gpRegisters[argument2].data;

    }

    public void execute() {
        byte opcode = cpu.opcode;
        byte operatingRegister = cpu.operatingRegister;
        byte operand1 = cpu.operand1;
        byte operand2 = cpu.operand2;

        if (opcode == 0) {
            // ADD
            gpRegisters[operatingRegister].data = (byte) (operand1 + operand2);
            byte result = gpRegisters[operatingRegister].data;

            // zero flag
            byte tmp1 = (byte) (operand1 & 0x000000FF);
            byte tmp2 = (byte) (operand2 & 0x000000FF);
            byte tmpRes = (byte) (tmp1 + tmp2);
            statusRegister[0] = (result == 0);
            // carry flag
            statusRegister[4] = ((tmpRes & (1 << 8)) == (1 << 8));
            // overflow flag
            statusRegister[3] = ((operand1 & (1 << 7)) == (operand2 & (1 << 7)))
                    & ((operand1 & (1 << 7)) != (result & (1 << 7)));
            // negative flag
            statusRegister[2] = (result < 0);
            // sign flag
            statusRegister[1] = statusRegister[3] ^ statusRegister[2];

        } else if (opcode == 1) {
            // SUB
            gpRegisters[operatingRegister].data = (byte) (operand1 - operand2);
            byte result = gpRegisters[operatingRegister].data;

            // overflow flag
            statusRegister[3] = ((operand1 & (1 << 7)) != (operand2 & (1 << 7)))
                    & ((operand2 & (1 << 7)) == (result & (1 << 7)));
            // negative flag
            statusRegister[2] = (result < 0);
            // sign flag
            statusRegister[1] = statusRegister[3] ^ statusRegister[2];
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 2) {
            // MUL
            gpRegisters[operatingRegister].data = (byte) (operand1 * operand2);
            byte result = gpRegisters[operatingRegister].data;

            // negative flag
            statusRegister[2] = (result < 0);
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 3) {
            // LDI
            gpRegisters[operatingRegister].data = operand2;

        } else if (opcode == 4) {
            // BEQZ
            if (operand1 == 0)
                programCounter += 1 + operand2;

        } else if (opcode == 5) {
            // AND
            gpRegisters[operatingRegister].data = (byte) (operand1 & operand2);
            byte result = gpRegisters[operatingRegister].data;

            // negative flag
            statusRegister[2] = (result < 0);
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 6) {
            // OR
            gpRegisters[operatingRegister].data = (byte) (operand1 | operand2);
            byte result = gpRegisters[operatingRegister].data;

            // negative flag
            statusRegister[2] = (result < 0);
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 7) {
            // JR
            String s = toBinaryStringSigned(operand1) + toBinaryStringSigned(operand2);
            programCounter = Short.parseShort(s);

        } else if (opcode == 8) {
            // SLC
            gpRegisters[operatingRegister].data = (byte) (operand1 << operand2 | operand1 >>> 8 - operand2);
            byte result = gpRegisters[operatingRegister].data;

            // negative flag
            statusRegister[2] = (result < 0);
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 9) {
            // SRC
            gpRegisters[operatingRegister].data = (byte) (operand1 >>> operand2 | operand1 << 8 - operand2);
            byte result = gpRegisters[operatingRegister].data;

            // negative flag
            statusRegister[2] = (result < 0);
            // zero flag
            statusRegister[0] = (result == 0);

        } else if (opcode == 10) {
            // LB
            gpRegisters[operatingRegister].data = dataMemory.memory[operand2];

        } else if (opcode == 11) {
            // SB
            dataMemory.memory[operand2] = operand1;
        }

    }

    public void printRegsiters() {
        System.out.println("General Purpose Registers:");
        System.out.println("----------------------------");

        for (int j = 0; j < gpRegisters.length; j++) {
            System.out.print("Register(" + j + ") = " + gpRegisters[j].data + " | ");
        }
        System.out.println("\n----------------------------\n");

        System.out.println("Status Register:");
        System.out.println("----------------------------");

        for (int i = 0; i < 5; i++) {
            if (i == 0)
                System.out.print("Z = " + statusRegister[i] + " | ");
            if (i == 1)
                System.out.print("S = " + statusRegister[i] + " | ");
            if (i == 2)
                System.out.print("N = " + statusRegister[i] + " | ");
            if (i == 3)
                System.out.print("V = " + statusRegister[i] + " | ");
            if (i == 4)
                System.out.print("C = " + statusRegister[i]);

        }

        System.out.println("\n----------------------------\n");
        System.out.println("Program Counter: " + programCounter + "\n");

    }

    public void printMemoryContent() {
        System.out.println("Data Memory Content:");
        System.out.println("----------------------------");
        for (int i = 0; i < dataMemory.memory.length; i++) {

            if (i != dataMemory.memory.length - 1)
                System.out.print("DMEM[" + i + "] = " + dataMemory.memory[i] + " | ");
            else
                System.out.print("DMEM[" + i + "] = " + dataMemory.memory[i]);

        }
        System.out.println("\n----------------------------\n");

        System.out.println("Instruction Memory Content:");
        System.out.println("----------------------------");
        for (int i = 0; i < instructionMemory.memory.length; i++) {

            if (i != instructionMemory.memory.length - 1)
                System.out.print("IMEM[" + i + "] = " + instructionMemory.memory[i] + " | ");
            else
                System.out.print("IMEM[" + i + "] = " + instructionMemory.memory[i]);

        }
        System.out.println("\n----------------------------");
    }

    // HELPER METHODS
    public short getInstructionBits(String instruction) {
        String[] instructionParts = instruction.split(" ");
        byte opcode;
        switch (instructionParts[0].toUpperCase()) {
            case "ADD":
                opcode = 0;
                break;
            case "SUB":
                opcode = 1;
                break;
            case "MUL":
                opcode = 2;
                break;
            case "LDI":
                opcode = 3;
                break;
            case "BEQZ":
                opcode = 4;
                break;
            case "AND":
                opcode = 5;
                break;
            case "OR":
                opcode = 6;
                break;
            case "JR":
                opcode = 7;
                break;
            case "SLC":
                opcode = 8;
                break;
            case "SRC":
                opcode = 9;
                break;
            case "LB":
                opcode = 10;
                break;
            case "SB":
                opcode = 11;
                break;
            default:
                opcode = -1;
        }

        String argument1 = Integer.toBinaryString(Integer.parseInt(instructionParts[1].substring(1)));
        String argument2; // "11"

        if (opcode == 3 || opcode == 4 || opcode == 8 || opcode == 9 || opcode == 10 || opcode == 11)
            argument2 = Integer.toBinaryString(Integer.parseInt(instructionParts[2]));
        else
            argument2 = Integer.toBinaryString(Integer.parseInt(instructionParts[2].substring(1)));

        String opcodeBit = Integer.toBinaryString(opcode);

        String resultInstructionBits = padLeftZeros(opcodeBit, 4) + padLeftZeros(argument1, 6)
                + padLeftZeros(argument2, 6);

        int tmpInstr = Integer.parseInt(resultInstructionBits, 2); // "1001 1111 1111 1111"

        int mod = tmpInstr % 10;

        instructionModify.add(mod);

        tmpInstr = tmpInstr / 10;

        short resultInstruction = (short) tmpInstr;

        return resultInstruction;

    }

    public static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public static String toBinaryStringSigned(byte value) {
        StringBuilder binaryString = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            binaryString.append((value >> i) & 1);
        }
        return binaryString.toString();
    }

}
