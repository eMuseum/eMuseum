package ub.edu.pis2014.pis12.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APIResponse;
import android.content.Context;

public class Usuari {
	// Singleton
	private static Usuari _instance = null;
	
	public static Usuari get()
	{
		if (_instance == null)
			_instance = new Usuari();
		
		return _instance;
	}
	
	// Atributs
	private Context context = null;
	private String username = null;
	private String lang = null;
	private String userKey = null;
	private boolean logged = false;
	private Comentari ultimComentari = null;
	
	// El constructor �s privat per obligar a utilitzar get()
	private Usuari()
	{
		this.username = null;
		this.lang = Locale.getDefault().getISO3Language().toUpperCase(Locale.ENGLISH);
		this.logged = false;
	}
	
	public void setContext(Context context)
	{
		this.context = context;
	}
	
	public String getLanguage()
	{
		return lang;
	}
	
	public boolean isLogged()
	{
		return logged;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getUserKey()
	{
		return userKey;
	}
	
	public Comentari getUltimComentari()
	{
		return ultimComentari;
	}
	
	public boolean sessionLoad()
	{
		FileInputStream fis;
		try {
			fis = context.openFileInput("usuari.dat");
		} catch (FileNotFoundException e1) {
			return false;
		}
		
		Scanner scanner = new Scanner(fis);		
		if (!scanner.hasNextLine())
		{
			scanner.close();
			return false;
		}
		
		String username = scanner.nextLine();
		
		if (!scanner.hasNextLine())
		{
			scanner.close();
			return false;
		}
		
		String password = scanner.nextLine();
		scanner.close();
		
		return login(username, password);
	}
	
	public boolean sessionSave(String[] loginInfo)
	{
		loginInfo[1] = encriptar(loginInfo[1]);
		if (!login(loginInfo[0], loginInfo[1]))
			return false;
		
		return sessionFileSave(loginInfo);
	}
	
	private boolean sessionFileSave(String[] info)
	{
		FileOutputStream fos;
		try 
		{
			fos = context.openFileOutput("usuari.dat", Context.MODE_PRIVATE);
		} 
		catch (FileNotFoundException e)
		{
			return false;
		} 
		
		PrintWriter out = new PrintWriter(fos); 
		out.println(info[0]);
		out.println(info[1]);
		out.flush();
		
		try {
			fos.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	private boolean login(String username, String password)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		// Creem un hashmap amb els valors a enviar al servidor
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("Username", username);
		arguments.put("Password", password);
		
		// Intentem un "Login" amb els valors d'adalt
		APIResponse resposta = API.query("Login", arguments);
		if (!resposta.hasErrors())
		{
			this.username = username;
			this.userKey = resposta.Resposta;
			this.logged = true;
			
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public void logout()
	{
		logged = false;
		context.deleteFile("usuari.dat");
	}
	
	private String encriptar(String password)
	{
		MessageDigest mDigest;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
	        byte[] result = mDigest.digest(password.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	         
	        return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return password;
		}
	}
	
	public boolean registrar(String[] registerInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		registerInfo[1] = encriptar(registerInfo[1]);
		String username = registerInfo[0];
		String password = registerInfo[1];
		String email = registerInfo[2];
		
		// Creem un hashmap amb els valors a enviar al servidor
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("Username", username);
		arguments.put("Password", password);
		arguments.put("Email", email);
		
		// Intentem un "Register" amb els valors d'adalt
		APIResponse resposta = API.query("Register", arguments);
		
		if (!resposta.hasErrors())
		{
			this.username = username;
			this.logged = true;
			this.userKey = resposta.Resposta;
			
			return sessionFileSave(registerInfo);
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public boolean recover(String[] recoverInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		String email = recoverInfo[0];
		
		// Creem un hashmap amb els valors a enviar al servidor
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("Email", email);
		
		// Intentem un "Recover" amb els valors d'adalt
		APIResponse resposta = API.query("Recover", arguments);
		
		if (!resposta.hasErrors())
		{
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public boolean comentar(String[] comentariInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		String id = comentariInfo[0];
		String type = comentariInfo[1];
		String comentari = comentariInfo[2];
		
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("ID", id);
		arguments.put("Type", type);
		arguments.put("Comment", comentari);
		
		// Intentem un "Comment" amb els valors d'adalt
		APIResponse resposta = API.query("Comment", arguments);
		
		if (!resposta.hasErrors())
		{
			ultimComentari = new Comentari(comentari, Integer.parseInt(resposta.Resposta), Integer.parseInt(id), username);
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public boolean editarComentari(String[] comentariInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		String id = comentariInfo[0];
		String comentari = comentariInfo[1];
		
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("ID", id);
		arguments.put("Comment", comentari);
		
		// Intentem un "EditComment" amb els valors d'adalt
		APIResponse resposta = API.query("EditComment", arguments);
		
		if (!resposta.hasErrors())
		{
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public boolean borrarComentari(String[] comentariInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		String id = comentariInfo[0];
		
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("ID", id);
		
		// Intentem un "DeleteComment" amb els valors d'adalt
		APIResponse resposta = API.query("DeleteComment", arguments);
		
		if (!resposta.hasErrors())
		{
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
	
	public boolean valorar(String[] valorarInfo)
	{
		// Creem la API, serveix per connectar amb el servidor
		// Automaticament connecta i prova d'enviar un missatge
		if (!API.connect())
			return false;
		
		String id = valorarInfo[0];
		String type = valorarInfo[1];
		String valoracio = valorarInfo[2];
		
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("ID", id);
		arguments.put("Type", type);
		arguments.put("Rating", valoracio);
		
		// Intentem un "Rate" amb els valors d'adalt
		APIResponse resposta = API.query("Rate", arguments);
		
		if (!resposta.hasErrors())
		{
			Dades.setLastRating(Float.parseFloat(resposta.Resposta), Integer.parseInt(id), Integer.parseInt(type));
			return true;
		}
		else if (resposta.queryHasErrors())
			API.setError(resposta.MissatgeError);
		
		return false;
	}
}
