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

package at.pkgs.distant.root;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;

public class DefaultHandler extends RootHandler {

	@WebFilter(urlPatterns = {"/default.htpl"})
	public static class Filter extends RootHandler.Filter {

	}

	@Override
	protected void handle() throws ServletException, IOException {
		if (this.getRequest().getParameterMap().containsKey("download")) {
			this.getResponse().sendRedirect(
					"https://github.com/pkgs-at/at.pkgs.distant/raw/master/" +
					"at.pkgs.distant.site/at.pkgs.distant.site.war");
			this.finish();
		}
		this.getResponse().sendResponse("text/plain", "default");
		this.finish();
	}

}
