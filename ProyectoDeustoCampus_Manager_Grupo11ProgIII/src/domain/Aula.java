package domain;

public class Aula extends Espacio {
	
	public Aula(String codigo, String nombre, int aforo) {
		super(codigo, nombre, aforo);
	}

	@Override
	public boolean requiereAprobacion() {
		return false;
	}

}
