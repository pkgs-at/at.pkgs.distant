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
import javax.servlet.annotation.WebServlet;
import at.pkgs.web.http.HttpRequest;
import at.pkgs.web.http.HttpResponse;

@WebServlet(
		name = "DownloadServlet",
		urlPatterns = { "/download/*" })
public class DownloadServlet extends ServiceServlet {

	private static final long serialVersionUID = 1L;

	@Path(
			methods = { "GET" },
			pattern = "^/at\\.pkgs\\.distant\\.site\\.war$")
	protected void doAtPkgsDistantSiteWarGet(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		response.sendRedirect(
				"https://github.com/pkgs-at/at.pkgs.distant/raw/master/" +
				"at.pkgs.distant.site/at.pkgs.distant.site.war");
	}

	@Override
	protected void service(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		super.service(request, response);
	}

}
