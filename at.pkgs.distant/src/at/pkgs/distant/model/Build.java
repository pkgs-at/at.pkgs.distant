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
public class Build {

	private final String name;

	private final String project;

	private final String target;

	private final String region;

	private final int invoked;

	private final int succeed;

	private final int aborted;

	private final boolean completed;

	private final String user;

	private final String comment;

	private final Timestamp timestamp;

	Build(
			String name,
			String project,
			String target,
			String region,
			int invoked,
			int succeed,
			int aborted,
			boolean completed,
			String user,
			String comment,
			Timestamp timestamp) {
		this.name = name;
		this.project = project;
		this.target = target;
		this.region = region;
		this.invoked = invoked;
		this.succeed = succeed;
		this.aborted = aborted;
		this.completed = completed;
		this.user = user;
		this.comment = comment;
		this.timestamp = timestamp;
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return this.name;
	}

	@XmlAttribute(name = "project")
	public String getProject() {
		return this.project;
	}

	@XmlAttribute(name = "target")
	public String getTarget() {
		return this.target;
	}

	@XmlAttribute(name = "region")
	public String getRegion() {
		return this.region;
	}

	@XmlAttribute(name = "invoked")
	public int getInvoked() {
		return this.invoked;
	}

	@XmlAttribute(name = "succeed")
	public int getSucceed() {
		return this.succeed;
	}

	@XmlAttribute(name = "aborted")
	public int getAborted() {
		return this.aborted;
	}

	@XmlAttribute(name = "completed")
	public boolean getCompleted() {
		return this.completed;
	}

	@XmlAttribute(name = "user")
	public String getUser() {
		return this.user;
	}

	@XmlElement(name = "Comment")
	public String getComment() {
		return this.comment;
	}

	@XmlAttribute(name = "timestamp")
	@XmlJavaTypeAdapter(Models.TimestampAdapter.class)
	public Timestamp getTimestamp() {
		return this.timestamp;
	}

}
