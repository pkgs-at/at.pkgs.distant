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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import at.pkgs.web.http.HttpRequest;
import at.pkgs.web.http.HttpResponse;
import at.pkgs.distant.ControlCommand;
import at.pkgs.distant.model.Site;
import at.pkgs.distant.model.Database;
import at.pkgs.distant.model.Build;

@WebServlet(
		name = "ControlServlet",
		urlPatterns = { "/control/*" },
		asyncSupported = true)
public class ControlServlet extends ServiceServlet {

	private static final long serialVersionUID = 1L;

	@Path(methods = { "GET" }, pattern = "^/poll/([^/]+)$")
	protected void doPollGet(
			HttpRequest request,
			HttpResponse response,
			final String server)
					throws ServletException, IOException {
		final PrintWriter writer;

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		writer = response.getWriter();
		this.enter(new Background(request, response) {

			private boolean command(
					ControlCommand command,
					String... arguments) {
				try {
					writer.print(command);
					for (String argument : arguments) {
						writer.print('&');
						writer.print(URLEncoder.encode(argument, "UTF-8"));
					}
					writer.print("\r\n");
					writer.flush();
					return !writer.checkError();
				}
				catch (Throwable ignored) {
					return false;
				}
			}

			@Override
			protected void handle(Throwable cause) {
				try {
					this.command(ControlCommand.ABORTED);
				}
				finally {
					super.handle(cause);
				}
			}

			private boolean poll() {
				Build build;

				build = Database.get().getFirstAvailableBuild(server);
				if (build == null) return false;
				this.command(
						ControlCommand.EXECUTE,
						build.getBuild(),
						/* FOR DEBUG *
						"-d",
						/* END DEBUG */
						"-Ddistant.build=" + build.getBuild(),
						"-Ddistant.project=" + build.getProject(),
						"-Ddistant.target=" + build.getTarget(),
						"-Ddistant.region=" + build.getRegion(),
						"-Ddistant.server=" + server,
						build.getTarget());
				return true;
			}

			@Override
			public void run() {
				try {
					int count;

					ControlServlet.register(server, this);
					count = 0;
					while (count < 360) {
						if (this.poll()) break;
						if (!this.command(ControlCommand.STANDBY)) break;
						try {
							Thread.sleep(10000L);
						}
						catch (InterruptedException ignored) {
							// do nothing
						}
						count ++;
					}
				}
				finally {
					ControlServlet.unregister(server, this);
				}
			}

		});
	}

	@Path(methods = { "GET" }, pattern = "^/build/([^/]+)$")
	protected void doBuildGet(
			HttpRequest request,
			HttpResponse response,
			String build)
					throws ServletException, IOException {
		File file;

		if (Database.get().getBuild(build) == null) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		file = new File(
				Site.load().getData(),
				"build." + build + ".zip");
		if (!file.exists() || !file.isFile()) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		response.sendResponse("application/zip", file);
	}

	@Path(methods = { "POST" }, pattern = "^/build/([^/]+)/([^/]+)$")
	protected void doBuildPost(
			HttpRequest request,
			HttpResponse response,
			String build,
			String server)
					throws ServletException, IOException {
		int status;
		StringBuilder output;

		if (Database.get().getBuild(build) == null) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		try {
			status = Integer.parseInt(request.getParameter("status"), 10);
		}
		catch (NumberFormatException cause) {
			response.sendError(HttpResponse.SC_BAD_REQUEST);
			return;
		}
		output = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;

			while ((line = reader.readLine()) != null)
				output.append(line).append('\n');
		}
		Database.get().setResultBuildServer(
				build,
				server,
				status,
				output.toString());
	}

	@Path(methods = { "GET" }, pattern = "^/bundled/([^/]+)$")
	protected void doBundledGet(
			HttpRequest request,
			HttpResponse response,
			String name)
					throws ServletException, IOException {
		File file;

		file = new File(
				request.getServletContext().getRealPath(
						"/WEB-INF/lib/" + name));
		if (!file.exists() || !file.isFile()) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		response.sendResponse("application/java-archive", file);
	}

	@Override
	protected void service(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		response.setHeader("Cache-Control", "no-cache");
		super.service(request, response);
	}

	private static final ConcurrentMap<String, Background> pollings =
			new ConcurrentHashMap<String, Background>();

	private static final ConcurrentMap<String, Long> expireds =
			new ConcurrentHashMap<String, Long>();

	private static void register(String server, Background instance) {
		synchronized (ControlServlet.class) {
			ControlServlet.pollings.put(server, instance);
			ControlServlet.expireds.put(server, Long.MAX_VALUE);
		}
	}

	private static void unregister(String server, Background instance) {
		synchronized (ControlServlet.class) {
			long expired;

			if (!ControlServlet.pollings.remove(server, instance)) return;
			expired = System.currentTimeMillis() + 10000L;
			ControlServlet.expireds.put(server, expired);
		}
	}

	public static List<String> getServers() {
		long now;
		List<String> list;

		now = System.currentTimeMillis();
		list = new ArrayList<String>();
		for (String server : ControlServlet.expireds.keySet())
			if ((long)expireds.get(server) > now) list.add(server);
		Collections.sort(list);
		return list;
	}

	public static void interrupt() {
		for (Background instance : ControlServlet.pollings.values())
			instance.getThread().interrupt();
	}

}
