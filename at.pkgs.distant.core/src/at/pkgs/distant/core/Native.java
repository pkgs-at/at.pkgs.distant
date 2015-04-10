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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import at.pkgs.jna.posix.POSIXHandler;
import at.pkgs.jna.posix.POSIXFactory;
import at.pkgs.jna.posix.POSIX;
import at.pkgs.jna.posix.Group;
import at.pkgs.jna.posix.Passwd;

public class Native {

	public class Handler implements POSIXHandler {

		@Override
		public boolean isVerbose() {
			return false;
		}

		@Override
		public InputStream getInputStream() {
			return System.in;
		}

		@Override
		public PrintStream getOutputStream() {
			return System.out;
		}

		@Override
		public PrintStream getErrorStream() {
			return System.err;
		}

		@Override
		public File getCurrentWorkingDirectory() {
			this.unimplementedError("getcwd");
			return null;
		}

		@Override
		public String[] getEnv() {
			this.unimplementedError("getenv");
			return null;
		}

		@Override
		public int getPID() {
			this.unimplementedError("getpid");
			return -1;
		}

		@Override
		public void warn(WARNING_ID warning, String message, Object... data) {
			// do nothing
		}

		@Override
		public void error(POSIX.ERRORS error, String message) {
			if (message == null)
				throw new RuntimeException(error.name());
			else
				throw new RuntimeException(error.name() + ": " + message);
		}

		@Override
		public void unimplementedError(String name) {
			throw new UnsupportedOperationException("not supported: " + name);
		}

	}

	private final POSIX posix;

	public Native() {
		this.posix = POSIXFactory.getPOSIX(new Handler(), true);
	}

	public int getProcessId() {
		return this.posix.getpid();
	}

	public void changeDirectory(String path) {
		if (Natives.chdir(path) != 0)
			throw new RuntimeException("failed on chdir(" + path + ")");
	}

	public void setGroup(String name) {
		int gid;

		if (name.matches("^\\d+$")) {
			gid = Integer.parseInt(name);
		}
		else {
			Group group;

			group = this.posix.getgrnam(name);
			if (group == null)
				throw new RuntimeException("group not found: " + name);
			if (group.getGID() > Integer.MAX_VALUE)
				throw new RuntimeException("too large gid value");
			gid = (int)group.getGID();
		}
		if (this.posix.setgid(gid) != 0)
			throw new RuntimeException("failed on setgid(" + gid + ")");
	}

	public void setUser(String name) {
		int uid;

		if (name.matches("^\\d+$")) {
			uid = Integer.parseInt(name);
		}
		else {
			Passwd passwd;

			passwd = this.posix.getpwnam(name);
			if (passwd == null)
				throw new RuntimeException("user not found: " + name);
			if (passwd.getUID() > Integer.MAX_VALUE)
				throw new RuntimeException("too large uid value");
			uid = (int)passwd.getUID();
		}
		if (this.posix.setuid(uid) != 0)
			throw new RuntimeException("failed on setuid(" + uid + ")");
	}

	public void setGroupAndUser(String name) {
		Passwd passwd;
		int gid;
		int uid;

		if (name.matches("^\\d+$"))
			passwd = this.posix.getpwuid(Integer.parseInt(name));
		else
			passwd = this.posix.getpwnam(name);
		if (passwd == null)
			throw new RuntimeException("user not found: " + name);
		if (passwd.getGID() > Integer.MAX_VALUE)
			throw new RuntimeException("too large gid value");
		gid = (int)passwd.getGID();
		if (passwd.getUID() > Integer.MAX_VALUE)
			throw new RuntimeException("too large uid value");
		uid = (int)passwd.getUID();
		if (this.posix.setgid(gid) != 0)
			throw new RuntimeException("failed on setgid(" + gid + ")");
		if (this.posix.setuid(uid) != 0)
			throw new RuntimeException("failed on setuid(" + uid + ")");
	}

}
