package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import db.GestorDatos;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;
import domain.Usuario;

public class VentanaMisReservas extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private GestorDatos gestor;
	private ReservaTableModel modelo;
	private JTable tabla;

	public VentanaMisReservas(JFrame padre, GestorDatos gestor, Usuario usuario) {
		super(padre, "Mis reservas", true); // modal: hasta cerrarlo no se puede usar la ventana de atrás
		this.gestor = gestor;
		modelo = new ReservaTableModel(gestor.getReservas(usuario));
		tabla = new JTable(modelo);

		// renderer con una expresión lambda: el texto del estado se pinta de un color según su valor
		tabla.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
			JLabel etiqueta = new JLabel(value.toString());
			etiqueta.setOpaque(true);
			etiqueta.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			if (value instanceof EstadoReserva) {
				etiqueta.setForeground(getColor((EstadoReserva) value));
			}
			return etiqueta;
		});

		JButton btnCancelar = new JButton("Cancelar reserva");
		btnCancelar.setEnabled(false);
		btnCancelar.addActionListener(e -> cancelar());
		tabla.getSelectionModel().addListSelectionListener(e -> btnCancelar.setEnabled(tabla.getSelectedRow() != -1));
		JPanel panelBotones = new JPanel();
		panelBotones.add(btnCancelar);

		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(panelBotones, BorderLayout.SOUTH);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(600, 300);
		setLocationRelativeTo(padre);
	}

	private Color getColor(EstadoReserva estado) {
		switch (estado) {
		case CONFIRMADA: return new Color(0, 128, 0);
		case PENDIENTE: return new Color(200, 120, 0);
		case RECHAZADA: return Color.RED;
		default: return Color.GRAY;
		}
	}

	// pide confirmación y cancela la reserva seleccionada
	private void cancelar() {
		Reserva reserva = modelo.getReserva(tabla.getSelectedRow());
		int respuesta = JOptionPane.showConfirmDialog(this, "¿Cancelar la reserva de " + reserva.getEspacio() + "?",
				"Cancelar reserva", JOptionPane.YES_NO_OPTION);
		if (respuesta == JOptionPane.YES_OPTION) {
			try {
				gestor.cambiarEstado(reserva, EstadoReserva.CANCELADA);
				modelo.fireTableDataChanged(); // la reserva ya está cancelada: solo hay que repintar la tabla
			} catch (ReservaException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede cancelar", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
}
