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

package at.pkgs.web.client;

import java.nio.charset.Charset;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LocationBuilder {

	public static class Mapper {

		private static final char[] HEXADECIMAL = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
		};

		private final String[] map;

		public Mapper(String passthru) {
			byte[] codes;

			this.map = new String[256];
			codes = passthru.getBytes();
			for (int index = 0; index < passthru.length(); index ++) {
				this.map[codes[index]] = Character
						.toString(passthru.charAt(index));
			}
			for (int code = 0; code < this.map.length; code ++) {
				if (this.map[code] != null) continue;
				this.map[code] = new StringBuilder()
						.append('%')
						.append(Mapper.HEXADECIMAL[code >> 4])
						.append(Mapper.HEXADECIMAL[code & 0x0F])
						.toString();
			}
		}

		public void appendTo(
				StringBuilder builder,
				String value,
				Charset charset) {
			for (int index = 0; index < value.length(); index ++) {
				int code;
				String character;

				code = value.codePointAt(index);
				if (code <= 0xFF) {
					builder.append(this.map[code]);
					continue;
				}
				if (code <= 0xFFFF) {
					character = String.valueOf((char)code);
				}
				else {
					character = String.valueOf(Character.toChars(code));
					index ++;
				}
				for (byte entity : charset.encode(character).array()) {
					builder
							.append('%')
							.append(Mapper.HEXADECIMAL[entity >> 4 & 0x0F])
							.append(Mapper.HEXADECIMAL[entity & 0x0F]);
				}
			}
		}

	}

	public static final String RFC3986_UNRESERVED =
			"abcdefghijklmnopqrstuvwxyz" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"0123456789" +
			"-._~";

	public static final Mapper PATH_MAPPER =
			new Mapper(
					LocationBuilder.RFC3986_UNRESERVED +
					"/");

	public static final Mapper QUERY_MAPPER =
			new Mapper(
					LocationBuilder.RFC3986_UNRESERVED +
					"&=");

	public static final Mapper COMPONENT_MAPPER =
			new Mapper(
					LocationBuilder.RFC3986_UNRESERVED);

	private final StringBuilder builder;

	private final Charset charset;

	public LocationBuilder(String base, Charset charset) {
		this.builder = new StringBuilder(base);
		this.charset = charset;
	}

	public LocationBuilder(String base, String encoding) {
		this(base, Charset.forName(encoding));
	}

	public LocationBuilder(String base) {
		this(base, "UTF-8");
	}

	public LocationBuilder path(String value) {
		int length;

		if (value == null) return this;
		length = this.builder.length();
		if (length > 0 && this.builder.charAt(length - 1) == '/') {
			if (value.startsWith("/"))
				LocationBuilder.PATH_MAPPER.appendTo(
						this.builder,
						value.substring(1),
						this.charset);
			else
				LocationBuilder.PATH_MAPPER.appendTo(
						this.builder,
						value,
						this.charset);
		}
		else {
			if (value.startsWith("/"))
				LocationBuilder.PATH_MAPPER.appendTo(
						this.builder,
						value,
						this.charset);
			else
				LocationBuilder.PATH_MAPPER.appendTo(
						this.builder.append('/'),
						value,
						this.charset);
		}
		return this;
	}

	public LocationBuilder query(String value) {
		if (value == null) return this;
		if (this.builder.indexOf("?") < 0)
			this.builder.append('?');
		LocationBuilder.QUERY_MAPPER.appendTo(
				this.builder,
				value,
				this.charset);
		return this;
	}

	public LocationBuilder query(String name, String value) {
		if (name == null) throw new IllegalArgumentException("name is null");
		if (value == null) return this;
		if (this.builder.lastIndexOf("?") < 0)
			this.builder.append('?');
		else
			this.builder.append('&');
		LocationBuilder.COMPONENT_MAPPER.appendTo(
				this.builder,
				name,
				this.charset);
		this.builder.append('=');
		LocationBuilder.COMPONENT_MAPPER.appendTo(
				this.builder,
				value,
				this.charset);
		return this;
	}

	public LocationBuilder fragment(String value) {
		if (value == null) return this;
		if (this.builder.lastIndexOf("#") < 0 && !value.startsWith("#"))
			this.builder.append('#');
		LocationBuilder.COMPONENT_MAPPER.appendTo(
				this.builder, value,
				this.charset);
		return this;
	}

	@Override
	public String toString() {
		return this.builder.toString();
	}

	public URL toURL()
			throws MalformedURLException {
		return new URL(this.toString());
	}

	public URI toURI()
			throws MalformedURLException, URISyntaxException {
		return this.toURL().toURI();
	}

}
