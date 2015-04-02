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

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.net.URLEncoder;

public class FormBuilder {

	private final String encoding;

	private final StringBuilder builder;

	public FormBuilder(String encoding) {
		this.encoding = encoding;
		this.builder = new StringBuilder();
	}

	public FormBuilder(Charset charset) {
		this(charset.name());
	}

	public FormBuilder() {
		this("UTF-8");
	}

	public String getEncoding() {
		return this.encoding;
	}

	public FormBuilder append(String name, String value) {
		if (name == null) throw new IllegalArgumentException("name is null");
		if (value == null) return this;
		if (this.builder.length() > 0) builder.append('&');
		try {
			this.builder
					.append(URLEncoder.encode(name, this.encoding))
					.append('=')
					.append(URLEncoder.encode(value, this.encoding));
		}
		catch (UnsupportedEncodingException cause) {
			throw new RuntimeException(cause);
		}
		return this;
	}

	@Override
	public String toString() {
		return this.builder.toString();
	}

	public void writeTo(
			OutputStream output,
			boolean close)
					throws IOException {
		output.write(this.toString().getBytes());
		if (close) output.close();
	}

	public void writeTo(
			OutputStream output)
				throws IOException {
		this.writeTo(output, false);
	}

}
