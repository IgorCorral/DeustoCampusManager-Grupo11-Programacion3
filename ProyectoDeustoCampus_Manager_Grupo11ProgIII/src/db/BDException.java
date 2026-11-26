package db;

// Excepción propia para los errores de la base de datos. Su mensaje explica qué ha fallado
// y se enseña al usuario en un JOptionPane; la SQLException original se guarda como causa.
public class BDException extends Exception {

	private static final long serialVersionUID = 1L;

	public BDException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
