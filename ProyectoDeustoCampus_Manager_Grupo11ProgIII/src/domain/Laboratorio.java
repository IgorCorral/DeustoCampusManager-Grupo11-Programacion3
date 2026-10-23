package domain;

public class Laboratorio extends Espacio {
	
	public Laboratorio(String codigo, String nombre, int aforo) {
		super(codigo, nombre, aforo);
	}

	@Override
	public boolean requiereAprobacion() {
		return true;
	}

}
