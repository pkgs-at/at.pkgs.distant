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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import at.pkgs.fatjar.LocalContext;

public class Program {

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

	public static int daemon(
			String control,
			String server)
					throws IOException {
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
		do {
			synchronized (Program.class) {
				if (Program.disposing) return 0;
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
		return Program.worker.exitValue();
	}

	public static int worker(
			String control,
			String server)
					throws IOException {
		FileOutputStream output;
		FileLock lock;

		output = null;
		lock = null;
		try {
			output = new FileOutputStream(".lock");
			lock = output.getChannel().tryLock();
			if (lock ==null) {
				System.err.println(
						"failed on acquire lock (maybe already running)");
				return 1;
			}
			new Worker(control, server).run();
		}
		finally {
			if (lock != null) lock.release();
			if (output != null) output.close();
		}
		return 0;
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
		try {
			if (argument.has(Option.DAEMONIZE)) {
				System.exit(Program.daemon(argument.get(0), argument.get(1)));
			}
			else {
				System.exit(Program.worker(argument.get(0), argument.get(1)));
			}
		}
		catch (IOException cause) {
			cause.printStackTrace(System.out);
			System.exit(1);
		}
	}

}
