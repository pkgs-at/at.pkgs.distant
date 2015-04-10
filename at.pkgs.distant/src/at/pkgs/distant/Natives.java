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
import java.lang.reflect.InvocationTargetException;

public class Natives {

	private class Invoker {

		private final Method method;

		private Invoker(String name, Class<?>... arguments) {
			try {
				this.method = Natives.this.core.getClass().getMethod(
						name,
						arguments);
			}
			catch (NoSuchMethodException cause) {
				throw new RuntimeException(cause);
			}
		}

		private Object invoke(Object... arguments) {
			try {
				return method.invoke(Natives.this.core, arguments);
			}
			catch (InvocationTargetException cause) {
				Throwable throwable;

				throwable = cause.getTargetException();
				if (throwable instanceof RuntimeException)
					throw (RuntimeException) throwable;
				else
					throw new RuntimeException(cause);
			}
			catch (IllegalAccessException cause) {
				throw new RuntimeException(cause);
			}
		}

	}

	private final Object core;

	private final Invoker getProcessId;

	private final Invoker changeDirectory;

	private final Invoker setGroup;

	private final Invoker setUser;

	private final Invoker setGroupAndUser;

	private Natives() {
		try {
			Class<?> type;

			type = Class.forName(
					Natives.class.getPackage().getName() + ".core.Native",
					true,
					Thread.currentThread().getContextClassLoader());
			this.core = type.newInstance();
		}
		catch (RuntimeException cause) {
			throw cause;
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
		this.getProcessId = new Invoker(
				"getProcessId");
		this.changeDirectory = new Invoker(
				"changeDirectory",
				String.class);
		this.setGroup = new Invoker(
				"setGroup",
				String.class);
		this.setUser = new Invoker(
				"setUser",
				String.class);
		this.setGroupAndUser = new Invoker(
				"setGroupAndUser",
				String.class);
	}

	private static Natives instance = null;

	private static Natives instance() {
		if (Natives.instance == null) {
			synchronized (Natives.class) {
				if (Natives.instance == null)
					Natives.instance = new Natives();
			}
		}
		return Natives.instance;
	}

	public static int getProcessId() {
		return (Integer)Natives.instance().getProcessId.invoke();
	}

	public static void changeDirectory(String path) {
		Natives.instance().changeDirectory.invoke(path);
	}

	public static void setGroup(String name) {
		Natives.instance().setGroup.invoke(name);
	}

	public static void setUser(String name) {
		Natives.instance().setUser.invoke(name);
	}

	public static void setGroupAndUser(String name) {
		Natives.instance().setGroupAndUser.invoke(name);
	}

}
