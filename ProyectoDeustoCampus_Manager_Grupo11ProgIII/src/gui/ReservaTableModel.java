package gui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import domain.Reserva;

public class ReservaTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private String[] cabecera = { "Espacio", "Fecha", "Hora", "Usuario", "Estado" };
	private List<Reserva> reservas;

	public ReservaTableModel(List<Reserva> reservas) {
		this.reservas = reservas;
	}

	// cambia las reservas que se muestran y avisa a la tabla para que se repinte
	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
		fireTableDataChanged();
	}

	public Reserva getReserva(int fila) {
		return reservas.get(fila);
	}

	@Override
	public String getColumnName(int columna) {
		return cabecera[columna];
	}

	@Override
	public int getRowCount() {
		return reservas.size();
	}

	@Override
	public int getColumnCount() {
		return cabecera.length;
	}

	@Override
	public Object getValueAt(int fila, int columna) {
		Reserva reserva = reservas.get(fila);
		switch (columna) {
		case 0: return reserva.getEspacio().toString();
		case 1: return reserva.getFecha().format(FORMATO_FECHA);
		case 2: return reserva.getHora();
		case 3: return reserva.getUsuario().getNombre();
		case 4: return reserva.getEstado();
		default: return null;
		}
	}
}