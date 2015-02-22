package ub.edu.pis2014.pis12.model;


public class Autor extends Element {

    /**
     * Constructor especificant el nom del museu
     *
     * @param nom Nom del museu
     */
    public Autor(int id, String nom, String descripcio, String imatge) {
        super(id, nom, descripcio, imatge);
    }

    @Override
    public int getMuseuId() {
        return 0;
    }
}
