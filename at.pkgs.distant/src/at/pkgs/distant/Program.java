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
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
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
import at.pkgs.fatjar.LocalContext;
import at.pkgs.web.client.Connections;
import at.pkgs.web.client.LocationBuilder;

public class Program implements Runnable {

	public static enum Option {

		DAEMONIZE(
				"run as the daemon mode (fork a worker and auto respawn)",
				"this feature works only Java SE 7 or lator"),

		DIRECTORY(
				"=DIRECTORY",
				"set the working directory"),

		GROUP(
				"=GID_OR_GROUP_NAME",
				"set the executing group"),

		USER(
				"=UID_OR_USER_NAME",
				"set the executing user"),

		PIDFILE(
				"=FILE",
				"write the running process ID to FILE");

		private final String value;

		private final List<String> descriptions;

		private Option(String... usages) {
			int index;
			List<String> descriptions;

			index = 0;
			if (usages.length < 1) throw new IllegalArgumentException();
			if (usages[0].startsWith("=")) {
				if (usages.length < 2) throw new IllegalArgumentException();
				this.value = usages[index ++];
			}
			else {
				this.value = null;
			}
			descriptions = new ArrayList<String>();
			for (; index < usages.length; index ++)
				descriptions.add(usages[index]);
			this.descriptions = Collections.unmodifiableList(descriptions);
		}

		public boolean hasValue() {
			return this.value != null;
		}

		public void print(PrintStream out) {
			out.print("  --");
			out.print(this.name().toLowerCase());
			if (this.value != null)
				out.print(this.value);
			out.println();
			for (String description : this.descriptions) {
				out.print("                       ");
				out.print(description);
				out.println();
			}
		}

	}

	public static class Argument {

		private final List<String> errors;

		private final List<String> arguments;

		private final Map<Option, String> options;

		public Argument() {
			this.errors = new ArrayList<String>();
			this.arguments = new ArrayList<String>();
			this.options = new HashMap<Option, String>();
		}

		public Argument parse(String argument) {
			int glue;
			String name;
			String value;
			Option option;

			if (!argument.startsWith("--")) {
				this.arguments.add(argument);
				return this;
			}
			glue = argument.indexOf('=');
			if (glue < 0) {
				name = argument.substring(2);
				value = null;
			}
			else {
				name = argument.substring(2, glue);
				value = argument.substring(glue + 1);
			}
			try {
				option = Option.valueOf(name.toUpperCase());
			}
			catch (IllegalArgumentException ignored) {
				this.errors.add("unknown option: " + name);
				return this;
			}
			if (option.hasValue() && value == null) {
				this.errors.add("option without value: " + name);
				return this;
			}
			if (!option.hasValue() && value != null) {
				this.errors.add("option with disused value: " + name);
				return this;
			}
			this.options.put(option, value);
			return this;
		}

		public Argument parse(String... arguments) {
			for (String argument : arguments) this.parse(argument);
			return this;
		}

		public boolean error() {
			return this.errors.size() > 0;
		}

		public String[] errors() {
			return this.errors.toArray(new String[this.errors.size()]);
		}

		public String get(int index) {
			return this.arguments.get(index);
		}

		public int size() {
			return this.arguments.size();
		}

		public boolean has(Option option) {
			return this.options.containsKey(option);
		}

		public String get(Option option) {
			return this.options.get(option);
		}

	}

	private static final String BUNDLED =
			Program.class.getPackage().getName() + ".bundled";

	private static final Pattern COMMAND_DELIMITOR = Pattern.compile("&");

	private final String control;

	private final String server;

	private long interval;

	private boolean respawn;

	public Program(String control, String server) {
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
					Program.class.getPackage().getName() + ".core.Runner",
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
				parts = Program.COMMAND_DELIMITOR.split(line);
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

	private static File file =
			new File(System.getProperty("java.class.path")).getAbsoluteFile();

	private static boolean disposing = false;

	private static Process worker = null;

	public static void usage(PrintStream out, String... errors) {
		if (errors.length > 0) {
			for (String error : errors) out.println(error);
			out.println();
		}
		out.print("Usage: java -jar ");
		out.print(Program.file.getName());
		out.println(" [OPTION...] CONTROL_URL SERVER");
		out.println();
		out.println("Options");
		for (Option option : Option.values()) option.print(out);
	}

	public static void daemonize(String control, String server) {
		File java;
		ProcessBuilder builder;

		java = new File(System.getProperty("java.home"), "bin/java");
		builder = new ProcessBuilder(
				java.getAbsolutePath(),
				"-jar",
				Program.file.getPath(),
				control,
				server);
		try {
			ProcessBuilder.class.getMethod("inheritIO").invoke(builder);
		}
		catch (Exception cause) {
			System.err.println(
					"--daemonize option requires Java SE 7 or lator");
			System.err.println();
			cause.printStackTrace(System.out);
			System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				Process worker;

				synchronized (Program.class) {
					if (Program.disposing) return;
					Program.disposing = true;
					worker = Program.worker;
				}
				if (worker == null) return;
				worker.destroy();
			}

		});
		try {
			do {
				synchronized (Program.class) {
					if (Program.disposing) return;
					Program.worker = builder.start();
				}
				while (true) {
					try {
						Program.worker.waitFor();
					}
					catch (InterruptedException ignored) {
						continue;
					}
					break;
				}
			}
			while (Program.worker.exitValue() == 0);
			System.exit(Program.worker.exitValue());
		}
		catch (IOException cause) {
			cause.printStackTrace(System.out);
			System.exit(1);
		}
	}

	public static void main(String... arguments) {
		Argument argument;

		argument = new Argument().parse(arguments);
		if (argument.error()) {
			Program.usage(System.err, argument.errors());
			System.exit(1);
		}
		if (argument.size() != 2) {
			Program.usage(System.err);
			System.exit(1);
		}
		Thread.currentThread().setContextClassLoader(
				new LocalContext(Program.BUNDLED).getClassLoader());
		if (argument.has(Option.PIDFILE)) {
			File file;
			int processId;

			file = new File(argument.get(Option.PIDFILE));
			processId = Natives.getProcessId();
			try {
				FileOutputStream output;

				output = new FileOutputStream(file);
				output.write(Integer.toString(processId).getBytes());
				output.close();
			}
			catch (IOException cause) {
				cause.printStackTrace(System.err);
				System.exit(1);
			}
		}
		if (argument.has(Option.DIRECTORY)) {
			Natives.changeDirectory(argument.get(Option.DIRECTORY));
		}
		if (argument.has(Option.GROUP)) {
			Natives.setGroup(argument.get(Option.GROUP));
		}
		if (argument.has(Option.USER)) {
			if (argument.has(Option.GROUP))
				Natives.setUser(argument.get(Option.USER));
			else
				Natives.setGroupAndUser(argument.get(Option.USER));
		}
		if (argument.has(Option.DAEMONIZE)) {
			Program.daemonize(argument.get(0), argument.get(1));
		}
		else {
			new Program(argument.get(0), argument.get(1)).run();
		}
	}

}
