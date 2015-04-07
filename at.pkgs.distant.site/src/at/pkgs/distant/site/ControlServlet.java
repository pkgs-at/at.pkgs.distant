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
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import at.pkgs.web.http.HttpRequest;
import at.pkgs.web.http.HttpResponse;
import at.pkgs.mail.Transport;
import at.pkgs.mail.Message;
import at.pkgs.distant.ControlCommand;
import at.pkgs.distant.model.Site;
import at.pkgs.distant.model.Database;
import at.pkgs.distant.model.Build;
import at.pkgs.distant.model.BuildServer;

@WebServlet(
		name = "ControlServlet",
		urlPatterns = { "/control/*" },
		asyncSupported = true)
public class ControlServlet extends ServiceServlet {

	private static final long serialVersionUID = 1L;

	protected Database getDatabase(HttpRequest request) {
		return Database.get(request.getContextPath());
	}

	@Path(
			methods = { "GET" },
			pattern = "^/poll/([^/]+)$")
	protected void doPollGet(
			HttpRequest request,
			HttpResponse response,
			final String name)
					throws ServletException, IOException {
		final Database database;
		final PrintWriter writer;

		database = this.getDatabase(request);
		response.setContentType("text/event-stream");
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
				BuildServer server;
				Build build;

				server = database.getFirstAvailableBuildServer(name);
				if (server == null) return false;
				build = database.getBuild(server.getBuild());
				this.command(
						ControlCommand.EXECUTE,
						build.getName(),
						/* FOR DEBUG *
						"-d",
						/* END DEBUG */
						"-Ddistant.build=" + build.getName(),
						"-Ddistant.project=" + build.getProject(),
						"-Ddistant.target=" + build.getTarget(),
						"-Ddistant.region=" + build.getRegion(),
						"-Ddistant.server=" + server.getName(),
						build.getTarget());
				return true;
			}

			@Override
			public void run() {
				try {
					int count;

					ControlServlet.register(name, this);
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
					ControlServlet.unregister(name, this);
				}
			}

		});
	}

	@Path(
			methods = { "GET" },
			pattern = "^/build/([^/]+)$")
	protected void doBuildGet(
			HttpRequest request,
			HttpResponse response,
			String name)
					throws ServletException, IOException {
		Database database;
		File file;

		database = this.getDatabase(request);
		if (database.getBuild(name) == null) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		file = new File(
				Site.load(request.getContextPath()).getData(),
				"build." + name + ".zip");
		if (!file.exists() || !file.isFile()) {
			response.sendError(HttpResponse.SC_NOT_FOUND);
			return;
		}
		response.sendResponse("application/zip", file);
	}

	private void send(
			Site.Mail mail,
			Build build,
			List<BuildServer> servers) {
		StringBuilder body;
		Message.MixedParts parts;
		Message message;
		Transport transport;

		if (mail == null) return;
		body = new StringBuilder();
		body.append("Distant Build Report").append("\r\n");
		body.append("\r\n");
		body.append("Build:   ").append(build.getName()).append("\r\n");
		body.append("Project: ").append(build.getProject()).append("\r\n");
		body.append("Target:  ").append(build.getTarget()).append("\r\n");
		body.append("Region:  ").append(build.getRegion()).append("\r\n");
		body.append("Invoked: ").append(build.getInvoked()).append("\r\n");
		body.append("Succeed: ").append(build.getSucceed()).append("\r\n");
		body.append("Aborted: ").append(build.getAborted()).append("\r\n");
		body.append("User:    ").append(build.getUser()).append("\r\n");
		body.append("Comment: ").append(build.getComment()).append("\r\n");
		body.append("Servers:");
		parts = new Message.MixedParts();
		parts.part(
				new Message.TextPart(
						body.toString(),
						"plain"));
		for (BuildServer server : servers) {
			byte[] content;

			body.append(' ').append(server.getName());
			body.append(':').append(server.getStatus());
			content = server.getOutput().getBytes(StandardCharsets.UTF_8);
			parts.part(
					new Message.AttachmentPart(
							Message.newDataHandler(
									"text/plain; charset=UTF-8",
									String.format(
											"%s.%s.txt",
											build.getName(),
											server.getName()),
									content)));
		}
		body.append("\r\n");
		message = new Message(Message.Encoding.JAPANESE);
		message.subject(
				String.format(
						"[distant build: %s] %s.%s on %s aborted: %d",
						build.getName(),
						build.getProject(),
						build.getTarget(),
						build.getRegion(),
						build.getAborted()));
		if (mail.getFrom() != null)
			message.from(
					mail.getFrom().getAddress(),
					mail.getFrom().getName());
		if (mail.getReplyTo() != null)
			message.replyTo(
					mail.getReplyTo().getAddress(),
					mail.getReplyTo().getName());
		for (Site.Address item : mail.getTo())
			message.to(
					item.getAddress(),
					item.getName());
		for (Site.Address item : mail.getCc())
			message.cc(
					item.getAddress(),
					item.getName());
		for (Site.Address item : mail.getBcc())
			message.bcc(
					item.getAddress(),
					item.getName());
		message.parts(parts);
		transport = new Transport(
				mail.getHostname(),
				mail.getPort(),
				mail.getSecure());
		if (mail.getUsername() != null)
			transport.authenticate(mail.getUsername(), mail.getPassword());
		try {
			transport.send(message);
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}

	@Path(
			methods = { "POST" },
			pattern = "^/build/([^/]+)/([^/]+)$")
	protected void doBuildPost(
			HttpRequest request,
			HttpResponse response,
			String name,
			String server)
					throws ServletException, IOException {
		Database database;
		int status;
		StringBuilder output;
		boolean completed;

		database = this.getDatabase(request);
		if (database.getBuild(name) == null) {
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
		completed = database.setResultBuildServer(
				name,
				server,
				status,
				output.toString());
		if (!completed) return;
		this.send(
				Site.load(request.getContextPath()).getMail(),
				database.getBuild(name),
				database.getBuildServers(name));
	}

	@Path(
			methods = { "GET" },
			pattern = "^/bundled/([^/]+)$")
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

	@Path(
			methods = { "GET" },
			pattern = "^/configure/systemd/distant-([^\\.]+)\\.service$")
	protected void doCongfigureServiceGet(
			HttpRequest request,
			HttpResponse response,
			String name)
					throws ServletException, IOException {
		String url;
		StringBuilder builder;

		url = request.getRequestURL().toString();
		url = url.substring(0, url.length() - request.getPathInfo().length());
		builder = new StringBuilder();
		try (BufferedReader reader =
				new BufferedReader(
						new InputStreamReader(
								this.getClass().getResourceAsStream(
										"ControlServlet.systemd")))) {
			String line;

			while ((line = reader.readLine()) != null)
				builder.append(String.format(line, url, name)).append('\n');
		}
		response.sendResponse("text/plain", builder.toString());
	}

	@Override
	protected void service(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
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
