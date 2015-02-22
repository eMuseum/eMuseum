package ub.edu.pis2014.pis12.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;

import ub.edu.pis2014.pis12.utils.EMuseumService.LocalBinder;

/**
 * Connector al servei que realitza especificament la feina de descarregar
 * la base de dades.
 * Desconnecta un cop acaba la descarrega i crida al metode abstracta
 * que ella mateixa defineix
 *
 * @author Guillem
 */
public abstract class DBDownConnection implements ServiceConnection {
    private EMuseumService service;
    private Context context;
    private String url;
    private File db;

    /**
     * Constructor
     *
     * @param context Context en el que correm
     * @param url     URL de descarrega
     * @param db      Arxiu de desti
     */
    public DBDownConnection(Context context, String url, File db) {
        this.context = context;
        this.url = url;
        this.db = db;
    }

    /**
     * Cridat quan connecta amb el servei
     */
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder ibinder) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        LocalBinder binder = (LocalBinder) ibinder;
        service = binder.getService();

        // Posem un callback
        service.setOnDBDownload(new OnDBDownload() {

            @Override
            public void onResult(boolean result) {
                // Un cop acabat, ens deslliguem
                context.unbindService(DBDownConnection.this);

                // Cridem el metode abstracte
                OnDownload(result);
            }
        });

        // Comencem la descarrega de la DB
        service.startDBDownload(url, db);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
    }

    /**
     * Metode abstracta que es crida un cop acaba la descarrega
     *
     * @param result Es true si s'ha completat, false sino
     */
    protected abstract void OnDownload(boolean result);
};