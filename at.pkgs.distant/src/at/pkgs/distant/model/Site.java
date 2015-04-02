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

package at.pkgs.distant.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.File;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;

/*
<Site data="/var/local/distant/">
	<Cluster name="prod.web">
		<Server name="web0" />
		<Server name="web1" />
	</Cluster>
	<Cluster name="test.web">
		<Server name="webx" />
		<Server name="weby" />
	</Cluster>
	<Project name="btmst">
		<Region name="prod">
			<Server cluster="prod.web" />
			<Server name="btmst" />
		</Region>
		<Region name="test">
			<Server cluster="test.web" />
			<Server name="btmst" />
		</Region>
	</Project>
	<Project name="reqin">
		<Region name="prod">
			<Server cluster="prod.web" />
		</Region>
		<Region name="test">
			<Server cluster="test.web" />
		</Region>
	</Project>
</Site>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Site")
public class Site {

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Server {

		private boolean merged;

		private String name;

		private String cluster;

		public Server() {
			this.merged = false;
		}

		@XmlAttribute(name = "name")
		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.name = value;
		}

		@XmlAttribute(name = "cluster")
		public String getCluster() {
			return this.cluster;
		}

		public void setCluster(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.cluster = value;
		}

		public Server merge() {
			this.merged = true;
			return this;
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Cluster {

		private boolean merged;

		private String name;

		private List<Server> servers;

		public Cluster() {
			this.merged = false;
		}

		@XmlAttribute(name = "name")
		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.name = value;
		}

		@XmlElement(name = "Server")
		public List<Server> getServers() {
			return this.servers;
		}

		public void setServers(List<Server> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.servers = value;
		}

		public Cluster merge() {
			for (Server item : this.servers)
				item.merge();
			this.merged = true;
			return this;
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Region {

		private boolean merged;

		private String name;

		private List<Server> servers;

		private Map<String, Server> serverMap;

		public Region() {
			this.merged = false;
		}

		@XmlAttribute(name = "name")
		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.name = value;
		}

		@XmlElement(name = "Server")
		public List<Server> getServers() {
			return this.servers;
		}

		public Server getServer(String name) {
			if (this.serverMap == null) return null;
			else return this.serverMap.get(name);
		}

		public void setServers(List<Server> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.servers = value;
		}

		public Region merge() {
			if (this.servers == null)
				this.servers = Collections.emptyList();
			this.servers = Collections.unmodifiableList(this.servers);
			this.serverMap = new HashMap<String, Server>();
			for (Server item : this.servers)
				this.serverMap.put(item.getName(), item.merge());
			this.merged = true;
			return this;
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Project {

		private boolean merged;

		private String name;

		private List<Region> regions;

		private Map<String, Region> regionMap;

		private Build build;

		public Project() {
			this.merged = false;
		}

		@XmlAttribute(name = "name")
		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.name = value;
		}

		@XmlElement(name = "Region")
		public List<Region> getRegions() {
			return this.regions;
		}

		public Region getRegion(String name) {
			if (this.regionMap == null) return null;
			else return this.regionMap.get(name);
		}

		public void setRegions(List<Region> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.regions = value;
		}

		public Build getBuild() {
			return this.build;
		}

		public Project merge() {
			if (this.regions == null)
				this.regions = Collections.emptyList();
			this.regions = Collections.unmodifiableList(this.regions);
			this.regionMap = new HashMap<String, Region>();
			for (Region item : this.regions)
				this.regionMap.put(item.getName(), item.merge());
			this.merged = true;
			return this;
		}
	}

	public static class Target {

		private boolean merged;

		private String name;

		public Target() {
			this.merged = false;
		}

		@XmlAttribute(name = "name")
		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.name = value;
		}

		private Target merge() {
			this.merged = true;
			return this;
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlRootElement(name = "project")
	public static class Build {

		private boolean merged;

		private String target;

		private List<Target> targets;

		private Map<String, Target> targetMap;

		private File file;

		public Build() {
			this.merged = false;
		}

		@XmlAttribute(name = "default")
		public String getTarget() {
			return this.target;
		}

		public void setTarget(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.target = value;
		}

		@XmlElement(name = "target")
		public List<Target> getTargets() {
			return this.targets;
		}

		public Target getTarget(String name) {
			if (this.targetMap == null) return null;
			else return this.targetMap.get(name);
		}

		public void setTargets(List<Target> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.targets = value;
		}

		public File getFile() {
			return this.file;
		}

		private Build merge() {
			if (this.targets == null)
				this.targets = Collections.emptyList();
			this.targets = Collections.unmodifiableList(this.targets);
			this.targetMap = new HashMap<String, Target>();
			for (Target item : this.targets)
				this.targetMap.put(item.getName(), item.merge());
			this.merged = true;
			return this;
		}

		public static Build load(String name) {
			File file;
			Build model;

			file = new File(
					System.getProperty("at.pkgs.distant.site"),
					"project.d/" + name + ".xml");
			model = JAXB.unmarshal(file, Build.class);
			model.file = file;
			return model.merge();
		}

	}

	private boolean merged;

	private String data;

	private String upload;

	private List<Cluster> clusters;

	private Map<String, Cluster> clusterMap;

	private List<Project> projects;

	private Map<String, Project> projectMap;

	public Site() {
		this.merged = false;
	}

	@XmlAttribute(name = "data")
	public String getData() {
		return this.data;
	}

	public void setData(String value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.data = value;
	}

	@XmlAttribute(name = "upload")
	public String getUpload() {
		return this.upload;
	}

	public void setUpload(String value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.upload = value;
	}

	@XmlElement(name = "Cluster")
	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public Cluster getCluster(String name) {
		if (this.clusterMap == null) return null;
		else return this.clusterMap.get(name);
	}

	public void setClusters(List<Cluster> value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.clusters = value;
	}

	@XmlElement(name = "Project")
	public List<Project> getProjects() {
		return this.projects;
	}

	public Project getProject(String name) {
		if (this.projectMap == null) return null;
		else return this.projectMap.get(name);
	}

	public void setProjects(List<Project> value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.projects = value;
	}

	private void merge(Region region) {
		List<Server> servers;

		if (region.servers == null) return;
		servers = new ArrayList<Server>();
		for (Server server : region.servers) {
			if (server.cluster != null) {
				Cluster cluster;

				cluster = this.getCluster(server.cluster);
				if (cluster == null) continue;
				for (Server include : cluster.servers) {
					if (include.name != null)
						servers.add(include);
				}
			}
			if (server.name != null) {
				servers.add(server);
				continue;
			}
		}
		region.servers = servers;
	}

	private Site merge() {
		if (this.clusters == null)
			this.clusters = Collections.emptyList();
		this.clusters = Collections.unmodifiableList(this.clusters);
		this.clusterMap = new HashMap<String, Cluster>();
		for (Cluster item : this.clusters)
			this.clusterMap.put(item.getName(), item.merge());
		if (this.projects == null)
			this.projects = Collections.emptyList();
		this.projects = Collections.unmodifiableList(this.projects);
		this.projectMap = new HashMap<String, Project>();
		for (Project item : this.projects) {
			if (item.regions != null)
				for (Region region : item.regions)
					this.merge(region);
			item.build = Build.load(item.getName());
			this.projectMap.put(item.getName(), item.merge());
		}
		this.merged = true;
		return this;
	}

	public static Site load() {
		return JAXB.unmarshal(
				new File(
						System.getProperty("at.pkgs.distant.site"),
						"site.xml"),
				Site.class).merge();
	}

}
