package ub.edu.pis2014.pis12.model;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ub.edu.pis2014.pis12.MainFragment;
import ub.edu.pis2014.pis12.R;
import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APIResponse;
import ub.edu.pis2014.pis12.utils.DBDownConnection;
import ub.edu.pis2014.pis12.utils.EMuseumService;

/**
 * Gestiona la base de dades interna, en tots
 * els sentits, inclos actualitzarla
 *
 * @author Guillem
 */
public class Dades {
    private static Dades _instance = null;
    // Base de dades
    private SQLiteDatabase db = null;
    private Context context = null;
    private DBDownConnection dbDownConnection = null;
    private ArrayList<Comentari> comentaris = new ArrayList<Comentari>();
    private float lastRating = 0;

    private Dades() {
    }

    /**
     * Fa una consulta a la base de dades, ajuntant mes d'una taula a la vegada
     * i ordenant o restringint en conjunt.
     *
     * @param museus True si busquem museus, false sino
     * @param autors True si busquem autors, false sino
     * @param obres  True si busquem obres, false sino
     * @param where  Restriccio where de la consulta (o null sino)
     * @param order  Order by de la consulta (o null sino)
     * @param limit  Limit de la consulta (o null sino)
     * @return Una array amb els elements obtinguts de la DB
     */
    public static ArrayList<Element> cercaMixta(boolean museus, boolean autors, boolean obres, String where, String order, String limit) {
        // Variable on guardarem l'array
        String query = "";

        // Si hem d'afegir museus
        if (museus) {
            query += "SELECT id as m_id, 0 as a_id, 0 as o_id, 0 as mid, 0 as aid, nom, direccio, valoracio, latitud, longitud, telefon, hora_laboral_inici, hora_laboral_fi, hora_festiu_inici, hora_festiu_fi, descripcio, imatge, 'TableA' as TableName FROM e_museus ";
        }
        // Si hem d'afegir autors
        if (autors) {
            // Afegim la unio entre taules
            if (query.length() > 0)
                query += "UNION ";
            query += "SELECT 0 as m_id, id as a_id, 0 as o_id, 0 as mid, 0 as aid, nom, '' as direccio, valoracio, 0 as latitud, 0 as longitud, '' as telefon, 0 as hora_laboral_inici, 0 as hora_laboral_fi, 0 as hora_festiu_inici, 0 as hora_festiu_fi, descripcio, imatge, 'TableB' as TableName FROM e_autors ";
        }
        // Si hem d'afegir obres
        if (obres) {
            // Afegim la unio entre taules
            if (query.length() > 0)
                query += "UNION ";
            query += "SELECT 0 as m_id, 0 as a_id, id as o_id, mid, aid, nom, '' as direccio, valoracio, 0 as latitud, 0 as longitud, '' as telefon, 0 as hora_laboral_inici, 0 as hora_laboral_fi, 0 as hora_festiu_inici, 0 as hora_festiu_fi, descripcio, imatge, 'TableC' as TableName FROM e_obres ";
        }

        // Construim l'array
        ArrayList<Element> elements = new ArrayList<Element>();
        // Si no hi ha consulta, retornem
        if (query.length() == 0)
            return elements;

        // Ho agrupem tot en un sol select, per tal de poder fer una cerca global
        // despres, d'entre tots els elements i no solament un
        query = "SELECT * FROM (" + query + ") ";

        // Posem where si n'hi ha
        if (where != null)
            query += "WHERE " + where + " ";

        // Posem order si n'hi ha, sino per tipus
        if (order != null)
            query += "ORDER BY " + order + " ";
        else
            query += "ORDER BY TableName ";

        // Posem limit si n'hi ha
        if (limit != null)
            query += "LIMIT " + limit + " ";

        // Fem la consulta
        Cursor c = null;
        try {
            c = instance().db.rawQuery(query, null);

            if (c != null) {
                while (c.moveToNext()) {
                    int m_id = c.getInt(c.getColumnIndex("m_id"));
                    int a_id = c.getInt(c.getColumnIndex("a_id"));
                    int o_id = c.getInt(c.getColumnIndex("o_id"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String direccio = c.getString(c.getColumnIndex("direccio"));
                    String telefon = c.getString(c.getColumnIndex("telefon"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));

                    // Si m_id es diferent de 0, significa que es un museu
                    if (m_id != 0) {
                        double latitud = c.getDouble(c.getColumnIndex("latitud"));
                        double longitud = c.getDouble(c.getColumnIndex("longitud"));
                        elements.add(new Museu(m_id, nom, direccio, telefon, descripcio, imatge, "", latitud, longitud));
                    }
                    // Si a_id es diferent de 0, significa que es un autor
                    else if (a_id != 0) {
                        elements.add(new Autor(a_id, nom, descripcio, imatge));
                    }
                    // Si o_id es diferent de 0, significa que es una obra
                    else if (o_id != 0) {
                        int mid = c.getInt(c.getColumnIndex("mid"));
                        int aid = c.getInt(c.getColumnIndex("aid"));
                        elements.add(new Obra(o_id, mid, aid, nom, descripcio, imatge));
                    }
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return elements;
    }

    /**
     * Retorna museus desde f fins a t (LIMIT f,t) segons una busca where
     *
     * @param f     Desde quin retornar
     * @param t     Fins a quin retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array de museus trobats
     */
    public static ArrayList<Museu> getMuseus(int f, int t, String where, String order) {
        ArrayList<Museu> museus = new ArrayList<Museu>();

        Cursor c = null;
        try {
            c = instance().db.query("e_museus",        // TABLE
                    new String[]{"id", "nom", "direccio", "telefon", "descripcio",
                            "imatge", "wifi_password", "latitud", "longitud", "hora_laboral_inici",
                            "hora_laboral_fi", "hora_festiu_inici", "hora_festiu_fi"}, // COLUMNS
                    where,                                // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    order,                                // ORDER BY
                    f + "," + t                            // LIMIT
            );

            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex("id"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String direccio = c.getString(c.getColumnIndex("direccio"));
                    String telefon = c.getString(c.getColumnIndex("telefon"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));
                    String wifiPassword = c.getString(c.getColumnIndex("wifi_password"));
                    double latitud = c.getDouble(c.getColumnIndex("latitud"));
                    double longitud = c.getDouble(c.getColumnIndex("longitud"));

                    int laboralInici = c.getInt(c.getColumnIndex("hora_laboral_inici"));
                    int laboralFi = c.getInt(c.getColumnIndex("hora_laboral_fi"));
                    int festiuInici = c.getInt(c.getColumnIndex("hora_festiu_inici"));
                    int festiuFi = c.getInt(c.getColumnIndex("hora_festiu_fi"));

                    Museu museu = new Museu(id, nom, direccio, telefon, descripcio, imatge, wifiPassword, latitud, longitud);
                    museu.setHoraObertLaborables(laboralInici);
                    museu.setHoraTancatLaborables(laboralFi);
                    museu.setHoraObertFestius(festiuInici);
                    museu.setHoraTancatFestius(festiuFi);
                    museus.add(museu);
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return museus;
    }

    /**
     * Retorna n museus desde segons una busca where
     *
     * @param n     Nombre de museus a retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array de museus trobats
     */
    public static ArrayList<Museu> getMuseus(int n, String where, String order) {
        return getMuseus(0, n, where, order);
    }

    /**
     * Retorna obres desde f fins a t (LIMIT f,t) segons una busca where
     *
     * @param f     Desde quin retornar
     * @param t     Fins a quin retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array d'obres trobats
     */
    public static ArrayList<Obra> getObres(int f, int t, String where, String order) {
        ArrayList<Obra> obres = new ArrayList<Obra>();

        Cursor c = null;
        try {
            c = instance().db.query("e_obres",            // TABLE
                    new String[]{"id", "mid", "aid", "nom", "descripcio", "imatge"}, // COLUMNS
                    where,                                // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    order,                                // ORDER BY
                    f + "," + t                            // LIMIT
            );

            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex("id"));
                    int mid = c.getInt(c.getColumnIndex("mid"));
                    int aid = c.getInt(c.getColumnIndex("aid"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));

                    obres.add(new Obra(id, mid, aid, nom, descripcio, imatge));
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return obres;
    }

    public static ArrayList<Element> getObres(Autor autor) {
        ArrayList<Element> obres = new ArrayList<Element>();
        String where = "aid=" + autor.identificador;

        Cursor c = null;
        try {
            c = instance().db.query("e_obres",            // TABLE
                    new String[]{"id", "mid", "aid", "nom", "descripcio", "imatge"}, // COLUMNS
                    where,                                // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    null,                                // ORDER BY
                    null                            // LIMIT
            );

            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex("id"));
                    int mid = c.getInt(c.getColumnIndex("mid"));
                    int aid = c.getInt(c.getColumnIndex("aid"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));

                    obres.add(new Obra(id, mid, aid, nom, descripcio, imatge));
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return obres;
    }

    public static ArrayList<Element> getObres(Museu museu) {
        ArrayList<Element> obres = new ArrayList<Element>();
        String where = "mid=" + museu.identificador;

        Cursor c = null;
        try {
            c = instance().db.query("e_obres",            // TABLE
                    new String[]{"id", "mid", "aid", "nom", "descripcio", "imatge"}, // COLUMNS
                    where,                                // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    null,                                // ORDER BY
                    null                            // LIMIT
            );

            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex("id"));
                    int mid = c.getInt(c.getColumnIndex("mid"));
                    int aid = c.getInt(c.getColumnIndex("aid"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));

                    obres.add(new Obra(id, mid, aid, nom, descripcio, imatge));
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return obres;
    }

    /**
     * Retorna n obres desde segons una busca where
     *
     * @param n     Nombre d'obres a retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array d'obres trobats
     */
    public static ArrayList<Obra> getObres(int n, String where, String order) {
        return getObres(0, n, where, order);
    }

    /**
     * Retorna autors desde f fins a t (LIMIT f,t) segons una busca where
     *
     * @param f     Desde quin retornar
     * @param t     Fins a quin retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array d'autors trobats
     */
    public static ArrayList<Autor> getAutors(int f, int t, String where, String order) {
        ArrayList<Autor> autors = new ArrayList<Autor>();

        Cursor c = null;
        try {
            c = instance().db.query("e_autors",        // TABLE
                    new String[]{"id", "nom", "descripcio", "imatge"}, // COLUMNS
                    where,                                // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    order,                                // ORDER BY
                    f + "," + t                            // LIMIT
            );

            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndex("id"));
                    String nom = c.getString(c.getColumnIndex("nom"));
                    String descripcio = c.getString(c.getColumnIndex("descripcio"));
                    String imatge = c.getString(c.getColumnIndex("imatge"));

                    autors.add(new Autor(id, nom, descripcio, imatge));
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return autors;
    }

    /**
     * Retorna n autors desde segons una busca where
     *
     * @param n     Nombre d'autors a retornar
     * @param where Parametres de cerca (null, sense restriccions)
     * @param order Ordenacio dels resultats (null, sense ordenar)
     * @return Array d'autors trobats
     */
    public static ArrayList<Autor> getAutors(int n, String where, String order) {
        return getAutors(0, n, where, order);
    }

    /**
     * Retorna un museu en concret
     *
     * @param id Identificador del museu
     * @return El museu si el troba o null
     */
    public static Museu getMuseu(int id) {
        ArrayList<Museu> museus = getMuseus(1, "id=" + id, null);
        if (museus.size() == 0)
            return null;

        return museus.get(0);
    }

    /**
     * Retorna un autor en concret
     *
     * @param id Identificador de l'autor
     * @return L'autor si el troba o null
     */
    public static Autor getAutor(int id) {
        ArrayList<Autor> autors = getAutors(1, "id=" + id, null);
        if (autors.size() == 0)
            return null;

        return autors.get(0);
    }

    /**
     * Retorna una obra en concret
     *
     * @param id Identificador de l'obra
     * @return L'obra si el troba o null
     */
    public static Obra getObra(int id) {
        ArrayList<Obra> obres = getObres(1, "id=" + id, null);
        if (obres.size() == 0)
            return null;

        return obres.get(0);
    }

    /**
     * Retorna la versio de la base de dades global
     *
     * @return Versio de la base de dades global
     */
    private static int getVersio() {
        int v = 0;
        Cursor c = null;
        try {
            c = instance().db.query("e_version",        // TABLE
                    new String[]{"name", "version"},    // COLUMNS
                    "name='Global'",                    // WHERE
                    null,                                // WHERE ARGS
                    null,                                // GROUP BY
                    null,                                // HAVING
                    null,                                // ORDER BY
                    "1"                                    // LIMIT
            );

            if (c != null) {
                if (c.moveToNext()) {
                    v = c.getInt(c.getColumnIndex("version"));
                }
            }
        } catch (SQLException e) {
        } finally {
            if (c != null)
                c.close();
        }

        return v;
    }

    /**
     * Comproba si la versio es igual a la del servidor o no
     *
     * @return Retorna true si cal actualitzar, false sino
     */
    public static boolean checkVersion() {
        if (!API.connect())
            return false;

        HashMap<String, String> arguments = new HashMap<String, String>();
        arguments.put("Version", String.valueOf(getVersio()));

        APIResponse response = API.query("CheckVersion", arguments);
        if (!response.hasErrors()) {
            return response.Resposta.equals("0");
        }

        return false;
    }

    /**
     * Si cal actualitzar, aquest metode es crida, per iniciar/lligar-se al
     * servei i descarregar
     *
     * @return Retorna true si inicia be la descarrega, false sino
     */
    public static boolean descarregaDB() {
        if (!API.connect())
            return false;

        APIResponse response = API.query("GetDBURL", null);
        if (response.hasErrors()) {
            API.setError(response.MissatgeError);
            return false;
        }

        instance().dbDownConnection = new DBDownConnection(instance().context, response.Resposta, instance().context.getDatabasePath("emuseum.db")) {
            @Override
            protected void OnDownload(boolean result) {
                if (result) {
                    Toast.makeText(instance().context, R.string.toast_db_updated, Toast.LENGTH_LONG).show();

                    // Borrem l'estat guardat del MainFragment
                    MainFragment.clearStatic();

                    // Reiniciem la app
                    Context context = instance().context.getApplicationContext();
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);
                } else {
                    Toast.makeText(instance().context, R.string.toast_db_update_error, Toast.LENGTH_LONG).show();
                }
            }
        };

        // Creem el servei a menys que ja estigui creat
        Intent i = new Intent(instance().context, EMuseumService.class);
        if (!EMuseumService.isRuning(instance().context)) {
            instance().context.startService(i);
        }

        // Intentem lligar-nos al servei
        instance().context.bindService(i, instance().dbDownConnection, Context.BIND_AUTO_CREATE);

        return true;
    }

    public static boolean fetchComments(String[] parametres) {
        if (!API.connect())
            return false;

        // Creem un hashmap amb els valors a enviar al servidor
        HashMap<String, String> arguments = new HashMap<String, String>();
        arguments.put("ID", parametres[0]); // ID
        arguments.put("Type", parametres[1]); // Type

        if (parametres.length > 2) {
            arguments.put("From", parametres[2]);
        }

        instance().comentaris.clear();
        APIResponse response = API.query("GetComments", arguments);
        if (!response.hasErrors()) {
            try {
                JSONObject object = new JSONObject("{commentsArray: " + response.Resposta + "}");
                JSONArray comments = object.getJSONArray("commentsArray");

                for (int i = 0; i < comments.length(); ++i) {
                    JSONObject comentari = comments.getJSONObject(i);
                    instance().comentaris.add(new Comentari(comentari.getString("Comment"), comentari.getInt("ID"), Integer.valueOf(parametres[0]), comentari.getString("Username"), comentari.getLong("Date")));
                }
            } catch (JSONException e) {
                return false;
            }

            return true;
        } else if (response.queryHasErrors())
            API.setError(response.MissatgeError);

        return false;
    }

    public static ArrayList<Comentari> getComments() {
        return instance().comentaris;
    }

    public static boolean getRating(String[] ratingInfo) {
        if (!API.connect())
            return false;

        // Creem un hashmap amb els valors a enviar al servidor
        HashMap<String, String> arguments = new HashMap<String, String>();
        arguments.put("ID", ratingInfo[0]); // ID
        arguments.put("Type", ratingInfo[1]); // Type

        APIResponse response = API.query("GetRating", arguments);
        if (!response.hasErrors()) {
            setLastRating(Float.parseFloat(response.Resposta), Integer.parseInt(ratingInfo[0]), Integer.parseInt(ratingInfo[1]));
            return true;
        } else if (response.queryHasErrors())
            API.setError(response.MissatgeError);

        instance().lastRating = 0;
        return false;
    }

    public static void setLastRating(float lastRating, int id, int tipus) {
        instance().lastRating = lastRating;

        String table = null;
        switch (TIPUS_ELEMENT.values()[tipus]) {
            case TIPUS_AUTOR:
                table = "e_autors";
                break;

            case TIPUS_MUSEU:
                table = "e_museus";
                break;

            case TIPUS_OBRA:
                table = "e_obres";
                break;

            default:
                break;
        }

        if (table == null)
            return;

        try {
            instance().db.execSQL("UPDATE " + table + " SET valoracio=" + lastRating + " WHERE id=" + id);
        } catch (SQLException e) {
        }
    }

    public static float getLastRating() {
        return instance().lastRating;
    }

    private static Dades instance() {
        if (_instance == null)
            _instance = new Dades();

        return _instance;
    }

    public static void initialize(Context context) {
        instance().context = context;
        instance().db = context.openOrCreateDatabase("emuseum.db", Context.MODE_PRIVATE, null);
    }

    public static void close() {
        instance().db.close();
    }
}
