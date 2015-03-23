package ub.edu.pis2014.pis12.controlador;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask.Status;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import ub.edu.pis2014.pis12.R;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.model.Usuari;

/**
 * La classe API ens serveix per comunicar-nos amb la API en el servidor
 *
 * @author Guillem
 */
public class API {
    // Clau unica de l'aplicacio, utilitzada en el mode "sense login" i pel primer
    // paquer del mode login
    private static final String AppKey = "c740f63ca8184204a983dfee6dbd807f35118975d9e346aaa6032306cec0e254";

    // Cada peticio al servidor fa servir una clau unica, per seguretat
    // I per evitar duplicats de misstages
    private static String RequestKey = "";

    private static boolean hasShownConnectionError = false;
    private static boolean hasErrors = false;
    private static boolean errorFromID = true;
    private static int errorStringID = 0;
    private static String errorString = "";

    private static APIOperation operation = null;

    public enum API_ERROR {
        API_ERROR_NONE,
        API_ERROR_INTERNAL,
        API_ERROR_QUERY
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Inicia un thread amb una consulta al servidor
     *
     * @param context  Context d'aplicacio
     * @param type     Tipus de consulta
     * @param notifier Notificador de finalitzacio
     * @param params   Parametres a enviar al thread
     */
    public static boolean StartOperation(Context context, TASK_TYPE type, APINotifier notifier, String... params) {
        if (isNetworkAvailable(context)) {
            operation = new APIOperation(context, type, notifier);
            operation.execute(params);
            hasShownConnectionError = false;
        } else if (!hasShownConnectionError) {
            Toast.makeText(context, R.string.API_no_network, Toast.LENGTH_LONG).show();
            notifier.onResult(false);
            hasShownConnectionError = true;
        }

        return !hasShownConnectionError;
    }

    /**
     * Cancela un thread si actualment n'hi ha cap
     *
     * @see Ha de ser cridat OBLIGATORIAMENT a onDestroy
     */
    public static void CancelOperation() {
        if (operation != null && operation.getStatus() == Status.RUNNING) {
            operation.cancel(true);
        }
    }

    /**
     * Genera un paquet per enviar al servidor en format JSON
     *
     * @author Guillem
     */
    private static class Packet {
        /**
         * Es el paquet que cobte la clau unica per fer la peticio
         * Ho fa a partir d'una clau d'aplicacio.
         *
         * @return Objecte JSON amb les dades per fer la peticio
         */
        static JSONObject getJSON(String AppKey) {
            return getJSON(AppKey, "RequestKey", null);
        }

        /**
         * Amb parametres, permet fer una peticio explicita. Solament es pot fer servir
         * un cop ja es te una clau unica de peticio
         *
         * @param PublicKey Clau de peticio
         * @param Function  Funcio del servidor
         * @param arguments Parametres a enviar
         * @return
         */
        static JSONObject getJSON(String PublicKey, String Function, Map<String, String> arguments) {
            // Objecte JSON
            JSONObject Packet = new JSONObject();

            try {
                // Header, inclou la clau publica
                JSONObject Header = new JSONObject();
                Header.put("PublicKey", PublicKey);
                Header.put("Language", Usuari.get().getLanguage());

                // Cos, inclou la funcio i els parametres
                JSONObject Body = new JSONObject();
                Body.put("Function", Function);

                // Si te parametres
                if (arguments != null) {
                    // Els posem en format "NomParametre: Valor, ..."
                    JSONObject Arguments = new JSONObject();
                    for (Map.Entry<String, String> entry : arguments.entrySet()) {
                        Arguments.put(entry.getKey(), entry.getValue());
                    }

                    // Els situem en el cos
                    Body.put("Arguments", Arguments);
                }

                // Ho posem tot en el paquet
                Packet.put("Auth", Header);
                Packet.put("Call", Body);
            } catch (JSONException e) {
                // ERROR
            }

            return Packet;
        }
    }

    public API() {
    }

    /**
     * Permet connectar amb el servidor sense especificar una clau d'usuari, fa servir la
     * de l'aplicacio
     *
     * @throws IOException
     * @throws JSONException
     */
    public static boolean connect() {
        if (Usuari.get().isLogged())
            return connect(Usuari.get().getUserKey());
        return connect(AppKey);
    }

