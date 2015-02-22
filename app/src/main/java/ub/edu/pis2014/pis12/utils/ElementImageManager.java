package ub.edu.pis2014.pis12.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

/**
 * 
 * Descarrega les imatges d'internet, per mostrar en les activities mentre que a
 * la vegada guarda internament una llista per claus (semblant a un HashMap) els
 * bitmaps descarregats. Evitar descarregar mes d'un cop i permet mostrar en
 * diferents llocs de l'aplicacio
 * 
 * @author Guillem
 * 
 */
public class ElementImageManager {
	// Instancia de la classe, pel singleton
	private static ElementImageManager _instance = null;

	// Executador, on corren els threads
	private final ExecutorService executor = Executors.newFixedThreadPool(1);
	private final ArrayList<CancelableRunnable> executorTasks = new ArrayList<CancelableRunnable>();

	// Activity en la que estem executant
	private Activity activity = null;
	// Llistes on guardarem els bitmaps, cal fer-ne 3 ja que els 3 tipus
	// d'element
	// poden tenir indexos en comu
	private final SparseArray<Bitmap> mapaObres = new SparseArray<Bitmap>();
	private final SparseArray<Bitmap> mapaAutors = new SparseArray<Bitmap>();
	private final SparseArray<Bitmap> mapaMuseus = new SparseArray<Bitmap>();

	/**
	 * Classe propia utilitzada per descarregar les imatges
	 * Permet cancelar
	 * 
	 * @author Guillem
	 *
	 */
	class CancelableRunnable extends FutureTask<Void> {
		public CancelableRunnable(Runnable runnable) {
			super(runnable, null);
		}

		@Override
		public void run() {			
			// Executem el thread
			super.run();

			// Treiem de la llista
			synchronized (executorTasks) {
				executorTasks.remove(this);
			}
		}
	}

	/**
	 * Comproba si un element ja ha estat afegit i descarregat
	 * 
	 * @param element Element a comprobar
	 * @return El bitmap si existeix o null
	 */
	private Bitmap exists(Element element) {
		// Segons el tipus busquem a cada llista
		if (element instanceof Obra) {
			return getObra(element);
		} else if (element instanceof Autor) {
			return getAutor(element);
		} else if (element instanceof Museu) {
			return getMuseu(element);
		}

		return null;
	}

