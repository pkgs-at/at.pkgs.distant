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
import at.pkgs.web.trio.AbstractHandler;
import at.pkgs.web.trio.AbstractServlet;

@WebServlet(name = "htpl", urlPatterns = {"*.htpl"})
public class RootServlet extends AbstractServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected AbstractHandler newDefaultHandler(
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		return new RootHandler() {

			@Override
			protected void handle()
					throws ServletException, IOException {
				// do nothing
			}

		};
	}

}
