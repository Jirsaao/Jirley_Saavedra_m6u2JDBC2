package dao;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Card;
import model.Player;

public class DaoImpl implements Dao {
	private Connection conexion;

	public void connect() throws SQLException {
		// TODO Auto-generated method stub
		final String host = "localhost";
		final int puerto = 3306;
		final String schema = "UF2_P1_UNO";
		final String usuario = "root";
		final String contraseña = "";

		String url = "jdbc:mysql://" + host + ":" + puerto + "/" + schema;
		try {
			conexion = DriverManager.getConnection(url, usuario, contraseña);
			System.out.println("Conexion exitosa.");
		} catch (SQLException e) {
			System.err.println("Error" + e.getMessage());
			throw e;
		}

	}

	public Player getPlayer(String user, String pass) throws SQLException {
		// TODO Auto-generated method stub
		// Prepared Statement
		String queryPlayer = "SELECT * FROM PLAYER WHERE USER= ? AND PASSWORD = ?;";
		PreparedStatement preparedStQueryPlayer = conexion.prepareStatement(queryPlayer);
		preparedStQueryPlayer.setString(1, user);
		preparedStQueryPlayer.setString(2, pass);
		ResultSet rs = preparedStQueryPlayer.executeQuery();
		// instancie player con el 'name'
		if (rs.next()) {

			Player player = new Player(rs.getString("USER"));

			player.setId(rs.getInt("Id"));
			player.setGames(rs.getInt("GAMES"));
			player.setVictories(rs.getInt("VICTORIES"));

			return player;
		} else {
			return null;
		}
	}

	public ArrayList<Card> getCards(int id_player) {
		ArrayList<Card> cardList = new ArrayList<>();

		String queryCard = "SELECT * FROM CARD LEFT JOIN GAME ON CARD.ID = GAME.ID WHERE id_Player = ? AND GAME.id is null";

		try (PreparedStatement preparedStQueryPlayer = conexion.prepareStatement(queryCard)) {
			preparedStQueryPlayer.setInt(1, id_player);
			ResultSet rs = preparedStQueryPlayer.executeQuery();

			while (rs.next()) {
				int idCard = rs.getInt("id");
				String numberCard = rs.getString("number");
				String colorCard = rs.getString("color");

				Card card = new Card(idCard, numberCard, colorCard, id_player);

				cardList.add(card);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return cardList;
	}

	public Card getLastCard() {
		String queryLastCard = "SELECT id_card FROM GAME WHERE id = (SELECT MAX(Id) FROM GAME)";

		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryLastCard)) {
			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				int idCard = rs.getInt("id_card");
				return getCard(idCard);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public int getLastIdCard(int id_Player) throws SQLException {
		String queryLastIdCard = "SELECT IFNULL(MAX(id), 0) + 1 AS nextId FROM CARD WHERE id_Player = ?";
		int nextIdCard = 0;
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryLastIdCard)) {
			preparedStatement.setInt(1, id_Player);

			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return rs.getInt("nextId");
			} else {
				return nextIdCard + 1;
			}
		}

	}

	public void saveCard(Card c) throws SQLException {
		String querySaveCard = "INSERT INTO CARD (id_Player, number, color) VALUES (?, ?, ?)";

		try (PreparedStatement preparedStatement = conexion.prepareStatement(querySaveCard)) {
			preparedStatement.setInt(1, c.getPlayerId());
			preparedStatement.setString(2, c.getNumber());
			preparedStatement.setString(3, c.getColor());

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() throws SQLException {
	    if (conexion != null && !conexion.isClosed()) {
	        conexion.close();
	        System.out.println("Disconnected from database.");
	    }
	}

	@Override
	public Card getCard(int cardId) throws SQLException {
		String queryGetCard = "SELECT * FROM CARD WHERE id = ?";
		Card card = null;

		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryGetCard)) {
			preparedStatement.setInt(1, cardId);
			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("id");
				String number = rs.getString("number");
				String color = rs.getString("color");
				int playerId = rs.getInt("id_player");

				card = new Card(id, number, color, playerId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return card;
	}

	@Override
	public void saveGame(Card card) throws SQLException {
		// TODO Auto-generated method stub
		int idCard = card.getId();
		String querySaveGame = "INSERT INTO GAME (id_card) VALUES (?)";

		try (PreparedStatement preparedStatement = conexion.prepareStatement(querySaveGame)) {
			preparedStatement.setInt(1, idCard);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deleteCard(Card card) throws SQLException {
		// TODO Auto-generated method stub
		String queryDeleteCard = "DELETE FROM CARD ";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryDeleteCard)) {
			preparedStatement.executeUpdate();
		}
		String queryDeleteGame = "DELETE FROM GAME ";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryDeleteGame)) {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
		}

	}

	@Override
	public void clearDeck(int playerId) throws SQLException {
		// DELETE cards
		String queryDeleteCards = "DELETE FROM CARD WHERE id_Player = ?";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryDeleteCards)) {
			preparedStatement.setInt(1, playerId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String queryDeleteGames = "DELETE FROM GAME WHERE id_Card IN (SELECT id FROM CARD WHERE id_Player = ?)";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryDeleteGames)) {
			preparedStatement.setInt(1, playerId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addVictories(int playerId) throws SQLException {
		
		String queryUpdateVictories = "UPDATE PLAYER SET VICTORIES = VICTORIES + 1 WHERE id = ?";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryUpdateVictories)) {
			preparedStatement.setInt(1, playerId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addGames(int playerId) throws SQLException {

		String queryUpdateGames = "UPDATE PLAYER SET GAMES = GAMES + 1 WHERE id = ?";
		try (PreparedStatement preparedStatement = conexion.prepareStatement(queryUpdateGames)) {
			preparedStatement.setInt(1, playerId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
