package db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import domain.Espacio;
import domain.Reserva;
import domain.Usuario;

public class GestorDatosMemoria implements GestorDatos {
	
	private List<Usuario> usuarios = new ArrayList<>();
	private List<Espacio> espacios = new ArrayList<>();
	private List<Reserva> reservas = new ArrayList<>();
	
	
	@Override
	public Usuario login(String email, String contrasena) {
		for (Usuario u : usuarios) {
			if (u.getEmail().equals(email) && u.comprobarContrasena(contrasena)) {
				return u;
			}
		}
		return null;
	}

	@Override
	public List<Espacio> buscarEspaciosLibres(LocalDate fecha, String hora) {
		List<Espacio> libres = new ArrayList<>();
		for (Espacio e : espacios) {
			if (estaLibre(e, fecha, hora)) {
				libres.add(e);
			}
		}
		return libres;
	}

	// si no hay ninguna reserva en ese espacio en un dia y fecha, el espacio esta libre
	private boolean estaLibre(Espacio espacio, LocalDate fecha, String hora) {
		for (Reserva r : reservas) {
			if (r.getEspacio().equals(espacio) && r.ocupa(fecha, hora)) {
				return false;
			}
		}
		return true;
	}
}