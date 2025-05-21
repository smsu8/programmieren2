// Refactored VM.java mit sealed interface und records

import java.util.*;

public class vm {
    boolean log = false;
    RegisterSet register;
    Command[] commands;
    Map<String, Integer> labelAddresses = new HashMap<>();

    vm(RegisterSet register, Command... commands) {
        this.register = register;
        this.commands = commands;
        indexLabels();
    }

    void setLog(boolean log) {
        this.log = log;
    }

    void indexLabels() {
        for (int i = 0; i < commands.length; i++) {
            if (commands[i] instanceof LABEL label)
                labelAddresses.put(label.name(), i);
        }
    }

    int getLabelAddress(String name) {
        if (!labelAddresses.containsKey(name))
            throw new IllegalArgumentException("Label not found: " + name);
        return labelAddresses.get(name);
    }

    boolean step() {
        int pc = register.pc;
        register.pc += commands[pc].execute(this);
        if (log) System.out.println(this);
        return pc != register.pc;
    }

    void run() {
        while (step());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(register);
        sb.append("COMMANDS:\n");
        for (int i = 0; i < commands.length; i++) {
            sb.append(i == register.pc ? ">>>>" : String.format("%4d", i)).append(" ").append(commands[i]).append("\n");
        }
        return sb.toString();
    }

    sealed interface Command permits LABEL, ADD, SUB, STA, LDA, JMP, JZ, JGE, HLT, IN, OUT {
        int execute(vm vm);
    }

    public static final class RegisterSet {
        int[] r;
        int acc;
        int pc;

        public RegisterSet(int... r) {
            this.r = r;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("REGISTERS:\n");
            for (int i = 0; i < r.length; i++) sb.append("r[").append(i).append("] = ").append(r[i]).append("\n");
            sb.append("acc = ").append(acc).append("\n");
            sb.append("pc = ").append(pc).append("\n");
            return sb.toString();
        }
    }

    record LABEL(String name) implements Command {
        public int execute(vm vm) { return 1; }
        public String toString() { return "LABEL " + name + ":"; }
    }

    record ADD(int index) implements Command {
        public int execute(vm vm) {
            vm.register.acc += vm.register.r[index];
            return 1;
        }
        public String toString() { return "    ADD acc += r[" + index + "]"; }
    }

    record SUB(int index) implements Command {
        public int execute(vm vm) {
            vm.register.acc -= vm.register.r[index];
            return 1;
        }
        public String toString() { return "    SUB acc -= r[" + index + "]"; }
    }

    record STA(int index) implements Command {
        public int execute(vm vm) {
            vm.register.r[index] = vm.register.acc;
            return 1;
        }
        public String toString() { return "    STA r[" + index + "] = acc"; }
    }

    record LDA(int index) implements Command {
        public int execute(vm vm) {
            vm.register.acc = vm.register.r[index];
            return 1;
        }
        public String toString() { return "    LDA acc = r[" + index + "]"; }
    }

    record JMP(String name) implements Command {
        public int execute(vm vm) {
            return vm.getLabelAddress(name) - vm.register.pc;
        }
        public String toString() { return "    JMP " + name; }
    }

    record JZ(String name) implements Command {
        public int execute(vm vm) {
            return vm.register.acc == 0 ? vm.getLabelAddress(name) - vm.register.pc : 1;
        }
        public String toString() { return "    JZ " + name; }
    }

    record JGE(String name) implements Command {
        public int execute(vm vm) {
            return vm.register.acc >= 0 ? vm.getLabelAddress(name) - vm.register.pc : 1;
        }
        public String toString() { return "    JGE " + name; }
    }

    record HLT() implements Command {
        public int execute(vm vm) { return 0; }
        public String toString() { return "    HLT"; }
    }

    record IN(String text) implements Command {
        private static final Scanner scanner = new Scanner(System.in);
        public int execute(vm vm) {
            System.out.print(text);
            vm.register.acc = scanner.nextInt();
            return 1;
        }
        public String toString() { return "    IN acc = \"" + text + "\""; }
    }

    record OUT(String text) implements Command {
        public int execute(vm vm) {
            System.out.println(text + vm.register.acc);
            return 1;
        }
        public String toString() { return "    OUT \"" + text + "\" <acc>"; }
    }
}
