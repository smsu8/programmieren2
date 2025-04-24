
/open vm.java
// Factorial.java - Berechnet die Fakultät einer Zahl
// Factorial.java - Berechnet die Fakultät einer Zahl

RegisterSet register = new RegisterSet(
    1, // r[0] = result
    0, // r[1] = n
    1, // r[2] = 1 (Konstante)
    0, // r[3] = Zähler innere Schleife
    0, // r[4] = temp = vorheriger result-Wert
    0, // r[5] = Eingabe
    0  // r[6] = immer 0 (für Reset von result)
);

Command[] commands = {
    new IN("Input a number > 0: "),
    new STA(1),       // r[1] = n
    new STA(5),       // r[5] = Eingabe speichern
    new LDA(2),       // acc = 1
    new STA(0),       // result = 1
    new LABEL("outer"),
    new LDA(1),       // acc = n
    new JZ("end"),
    new STA(3),       // r[3] = counter = n
    new LDA(0),       // acc = result
    new STA(4),       // r[4] = temp = result
    new LDA(6),       // acc = 0
    new STA(0),       // result auf 0 setzen
    new LABEL("inner"),
    new LDA(3),       // acc = counter
    new JZ("done_mult"),
    new LDA(0),       // acc = result
    new ADD(4),       // acc += temp
    new STA(0),       // result = acc
    new LDA(3),
    new SUB(2),
    new STA(3),
    new JMP("inner"),
    new LABEL("done_mult"),
    new LDA(1),
    new SUB(2),
    new STA(1),
    new JMP("outer"),
    new LABEL("end"),
    new LDA(0),
    new OUT("The result is: "),
    new HLT()
};


VM vm = new VM(register, commands);
// vm.setLog(true);
