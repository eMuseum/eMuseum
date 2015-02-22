package ub.edu.pis2014.pis12.model;

import android.annotation.SuppressLint;

import java.util.Calendar;
import java.util.Date;

@SuppressLint("DefaultLocale")
public class Comentari {

    private int id;
    private int id_element;
    private String usuari;
    private String text;
    private long data;

    public Comentari(String txt, int id, int id_element, String usuari) {
        this.text = txt;
        this.id = id;
        this.id_element = id_element;
        this.usuari = usuari;
        this.data = (new Date()).getTime();
    }

    public Comentari(String txt, int id, int id_element, String usuari, long data) {
        this.text = txt;
        this.id = id;
        this.id_element = id_element;
        this.usuari = usuari;
        this.data = data * 1000;
    }

    public void setText(String txt) {
        text = txt;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }

    public int getIdelement() {
        return id_element;
    }

    public String getUsuari() {
        return usuari;
    }

    public long getData() {
        return data;
    }

    public CharSequence getDataFormat() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(data);
        String minut = String.format("%02d", c.get(Calendar.MINUTE));
        String hora = String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
        return c.get(Calendar.DATE) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR) + " a les " + hora + ":" + minut;
    }
}
