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

/*
 * usage:
 *     <script src="jquery.js"></script>
 *     <!--[if lt IE 8]>
 *     <script src="ltie8.js"></script>
 *     <![endif]-->
 *     <!--[if lt IE 9]>
 *     <script src="ltie9.js"></script>
 *     <![endif]-->
 */
(function(root, unasigned) {
	/*
	 * fix <IE9 enter key does not apply submit button
	 * HTML5 specification: http://www.w3.org/TR/html5/forms.html#implicit-submission
	 */
	$(root.document).on('keypress', 'form input, form select', function(event) {
		if (event.keyCode != 13) return;
		$(this).closest('form').find('button:submit:not(disabled):first').click();
		return false;
	});
})(this);
