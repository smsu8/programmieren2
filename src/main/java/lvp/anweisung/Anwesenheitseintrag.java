

class Anwesenheitseintrag {
    Status status;
    String kommentar;

    Anwesenheitseintrag(Status status, String kommentar) {
        this.status = status;
        this.kommentar = kommentar;
    }

    public Status getStatus() {
        return status;
    }

    public String getKommentar() {
        return kommentar;
    }

    @Override
    public String toString() {
        return status + (kommentar != null && !kommentar.isEmpty() ? " (" + kommentar + ")" : "");
    }
}

