package domain;

public abstract class Espacio{

	private String codigo;
	private String nombre;
	private int aforo;

	public Espacio(String codigo, String nombre, int aforo) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.aforo = aforo;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public int getAforo() {
		return aforo;
	}

	public abstract boolean requiereAprobacion();

	@Override
	public String toString() {
		return codigo + " " + nombre;
	}

}
