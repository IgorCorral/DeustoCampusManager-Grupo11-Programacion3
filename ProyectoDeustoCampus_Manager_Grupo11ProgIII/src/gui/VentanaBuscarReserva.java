package gui;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import db.GestorDatos;
import domain.Espacio;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;
import domain.Usuario;


public class VentanaBuscarReserva extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static final String[] HORAS = { "08:00", "10:00", "12:00", "15:00", "17:00", "19:00" };

	private GestorDatos gestor;
	private Usuario usuario;
	private List<LocalDate> fechas = new ArrayList<>();
	private List<Espacio> espacios = new ArrayList<>(); // los espacios que se ven en la tabla
	private JComboBox<String> comboFecha = new JComboBox<>();
	private JComboBox<String> comboHora = new JComboBox<>(HORAS);
	private DefaultTableModel modelo = new DefaultTableModel(new String[] { "Código", "Nombre", "Aforo", "Necesita aprobación" }, 0);
	
	private JTable tabla = new JTable(modelo) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int fila, int columna) {
			return false;
		}
	};

	public VentanaBuscarReserva(GestorDatos gestor, Usuario usuario) {
		this.gestor = gestor;
		this.usuario = usuario;

		// se reserva con al menos un día de antelación y hasta una semana después
		for (int i = 1; i <= 7; i++) {
			LocalDate fecha = LocalDate.now().plusDays(i);
			fechas.add(fecha);
			comboFecha.addItem(fecha.format(ReservaTableModel.FORMATO_FECHA));
		}
		
		comboFecha.addActionListener(e -> buscar()); // al cambiar la fecha o la hora se busca otra vez
		comboHora.addActionListener(e -> buscar());
		
		JPanel panelFiltros = new JPanel();
		panelFiltros.add(new JLabel("Fecha:"));
		panelFiltros.add(comboFecha);
		panelFiltros.add(new JLabel("Hora:"));
		panelFiltros.add(comboHora);
		
		JButton btnReservar = new JButton("Reservar");
		btnReservar.setEnabled(false); // desactivado hasta que se seleccione una fila
		btnReservar.addActionListener(e -> reservar());
		// EVENTO DE LA TABLA: cada vez que cambia la selección, el botón se activa si hay una fila
		tabla.getSelectionModel().addListSelectionListener(e -> btnReservar.setEnabled(tabla.getSelectedRow() != -1));
		
		JButton btnMisReservas = new JButton("Mis reservas");
		btnMisReservas.addActionListener(e -> { // lambda de varias líneas: va entre llaves
	    	new VentanaMisReservas(this, gestor, usuario).setVisible(true);
	    	buscar(); // al cerrarlo se actualiza la tabla por si se ha cancelado algo
	    });
	    JPanel panelBotones = new JPanel();
		panelBotones.add(btnReservar);
		panelBotones.add(btnMisReservas);
		
		add(panelFiltros, BorderLayout.NORTH);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(panelBotones, BorderLayout.SOUTH);
		setTitle("Buscar y reservar - " + usuario.getNombre());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(650, 350);
		setLocationRelativeTo(null);
		buscar();
	}

	// carga en la tabla los espacios libres en la fecha y hora elegidas
	private void buscar() {
		LocalDate fecha = fechas.get(comboFecha.getSelectedIndex());
		espacios = gestor.buscarEspaciosLibres(fecha, (String) comboHora.getSelectedItem());
		modelo.setRowCount(0);
		for (Espacio e : espacios) {
			String aprobacion = e.requiereAprobacion() ? "Sí" : "No";
			modelo.addRow(new Object[] { e.getCodigo(), e.getNombre(), e.getAforo(), aprobacion });
		}
	}
	
	private void reservar() {
		Espacio espacio = espacios.get(tabla.getSelectedRow());
		LocalDate fecha = fechas.get(comboFecha.getSelectedIndex());
		try {
			Reserva reserva = gestor.crearReserva(usuario, espacio, fecha, (String) comboHora.getSelectedItem());
			if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
				JOptionPane.showMessageDialog(this, "Reserva confirmada: " + espacio);
			}else {
				JOptionPane.showMessageDialog(this, "Solicitud enviada. Un administrador tiene que aprobarla.");
			}
			buscar();
		}catch (ReservaException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede reservar", JOptionPane.ERROR_MESSAGE);
		}
	}

}
