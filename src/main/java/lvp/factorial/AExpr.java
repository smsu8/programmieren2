

import java.util.Map;
import java.util.logging.Logger;

sealed interface Expr permits Num, Var, BiOp, Neg, Bool, Not, And, Or, Ternary {
    Expr eval(Map<String, Expr> env);

    String toStringPrecedence(int parentPrec);

    default String toStringExpr() {
        return toStringPrecedence(0);
    }
}

enum Op {
    ADD("+", 1),
    SUB("-", 1),
    MUL("*", 2),
    DIV("/", 2);

    final String symbol;
    final int precedence;

    Op(String symbol, int precedence) {
        this.symbol = symbol;
        this.precedence = precedence;
    }

    double apply(double a, double b) {
        return switch (this) {
            case ADD -> a + b;
            case SUB -> a - b;
            case MUL -> a * b;
            case DIV -> a / b;
        };
    }
}

record Num(double value) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return this;
    }

    public String toStringPrecedence(int parentPrec) {
        return Double.toString(value);
    }
}

record Var(String name) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return env.getOrDefault(name, new Num(0));
    }

    public String toStringPrecedence(int parentPrec) {
        return name;
    }
}

record BiOp(Op op, Expr left, Expr right) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        double l = ((Num) left.eval(env)).value();
        double r = ((Num) right.eval(env)).value();
        if (op == Op.DIV && r == 0) throw new ArithmeticException("Division by zero");
        return new Num(op.apply(l, r));
    }

    public String toStringPrecedence(int parentPrec) {
        String leftStr = left.toStringPrecedence(op.precedence);
        String rightStr = right.toStringPrecedence(op.precedence + 1);
        String result = leftStr + " " + op.symbol + " " + rightStr;
        return parentPrec > op.precedence ? "(" + result + ")" : result;
    }
}

record Neg(Expr expr) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return new Num(-((Num) expr.eval(env)).value());
    }

    public String toStringPrecedence(int parentPrec) {
        String inner = expr.toStringPrecedence(3);
        return parentPrec > 2 ? "-(" + inner + ")" : "-" + inner;
    }
}

// Boolesche Ausdrücke
record Bool(boolean value) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return this;
    }

    public String toStringPrecedence(int parentPrec) {
        return Boolean.toString(value);
    }
}

record Not(Expr expr) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return new Bool(!((Bool) expr.eval(env)).value());
    }

    public String toStringPrecedence(int parentPrec) {
        return "!" + expr.toStringPrecedence(3);
    }
}

record And(Expr left, Expr right) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return new Bool(((Bool) left.eval(env)).value() && ((Bool) right.eval(env)).value());
    }

    public String toStringPrecedence(int parentPrec) {
        String l = left.toStringPrecedence(1);
        String r = right.toStringPrecedence(2);
        String result = l + " && " + r;
        return parentPrec > 1 ? "(" + result + ")" : result;
    }
}

record Or(Expr left, Expr right) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return new Bool(((Bool) left.eval(env)).value() || ((Bool) right.eval(env)).value());
    }

    public String toStringPrecedence(int parentPrec) {
        String l = left.toStringPrecedence(0);
        String r = right.toStringPrecedence(1);
        String result = l + " || " + r;
        return parentPrec > 0 ? "(" + result + ")" : result;
    }
}

record Ternary(Expr condition, Expr thenBranch, Expr elseBranch) implements Expr {
    public Expr eval(Map<String, Expr> env) {
        return ((Bool) condition.eval(env)).value() ? thenBranch.eval(env) : elseBranch.eval(env);
    }

    public String toStringPrecedence(int parentPrec) {
        String result = condition.toString() + " ? " + thenBranch.toString() + " : " + elseBranch.toString();
        return parentPrec > 0 ? "(" + result + ")" : result;
    }
}
void main() {
    Logger logger = Logger.getLogger("AExpr");
    // 1 + 2 * 3 ==> 7
    Expr expr1 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.MUL, new Num(3), new Num(3))); // 1 + 2 * 3
        Expr expr2 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.DIV, new BiOp(Op.MUL, new Num(2), new Num(3)), new Num(4))); // 1 + 2*3/4
        Expr expr3 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.MUL, new Var("x"), new Num(3))); // 1 + x * 3
    System.out.println(expr3.eval(Map.of("x", new Num(7)))); // 22.0
}

public class AExpr {
    public static void main(String[] args) {
        Map<String, Expr> env = Map.of("x", new Num(7), "b", new Bool(true));

        Expr expr1 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.MUL, new Num(2), new Num(3))); // 1 + 2 * 3
        Expr expr2 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.DIV, new BiOp(Op.MUL, new Num(2), new Num(3)), new Num(4))); // 1 + 2*3/4
        Expr expr3 = new BiOp(Op.ADD, new Num(1), new BiOp(Op.MUL, new Var("x"), new Num(3))); // 1 + x * 3
        Expr expr4 = new Neg(new Num(5)); // -5
        Expr expr5 = new Neg(new BiOp(Op.ADD, new Num(1), new Num(2))); // -(1 + 2)
        Expr expr6 = new And(new Bool(true), new Not(new Bool(false))); // true && !false
        Expr expr7 = new Ternary(new Var("b"), new Num(10), new Num(20)); // b ? 10 : 20

        Expr[] all = { expr1, expr2, expr3, expr4, expr5, expr6, expr7 };
        for (Expr e : all) {
            System.out.println(e + " = " + e.eval(env));
        }
    }
}

