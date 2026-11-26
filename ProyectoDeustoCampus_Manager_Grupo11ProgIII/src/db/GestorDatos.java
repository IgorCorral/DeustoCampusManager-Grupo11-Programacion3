package db;

import java.time.LocalDate;
import java.util.List;

import domain.Espacio;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;
import domain.Usuario;

// Todos los métodos lanzan BDException si falla la base de datos
public interface GestorDatos {
	
	//Devuelve el usuario si el email y la contraseña son correctos, sino devuelve NULL
	Usuario login(String email, String contraseña) throws BDException;
	
	List<Espacio> buscarEspaciosLibres(LocalDate fecha, String hora) throws BDException;

	// la reserva se crea PENDIENTE o CONFIRMADA según el tipo de espacio
	Reserva crearReserva(Usuario usuario, Espacio espacio, LocalDate fecha, String hora)
			throws ReservaException, BDException;

	List<Reserva> getReservas(Usuario usuario) throws BDException;

	List<Reserva> getReservasPendientes() throws BDException;

	// sirve para cancelar, aprobar o rechazar una reserva que sigue activa
	void cambiarEstado(Reserva reserva, EstadoReserva nuevoEstado) throws ReservaException, BDException;
}
