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

package at.pkgs.distant.site;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import at.pkgs.web.http.HttpRequest;
import at.pkgs.web.http.HttpResponse;

public abstract class ServiceServlet extends HttpServlet {

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Path {

		public String[] methods();

		public String pattern();

	}

	public class Dispatcher {

		private final Set<String> methods;

		private final Pattern pattern;

		private final Method target;

		private Dispatcher(Path path, Method target) {
			Set<String> methods;

			methods = new HashSet<String>();
			for (String method : path.methods()) methods.add(method);
			this.methods = Collections.unmodifiableSet(methods);
			this.pattern = Pattern.compile(path.pattern());
			this.target = target;
		}

		public boolean dispatch(
				HttpRequest request,
				HttpResponse response)
						throws ServletException, IOException {
			String path;
			Matcher matcher;
			List<Object> parameters;

			if (!this.methods.contains(request.getMethod())) return false;
			path = request.getPathInfo();
			matcher = this.pattern.matcher(path == null ? "/" : path);
			if (!matcher.matches()) return false;
			parameters = new ArrayList<Object>();
			parameters.add(request);
			parameters.add(response);
			for (int index = 0; index < matcher.groupCount(); index ++)
				parameters.add(matcher.group(index + 1));
			try {
				this.target.invoke(ServiceServlet.this, parameters.toArray());
			}
			catch (IllegalAccessException cause) {
				throw new RuntimeException(cause);
			}
			catch (IllegalArgumentException cause) {
				throw new RuntimeException(cause);
			}
			catch (InvocationTargetException wrapper) {
				Throwable cause;

				cause = wrapper.getTargetException();
				if (cause instanceof ServletException)
					throw (ServletException)cause;
				if (cause instanceof IOException)
					throw (IOException)cause;
				throw new RuntimeException(cause);
			}
			return true;
		}

	}

	public static abstract class Background implements Runnable {

		private class Runner implements Runnable {

			@Override
			public void run() {
				try {
					Background.this.thread = Thread.currentThread();
					Background.this.run();
					Background.this.complete();
				}
				catch (Throwable cause) {
					Background.this.handle(cause);
				}
			}

		}

		private final HttpRequest request;

		private final HttpResponse response;

		private boolean completed;

		private Thread thread;

		private AsyncContext context;

		public Background(
				HttpRequest request,
				HttpResponse response) {
			this.request = request;
			this.response = response;
			this.completed = false;
		}

		protected HttpRequest getRequest() {
			return this.request;
		}

		protected HttpResponse getResponse() {
			return this.response;
		}

		protected Thread getThread() {
			return this.thread;
		}

		protected void complete() {
			if (this.completed) return;
			this.completed = true;
			try {
				this.context.complete();
			}
			catch (IllegalStateException ignored) {
				// do nothing
			}
		}

		protected void handle(Throwable cause) {
			this.complete();
			if (cause instanceof RuntimeException)
				throw (RuntimeException)cause;
			else
				throw new RuntimeException(cause);
		}

		private void start() {
			try {
				this.context = this.request.startAsync(
						this.request,
						this.response);
				this.context.setTimeout(0L);
				this.context.start(new Runner());
			}
			catch (Throwable cause) {
				this.handle(cause);
			}
		}

	}

	private static final long serialVersionUID = 1L;

	private List<Dispatcher> dispatchers;

	@Override
	public void init(
			ServletConfig config)
					throws ServletException {
		List<Dispatcher> dispatchers;

		super.init(config);
		dispatchers = new ArrayList<Dispatcher>();
		for (Method method : this.getClass().getDeclaredMethods()) {
			Path path;

			if (Modifier.isStatic(method.getModifiers())) continue;
			path = method.getAnnotation(Path.class);
			if (path == null) continue;
			method.setAccessible(true);
			dispatchers.add(new Dispatcher(path, method));
		}
		this.dispatchers = Collections.unmodifiableList(dispatchers);
	}

	protected void enter(Background background) {
		background.start();
	}

	protected void service(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		for (Dispatcher dispatcher : this.dispatchers)
			if (dispatcher.dispatch(request, response)) return;
		response.sendError(HttpResponse.SC_NOT_FOUND);
	}

	@Override
	protected void service(
			HttpServletRequest rawRequest,
			HttpServletResponse rawResponse)
					throws ServletException, IOException {
		HttpRequest request;
		HttpResponse response;

		request = new HttpRequest(rawRequest);
		response = new HttpResponse(request, rawResponse);
		this.service(request, response);
	}

}
