package db;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import domain.Espacio;
import domain.EstadoReserva;
import domain.Reserva;
import domain.ReservaException;
import domain.Rol;
import domain.Usuario;

// Capa de datos con SQLite, con la misma estructura que el GestorBD de las prácticas de JDBC.
// Cada método abre su conexión con try-with-resources (se cierra sola aunque haya un error)
// y usa PreparedStatement para pasar los datos a la sentencia SQL sin peligro de inyección SQL.
// Si algo falla se guarda un aviso en el log y se lanza una BDException para que la ventana avise.
public class GestorBD implements GestorDatos {

	private final String PROPERTIES_FILE = "conf/app.properties";
	private final String EMAIL_ADMIN = "admin@deusto.es";

	private Properties properties;
	private String connectionString;

	private static Logger logger = Logger.getLogger(GestorBD.class.getName());

	public GestorBD() throws BDException {
		try (FileReader fr = new FileReader(PROPERTIES_FILE)) {
			//Lectura del fichero properties
			properties = new Properties();
			properties.load(fr);
			connectionString = properties.getProperty("connection");

			//Cargar el driver de SQLite
			Class.forName(properties.getProperty("driver"));
		} catch (Exception ex) {
			logger.warning(String.format("Error al cargar la configuración: %s", ex.getMessage()));
			throw new BDException("No se ha podido cargar la configuración de la base de datos", ex);
		}
	}

	// Crea las tablas si no existen (al conectarse la primera vez se crea el fichero .db)
	// y da de alta al administrador si todavía no está
	public void crearBBDD() throws BDException {
		String sql1 = "CREATE TABLE IF NOT EXISTS Usuario (\n"
				+ " email TEXT PRIMARY KEY,\n"
				+ " nombre TEXT NOT NULL,\n"
				+ " contrasena TEXT NOT NULL,\n"
				+ " rol TEXT NOT NULL\n"
				+ ");";

		String sql2 = "CREATE TABLE IF NOT EXISTS Espacio (\n"
				+ " codigo TEXT PRIMARY KEY,\n"
				+ " nombre TEXT NOT NULL,\n"
				+ " aforo INTEGER NOT NULL,\n"
				+ " tipo TEXT NOT NULL\n"
				+ ");";

		String sql3 = "CREATE TABLE IF NOT EXISTS Reserva (\n"
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
				+ " email_usuario TEXT NOT NULL,\n"
				+ " codigo_espacio TEXT NOT NULL,\n"
				+ " fecha TEXT NOT NULL,\n"
				+ " hora TEXT NOT NULL,\n"
				+ " estado TEXT NOT NULL,\n"
				+ " FOREIGN KEY(email_usuario) REFERENCES Usuario(email),\n"
				+ " FOREIGN KEY(codigo_espacio) REFERENCES Espacio(codigo)\n"
				+ ");";

		//Se abre la conexión y se crea un PreparedStatement para crear cada tabla
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt1 = con.prepareStatement(sql1);
				PreparedStatement pStmt2 = con.prepareStatement(sql2);
				PreparedStatement pStmt3 = con.prepareStatement(sql3)) {
			if (!pStmt1.execute() && !pStmt2.execute() && !pStmt3.execute()) {
				logger.info("Se han creado las tablas");
			}
		} catch (SQLException ex) {
			logger.warning(String.format("Error al crear las tablas: %s", ex.getMessage()));
			throw new BDException("No se han podido crear las tablas de la base de datos", ex);
		}

