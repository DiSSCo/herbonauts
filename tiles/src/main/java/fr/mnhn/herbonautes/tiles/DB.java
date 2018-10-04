package fr.mnhn.herbonautes.tiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.mnhn.herbonautes.tiles.exceptions.DBException;

public class DB {
	
	private Connection connection = null;
	
	public DB() {
		init();
	}
	
	private void init() {
		try {
			Class.forName(Conf.DB_DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			throw new DBException("Driver introuvable", e);
		}
	}
	 
	private Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) { 
				connection = DriverManager.getConnection(Conf.DB_URL, Conf.DB_USER, Conf.DB_PASSWORD);
			}
		} catch (SQLException e) {
			throw new DBException("Connexion impossible", e);
		}
		return connection;
	}
	
	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new DBException("Fermeture de la connexion", e);
			}
		}
	}
	
	public ResultSet select(String query) {
		Connection conn = getConnection();
		try {
			Statement select = conn.createStatement();
			select.setFetchSize(1);
			ResultSet rs = select.executeQuery(query);
			return rs.next() ? rs : null;
		} catch (SQLException e) {
			closeConnection();
			throw new DBException("Connexion impossible", e);
		} 
	}

	public ResultSet selectMultiple(String query, Long ... params) {
		Connection conn = getConnection();
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			if (params != null) {
				int i = 0;
				for (Long param : params) {
					statement.setInt(++i, param.intValue());
				}
			}
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch (SQLException e) {
			closeConnection();
			throw new DBException("Connexion impossible", e);
		}
	}
	
	public void update(String query, Long ... params) {
		Connection conn = getConnection();
		try {
			PreparedStatement preparedUpdate = conn.prepareStatement(query);
			if (params != null) {
				int i = 0;
				for (Long param : params) {
					preparedUpdate.setInt(++i, param.intValue());
				}
			}
			preparedUpdate.executeUpdate();
		} catch (SQLException e) {
			closeConnection();
			throw new DBException("Connexion impossible", e);
		} finally {
			closeConnection();
		} 
	}
}
