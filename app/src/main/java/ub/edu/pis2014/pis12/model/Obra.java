package ub.edu.pis2014.pis12.model;

/**
 * Clase obra hereta de Element
 * Conte l'enter museu que indica l'identificador del museu on esta inclosa l'obra
 */
public class Obra extends Element {
	
	private int idmuseu;
	private int idautor;
	
	/**
	 * Crea una nova obra especificant el nom
	 * 
	 * @param titol Titol de l'obra
	 */
	public Obra(int id, int mid, int aid, String titol, String descripcio, String imatge) {
		super(id, titol, descripcio, imatge);
		
		this.idmuseu = mid;
		this.idautor = aid;
	}
	
	@Override
	public int getMuseuId()
	{
		return idmuseu;
	}
	
	/**
	 * Estableix l'identificador del autor
	 * 
	 * @param id Identificador
	 */
	public int getAutorId(){
		return idautor;
	}
}
