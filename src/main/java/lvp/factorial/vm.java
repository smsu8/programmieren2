

import java.util.Scanner;

class VM {
    boolean log = false;
    RegisterSet register;
    Command[] commands;
    VM(RegisterSet register, Command... commands) {
        this.register = register;
        this.commands = commands;
    }
    void setLog(boolean log) {
        this.log = log;
    }
    int getLabelAddress(String name) {
        for (int i = 0; i < commands.length; i++) {
            if (commands[i] instanceof LABEL) {
                LABEL label = (LABEL) commands[i];
                if (label.name.equals(name))
                    return i;
            }
        }
        throw new IllegalArgumentException("Error: Expected label with name " + name);
    }   
    boolean step() {
        int pc = register.pc;
        register.pc += commands[register.pc].execute(this);
        if (log) System.out.println(toString());
        return pc != register.pc;
    }
    @SuppressWarnings("empty-statement")
    void run() {
        while (step());
    }
    public String toString() {
        String s = register.toString();
        s += "COMMANDS:\n";
        for (int address = 0; address < commands.length; address++)
            s += (address == register.pc ? ">>>>" :
                  String.format("%4d", address)) + 
                  " " + commands[address] + "\n";
        return s.substring(0, s.length() - 1); // `return s;` is ok
    }
}

class RegisterSet {
    int[] r;
    int acc;
    int pc;
    RegisterSet(int... r) {
        this.r = r;
    }
    @Override public String toString() {
        String s = "REGISTERS:\n";
        for (int i = 0; i < r.length; i++)
            s += "r[" + i + "] = " + r[i] + "\n";
        s += "acc = " + acc + "\n";
        s += "pc = " + pc + "\n";
        return s;
    }
}

abstract class Command {
    abstract int execute(VM vm);
}

class LABEL extends Command {
    String name;
    LABEL(String name) {
        this.name = name;
    }
    int execute(VM vm) {
        return 1;
    }
    public String toString() {
        return "LABEL " + name + ":";
    }
}

class ADD extends Command {
    int index;
    ADD(int index) {
        this.index = index;
    }
    @Override int execute(VM vm) {
        vm.register.acc += vm.register.r[index];
        return 1;
    }
    public String toString() {
        return "    ADD acc += r[" + index + "]";
    }
}

class SUB extends Command {
    int index;
    SUB(int index) {
        this.index = index;
    }
    @Override int execute(VM vm) {
        vm.register.acc -= vm.register.r[index];
        return 1;
    }
    public String toString() {
        return "    SUB acc -= r[" + index + "]";
    }
}

class STA extends Command {
    int index;
    STA(int index) {
        this.index = index;
    }
    @Override int execute(VM vm) {
        vm.register.r[index] = vm.register.acc;
        return 1;
    }
    public String toString() {
        return "    STA r[" + index + "] = acc";
    }
}

class LDA extends Command {
    int index;
    LDA(int index) {
        this.index = index;
    }
    @Override int execute(VM vm) {
        vm.register.acc = vm.register.r[index];
        return 1;
    }
    public String toString() {
        return "    LDA acc = r[" + index + "]";
    }
}

class JMP extends Command {
    String name;
    JMP(String name) {
        this.name = name;
    }
    @Override int execute(VM vm) {
        return vm.getLabelAddress(name) - vm.register.pc;
    }
    public String toString() {
        return "    JMP " + name;
    }
}

class JZ extends Command {
    String name;
    JZ(String name) {
        this.name = name;
    }
    @Override int execute(VM vm) {
        if (vm.register.acc == 0)
            return vm.getLabelAddress(name) - vm.register.pc;
        return 1;
    }
    public String toString() {
        return "    JZ " + name;
    }
}

class JGE extends Command {
    String name;
    JGE(String name) {
        this.name = name;
    }
    @Override int execute(VM vm) {
        if (vm.register.acc >= 0)
            return vm.getLabelAddress(name) - vm.register.pc;
        return 1;
    }
    public String toString() {
        return "    JGE " + name;
    }
}

class HLT extends Command {
    @Override int execute(VM vm) {
        return 0;
    }
    public String toString() {
        return "    HLT";
    }
}

class IN extends Command {
    Scanner scanner = new Scanner(System.in);
    String text = "Input an integer: ";
    IN(String text) {
        this.text = text;
    }
    @Override int execute(VM vm) {
        System.out.print(text);
        vm.register.acc = scanner.nextInt();
        return 1;
    }
    public String toString() {
        return "    IN acc = \"" + text + "\"";
    }
}

class OUT extends Command {
    String text;
    OUT(String text) {
        this.text = text;
    }
    @Override int execute(VM vm) {
        System.out.println(text + vm.register.acc);
        return 1;
    }
    public String toString() {
        return "    OUT \"" + text + "\" <acc>";
    }
}
