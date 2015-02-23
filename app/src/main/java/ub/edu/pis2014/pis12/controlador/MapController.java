package ub.edu.pis2014.pis12.controlador;

/**
 * Created by Dani on 23/02/2015.
 */
public class MapController {
    private static MapController instance = new MapController();

    public static MapController getInstance() {
        return instance;
    }

    private MapController() {
    }

    //Aqui aniran els metodes del mapa
}
