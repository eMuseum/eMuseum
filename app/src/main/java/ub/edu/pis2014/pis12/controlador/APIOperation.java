package ub.edu.pis2014.pis12.controlador;

import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Usuari;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public final class APIOperation extends AsyncTask<String, Void, Boolean>
{
	public enum TASK_TYPE
	{
		API_SESSION_LOAD,
		API_SESSION_SAVE,
		API_RECOVER,
		API_REGISTER,
		API_CHECK_VERSION,
		API_DOWNLOAD_DB,
		API_COMMENT,
		API_EDIT_COMMENT,
		API_DELETE_COMMENT,
		API_GET_COMMENTS,
		API_RATE,
		API_GET_RATING
	}
	
	private Context context;
	private TASK_TYPE type;
	private APINotifier notifier = null;

	public APIOperation(Context context, TASK_TYPE type, APINotifier notifier)
	{
		super();
		this.context = context;
		this.type = type;
		this.notifier = notifier;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {	
		switch (type)
		{
			case API_SESSION_LOAD:
				return Usuari.get().sessionLoad();
			case API_SESSION_SAVE:
				return Usuari.get().sessionSave(params);
			case API_RECOVER:
				return Usuari.get().recover(params);
			case API_REGISTER:
				return Usuari.get().registrar(params);
			case API_CHECK_VERSION:
				return Dades.checkVersion();
			case API_DOWNLOAD_DB:
				return Dades.descarregaDB();
			case API_COMMENT:
				return Usuari.get().comentar(params);
			case API_EDIT_COMMENT:
				return Usuari.get().editarComentari(params);
			case API_DELETE_COMMENT:
				return Usuari.get().borrarComentari(params);
			case API_GET_COMMENTS:
				return Dades.fetchComments(params);
			case API_RATE:
				return Usuari.get().valorar(params);
			case API_GET_RATING:
				return Dades.getRating(params);
				
			default:
				return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (API.hasErrors())
		{
			if (API.showErrorFromID())
			{
				Toast.makeText(context, API.getErrorStringID(), Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(context, API.getErrorString(), Toast.LENGTH_LONG).show();
			}
		}
		
		if (notifier != null)
			notifier.onResult(result);
	}
}
