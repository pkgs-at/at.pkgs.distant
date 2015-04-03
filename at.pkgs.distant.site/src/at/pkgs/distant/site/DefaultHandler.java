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
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import at.pkgs.distant.Archive;
import at.pkgs.distant.model.Site;
import at.pkgs.distant.model.Database;

public class DefaultHandler extends SiteHandler {

	@WebFilter(urlPatterns = {"/default.htpl"})
	public static class Filter extends SiteHandler.Filter {

	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlRootElement(name = "Model")
	public static class Model {

		private List<Site.Server> standbyServers;

		@XmlElementWrapper(name = "StandbyServers")
		@XmlElement(name = "Server")
		public List<Site.Server> getStandbyServers() {
			return this.standbyServers;
		}

		public void setStandbyServers(List<Site.Server> value) {
			this.standbyServers = value;
		}

		public void addStandbyServer(Site.Server value) {
			if (this.standbyServers == null)
				this.standbyServers = new ArrayList<Site.Server>();
			this.standbyServers.add(value);
		}

		public void addStandbyServer(String name) {
			Site.Server value;

			value = new Site.Server();
			value.setName(name);
			this.addStandbyServer(value);
		}

	}

	protected Model model() {
		Model model;

		model = new Model();
		for (String name : ControlServlet.getServers())
			model.addStandbyServer(name);
		return model;
	}

	protected boolean build() {
		Site.Project project;
		Site.Target target;
		Site.Region region;
		List<String> servers;
		File upload;
		String build;
		Archive archive;

		if (!this.validateToken("token")) {
			this.addErrorMessage("Token missmatch");
			return false;
		}
		project = this.getSite().getProject(
				this.getRequest().getParameter("project"));
		if (project == null) {
			this.addErrorMessage(
					"Unknown project: %s",
					this.getRequest().getParameter("project"));
			return false;
		}
		target = project.getBuild().getTarget(
				this.getRequest().getParameter("target"));
		if (target == null) {
			this.addErrorMessage(
					"Unknown target: %s",
					this.getRequest().getParameter("target"));
			return false;
		}
		region = project.getRegion(
				this.getRequest().getParameter("region"));
		if (region == null) {
			this.addErrorMessage(
					"Unknown region: %s",
					this.getRequest().getParameter("region"));
			return false;
		}
		servers = new ArrayList<String>();
		for (Site.Server server : region.getServers())
			servers.add(server.getName());
		upload = new File(this.getSite().getUpload(), project.getName());
		if (!upload.exists()) {
			this.addErrorMessage(
					"Upload directory not found: %s",
					upload);
			return false;
		}
		build = Database.get().getBuildIdentity();
		archive = new Archive(
				new File(
						this.getSite().getData(),
						"build." + build + ".zip"));
		try (Archive.Appender appender = archive.deflate()) {
			appender.append(upload);
			appender.append("build.xml", project.getBuild().getFile());
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		Database.get().newBuild(
				build,
				project.getName(),
				target.getName(),
				region.getName(),
				servers,
				this.getUser());
		ControlServlet.interrupt();
		this.addSuccessMessage("New build: %s", build);
		return true;
	}

	@Override
	protected void handle() throws ServletException, IOException {
		String action;

		action = this.getRequest().getParameter("action");
		if (action == null) action = "default";
		switch (action) {
		case "token" :
			this.issueToken();
			return;
		case "refresh" :
			this.getResponse().setContentType("application/xml");
			JAXB.marshal(this.model(), this.getResponse().getOutputStream());
			this.finish();
			return;
		case "build" :
			if (this.build()) {
				// TODO redirect
				this.finish();
				return;
			}
			return;
		}
	}

}
