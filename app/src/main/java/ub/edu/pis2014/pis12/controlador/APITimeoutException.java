package ub.edu.pis2014.pis12.controlador;

public class APITimeoutException extends Exception {
	public APITimeoutException()
	{
        super();
    }

	public APITimeoutException(String message)
	{
        super(message);
    }
}
