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
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Models {

	public static class TimestampAdapter
	extends XmlAdapter<Long, Timestamp> {

		@Override
		public Long marshal(Timestamp value) throws Exception {
			if (value == null) return null;
			return value.getTime();
		}

		@Override
		public Timestamp unmarshal(Long element) throws Exception {
			if (element == null) return null;
			return new Timestamp(element);
		}

	}

}