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

import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Archive {

	public class Appender implements Closeable {

		private final ZipOutputStream output;

		private final Set<String> directories;

		private final byte[] buffer;

		private Appender() throws IOException {
			this.output = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(
									Archive.this.archive)));
			this.directories = new HashSet<String>();
			this.buffer = new byte[4096];
		}

		public Appender append(String name, File file) throws IOException {
			if (!file.exists())
				throw new FileNotFoundException(file.toString());
			if (file.isDirectory()) {
				if (name == null) name = "";
				if (name.length() > 0) {
					name += "/";
					if (this.directories.add(name)) {
						this.output.putNextEntry(new ZipEntry(name));
						this.output.closeEntry();
					}
				}
				for (File item : file.listFiles())
					this.append(name + item.getName(), item);
			}
			else {
				InputStream input;

				this.output.putNextEntry(new ZipEntry(name));
				input = new BufferedInputStream(
						new FileInputStream(
								file));
				try {
					int size;

					while ((size = input.read(this.buffer)) > 0)
						this.output.write(this.buffer, 0, size);
				}
				finally {
					input.close();
				}
				this.output.closeEntry();
			}
			return this;
		}

		public Appender append(File file) throws IOException {
			if (file.isDirectory())
				return this.append(null, file);
			else
				return this.append(file.getName(), file);
		}

		@Override
		public void close() throws IOException {
			this.output.close();
		}

	}

	private final File archive;

	public Archive(File archive) {
		this.archive = archive;
	}

	public Appender deflate() throws IOException {
		return new Appender();
	}

	public void inflate(File base) throws IOException {
		ZipInputStream input;

		input = new ZipInputStream(
				new BufferedInputStream(
						new FileInputStream(
								this.archive)));
		try {
			byte[] buffer;
			ZipEntry entry;

			buffer = new byte[4096];
			while ((entry = input.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					File directory;

					directory = new File(base, entry.getName());
					if (!directory.exists()) directory.mkdirs();
				}
				else {
					File file;
					File directory;
					OutputStream output;

					file = new File(base, entry.getName());
					directory = file.getParentFile();
					if (!directory.exists()) directory.mkdirs();
					output = new BufferedOutputStream(
							new FileOutputStream(
									file));
					try {
						int size;

						while ((size = input.read(buffer)) > 0)
							output.write(buffer, 0, size);
					}
					finally {
						output.close();
					}
				}
			}
		}
		finally {
			input.close();
		}
	}

	public static void main(String... arguments) throws IOException {
		Appender appender;

		if (arguments.length < 1) {
			System.err.println("usage: ARCHIVE (FILE | DIRECTORY)...");
			System.exit(-1);
			return;
		}
		appender = 	new Archive(new File(arguments[0])).deflate();
		for (int index = 1; index < arguments.length; index ++)
			appender.append(new File(arguments[index]));
		appender.close();
	}

}
