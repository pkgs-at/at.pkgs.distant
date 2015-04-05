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

package at.pkgs.distant.site;

import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.Serializable;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import at.pkgs.distant.model.Site;
import at.pkgs.web.http.HttpRequest;
import at.pkgs.web.http.HttpResponse;
import at.pkgs.web.trio.AbstractHandler;
import at.pkgs.web.trio.ContextHolder;

public abstract class SiteHandler extends AbstractHandler{

	private static class Token implements Serializable {

		private static final long serialVersionUID = 1L;

		private String value;

		private long expired;

	}

	private static final Pattern AUTHORIZATION_BASIC = Pattern.compile(
			"^Basic\\s+(\\S+)");

	private String user;

	private Site site;

	private String user() throws IOException {
		String authorization;
		String decoded;
		Matcher matcher;
		int index;
		String name;

		authorization = this.getRequest().getHeader("Authorization");
		if (authorization == null) return null;
		matcher = SiteHandler.AUTHORIZATION_BASIC.matcher(authorization);
		if (!matcher.matches()) return null;
		decoded = new String(
				Base64.getDecoder().decode(matcher.group(1)),
				"UTF-8");
		index = decoded.indexOf(':');
		if (index < 0) return null;
		name = decoded.substring(0, index);
		return name.length() > 0 ? name : null;
	}

	@Override
	public void initialize(
			ContextHolder holder,
			HttpRequest request,
			HttpResponse response)
					throws ServletException, IOException {
		super.initialize(holder, request, response);
		this.user = this.user();
		this.getRequest().setCharacterEncoding("UTF-8");
		this.getResponse().setCharacterEncoding("UTF-8");
		this.getResponse().setHeader("Cache-Control", "no-cache");
	}

	public String getUser() {
		return this.user;
	}

	protected String getTokenSessionName() {
		return this.getClass().getName() + ".token";
	}

	protected long getTokenLife() {
		return 15000L;
	}

	protected void issueToken() throws IOException {
		Token token;
		HttpSession session;

		if (!this.getRequest().methodIs("POST")) return;
		token = new Token();
		token.value = UUID.randomUUID().toString().toUpperCase();
		token.expired = System.currentTimeMillis() + this.getTokenLife();
		session = this.getRequest().getSession(true);
		session.setAttribute(this.getTokenSessionName(), token);
		this.getResponse().sendResponse("text/plain", token.value);
		this.finish();
	}

	protected boolean validateToken(String name) {
		String value;
		HttpSession session;
		Token token;

		session = this.getRequest().getSession(false);
		if (session == null) return false;
		token = (Token)session.getAttribute(this.getTokenSessionName());
		if (token == null) return false;
		session.removeAttribute(this.getTokenSessionName());
		if (token.expired < System.currentTimeMillis()) return false;
		value = this.getRequest().getParameter(name);
		if (value == null) return false;
		return value.equals(token.value);
	}

	public Site getSite() {
		if (this.site == null) this.site = Site.load();
		return this.site;
	}

}
