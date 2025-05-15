
import java.util.*;
import java.util.stream.*;

public class anwesenheitsmanager {

    // Hilfsmethode: Erstellt Liste mit Einträgen für Wochen
    static List<Anwesenheitseintrag> wochen(Status... statusListe) {
        List<Anwesenheitseintrag> liste = new ArrayList<>();
        for (Status s : statusListe) {
            liste.add(new Anwesenheitseintrag(s, ""));
        }
        return liste;
    }

    public static void main(String[] args) {
        Map<String, List<Anwesenheitseintrag>> daten = new HashMap<>();

        daten.put("Anna",  wochen(Status.ANWESEND, Status.ANWESEND, Status.ENTSCHULDIGT, Status.VERSPÄTET, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND));
        daten.put("Ben",   wochen(Status.ABWESEND, Status.ANWESEND, Status.ANWESEND, Status.VERSPÄTET, Status.ENTSCHULDIGT, Status.ANWESEND, Status.ANWESEND));
        daten.put("Clara", wochen(Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ABWESEND));
        daten.put("David", wochen(Status.ENTSCHULDIGT, Status.ENTSCHULDIGT, Status.ABWESEND, Status.ANWESEND, Status.VERSPÄTET, Status.ANWESEND, Status.ANWESEND));
        daten.put("Eva",   wochen(Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.VERSPÄTET, Status.ENTSCHULDIGT));
        daten.put("Fritz", wochen(Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND, Status.ANWESEND));

        // ✅ Wer hat mind. 6 Teilnahmen (inkl. verspätet), max. 1 Entschuldigung?
        System.out.println("\nStudierende mit ≥6 Teilnahmen (inkl. verspätet) und ≤1 Entschuldigung:");
        for (var entry : daten.entrySet()) {
            long teilnahmen = entry.getValue().stream()
                .filter(e -> e.getStatus() == Status.ANWESEND || e.getStatus() == Status.VERSPÄTET)
                .count();
            long entschuldigungen = entry.getValue().stream()
                .filter(e -> e.getStatus() == Status.ENTSCHULDIGT)
                .count();
            if (teilnahmen >= 6 && entschuldigungen <= 1) {
                System.out.println("- " + entry.getKey());
            }
        }

        // 📊 Wie viele waren in Woche 3 anwesend?
        int woche = 2;
        long anwesend = daten.values().stream()
            .filter(liste -> liste.size() > woche)
            .map(liste -> liste.get(woche))
            .filter(e -> e.getStatus() == Status.ANWESEND || e.getStatus() == Status.VERSPÄTET)
            .count();

        long gesamt = daten.size();
        double prozent = 100.0 * anwesend / gesamt;

        System.out.printf("\nWoche %d: %.0f%% (%d von %d) anwesend\n", woche + 1, prozent, anwesend, gesamt);

        // 📅 Statistik pro Woche
        System.out.println("\nÜbersicht pro Woche:");
        for (int w = 0; w < 7; w++) {
            final int weekIndex = w;
            Map<Status, Long> statistik = daten.values().stream()
                .filter(liste -> liste.size() > weekIndex)
                .map(liste -> liste.get(weekIndex).getStatus())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            System.out.println("Woche " + (weekIndex + 1) + ": " + statistik);
        }
    }
}

