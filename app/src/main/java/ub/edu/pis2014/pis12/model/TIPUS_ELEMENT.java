package ub.edu.pis2014.pis12.model;

public enum TIPUS_ELEMENT {
    TIPUS_OBRA(0),
    TIPUS_AUTOR(1),
    TIPUS_MUSEU(2);

    private int value;

    private TIPUS_ELEMENT(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TIPUS_ELEMENT getFromElement(Element element) {
        if (element instanceof Obra)
            return TIPUS_OBRA;

        if (element instanceof Autor)
            return TIPUS_AUTOR;

        return TIPUS_MUSEU;
    }
}