    /**
     * Permet connectar amb el servidor especificant una clau d'usuari i, per tant, fer peticions
     * com comentar, valorar, etc.
     *
     * @param key Clau d'usuari, obtinguda despres d'un Login
     * @throws IOException
     * @throws JSONException
     */
    public static boolean connect(String key) {
        // Restableix que no hi ha errors
        hasErrors = false;

        // Connecta, creant un packet amb una clau especifica, i guarda com a
        // clau de peticio la resposta (Return) del servidor
        try {
            RequestKey = connectAndRead(Packet.getJSON(key)).getString("Return");
        } catch (IOException e) {
            setError(R.string.API_error_connexio);
            return false;
        } catch (APITimeoutException e) {
            setError(R.string.API_error_timeout);
            return false;
        } catch (JSONException e) {
            setError(R.string.API_error_json);
            return false;
        }

        return true;
    }

    /**
     * Metode privat que connecta amb el servidor i obte una resposta
     *
     * @param json JSON a enviar
     * @return JSON de resposta
     * @throws JSONException
     * @throws IOException
     */
    private static JSONObject connectAndRead(JSONObject json) throws JSONException, IOException, APITimeoutException {
        // Crea el socket
        Socket socket = new Socket();
        // Temps maxim de lectura de 2 segons
        socket.setSoTimeout(4000);
        // Connecta amb un timeout de 2 segons
        socket.connect(new InetSocketAddress("62.210.253.55", 8000), 4000);

        // Envia el JSON
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.write(json.toString());
        out.flush();

        // Llegeix la resposta
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        // Fins que obtenim -1 (s'ha acabat)
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                int read = in.read();
                if (read == -1)
                    break;

                builder.append((char) read);
            } catch (IOException e) {
                socket.close();
                throw new APITimeoutException();
            }
        }
        socket.close();

        // Retornem la cadena en JSON
        return new JSONObject(builder.toString());
    }

    /**
     * Permet fer una consulta al servidor, especificant quina i com
     *
     * @param Function  Consulta a fer
     * @param Arguments Parametres de la consulta
     * @return
     */
    public static APIResponse query(String Function, Map<String, String> Arguments) {
        APIResponse resposta = new APIResponse();
        try {
            JSONObject object = null;
            object = connectAndRead(Packet.getJSON(RequestKey, Function, Arguments));

            API_ERROR error = object.getInt("HasErrors") == 1 ? API_ERROR.API_ERROR_QUERY : API_ERROR.API_ERROR_NONE;
            resposta.Error = error;
            resposta.Resposta = object.getString("Return");
            resposta.MissatgeError = object.getString("Message");

            return resposta;
        } catch (IOException e) {
            setError(R.string.API_error_connexio);
            return resposta;
        } catch (APITimeoutException e) {
            setError(R.string.API_error_timeout);
            return resposta;
        } catch (JSONException e) {
            setError(R.string.API_error_json);
            return resposta;
        }
    }

    /**
     * Estableix un missatge d'error que es mostrara quan acabi
     * el thread
     *
     * @param message Missatge a mostrar
     */
    public static void setError(int messageId) {
        // Es possible que la propia API ja hagi generat algun error
        hasErrors = true;
        errorFromID = true;
        errorStringID = messageId;
    }

    /**
     * Estableix un missatge d'error que es mostrara quan acabi
     * el thread
     *
     * @param message Missatge a mostrar
     */
    public static void setError(String messageId) {
        // Es possible que la propia API ja hagi generat algun error
        hasErrors = true;
        errorFromID = false;
        errorString = messageId;
    }

    /**
     * Retorna si hem establert un missatge d'error o no
     *
     * @return True si hem establert missatge d'error
     */
    public static boolean hasErrors() {
        return hasErrors;
    }

    /**
     * Retorna si el missatge d'error s'ha establit a partir d'un ID d'R
     * o be a partir d'una String
     *
     * @return
     */
    public static boolean showErrorFromID() {
        return errorFromID;
    }

    /**
     * Retorna el missatge d'error, si n'hi ha
     *
     * @return El missatge d'error
     */
    public static int getErrorStringID() {
        return errorStringID;
    }

    /**
     * Retorna el missatge d'error, si n'hi ha
     *
     * @return El missatge d'error
     */
    public static String getErrorString() {
        return errorString;
    }
}
