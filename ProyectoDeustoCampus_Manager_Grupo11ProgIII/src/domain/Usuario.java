package domain;

public class Usuario {

	private String email;
	private String nombre;
	private String contrasena;
	private Rol rol;

	public Usuario(String email, String nombre, String contrasena, Rol rol) {
		this.email = email;
		this.nombre = nombre;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	public String getEmail() {
		return email;
	}

	public String getNombre() {
		return nombre;
	}

	public Rol getRol() {
		return rol;
	}

	// no hay getContrasena(): la contraseña no sale de la clase, solo se comprueba aquí
	public boolean comprobarContrasena(String contrasena) {
		return this.contrasena.equals(contrasena);
	}
}



