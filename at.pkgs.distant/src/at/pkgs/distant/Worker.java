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

package at.pkgs.distant;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.HttpURLConnection;
import at.pkgs.web.client.Connections;
import at.pkgs.web.client.LocationBuilder;

public class Worker implements Runnable {

	private static final Pattern COMMAND_DELIMITOR = Pattern.compile("&");

	private final String control;

	private final String server;

	private long interval;

	private boolean respawn;

	public Worker(String control, String server) {
		this.control = control;
		this.server = server;
		this.interval = 0L;
		this.respawn = false;
	}

	public void download(
			String build,
			File file)
					throws IOException {
		HttpURLConnection connection;
		InputStream input;
		OutputStream output;

		connection = null;
		input = null;
		output = null;
		try {
			byte[] buffer;
			int size;

			connection = Connections.openHttpURLConnection(
					new LocationBuilder(this.control)
							.path("build")
							.path(build)
							.toString());
			connection.connect();
			input = new BufferedInputStream(connection.getInputStream());
			output = new BufferedOutputStream(new FileOutputStream(file));
			buffer = new byte[4096];
			while ((size = input.read(buffer)) > 0)
				output.write(buffer, 0, size);
		}
		finally {
			if (output != null) output.close();
			if (input != null) input.close();
			if (connection != null) connection.disconnect();
		}
	}

	public int build(PrintStream console, String... arguments) {
		PrintStream out;
		PrintStream err;

		out = System.out;
		err = System.err;
		System.setOut(console);
		System.setErr(console);
		try {
			Class<?> runner;
			Method execute;
			int status;

			runner = Class.forName(
					Worker.class.getPackage().getName() + ".core.Runner",
					true,
					Thread.currentThread().getContextClassLoader());
			execute = runner.getMethod("execute", String[].class);
			status = (Integer)execute.invoke(null, (Object)arguments);
			return status;
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
		finally {
			System.setErr(err);
			System.setOut(out);
		}
	}

	public void upload(
			String name,
			int status,
			String output)
					throws IOException {
		HttpURLConnection connection;

		connection = null;
		try {
			Writer writer;

			connection = Connections.openHttpURLConnection(
					new LocationBuilder(this.control)
							.path("build")
							.path(name)
							.path(this.server)
							.query("status", Integer.toString(status, 10))
							.toString());
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty(
					"Expect",
					"100-continue");
			connection.setRequestProperty(
					"Content-Type",
					"text/plain; charset=UTF-8");
			connection.connect();
			writer = new OutputStreamWriter(
					new BufferedOutputStream(
							connection.getOutputStream()),
					"UTF-8");
			writer.write(output);
			writer.close();
			connection.getResponseCode();
		}
		finally {
			if (connection != null) connection.disconnect();
		}
	}

	public void delete(File file) {
		if (!file.exists()) return;
		if (file.isDirectory())
			for (File item : file.listFiles()) this.delete(item);
		file.delete();
	}

	public String parse(String part) {
		try {
			return URLDecoder.decode(part, "UTF-8");
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
	}

	public String[] parse(String[] parts, int offset) {
		List<String> arguments;

		arguments = new ArrayList<String>();
		for (int index = offset; index < parts.length; index ++)
			arguments.add(this.parse(parts[index]));
		return arguments.toArray(new String[arguments.size()]);
	}

	public void execute(
			String name,
			String[] arguments)
					throws IOException {
		File downloaded;
		File directory;
		ByteArrayOutputStream buffer;
		PrintStream console;
		String[] parameters;
		int status;

		downloaded = new File("build." + name + ".zip");
		this.delete(downloaded);
		this.download(name, downloaded);
		directory = new File("build." + name);
		this.delete(directory);
		new Archive(downloaded).inflate(directory);
		this.delete(downloaded);
		parameters = new String[arguments.length + 2];
		parameters[0] = "-f";
		parameters[1] = new File(directory, "build.xml").getAbsolutePath();
		System.arraycopy(arguments, 0, parameters, 2, arguments.length);
		buffer = new ByteArrayOutputStream();
		console = new PrintStream(buffer, false, "UTF-8");
		console.append("launch ant for build: ").append(name).println();
		for (String parameter : parameters)
			console.append(' ').append(parameter);
		console.println();
		status = this.build(console, parameters);
		if (status == 0) this.delete(directory);
		console.flush();
		this.upload(name, status, buffer.toString("UTF-8"));
	}

	public void poll() throws IOException {
		BufferedReader reader;

		reader = null;
		try {
			HttpURLConnection connection;
			String line;

			connection = Connections.openHttpURLConnection(
					new LocationBuilder(this.control)
							.path("poll")
							.path(this.server)
							.toString());
			connection.connect();
			reader = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			while (!this.respawn) {
				String[] parts;

				line = reader.readLine();
				if (line == null) break;
				this.interval = 0L;
				parts = Worker.COMMAND_DELIMITOR.split(line);
				switch (ControlCommand.valueOf(parts[0])) {
				case STANDBY :
					break;
				case EXECUTE :
					this.execute(this.parse(parts[1]), this.parse(parts, 2));
					this.respawn = true;
					break;
				case ABORTED :
					throw new IOException("control aborted");
				}
			}
			connection.disconnect();
		}
		finally {
			if (reader != null) reader.close();
		}
	}

	@Override
	public void run() {
		while (!this.respawn) {
			try {
				this.poll();
			}
			catch (UnsupportedEncodingException cause) {
				throw new RuntimeException(cause);
			}
			catch (IOException cause) {
				cause.printStackTrace(System.err);
				try {
					Thread.sleep(this.interval);
				}
				catch (InterruptedException ignored) {
					// do nothing
				}
				this.interval = Math.min(60000L, this.interval + 1000L);
			}
		}
	}

}
