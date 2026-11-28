package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import db.BDException;
import db.GestorDatos;

// Ventana para crear una cuenta de usuario nueva
public class DialogoRegistro extends JDialog {

	private static final long serialVersionUID = 1L;

	private GestorDatos gestor;
	private JTextField txtEmail = new JTextField(20);
	private JTextField txtNombre = new JTextField(20);
	private JPasswordField txtContrasena = new JPasswordField(20);

	public DialogoRegistro(JFrame padre, GestorDatos gestor) {
		super(padre, "Crear cuenta", true); // modal: hasta cerrarlo no se puede usar el login
		this.gestor = gestor;

		JPanel panelDatos = new JPanel(new GridLayout(3, 2, 5, 5));
		panelDatos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelDatos.add(new JLabel("Email:"));
		panelDatos.add(txtEmail);
		panelDatos.add(new JLabel("Nombre:"));
		panelDatos.add(txtNombre);
		panelDatos.add(new JLabel("Contraseña:"));
		panelDatos.add(txtContrasena);

		JButton btnCrear = new JButton("Crear cuenta");
		btnCrear.addActionListener(e -> crearCuenta());
		getRootPane().setDefaultButton(btnCrear);
		JPanel panelBoton = new JPanel();
		panelBoton.add(btnCrear);

		add(panelDatos, BorderLayout.CENTER);
		add(panelBoton, BorderLayout.SOUTH);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(padre);
	}

	// guarda el usuario nuevo en la base de datos si no hay otro con el mismo email
	private void crearCuenta() {
		String email = txtEmail.getText().trim();
		String nombre = txtNombre.getText().trim();
		String contrasena = new String(txtContrasena.getPassword());
		if (email.isEmpty() || nombre.isEmpty() || contrasena.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Rellena todos los campos.");
			return;
		}
		try {
			if (gestor.registrarUsuario(email, nombre, contrasena)) {
				JOptionPane.showMessageDialog(this, "Cuenta creada. Ya puedes entrar con tu email y tu contraseña.");
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Ya hay una cuenta con ese email.");
			}
		} catch (BDException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Base de datos", JOptionPane.ERROR_MESSAGE);
		}
	}
}
