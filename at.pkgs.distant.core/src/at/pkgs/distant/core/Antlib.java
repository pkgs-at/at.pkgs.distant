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

package at.pkgs.distant.core;

import java.util.Properties;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.tools.ant.Project;

@XmlRootElement(name = "antlib")
public class Antlib {

	public static class Basedef {

		@XmlAttribute(name = "resource")
		public String resource;

		@XmlAttribute(name = "name")
		public String name;

		@XmlAttribute(name = "classname")
		public String classname;

	}

	public static class Typedef extends Basedef {

		// nothing

	}

	public static class Taskdef extends Basedef {

		// nothing

	}

	@XmlElements({
		@XmlElement(name = "typedef", type = Typedef.class),
		@XmlElement(name = "taskdef", type = Taskdef.class),
	})
	public List<Basedef> items;

	protected static void typedef(
			Project project,
			String name,
			String classname) {
		Class<?> type;

		try {
			type = Class.forName(classname);
		}
		catch (ClassNotFoundException cause) {
			throw new RuntimeException(cause);
		}
		project.addDataTypeDefinition(name, type);
	}

	public static void typedefs(
			Project project,
			String resource) {
		Properties properties;
		InputStream input;

		properties = new Properties();
		input = null;
		if (!resource.startsWith("/")) resource = "/" + resource;
		try {
			try {
				input = Antlib.class.getResourceAsStream(resource);
				properties.load(input);
			}
			finally {
				if (input != null) input.close();
			}
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		for (Object key : properties.keySet())
			Antlib.typedef(
					project,
					(String)key,
					properties.getProperty((String)key));
	}

	protected static void taskdef(
			Project project,
			String name,
			String classname) {
		Class<?> type;

		try {
			type = Class.forName(classname);
		}
		catch (ClassNotFoundException cause) {
			throw new RuntimeException(cause);
		}
		project.addTaskDefinition(name, type);
	}

	public static void taskdefs(
			Project project,
			String resource) {
		Properties properties;
		InputStream input;

		properties = new Properties();
		input = null;
		if (!resource.startsWith("/")) resource = "/" + resource;
		try {
			try {
				input = Antlib.class.getResourceAsStream(resource);
				properties.load(input);
			}
			finally {
				if (input != null) input.close();
			}
		}
		catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		for (Object key : properties.keySet())
			Antlib.taskdef(
					project,
					(String)key,
					properties.getProperty((String)key));
	}

	public static void include(
			Project project,
			String resource) {
		Antlib antlib;

		antlib = JAXB.unmarshal(
				Antlib.class.getResource(resource),
				Antlib.class);
		for (Basedef item : antlib.items) {
			if (item.resource != null) {
				if (item.resource.endsWith(".xml")) {
					Antlib.include(project, item.resource);
					continue;
				}
				if (item instanceof Typedef) {
					Antlib.typedefs(project, item.resource);
					continue;
				}
				if (item instanceof Taskdef) {
					Antlib.taskdefs(project, item.resource);
					continue;
				}
			}
			else {
				if (item instanceof Typedef) {
					Antlib.typedef(project, item.name, item.classname);
					continue;
				}
				if (item instanceof Taskdef) {
					Antlib.taskdef(project, item.name, item.classname);
					continue;
				}
			}
		}
	}

}
