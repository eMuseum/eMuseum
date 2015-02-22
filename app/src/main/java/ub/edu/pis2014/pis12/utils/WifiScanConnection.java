package ub.edu.pis2014.pis12.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import ub.edu.pis2014.pis12.utils.EMuseumService.LocalBinder;

/**
 * Connector al servei que realitza especificament la feina de descarregar
 * la base de dades.
 * Desconnecta un cop acaba la descarrega i crida al metode abstracta
 * que ella mateixa defineix
 *
 * @author Guillem
 */
public abstract class WifiScanConnection implements ServiceConnection {
    protected EMuseumService service;
    protected Context context;

    /**
     * Constructor
     *
     * @param context Context en el que correm
     * @param url     URL de descarrega
     * @param db      Arxiu de desti
     */
    public WifiScanConnection(Context context) {
        this.context = context;
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

        onConnected();

        // Desconnectem
        context.unbindService(WifiScanConnection.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
    }

    protected abstract void onConnected();
};