	/**
	 * Funcio encarregada de descarregar la imatge. Solament es crida
	 * internament quan una cerca no produeix el resultat esperat (la imatge no
	 * esta en la llista)
	 * 
	 * @param element
	 *            Element del que volem descarregar
	 * @param onImageDownload
	 *            Callback a executar al acabar
	 */
	private void download(final Element element,
			final OnImageDownload onImageDownload) {
		// Creem la tasca a correr en segon pla

		final CancelableRunnable task = new CancelableRunnable(new Runnable() {

			@Override
			public void run() {
				// Comprobem que no estigui ja afegit
				Bitmap bitmap = exists(element);

				// Si no existeix
				if (bitmap == null)
				{
					// Intentem obtenir el bitamp
					try {
						bitmap = BitmapFactory.decodeStream((InputStream) new URL(
								element.getImatgeURL()).getContent());
					}
					// Ignorem les excepcions, no ens cal tenir-les en compte
					catch (MalformedURLException e) {} 
					catch (IOException e) {}
	
					end(bitmap);
	
					// Si l'element es una obra
					if (element instanceof Obra) {
						// Synchronized serveix per, a mode de mutex, evitar un
						// acces simultani
						// en mes d'un thread al mateix objecte
						synchronized (mapaObres) {
							mapaObres.append(element.getId(), bitmap);
						}
					}
					// Si l'element es un autor
					else if (element instanceof Autor) {
						// Synchronized, evitar simultanies operacions al mateix
						// temps
						synchronized (mapaAutors) {
							mapaAutors.append(element.getId(), bitmap);
						}
					}
					// Si l'element es un museu
					if (element instanceof Museu) {
						// Synchronized, evitar simultanies operacions al mateix
						// temps
						synchronized (mapaMuseus) {
							mapaMuseus.append(element.getId(), bitmap);
						}
					}
				}
			}
			
			private void end(Bitmap bitmap)
			{
				// Establim el bitmap en l'element
				element.setImatge(bitmap);

				/*
				 * Alguna de les crides realitzades dins del onFinish poden
				 * afectar a la vista, i en android solament el thread principal
				 * (UiThread) pot fer-ho. D'altre forma es produeixen
				 * excepcions. Per tant, executem una segona tasca pero en el
				 * thread principal, que unicament servira per cridar l'onFinish
				 */
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// Cridem onFinish
						onImageDownload.onFinish(element);
					}
				});
			}
		});

		// Afegim a la llista
		synchronized (executorTasks) {
			executorTasks.add(task);
		}

		// Executem la tasca en el nostre Executor
		executor.execute(task);
	}

	/**
	 * Implementacio interna del metode cancelDownloads
	 */
	private void cancel() {
		synchronized (executorTasks) {
			Iterator<CancelableRunnable> it = executorTasks.iterator();
			while (it.hasNext()) {
				it.next().cancel(true);
			}
			executorTasks.clear();
		}
	}

	/**
	 * Metode d'us intern que retorna un bitmap d'una obra si esta en la llista,
	 * o null
	 * 
	 * @param element
	 *            Element del que busquem bitmap
	 * @return El bitmap si el troba o null
	 */
	private Bitmap getObra(Element element) {
		Bitmap bitmap = null;
		// Synchronized, evitar simultanies operacions al mateix temps
		synchronized (mapaObres) {
			bitmap = mapaObres.get(element.getId());
		}

		return bitmap;
	}

	/**
	 * Metode d'us intern que retorna un bitmap d'un autor si esta en la llista,
	 * o null
	 * 
	 * @param element
	 *            Element del que busquem bitmap
	 * @return El bitmap si el troba o null
	 */
	private Bitmap getAutor(Element element) {
		Bitmap bitmap = null;
		// Synchronized, evitar simultanies operacions al mateix temps
		synchronized (mapaAutors) {
			bitmap = mapaAutors.get(element.getId());
		}

		return bitmap;
	}

	/**
	 * Metode d'us intern que retorna un bitmap d'un museu si esta en la llista,
	 * o null
	 * 
	 * @param element
	 *            Element del que busquem bitmap
	 * @return El bitmap si el troba o null
	 */
	private Bitmap getMuseu(Element element) {
		Bitmap bitmap = null;
		// Synchronized, evitar simultanies operacions al mateix temps
		synchronized (mapaMuseus) {
			bitmap = mapaMuseus.get(element.getId());
		}

		return bitmap;
	}

	public static void getImage(Element element, OnImageDownload onImageDownload) {
		Bitmap bitmap = instance().exists(element);

		// Si l'hem trobat
		if (bitmap != null) {
			// Establim en l'element i cridem onFinish.
			// No es necessari fer-ho en un Runnable a UiThread, ja que ja hi
			// som
			element.setImatge(bitmap);
			onImageDownload.onFinish(element);
		} else {
			// Si no l'hem trobat, el posem a descarregar
			instance().download(element, onImageDownload);
		}
	}

	/**
	 * Permet establir la activity sobre la que estem actualment
	 * 
	 * @param activity
	 *            Activity en que estem
	 */
	public static void setActivity(Activity activity) {
		instance().activity = activity;
	}

	/**
	 * Cancela qualsevol descarrega pendent
	 */
	public static void cancelDownloads() {
		instance().cancel();
	}

	/**
	 * Retorna una instancia valida i unica de la clase
	 * 
	 * @return Instancia de la propia clase
	 */
	private static ElementImageManager instance() {
		if (_instance == null)
			_instance = new ElementImageManager();

		return _instance;
	}
}