		//El administrador tiene que existir siempre para poder aprobar las solicitudes
		if (getUsuario(EMAIL_ADMIN) == null) {
			insertarUsuario(EMAIL_ADMIN, "Administración", "admin", Rol.ADMIN);
		}
	}

	// Devuelve el usuario con ese email o null si no existe
	public Usuario getUsuario(String email) throws BDException {
		Usuario usuario = null;
		String sql = "SELECT * FROM Usuario WHERE email = ? LIMIT 1";
		//Se abre la conexión y se crea el PreparedStatement con la sentencia SQL
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			//El ? de la sentencia se sustituye por el email
			pStmt.setString(1, email);
			ResultSet rs = pStmt.executeQuery();
			if (rs.next()) {
				usuario = new Usuario(rs.getString("email"), rs.getString("nombre"), rs.getString("contrasena"),
						Rol.valueOf(rs.getString("rol")));
			}
			rs.close();
		} catch (SQLException ex) {
			logger.warning(String.format("Error al buscar el usuario %s: %s", email, ex.getMessage()));
			throw new BDException("No se ha podido consultar el usuario", ex);
		}
		return usuario;
	}

	// Devuelve el usuario si el email y la contraseña son correctos, si no devuelve null
	@Override
	public Usuario login(String email, String contrasena) throws BDException {
		Usuario usuario = getUsuario(email);
		if (usuario != null && usuario.comprobarContrasena(contrasena)) {
			return usuario;
		}
		return null;
	}

	// Inserta un usuario nuevo en la tabla Usuario
	private void insertarUsuario(String email, String nombre, String contrasena, Rol rol) throws BDException {
		String sql = "INSERT INTO Usuario (email, nombre, contrasena, rol) VALUES (?, ?, ?, ?)";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, email);
			pStmt.setString(2, nombre);
			pStmt.setString(3, contrasena);
			pStmt.setString(4, rol.toString());
			if (pStmt.executeUpdate() != 1) {
				logger.warning(String.format("No se ha insertado el usuario %s", email));
			} else {
				logger.info(String.format("Se ha insertado el usuario %s", email));
			}
		} catch (SQLException ex) {
			logger.warning(String.format("Error al insertar el usuario %s: %s", email, ex.getMessage()));
			throw new BDException("No se ha podido guardar el usuario", ex);
		}
	}

	// Devuelve todos los espacios ordenados por código
	public List<Espacio> getEspacios() throws BDException {
		List<Espacio> espacios = new ArrayList<>();
		String sql = "SELECT * FROM Espacio ORDER BY codigo";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			ResultSet rs = pStmt.executeQuery();
			//Se recorre el ResultSet y se crea un objeto por cada fila
			while (rs.next()) {
				espacios.add(Espacio.crear(rs.getString("tipo"), rs.getString("codigo"), rs.getString("nombre"),
						rs.getInt("aforo")));
			}
			rs.close();
		} catch (SQLException ex) {
			logger.warning(String.format("Error al recuperar los espacios: %s", ex.getMessage()));
			throw new BDException("No se han podido consultar los espacios", ex);
		}
		return espacios;
	}

	// Devuelve el espacio con ese código o null si no existe
	public Espacio getEspacio(String codigo) throws BDException {
		Espacio espacio = null;
		String sql = "SELECT * FROM Espacio WHERE codigo = ? LIMIT 1";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, codigo);
			ResultSet rs = pStmt.executeQuery();
			if (rs.next()) {
				espacio = Espacio.crear(rs.getString("tipo"), rs.getString("codigo"), rs.getString("nombre"),
						rs.getInt("aforo"));
			}
			rs.close();
		} catch (SQLException ex) {
			logger.warning(String.format("Error al buscar el espacio %s: %s", codigo, ex.getMessage()));
			throw new BDException("No se ha podido consultar el espacio", ex);
		}
		return espacio;
	}

	// Devuelve los espacios que no tienen ninguna reserva activa (pendiente o confirmada) en esa fecha y hora.
	// Las fechas se guardan como texto con el formato de LocalDate.toString(), por ejemplo 2026-11-30
	@Override
	public List<Espacio> buscarEspaciosLibres(LocalDate fecha, String hora) throws BDException {
		//Primero se buscan los códigos de los espacios ocupados a esa hora
		List<String> ocupados = new ArrayList<>();
		String sql = "SELECT codigo_espacio FROM Reserva WHERE fecha = ? AND hora = ? AND (estado = ? OR estado = ?)";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, fecha.toString());
			pStmt.setString(2, hora);
			pStmt.setString(3, EstadoReserva.PENDIENTE.toString());
			pStmt.setString(4, EstadoReserva.CONFIRMADA.toString());
			ResultSet rs = pStmt.executeQuery();
			while (rs.next()) {
				ocupados.add(rs.getString("codigo_espacio"));
			}
			rs.close();
		} catch (SQLException ex) {
			logger.warning(String.format("Error al buscar los espacios libres: %s", ex.getMessage()));
			throw new BDException("No se han podido buscar los espacios libres", ex);
		}

		//Después se quedan todos los espacios que no están en esa lista
		List<Espacio> libres = new ArrayList<>();
		for (Espacio e : getEspacios()) {
			if (!ocupados.contains(e.getCodigo())) {
				libres.add(e);
			}
		}
		return libres;
	}

	// Crea la reserva si el espacio sigue libre (si no, lanza ReservaException) y la guarda
	@Override
	public Reserva crearReserva(Usuario usuario, Espacio espacio, LocalDate fecha, String hora)
			throws ReservaException, BDException {
		//contains usa el equals de Espacio, que compara los códigos
		if (!buscarEspaciosLibres(fecha, hora).contains(espacio)) {
			throw new ReservaException("El espacio " + espacio.getCodigo() + " ya está reservado a esa hora.");
		}
		// polimorfismo: cada tipo de espacio decide si la reserva necesita aprobación
		EstadoReserva estado = espacio.requiereAprobacion() ? EstadoReserva.PENDIENTE : EstadoReserva.CONFIRMADA;
		Reserva reserva = new Reserva(usuario, espacio, fecha, hora, estado);
		insertarReserva(reserva);
		return reserva;
	}

	// Inserta una reserva en la tabla Reserva. El id lo pone SQLite solo (AUTOINCREMENT)
	private void insertarReserva(Reserva reserva) throws BDException {
		String sql = "INSERT INTO Reserva (email_usuario, codigo_espacio, fecha, hora, estado) VALUES (?, ?, ?, ?, ?)";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, reserva.getUsuario().getEmail());
			pStmt.setString(2, reserva.getEspacio().getCodigo());
			pStmt.setString(3, reserva.getFecha().toString());
			pStmt.setString(4, reserva.getHora());
			pStmt.setString(5, reserva.getEstado().toString());
			if (pStmt.executeUpdate() != 1) {
				logger.warning(String.format("No se ha insertado la reserva de %s", reserva.getEspacio()));
			} else {
				logger.info(String.format("Se ha insertado la reserva de %s", reserva.getEspacio()));
			}
		} catch (SQLException ex) {
			logger.warning(String.format("Error al insertar la reserva: %s", ex.getMessage()));
			throw new BDException("No se ha podido guardar la reserva", ex);
		}
	}

	// Reservas de un usuario, ordenadas por fecha y hora
	@Override
	public List<Reserva> getReservas(Usuario usuario) throws BDException {
		return leerReservas("SELECT * FROM Reserva WHERE email_usuario = ? ORDER BY fecha, hora", usuario.getEmail());
	}

	// Solicitudes que tiene que aprobar o rechazar el administrador
	@Override
	public List<Reserva> getReservasPendientes() throws BDException {
		return leerReservas("SELECT * FROM Reserva WHERE estado = ? ORDER BY fecha, hora",
				EstadoReserva.PENDIENTE.toString());
	}

	// Ejecuta una consulta de reservas que tiene un solo parámetro y crea un objeto Reserva por fila.
	// Igual que en las prácticas no se usa JOIN: el usuario y el espacio se buscan con getUsuario y getEspacio
	private List<Reserva> leerReservas(String sql, String parametro) throws BDException {
		List<Reserva> reservas = new ArrayList<>();
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, parametro);
			ResultSet rs = pStmt.executeQuery();
			while (rs.next()) {
				Usuario usuario = getUsuario(rs.getString("email_usuario"));
				Espacio espacio = getEspacio(rs.getString("codigo_espacio"));
				Reserva reserva = new Reserva(usuario, espacio, LocalDate.parse(rs.getString("fecha")),
						rs.getString("hora"), EstadoReserva.valueOf(rs.getString("estado")));
				reserva.setId(rs.getInt("id"));
				reservas.add(reserva);
			}
			rs.close();
		} catch (SQLException ex) {
			logger.warning(String.format("Error al recuperar las reservas: %s", ex.getMessage()));
			throw new BDException("No se han podido consultar las reservas", ex);
		}
		return reservas;
	}

	// Cancela, aprueba o rechaza una reserva que sigue activa y que no es de un día que ya ha pasado
	@Override
	public void cambiarEstado(Reserva reserva, EstadoReserva nuevoEstado) throws ReservaException, BDException {
		if (!reserva.estaActiva()) {
			throw new ReservaException("Esta reserva ya está cancelada o rechazada.");
		}
		if (reserva.getFecha().isBefore(LocalDate.now())) {
			throw new ReservaException("No se puede cambiar una reserva de un día que ya ha pasado.");
		}
		//En el WHERE también se mira el estado por si otra persona la ha cambiado mientras tanto
		String sql = "UPDATE Reserva SET estado = ? WHERE id = ? AND (estado = ? OR estado = ?)";
		try (Connection con = DriverManager.getConnection(connectionString);
				PreparedStatement pStmt = con.prepareStatement(sql)) {
			pStmt.setString(1, nuevoEstado.toString());
			pStmt.setInt(2, reserva.getId());
			pStmt.setString(3, EstadoReserva.PENDIENTE.toString());
			pStmt.setString(4, EstadoReserva.CONFIRMADA.toString());
			if (pStmt.executeUpdate() != 1) {
				throw new ReservaException("La reserva ya no está activa. Vuelve a cargar la lista.");
			}
			logger.info(String.format("La reserva %d ahora está %s", reserva.getId(), nuevoEstado));
		} catch (SQLException ex) {
			logger.warning(String.format("Error al cambiar el estado de la reserva: %s", ex.getMessage()));
			throw new BDException("No se ha podido cambiar el estado de la reserva", ex);
		}
		//El objeto solo se cambia si se ha cambiado en la base de datos
		reserva.setEstado(nuevoEstado);
	}
}
