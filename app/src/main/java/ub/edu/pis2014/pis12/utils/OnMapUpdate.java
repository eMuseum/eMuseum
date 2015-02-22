package ub.edu.pis2014.pis12.utils;

import android.location.Location;

import ub.edu.pis2014.pis12.model.Museu;

public abstract class OnMapUpdate {
    private Location location;

    public OnMapUpdate(Museu museu) {
        location = new Location(museu.getTitol());
        location.setLatitude(museu.getLatitud());
        location.setLongitude(museu.getLongitud());
    }

    public Location getLocation() {
        return location;
    }

    public abstract void onUpdate(double distance);
}
