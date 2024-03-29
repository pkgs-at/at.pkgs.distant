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
<Site
		build="./demo.d/"
		data="/var/local/distant/demo/"
		resource="/var/www/webdav/demo/">
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
	<Mail
			hostname="smtp.gmail.com"
			port="465"
			secure="true"
			user="sysadm.architector@gmail.com"
			password="********">
		<From
			name=""
			address="" />
		<ReplyTo
			name=""
			address="" />
		<To
			name=""
			address="" />
		<Cc
			name=""
			address="" />
		<Bcc
			name=""
			address="" />
	</Mail>
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

	@XmlAccessorType(XmlAccessType.NONE)
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

		public static Build load(File file) {
			Build model;

			model = JAXB.unmarshal(file, Build.class);
			model.file = file;
			return model.merge();
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Address {

		private boolean merged;

		private String name;

		private String address;

		public Address() {
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

		@XmlAttribute(name = "address")
		public String getAddress() {
			return this.address;
		}

		public void setAddress(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.address = value;
		}

		private Address merge() {
			this.merged = true;
			return this;
		}

	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Mail {

		private boolean merged;

		private String hostname;

		private int port;

		private boolean secure;

		private String username;

		private String password;

		private Address from;

		private Address replyTo;

		private List<Address> to;

		private List<Address> cc;

		private List<Address> bcc;

		public Mail() {
			this.merged = false;
		}

		@XmlAttribute(name = "hostname")
		public String getHostname() {
			return this.hostname;
		}

		public void setHostname(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.hostname = value;
		}

		@XmlAttribute(name = "port")
		public int getPort() {
			return this.port;
		}

		public void setPort(int value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.port = value;
		}

		@XmlAttribute(name = "secure")
		public boolean getSecure() {
			return this.secure;
		}

		public void setSecure(boolean value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.secure = value;
		}

		@XmlAttribute(name = "username")
		public String getUsername() {
			return this.username;
		}

		public void setUsername(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.username = value;
		}

		@XmlAttribute(name = "password")
		public String getPassword() {
			return this.password;
		}

		public void setPassword(String value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.password = value;
		}

		@XmlElement(name = "From")
		public Address getFrom() {
			return this.from;
		}

		public void setFrom(Address value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.from = value;
		}

		@XmlElement(name = "ReplyTo")
		public Address getReplyTo() {
			return this.replyTo;
		}

		public void setReplyTo(Address value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.replyTo = value;
		}

		@XmlElement(name = "To")
		public List<Address> getTo() {
			return this.to;
		}

		public void setTo(List<Address> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.to = value;
		}

		@XmlElement(name = "Cc")
		public List<Address> getCc() {
			return this.cc;
		}

		public void setCc(List<Address> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.cc = value;
		}

		@XmlElement(name = "Bcc")
		public List<Address> getBcc() {
			return this.bcc;
		}

		public void setBcc(List<Address> value) {
			if (this.merged)
				throw new IllegalStateException("already merged");
			this.bcc = value;
		}

		private Mail merge() {
			if (this.from != null)
				this.from.merge();
			if (this.replyTo != null)
				this.replyTo.merge();
			if (this.to == null)
				this.to = Collections.emptyList();
			this.to = Collections.unmodifiableList(this.to);
			for (Address item : this.to)
				item.merge();
			if (this.cc == null)
				this.cc = Collections.emptyList();
			this.cc = Collections.unmodifiableList(this.cc);
			for (Address item : this.cc)
				item.merge();
			if (this.bcc == null)
				this.bcc = Collections.emptyList();
			this.bcc = Collections.unmodifiableList(this.bcc);
			for (Address item : this.bcc)
				item.merge();
			this.merged = true;
			return this;
		}

	}

	private boolean merged;

	private String build;

	private String data;

	private String resource;

	private List<Cluster> clusters;

	private Map<String, Cluster> clusterMap;

	private List<Project> projects;

	private Map<String, Project> projectMap;

	private Mail mail;

	private File file;

	public Site() {
		this.merged = false;
	}

	@XmlAttribute(name = "build")
	public String getBuild() {
		return this.build;
	}

	public void setBuild(String value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.build = value;
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

	@XmlAttribute(name = "resource")
	public String getResource() {
		return this.resource;
	}

	public void setResource(String value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.resource = value;
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

	@XmlElement(name = "Mail")
	public Mail getMail() {
		return this.mail;
	}

	public void setMail(Mail value) {
		if (this.merged)
			throw new IllegalStateException("already merged");
		this.mail = value;
	}

	public File getFile() {
		return this.file;
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
		File base;

		base = this.file.getParentFile();
		if (!new File(this.build).isAbsolute())
			this.build = new File(base, this.build).getAbsolutePath();
		if (!new File(this.data).isAbsolute())
			this.data = new File(base, this.data).getAbsolutePath();
		if (!new File(this.resource).isAbsolute())
			this.resource = new File(base, this.resource).getAbsolutePath();
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
			File build;

			if (item.regions != null)
				for (Region region : item.regions)
					this.merge(region);
			build = new File(this.build, item.getName() + ".xml");
			item.build = Build.load(build);
			this.projectMap.put(item.getName(), item.merge());
		}
		if (this.mail != null)
			this.mail.merge();
		this.merged = true;
		return this;
	}

	public static Site load(String name) {
		String path;
		File file;
		Site model;

		path = System.getProperty("at.pkgs.distant.site:" + name);
		if (path == null) path = System.getProperty("at.pkgs.distant.site");
		file = new File(path);
		model = JAXB.unmarshal(file, Site.class);
		model.file = file;
		return model.merge();
	}

}
