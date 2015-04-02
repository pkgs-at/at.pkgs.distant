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

import org.apache.tools.ant.Main;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;

public class Runner extends Main {

	private int status;

	@Override
	protected void addBuildListeners(Project project) {
		super.addBuildListeners(project);
		project.addBuildListener(new BuildListener() {

			public void buildStarted(BuildEvent event) {
				Antlib.include(
						event.getProject(),
						"/net/sf/antcontrib/antlib.xml");
			}

			public void buildFinished(BuildEvent event) {
				// do nothing
			}

			public void targetStarted(BuildEvent event) {
				// do nothing
			}

			public void targetFinished(BuildEvent event) {
				// do nothing
			}

			public void taskStarted(BuildEvent event) {
				// do nothing
			}

			public void taskFinished(BuildEvent event) {
				// do nothing
			}

			public void messageLogged(BuildEvent event) {
				// do nothing
			}

		});
	}

	@Override
	public void exit(int status) {
		this.status = status;
	}

	public static int execute(String... arguments) {
		Runner runner;

		runner = new Runner();
		runner.startAnt(arguments, null, null);
		return runner.status;
	}

}
