package main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import db.BDException;
import db.GestorBD;
import gui.VentanaLogin;

public class Main {

	public static void main(String[] args) {
		try {
			GestorBD gestorBD = new GestorBD();
			//Se crean las tablas si no existen
			gestorBD.crearBBDD();
			SwingUtilities.invokeLater(() -> new VentanaLogin(gestorBD).setVisible(true));
		} catch (BDException ex) {
			//Sin base de datos no se puede usar la aplicación: se avisa y se termina
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Base de datos", JOptionPane.ERROR_MESSAGE);
		}
	}
}
