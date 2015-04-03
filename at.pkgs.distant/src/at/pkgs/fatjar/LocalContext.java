/*
 * Copyright (c) 2009-2014, Architector Inc., Japan
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

package at.pkgs.fatjar;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LocalContext {

	public static interface Action {

		public void action();

	}

	public static interface Function<Type> {

		public Type function();

	}

	public static final String TEMPORARY_PREFIX =
			LocalContext.class.getPackage().getName() + ".";

	public static final String LOCK_SUFFIX = ".lock";

	public static final String ROOT_SUFFIX = ".root";

	private final URLClassLoader loader;

	public LocalContext(String base) {
		String prefix;
		URL resource;
		String protocol;
		List<URL> classpath;
		ClassLoader parent;

		prefix = base.replace('.', '/') + '/';
		resource = LocalContext.class.getClassLoader().getResource(prefix);
		if (resource == null)
			throw new RuntimeException(
					String.format(
							"cannot read %s",
							base));
		protocol = resource.getProtocol();
		if (protocol.equals("file")) {
			File directory;

			try {
				directory = new File(resource.toURI());
			}
			catch (URISyntaxException cause) {
				throw new RuntimeException(cause);
			}
			classpath = this.prepareFileResources(directory);
		}
		else if (protocol.equals("jar")) {
			try {
				JarURLConnection connection;
				JarFile archive;

				connection = (JarURLConnection)resource.openConnection();
				archive = connection.getJarFile();
				try {
					classpath = this.prepareJarResources(archive, prefix);
				}
				finally {
					archive.close();
				}
			}
			catch (IOException cause) {
				throw new RuntimeException(cause);
			}
		}
		else {
			throw new RuntimeException(
					String.format(
							"unsupported classpath",
							resource));
		}
		parent = ClassLoader.getSystemClassLoader();
		while (parent.getParent() != null && parent.getParent() != parent)
			parent = parent.getParent();
		this.loader = new URLClassLoader(
				classpath.toArray(new URL[classpath.size()]),
				parent);
	}

	protected void delete(File file) {
		if (file.isDirectory())
			for (File child : file.listFiles())
				this.delete(child);
		file.delete();
	}

	protected void cleanupTemporaryDirectory(File temporary) {
		for (File root : temporary.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (!name.startsWith(LocalContext.TEMPORARY_PREFIX))
					return false;
				if (!name.endsWith(LocalContext.ROOT_SUFFIX))
					return false;
				return true;
			}

		})) {
			StringBuilder name;
			File lock;

			name = new StringBuilder(root.getName());
			name.setLength(name.length() - LocalContext.ROOT_SUFFIX.length());
			name.append(LocalContext.LOCK_SUFFIX);
			lock = new File(temporary, name.toString());
			if (lock.exists()) continue;
			delete(root);
		}
	}

	protected File createTemporaryDirectory() {
		File temporary;
		File lock;
		StringBuilder name;
		File root;

		temporary = new File(System.getProperty("java.io.tmpdir"));
		this.cleanupTemporaryDirectory(temporary);
		try {
			lock = File.createTempFile(
					LocalContext.TEMPORARY_PREFIX,
					LocalContext.LOCK_SUFFIX,
					temporary);
			lock.deleteOnExit();
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		name = new StringBuilder(lock.getName());
		name.setLength(name.length() - LocalContext.LOCK_SUFFIX.length());
		name.append(LocalContext.ROOT_SUFFIX);
		root = new File(temporary, name.toString());
		if (!root.mkdir())
			throw new RuntimeException(
					String.format(
							"failed on create temporary root: %s",
							root));
		return root;
	}

	protected List<URL> prepareFileResources(File directory) {
		List<URL> list;

		list = new ArrayList<URL>();
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				list.addAll(this.prepareFileResources(file));
			}
			else {
				URL url;

				if (!file.isFile()) continue;
				if (!file.getName().endsWith(".jar")) continue;
				try {
					url = file.toURI().toURL();
				}
				catch (MalformedURLException cause) {
					throw new RuntimeException(cause);
				}
				list.add(url);
			}
		}
		return list;
	}

	protected List<URL> prepareJarResources(JarFile archive, String prefix) {
		List<URL> list;
		File root;
		Enumeration<JarEntry> entries;
		byte[] buffer;

		list = new ArrayList<URL>();
		root = this.createTemporaryDirectory();
		entries = archive.entries();
		buffer = new byte[4096];
		while (entries.hasMoreElements()) {
			JarEntry entry;
			String name;
			File file;
			URL url;

			entry = entries.nextElement();
			if (entry.isDirectory()) continue;
			name = entry.getName();
			if (!name.startsWith(prefix)) continue;
			file = new File(root, name.substring(prefix.length()));
			file.getParentFile().mkdirs();
			try {
				InputStream input;
				OutputStream output;

				input = null;
				output = null;
				try {
					int size;

					input = archive.getInputStream(entry);
					output = new FileOutputStream(file);
					while ((size = input.read(buffer)) >= 0)
						output.write(buffer, 0, size);
				}
				finally {
					if (output != null) output.close();
					if (input != null) input.close();
				}
			}
			catch (IOException cause) {
				throw new RuntimeException(cause);
			}
			try {
				url = file.toURI().toURL();
			}
			catch (MalformedURLException cause) {
				throw new RuntimeException(cause);
			}
			list.add(url);
		}
		return list;
	}

	public ClassLoader getClassLoader() {
		return this.loader;
	}

	public void invoke(Action action) {
		ClassLoader loader;

		loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClassLoader());
		try {
			action.action();
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	public <Type> Type invoke(Function<Type> function) {
		ClassLoader loader;

		loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClassLoader());
		try {
			return function.function();
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}

	}

	public Class<?> loadClass(final String name) {
		return this.invoke(new Function<Class<?>>() {

			@Override
			public Class<?> function() {
				try {
					return LocalContext.this.getClassLoader().loadClass(name);
				}
				catch (ClassNotFoundException cause) {
					throw new RuntimeException(cause);
				}
			}

		});
	}

}
