package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import db.GestorDatos;
import domain.Rol;
import domain.Usuario;

public class VentanaLogin extends JFrame {

	private static final long serialVersionUID = 1L;

	private GestorDatos gestor;
	private JTextField txtEmail = new JTextField(20);
	private JPasswordField txtContrasena = new JPasswordField(20);

	public VentanaLogin(GestorDatos gestor) {
		this.gestor = gestor;

		JPanel panelDatos = new JPanel(new GridLayout(2, 2, 5, 5));
		panelDatos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelDatos.add(new JLabel("Email:"));
		panelDatos.add(txtEmail);
		panelDatos.add(new JLabel("Contraseña:"));
		panelDatos.add(txtContrasena);

		JButton btnEntrar = new JButton("Entrar");
		btnEntrar.addActionListener(e -> iniciarSesion());
		getRootPane().setDefaultButton(btnEntrar); 
		JPanel panelBoton = new JPanel();
		panelBoton.add(btnEntrar);

		add(panelDatos, BorderLayout.CENTER);
		add(panelBoton, BorderLayout.SOUTH);
		setTitle("DeustoCampus Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private void iniciarSesion() {
		String email = txtEmail.getText().trim();
		String contrasena = new String(txtContrasena.getPassword());
		Usuario usuario = gestor.login(email, contrasena);

		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Email o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
			txtContrasena.setText("");
		} else {
			if (usuario.getRol() == Rol.ADMIN) {
				new VentanaAdmin(gestor).setVisible(true);
			} else {
				new VentanaBuscarReserva(gestor, usuario).setVisible(true);
			}
			dispose();
		}
	}
}
