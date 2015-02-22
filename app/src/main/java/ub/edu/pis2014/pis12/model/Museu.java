package ub.edu.pis2014.pis12.model;

/**
 * Clase museu Conte una llista d'obres amb les obres del museu
 */
public class Museu extends Element {
    private double latitud;
    private double longitud;
    private String direccio;
    private int labObert;
    private int labTancat;
    private int festObert;
    private int festTancat;
    private String telefon;
    private String wifiPassword;

    /**
     * Constructor especificant el nom del museu
     *
     * @param nom Nom del museu
     */
    public Museu(int id, String nom, String direccio, String telefon,
                 String descripcio, String imatge, String wifiPassword,
                 double latitud, double longitud) {

        super(id, nom, descripcio, imatge);
        this.direccio = direccio;
        this.telefon = telefon;
        this.wifiPassword = wifiPassword;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public void setDireccio(String dir) {
        direccio = dir;
    }

    public String getDireccio() {
        return direccio;
    }

    public void setTelefon(String tel) {
        telefon = tel;
    }

    public String getTelefon() {
        return telefon;
    }

    @Override
    public int getMuseuId() {
        return identificador;
    }

    public void setHoraObertLaborables(int i) {
        labObert = i;
    }

    public void setHoraTancatLaborables(int i) {
        labTancat = i;
    }

    public void setHoraObertFestius(int i) {
        festObert = i;
    }

    public void setHoraTancatFestius(int i) {
        festTancat = i;
    }

    public int getHoraObertLaborables() {
        if (labObert == 0)
            return 0;

        return Integer.parseInt(String.valueOf(labObert).substring(0, 2));
    }

    public int getHoraTancatLaborables() {
        if (labTancat == 0)
            return 0;

        return Integer.parseInt(String.valueOf(labTancat).substring(0, 2));
    }

    public int getHoraObertFestius() {
        if (festObert == 0)
            return 0;

        return Integer.parseInt(String.valueOf(festObert).substring(0, 2));
    }

    public int getHoraTancatFestius() {
        if (festTancat == 0)
            return 0;

        return Integer.parseInt(String.valueOf(festTancat).substring(0, 2));
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public double getLatitud() {
        return this.latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return this.longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
