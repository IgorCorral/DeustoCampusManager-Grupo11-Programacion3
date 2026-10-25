package gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import db.GestorDatos;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;


public class VentanaAdmin extends JFrame {

	private static final long serialVersionUID = 1L;

	private GestorDatos gestor;
	private ReservaTableModel modelo;
	private JTable tabla;

	public VentanaAdmin(GestorDatos gestor) {
		this.gestor = gestor;
		modelo = new ReservaTableModel(gestor.getReservasPendientes());
		tabla = new JTable(modelo);

		JButton btnAprobar = new JButton("Aprobar");
		btnAprobar.addActionListener(e -> cambiarEstado(EstadoReserva.CONFIRMADA));
		JButton btnRechazar = new JButton("Rechazar");
		btnRechazar.addActionListener(e -> cambiarEstado(EstadoReserva.RECHAZADA));
		JPanel panelBotones = new JPanel();
		panelBotones.add(btnAprobar);
		panelBotones.add(btnRechazar);

		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(panelBotones, BorderLayout.SOUTH);
		setTitle("Administración - Solicitudes de reserva pendientes");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(650, 300);
		setLocationRelativeTo(null);
	}

	// aprueba o rechaza la solicitud seleccionada y recarga la lista de pendientes
	private void cambiarEstado(EstadoReserva nuevoEstado) {
		int fila = tabla.getSelectedRow();
		if (fila == -1) {
			JOptionPane.showMessageDialog(this, "Selecciona una solicitud de la tabla.");
			return;
		}
		Reserva reserva = modelo.getReserva(fila);
		try {
			gestor.cambiarEstado(reserva, nuevoEstado);
			modelo.setReservas(gestor.getReservasPendientes());
		} catch (ReservaException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
