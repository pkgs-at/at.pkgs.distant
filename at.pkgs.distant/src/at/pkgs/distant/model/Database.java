/*
 * Copyright (c) 2009-2015, Architector Inc., Japan
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.pkgs.distant.model;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Database {

	private static final String QUERY_PATH = new StringBuilder()
			.append('/')
			.append(Database.class.getPackage().getName().replace('.', '/'))
			.append("/sql/")
			.toString();

	private static final String DATABASE_VERSION = "0100";

	private final String source;

	private final Properties statements;

	private final Connection connection;

	private Database(String name) {
		Site site;
		StringBuilder builder;

		try {
			Class.forName("org.h2.Driver");
		}
		catch (ClassNotFoundException cause) {
			throw new RuntimeException(cause);
		}
		site = Site.load(name);
		builder = new StringBuilder("jdbc:h2:file:");
		builder.append(new File(site.getData(), "site").getAbsolutePath());
		builder.append(";DB_CLOSE_DELAY=0");
		builder.append(";AUTO_RECONNECT=TRUE");
		this.source = builder.toString();
		this.statements = new Properties();
		try {
			InputStream input;

			input = this.getClass().getResourceAsStream(
					Database.QUERY_PATH + "statement.xml");
			try {
				this.statements.loadFromXML(input);
			}
			finally {
				input.close();
			}
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		this.connection = this.connection();
		try {
			this.runscript(
					this.connection,
					"create_table.sql");
			while (true) {
				String current;

				current = this.getPreference(
						this.connection,
						"database.version",
						"0000");
				if (current.equals(Database.DATABASE_VERSION)) break;
				System.out.println("migrate from " + current);
				this.runscript(
						this.connection,
						"migrate_from_" + current + ".sql");
			}
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
	}

	private Connection connection() {
		try {
			return DriverManager.getConnection(this.source);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
	}

	private void close(Connection connection) {
		try {
			if (connection != null) connection.close();
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
	}

	private void runscript(
			Connection connection,
			String name)
					throws SQLException {
		StringBuilder query;
		Statement statement;

		query = new StringBuilder("RUNSCRIPT");
		query.append(" FROM 'classpath:")
				.append(Database.QUERY_PATH).append(name)
				.append('\'');
		query.append(" CHARSET 'UTF-8'");
		statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(query.toString());
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	private PreparedStatement prepare(
			Connection connection,
			String name)
					throws SQLException {
		return connection.prepareStatement(this.statements.getProperty(name));
	}

	private String getPreference(
			Connection connection,
			String name,
			String alternative)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			ResultSet result;
			String value;

			statement = this.prepare(
					connection,
					"preference.get");
			statement.setString(1, name);
			result = statement.executeQuery();
			if (!result.next()) return alternative;
			value = result.getString(1);
			return value != null ? value : alternative;
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	private String getPreference(
			Connection connection,
			String name)
					throws SQLException {
		return this.getPreference(connection, name, null);
	}

	public String getPreference(
			String name,
			String alternative) {
		Connection connection;

		connection = this.connection();
		try {
			return this.getPreference(connection, name, alternative);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	public String getPreference(
			String key) {
		return this.getPreference(key, null);
	}

	private void setPreference(
			Connection connection,
			String name,
			String value)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			statement = this.prepare(
					connection,
					"preference.set");
			statement.setString(1, name);
			statement.setString(2, value);
			statement.executeUpdate();
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	public void setPreference(String name, String value) {
		Connection connection;

		connection = this.connection();
		try {
			this.setPreference(connection, name, value);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	public String getBuildName() {
		Connection connection;

		connection = this.connection();
		try {
			long current;
			long first;
			String name;

			connection.setTransactionIsolation(
					Connection.TRANSACTION_SERIALIZABLE);
			connection.setAutoCommit(false);
			current = Long.parseLong(
					this.getPreference(
							connection,
							"build.name"),
					10);
			first = Long.parseLong(
					String.format(
							"%1$tY%1$tm%1$td0001",
							new Date(System.currentTimeMillis())),
					10);
			name = String.format(
					"%012d",
					Math.max(current + 1, first));
			this.setPreference(connection, "build.name", name);
			connection.commit();
			return name;
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	public void newBuild(
			String name,
			String project,
			String target,
			String region,
			List<String> servers,
			String user,
			String comment) {
		Connection connection;

		connection = this.connection();
		try {
			PreparedStatement statement;

			connection.setTransactionIsolation(
					Connection.TRANSACTION_SERIALIZABLE);
			connection.setAutoCommit(false);
			statement = this.prepare(
					connection,
					"build.new");
			statement.setString(1, name);
			statement.setString(2, project);
			statement.setString(3, target);
			statement.setString(4, region);
			statement.setInt(5, servers.size());
			statement.setString(6, user);
			statement.setString(7, comment);
			statement.executeUpdate();
			statement = this.prepare(
					connection,
					"build_server.new");
			for (String server : servers) {
				statement.clearParameters();
				statement.setString(1, name);
				statement.setString(2, server);
				statement.executeUpdate();
			}
			connection.commit();
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	private Build getBuild(
			Connection connection,
			String name)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			ResultSet result;

			statement = this.prepare(
					connection,
					"build.get");
			statement.setString(1, name);
			result = statement.executeQuery();
			if (!result.next()) return null;
			return new Build(
					result.getString(1),
					result.getString(2),
					result.getString(3),
					result.getString(4),
					result.getInt(5),
					result.getInt(6),
					result.getInt(7),
					result.getBoolean(8),
					result.getString(9),
					result.getString(10),
					result.getTimestamp(11));
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	public Build getBuild(
			String name) {
		Connection connection;

		connection = this.connection();
		try {
			return this.getBuild(connection, name);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	public List<Build> listBuild(int limit, int offset) {
		Connection connection;

		connection = this.connection();
		try {
			PreparedStatement statement;
			List<Build> list;
			ResultSet result;

			statement = this.prepare(
					connection,
					"build.list");
			statement.setInt(1, limit);
			statement.setInt(2, offset);
			list = new ArrayList<Build>();
			result = statement.executeQuery();
			while (result.next())
				list.add(
						new Build(
								result.getString(1),
								result.getString(2),
								result.getString(3),
								result.getString(4),
								result.getInt(5),
								result.getInt(6),
								result.getInt(7),
								result.getBoolean(8),
								result.getString(9),
								result.getString(10),
								result.getTimestamp(11)));
			return list;
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	private void setCountBuild(
			Connection connection,
			String name)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			statement = this.prepare(
					connection,
					"build.set_count");
			statement.setString(1, name);
			statement.executeUpdate();
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	private boolean setCompletedBuild(
			Connection connection,
			String name)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			statement = this.prepare(
					connection,
					"build.set_completed");
			statement.setString(1, name);
			return statement.executeUpdate() > 0;
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	public List<BuildServer> getBuildServers(String build) {
		Connection connection;

		connection = this.connection();
		try {
			PreparedStatement statement;
			List<BuildServer> list;
			ResultSet result;

			statement = this.prepare(
					connection,
					"build_server.get");
			statement.setString(1, build);
			list = new ArrayList<BuildServer>();
			result = statement.executeQuery();
			while (result.next())
				list.add(
						new BuildServer(
								result.getString(1),
								result.getString(2),
								result.getInt(3),
								result.getString(4),
								result.getTimestamp(5)));
			return list;
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	private BuildServer getFirstAvailableBuildServer(
			Connection connection,
			String name)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			ResultSet result;

			statement = this.prepare(
					connection,
					"build_server.get_first_available");
			statement.setString(1, name);
			result = statement.executeQuery();
			if (!result.next()) return null;
			return new BuildServer(
					result.getString(1),
					result.getString(2),
					result.getInt(3),
					result.getString(4),
					result.getTimestamp(5));
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			if (statement != null) statement.close();
		}
	}

	public BuildServer getFirstAvailableBuildServer(
			String name) {
		Connection connection;

		connection = this.connection();
		try {
			return this.getFirstAvailableBuildServer(connection, name);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	private boolean setResultBuildServer(
			Connection connection,
			String build,
			String name,
			int status,
			String output)
					throws SQLException {
		PreparedStatement statement;

		statement = null;
		try {
			statement = this.prepare(
					connection,
					"build_server.set_result");
			statement.setInt(1, status);
			statement.setString(2, output);
			statement.setString(3, build);
			statement.setString(4, name);
			statement.executeUpdate();
		}
		finally {
			if (statement != null) statement.close();
		}
		this.setCountBuild(connection, build);
		return this.setCompletedBuild(connection, build);
	}

	public boolean setResultBuildServer(
			String build,
			String name,
			int status,
			String output) {
		Connection connection;

		connection = this.connection();
		try {
			return this.setResultBuildServer(
					connection,
					build,
					name,
					status,
					output);
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
		finally {
			this.close(connection);
		}
	}

	private static Database instance;

	public static Database get(String name) {
		if (Database.instance == null) {
			synchronized (Database.class) {
				if (Database.instance == null)
					Database.instance = new Database(name);
			}
		}
		return Database.instance;
	}

	public static void shutdown() {
		if (Database.instance == null) return;
		try {
			Database instance;
			Statement statement;

			synchronized (Database.class) {
				instance = Database.instance;
				Database.instance = null;
			}
			if (instance == null) return;
			statement = null;
			try {
				statement = instance.connection.createStatement();
				statement.execute("SHUTDOWN DEFRAG");
			}
			finally {
				if (statement != null) statement.close();
			}
		}
		catch (SQLException cause) {
			throw new RuntimeException(cause);
		}
	}

}
