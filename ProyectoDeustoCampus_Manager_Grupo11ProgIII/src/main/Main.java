package main;

import javax.swing.SwingUtilities;

import db.GestorDatos;
import db.GestorDatosMemoria;
import gui.VentanaLogin;

public class Main {

	public static void main(String[] args) {
		GestorDatos gestor = new GestorDatosMemoria();
		SwingUtilities.invokeLater(() -> new VentanaLogin(gestor).setVisible(true));
	}
}
