package it.polito.tdp.meteo.DAO;

	import java.sql.Connection;
	import java.sql.SQLException;
	import com.zaxxer.hikari.HikariDataSource;

	/************ CONNECTION POOLING *************/

	public class ConnectDB {
		
		// check user e password
		static private final String jdbcUrl = "jdbc:mysql://localhost/meteo?user=root&password=240899SQL";

		//variabile statica per permettermi di chiamare Hikari solo alla prima connection
		static private final HikariDataSource ds = null;
		
		public static Connection getConnection() {
			
			// per DIMINUIRE TEMPO DI CONNESSIONE
			// creare la data source solo la prima volta che viene chiamata la connection
			// grazie a variabile statica
			if(ds==null) {
				HikariDataSource ds = new HikariDataSource();
				
				ds.setJdbcUrl(jdbcUrl);
				
			}

			try {
					Connection connection = ds.getConnection();
					return connection;

			} catch (SQLException e) {

				e.printStackTrace();
				throw new RuntimeException("Cannot get a connection " + jdbcUrl, e);
			}
		}
	}

