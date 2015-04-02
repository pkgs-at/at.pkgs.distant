/*
 * Copyright (c) 2009-2014, Architector Inc., Japan
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
 * this library depends on:
 * - at.pkgs.js                        tested v1.0.0
 * - polyfill: JSON                    tested with json3 v3.3.2
 * - sugar.js                          tested v1.4.1
 * - jQuery                            tested v1.11.1
 */
(function(root, $, pkgs, unasigned) {
	var getValueOrText;
	var at;

	getValueOrText = function($element) {
		var value;

		value = $element.attr('value');
		return value === unasigned ? $element.text() : value;
	};
	$.at = pkgs;
	$.at.makeLazyAction = function(action) {
		var invoking;

		invoking = false;
		return function() {
			if (invoking) return;
			invoking = true;
			root.setTimeout(function() { invoking = false; action.call(root); }, 0);
		};
	};
	$.at.dataBindingPattern = /^(.*?)(?::(\??)(.*?)(\??))?$/;
	$.at.listChildDataBindings = function(parent, name, found) {
		var node;

		if (Object.isArray(name)) {
			found = arguments[1];
			name = null;
		}
		found = found || new Array();
		for (node = parent.firstChild; node; node = node.nextSibling) {
			var binding;

			if (node.nodeType != 1) continue;
			binding = node.getAttribute('data-binding');
			if (binding) {
				if (name) {
					if (binding == name || binding.startsWith(name + ':'))
						found.push(node);
				}
				else {
					found.push(node);
				}
			}
			else {
				$.at.listChildDataBindings(node, name, found);
			}
		}
		return found;
	};
	$.at.toText = ({
		booleanFormat: 'true|false',
		Boolean: function(format) {
			format = format || $.at.toText.booleanFormat;
			return format.split('|')[this ? 0 : 1];
		},
		numberFormat: '0|,',
		Number: function(format) {
			var parameters;

			format = format || $.at.toText.numberFormat;
			parameters = format.split('|');
			if (parameters.length > 0) parameters[0] = parseInt(parameters[0], 10);
			return this.format.apply(this, parameters);
		},
		datetimeFormat: '{yyyy}-{MM}-{dd} {HH}:{mm}:{ss}',
		Datetime: function(format) {
			format = format || $.at.toText.datetimeFormat;
			return this.format(format);
		},
		dateFormat: '{yyyy}-{MM}-{dd}',
		Date: function(format) {
			format = format || $.at.toText.dateFormat;
			return this.format(format);
		},
		timeFormat: '{HH}:{mm}:{ss}',
		Time: function(format) {
			format = format || $.at.toText.timeFormat;
			return this.format(format);
		},
		String: function(format) {
			var value;

			value = this;
			if (!format) return this;
			format.split('|').each(function(rule) {
				var pair;

				pair = rule.split(':');
				if (pair[0] != value) return;
				value = pair[1];
				return false;
			});
			return value;
		}
	});
	$.at.formatValue = function(type, value, format) {
		if (value === unasigned) return unasigned;
		if (value === null) return null;
		if (format && format.startsWith('`') && format.endsWith('`')) {
			return (function() {
				return eval(format.substring(1, format.length - 1));
			}).call(value);
		}
		switch (type) {
		case 'Timestamp' :
		case 'Date' :
		case 'Time' :
			value = Date.create(value);
			if (!value.isValid()) return null;
			break;
		}
		if (Object.has($.at.toText, type))
			return $.at.toText[type].call(value, format);
		else
			return value.toString();
	};
	$.at.toJSON = ({
		datetimeFormat: '{yyyy}-{MM}-{dd} {HH}:{mm}:{ss}',
		Datetime: function() {
			return this.format($.at.toJSON.datetimeFormat);
		},
		dateFormat: '{yyyy}-{MM}-{dd}',
		Date: function() {
			return this.format($.at.toJSON.dateFormat);
		},
		timeFormat: '{HH}:{mm}:{ss}',
		Time: function() {
			return this.format($.at.toJSON.datetimeFormat);
		}
	});
	$.at.parseString = function(optional, nullable, type, value) {
		var alternative;

		if (value === unasigned) return unasigned;
		if (value === null && optional) return unasigned;
		if (value === null && nullable) return null;
		if (value === null) value = '';
		alternative = '';
		switch (type) {
		case 'String' :
			alternative = '';
			break;
		case 'Boolean' :
			alternative = false;
			value = value.trim();
			if (value.length <= 0)
				value = null;
			else if (value == 'false')
				value = false;
			else
				value = value ? true : false;
			break;
		case 'Number' :
			alternative = 0;
			value = value.trim();
			if (value.length <= 0)
				value = null;
			else
				value = value.toNumber();
			break;
		case 'Timestamp' :
		case 'Date' :
		case 'Time' :
			alternative = '';
			value = value.trim();
			if (value.length <= 0) {
				value = null;
			}
			else {
				value = Date.create(value);
				if (!value.isValid()) value = null;
			}
			break;
		}
		if (value === unasigned) return unasigned;
		if (value === null && optional) return unasigned;
		if (value === null && nullable) return null;
		if (value === null) return alternative;
		if (Object.has($.at.toJSON, type)) value.toJSON = $.at.toJSON[type];
		return value;
	};
	$.at.load = function(parent, data) {
		var getValue;
		var setValue;

		getValue = function($element) {
			var attributes;
			var optional;
			var nullable;
			var type;

			attributes = $element.attr('data-binding').match($.at.dataBindingPattern);
			optional = attributes[2] == '?';
			nullable = attributes[4] == '?';
			type = attributes[3] || 'String';
			return  $.at.parseString(optional, nullable, type, getValueOrText($element));
		};
		setValue = function($element, value) {
			var element;
			var attributes;
			var optional;
			var nullable;
			var type;

			if (value === null) return;
			element = $element.get(0);
			attributes = $element.attr('data-binding').match($.at.dataBindingPattern);
			optional = attributes[2] == '?';
			nullable = attributes[4] == '?';
			type = attributes[3] || 'String';
			switch (type) {
			case 'Object' :
				if ($element.is('[data-constructor]')) {
					(function() {
						eval($element.attr('data-constructor'));
					}).call($element.get(0));
				}
				$.at.load(element, value);
				break;
			case 'Array' :
				if ($element.is('select')) {
					$.at.listChildDataBindings(element, '@').each(function(element) {
						var $element;

						$element = $(element);
						$element.prop('selected', value.indexOf(getValue($element)) >= 0);
					});
				}
				else {
					if ($element.is('[data-constructor]')) {
						value.each(function(value) {
							(function() {
								eval($element.attr('data-constructor'));
							}).call($element.get(0));
						});
					}
					$.at.listChildDataBindings(element, '@').each(function(element, index) {
						var $element;

						$element = $(element);
						if ($element.is(':checkbox,:radio'))
							$element.prop('checked', value.indexOf(getValue($element)) >= 0);
						else
							setValue($element, value[index]);
					});
				}
				break;
			default :
				if (!$element.is(':input')) {
					value = $.at.formatValue(type, value, $element.attr('data-format'));
					if ($element.is('[data-class]'))
						$element.addClass($element.attr('data-class') + value);
					else
						$element.text(value);
				}
				else if ($element.is(':checkbox')) {
					if (Object.isBoolean(value))
						$element.prop('checked', value);
					else
						$element.prop('checked', value == $.at.parseString(optional, nullable, type, getValueOrText($element)));
				}
				else if ($element.is('select')) {
					$element.find('option').each(function() {
						var $this;

						$this = $(this);
						$this.prop('selected', value == $.at.parseString(optional, nullable, type, getValueOrText($this)));
					});
				}
				else {
					value = $.at.formatValue(type, value, $element.attr('data-format'));
					$element.val(value);
				}
				break;
			}
		};
		if (data === unasigned || data === null) return;
		Object.keys(data, function(name, value) {
			var elements;

			elements = $.at.listChildDataBindings(parent, name);
			if (elements.length <= 0) return;
			if (elements.length > 1) {
				elements.each(function (element) {
					var $element;

					$element = $(element);
					$element.prop('checked', value == getValue($element));
				});
			}
			else {
				setValue($(elements[0]), value);
			}
		});
	};
	$.at.store = function(parent, data) {
		this.listChildDataBindings(parent).each(function(element) {
			var $data;
			var attributes;
			var optional;
			var nullable;
			var type;
			var name;
			var value;

			$data = $(element);
			attributes = $data.attr('data-binding').match($.at.dataBindingPattern);
			optional = attributes[2] == '?';
			nullable = attributes[4] == '?';
			type = attributes[3] || 'String';
			name = attributes[1];
			switch (type) {
			case 'Object' :
				value = new Object();
				$.at.store(element, value);
				if (Object.size(value) <= 0) {
					if (optional) value = unasigned;
					else if (nullable) value = null;
				}
				break;
			case 'Array' :
				value = new Array();
				$.at.store(element, value);
				if (value.length <= 0) {
					if (optional) value = unasigned;
					else if (nullable) value = null;
				}
				break;
			default :
				if ($data.is('option')) {
					if ($data.is(':selected'))
						value = getValueOfText($data.attr);
					else
						value = unasigned;
				}
				else if (!$data.is(':input')) {
					value = $data.text();
				}
				else if ($data.is(':radio')) {
					if ($data.is(':checked'))
						value = getValueOrText($data);
					else if (!optional && !Object.isArray(data) && !Object.has(data, name))
						value = null;
					else
						value = unasigned;
				}
				else if ($data.is(':checkbox')) {
					if (type == 'Boolean') {
						value = $data.is(':checked') ? 'true' : '';
					}
					else {
						if ($data.is(':checked'))
							value = getValueOrText($data);
						else
							value = unasigned;
					}
				}
				else {
					value = $data.val();
				}
				break;
			}
			value = $.at.parseString(optional, nullable, type, value);
			if (value === unasigned) return;
			if (Object.isArray(data) && name == '@')
				data.push(value);
			else
				data[name] = value;
		});
	};
	at = function($) {
		this.$ = $;
	};
	at.fn = at.prototype;
	at.fn.$ = null;
	at.fn.template = function() {
		return $.at.template(this.$.html());
	};
	at.fn.load = function(data) {
		if (this.$.length > 0) $.at.load(this.$.get(0), data);
		return this;
	};
	at.fn.store = function(data, stringify, replacer, space) {
		if (Object.isBoolean(data)) {
			space = arguments[2];
			replacer = arguments[1];
			stringify = arguments[0];
			data = unasigned;
		}
		data = data || new Object();
		if (this.$.length > 0) $.at.store(this.$.get(0), data);
		if (!stringify) return data;
		else return JSON.stringify(data, replacer, space);
	};
	$.fn.at = function() {
		return new at(this);
	};
})(this, jQuery, at.pkgs);
