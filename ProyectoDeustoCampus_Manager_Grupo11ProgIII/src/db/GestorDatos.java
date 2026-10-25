package db;

import java.time.LocalDate;
import java.util.List;

import domain.Espacio;
import domain.Usuario;

public interface GestorDatos {
	
	//Devuelve el usuario si el email y la contraseña son correctos, sino devuelve NULL
	Usuario login(String email, String contraseña);
	
	List<Espacio> buscarEspaciosLibres(LocalDate fecha, String hora);
	
	
	
}
