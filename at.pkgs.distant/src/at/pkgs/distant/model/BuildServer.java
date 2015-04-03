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

import java.sql.Timestamp;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.NONE)
public class BuildServer {

	private final String build;

	private final String server;

	private final int status;

	private final String output;

	private final Timestamp timestamp;

	BuildServer(
			String build,
			String server,
			int status,
			String output,
			Timestamp timestamp) {
		this.build = build;
		this.server = server;
		this.status = status;
		this.output = output;
		this.timestamp = timestamp;
	}

	@XmlAttribute(name = "build")
	public String getBuild() {
		return this.build;
	}

	@XmlAttribute(name = "server")
	public String getServer() {
		return this.server;
	}

	@XmlAttribute(name = "status")
	public int getStatus() {
		return this.status;
	}

	@XmlElement(name = "Output")
	public String getOutput() {
		return this.output;
	}

	@XmlAttribute(name = "timestamp")
	@XmlJavaTypeAdapter(Models.TimestampAdapter.class)
	public Timestamp getTimestamp() {
		return this.timestamp;
	}

}
