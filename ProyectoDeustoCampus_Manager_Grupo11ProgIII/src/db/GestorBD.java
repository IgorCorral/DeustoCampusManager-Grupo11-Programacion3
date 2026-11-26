package db;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import domain.Rol;
import domain.Usuario;

// Capa de datos con SQLite, con la misma estructura que el GestorBD de las prácticas de JDBC.
// Cada método abre su conexión con try-with-resources (se cierra sola aunque haya un error)
// y usa PreparedStatement para pasar los datos a la sentencia SQL sin peligro de inyección SQL.
// Si algo falla se guarda un aviso en el log y se lanza una BDException para que la ventana avise.
public class GestorBD {

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
}
