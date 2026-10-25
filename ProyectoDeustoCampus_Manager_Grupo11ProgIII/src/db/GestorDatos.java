package db;

import java.time.LocalDate;
import java.util.List;

import domain.Espacio;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;
import domain.Usuario;

public interface GestorDatos {
	
	//Devuelve el usuario si el email y la contraseña son correctos, sino devuelve NULL
	Usuario login(String email, String contraseña);
	
	List<Espacio> buscarEspaciosLibres(LocalDate fecha, String hora);

	// la reserva se crea PENDIENTE o CONFIRMADA según el tipo de espacio
	Reserva crearReserva(Usuario usuario, Espacio espacio, LocalDate fecha, String hora) throws ReservaException;

	List<Reserva> getReservas(Usuario usuario);

	List<Reserva> getReservasPendientes();

	// sirve para cancelar, aprobar o rechazar una reserva que sigue activa
	void cambiarEstado(Reserva reserva, EstadoReserva nuevoEstado) throws ReservaException;
}
