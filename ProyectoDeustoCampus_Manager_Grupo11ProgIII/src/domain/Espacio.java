package domain;
import java.util.Objects;
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
	// "Aula" o "Laboratorio": es lo que se guarda en la columna tipo de la base de datos    public abstract String getTipo();    
	// Crea un Aula o un Laboratorio según el tipo. Se usa al leer de la base de datos y de los CSV
	public static Espacio crear(String tipo, String codigo, String nombre, int aforo) {        
		if (tipo.equals("Laboratorio")) {
			return new Laboratorio(codigo, nombre, aforo); 
		}        
		return new Aula(codigo, nombre, aforo);    
		}    
	// Dos espacios son iguales si tienen el mismo código. Hace falta porque cada consulta a la base de datos    
	// crea objetos nuevos. Generado con Eclipse: Source > Generate hashCode() and equals()    
	@Override    
	public int hashCode() {        
		return Objects.hash(codigo);    
		}    
	
	@Override    public boolean equals(Object obj) {
		if (this == obj)            
			return true;        
		if (obj == null)            
			return false;        
		if (getClass() != obj.getClass())            
			return false;        
		Espacio other = (Espacio) obj;        
		return Objects.equals(codigo, other.codigo);    
		}
	
	@Override
	public String toString() {
		return codigo + " " + nombre;
	}

}
