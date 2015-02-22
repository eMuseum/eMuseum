package ub.edu.pis2014.pis12.model;

import android.graphics.Bitmap;

/**
 * Clase base
 * Conte els elements basics del contingut de la nostra app
 */
public abstract class Element {
    protected int identificador;
    protected String titol;
    private String descripcio;
    private String imatgeURL;
    private Bitmap imatge;
    private float valoracio;

    /**
     * Constructor establint el titol
     *
     * @param titol Nom de l'element
     */
    public Element(int id, String titol, String descripcio, String url) {
        this.identificador = id;
        this.titol = titol;
        this.descripcio = descripcio;
        this.imatgeURL = url;
    }

    /**
     * Estableix l'identificador
     *
     * @param id Identificador
     */
    public void setId(int id) {
        identificador = id;
    }

    /**
     * Obte l'identificador
     *
     * @return Identificador
     */
    public int getId() {
        return identificador;
    }

    public abstract int getMuseuId();

    /**
     * Estableix el titol de l'element
     *
     * @param titol Titol de l'element
     */
    public void setTitol(String titol) {
        this.titol = titol;
    }

    /**
     * Obte el titol de l'element
     *
     * @return Titol de l'element
     */
    public String getTitol() {
        return titol;
    }

    /**
     * Estableix la informacio de l'element
     *
     * @param inf String de la informacio
     */
    public void setDescripcio(String inf) {
        descripcio = inf;
    }

    /**
     * Retorna un string amb la informacio de l'element
     *
     * @return informacio
     */
    public String getDescripcio() {
        return descripcio;
    }

    /**
     * Retorna la direccio URL d'on treure la imatge
     *
     * @return Direccio URL
     */
    public String getImatgeURL() {
        return imatgeURL;
    }

    /**
     * Estableix la imatge com a bitmap
     *
     * @param imatge Bitmap de la imatge
     */
    public void setImatge(Bitmap imatge) {
        this.imatge = imatge;
    }

    /**
     * Retorna la imatge en forma de bitmap
     *
     * @return Bitmap
     */
    public Bitmap getImatge() {
        return imatge;
    }

    /**
     * Imprimeix el titol al mostrar l'objecte
     *
     * @return Titol de l'element
     */
    public String toString() {
        return titol;
    }

    /**
     * Estableix la valoracio total de l'element
     *
     * @param valo Valoracio
     */
    public void setValoracio(float valoracio) {
        this.valoracio = valoracio;
    }

    /**
     * Retorna la valoracio de l'element
     *
     * @return valoracio de l'element
     */
    public float getValoracio() {
        return valoracio;
    }
}
