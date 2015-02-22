package ub.edu.pis2014.pis12.controlador;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Iterator;

import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;

public class SearchAdapter extends ArrayAdapter<Element> {
    private final ArrayList<Element> items;

    public SearchAdapter(Context context, int resource,
                         ArrayList<Element> objects) {
        super(context, resource, objects);

        items = objects;
    }

    public void eliminarMuseus() {
        Iterator<Element> it = items.iterator();
        while (it.hasNext()) {
            Element element = it.next();
            if (element instanceof Museu)
                it.remove();
        }
    }

    public void eliminarObres() {
        Iterator<Element> it = items.iterator();
        while (it.hasNext()) {
            Element element = it.next();
            if (element instanceof Obra)
                it.remove();
        }
    }

    public void eliminarAutors() {
        Iterator<Element> it = items.iterator();
        while (it.hasNext()) {
            Element element = it.next();
            if (element instanceof Autor)
                it.remove();
        }
    }
}
