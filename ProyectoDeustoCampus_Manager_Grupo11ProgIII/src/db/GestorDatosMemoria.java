package db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import domain.Aula;
import domain.Espacio;
import domain.EstadoReserva;
import domain.Laboratorio;
import domain.Reserva;
import domain.Rol;
import domain.Usuario;

public class GestorDatosMemoria implements GestorDatos {
	
	private List<Usuario> usuarios = new ArrayList<>();
	private List<Espacio> espacios = new ArrayList<>();
	private List<Reserva> reservas = new ArrayList<>();
	
		public GestorDatosMemoria() {
			Usuario ane = new Usuario("ane@opendeusto.es", "Ane Etxeberria", "1234", Rol.USUARIO);
			Usuario jon = new Usuario("jon@opendeusto.es", "Jon Ibarra", "1234", Rol.USUARIO);
			usuarios.addAll(List.of(ane, jon, new Usuario("admin@deusto.es", "Administración", "admin", Rol.ADMIN)));

			Espacio magna = new Aula("A-001", "Aula magna", 150);
			Espacio a101 = new Aula("A-101", "Aula 101", 40);
			Espacio a102 = new Aula("A-102", "Aula 102", 30);
			Espacio a201 = new Aula("A-201", "Aula 201", 60);
			Espacio redes = new Laboratorio("L-301", "Laboratorio de redes", 20);
			Espacio gpu = new Laboratorio("L-302", "Laboratorio de GPU", 16);
			Espacio electronica = new Laboratorio("L-303", "Laboratorio de electrónica", 24);
			espacios.addAll(List.of(magna, a101, a102, a201, redes, gpu, electronica));

			// fechas relativas a hoy para que siempre haya reservas pasadas y futuras
			LocalDate hoy = LocalDate.now();
			reservas.add(new Reserva(ane, a101, hoy.plusDays(1), "10:00", EstadoReserva.CONFIRMADA));
			reservas.add(new Reserva(ane, gpu, hoy.plusDays(2), "12:00", EstadoReserva.PENDIENTE));
			reservas.add(new Reserva(ane, a102, hoy.minusDays(2), "08:00", EstadoReserva.CONFIRMADA));
			reservas.add(new Reserva(ane, a201, hoy.plusDays(3), "15:00", EstadoReserva.CANCELADA));
			reservas.add(new Reserva(jon, redes, hoy.plusDays(1), "10:00", EstadoReserva.PENDIENTE));
			reservas.add(new Reserva(jon, magna, hoy.plusDays(1), "12:00", EstadoReserva.CONFIRMADA));
			reservas.add(new Reserva(jon, electronica, hoy.plusDays(4), "17:00", EstadoReserva.RECHAZADA));
			reservas.add(new Reserva(jon, gpu, hoy.plusDays(3), "08:00", EstadoReserva.PENDIENTE));
		}
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