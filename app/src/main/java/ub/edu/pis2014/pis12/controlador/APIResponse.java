package ub.edu.pis2014.pis12.controlador;

import ub.edu.pis2014.pis12.controlador.API.API_ERROR;

public class APIResponse
{
	// Per defecte assumim que hi ha hagut un error intern
	public API_ERROR Error = API_ERROR.API_ERROR_INTERNAL;
	public String Resposta = "";
	public String MissatgeError = "";
	
	/**
	 * Retorna si hi ha hagut algun error (intern o query)
	 * 
	 * @return Retorna true si n'hi ha, false sino
	 */
	public boolean hasErrors()
	{
		return Error != API_ERROR.API_ERROR_NONE;
	}
	
	/**
	 * Retorna si hi ha hagut un error, i si n'hi ha hagut, si es
	 * troba en la query
	 * 
	 * @return Retorna true si hi ha un error de query, false sino
	 */
	public boolean queryHasErrors()
	{
		return Error == API_ERROR.API_ERROR_QUERY;
	}
}