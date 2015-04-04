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
import java.io.IOException;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import at.pkgs.web.http.HttpResponse;
import at.pkgs.distant.model.Database;
import at.pkgs.distant.model.Build;
import at.pkgs.distant.model.BuildServer;

public class BuildHandler extends SiteHandler {

	@WebFilter(urlPatterns = {"/build.htpl"})
	public static class Filter extends SiteHandler.Filter {

	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlRootElement(name = "Model")
	public static class Model {

		private List<BuildServer> buildServers;

		@XmlElementWrapper(name = "BuildServers")
		@XmlElement(name = "BuildServer")
		public List<BuildServer> getBuildServers() {
			return this.buildServers;
		}

		public void setBuildServers(List<BuildServer> value) {
			this.buildServers = value;
		}
	}

	private Build build;

	private List<BuildServer> buildServers;

	public Build getBuild() {
		if (this.build == null) {
			this.build = Database.get().getBuild(
					this.getRequest().getParameter("build"));
		}
		return this.build;
	}

	public List<BuildServer> getBuildServers() {
		if (this.buildServers == null) {
			this.buildServers = Database.get().getBuildServer(
					this.getRequest().getParameter("build"));
		}
		return this.buildServers;
	}

	protected Model model() {
		Model model;

		model = new Model();
		model.setBuildServers(this.getBuildServers());
		return model;
	}

	@Override
	protected void handle() throws ServletException, IOException {
		String action;

		if (this.getBuild() == null) {
			this.getResponse().sendError(HttpResponse.SC_NOT_FOUND);
			this.finish();
			return;
		}
		action = this.getRequest().getParameter("action");
		if (action == null) action = "default";
		switch (action) {
		case "refresh" :
			this.getResponse().setContentType("application/xml");
			JAXB.marshal(this.model(), this.getResponse().getOutputStream());
			this.finish();
			return;
		}
	}

}
