public class Processor {

    // stores values of current executing instruction
    byte operatingRegister;
    byte opcode;
    byte operand1;
    byte operand2;

    public Processor() {
        this.opcode = 0;
        this.operand1 = 0;
        this.operand2 = 0;
        this.operatingRegister = -1;
    }
}
