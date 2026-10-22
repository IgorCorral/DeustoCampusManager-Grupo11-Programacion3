package domain;

import java.time.LocalDate;

public class Reserva {
	private Usuario usuario;
	private Espacio espacio;
	private LocalDate fecha;
	private String hora; 
	private EstadoReserva estado;

	public Reserva(Usuario usuario, Espacio espacio, LocalDate fecha, String hora, EstadoReserva estado) {
		this.usuario = usuario;
		this.espacio = espacio;
		this.fecha = fecha;
		this.hora = hora;
		this.estado = estado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Espacio getEspacio() {
		return espacio;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public String getHora() {
		return hora;
	}

	public EstadoReserva getEstado() {
		return estado;
	}

	public void setEstado(EstadoReserva estado) {
		this.estado = estado;
	}

	public boolean estaActiva() {
		return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA;
	}

	public boolean ocupa(LocalDate fecha, String hora) {
		return estaActiva() && this.fecha.equals(fecha) && this.hora.equals(hora);
	}
}
