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
	 * fix <IE8 submit button label instead of value
	 */
	$(root.document).on('click', 'form button:submit', function() {
		var $this;
		var $form;
		var name;

		$this = $(this);
		$form = $this.closest('form');
		if ($this.attr('name')) {
			$this.attr('data-__fixie_name__', $this.attr('name'));
			$this.attr('name', '__fixie_void__');
		}
		name = $this.attr('data-__fixie_name__');
		$form.find('input:hidden[name="' + name + '"]').remove();
		$('<input type="hidden" />')
			.attr('name', name)
			.attr('value', $this.attr('value'))
			.appendTo($form);
	});
	/*
	 * fix <IE8 bootstrap3 modal z-index
	 */
	$(function() {
		$('.modal').appendTo($('body'));
	});
})(this);
