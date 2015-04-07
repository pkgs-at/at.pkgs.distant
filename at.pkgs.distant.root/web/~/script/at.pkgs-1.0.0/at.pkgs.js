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

/**
 * @file Core components of at.pkgs ECMAScript / JavaScript library.
 * @version 1.0.0
 * @author 鈴木 聰太郎 <sotaro.suzuki@architector.jp>
 * @copyright 2009-2014, Architector Inc., Japan
 * @namespace at.pkgs
 */

(at = this.at || {}).pkgs = new (function(_root_) {
	/**
	 * 文字列の簡易ハッシュ値を計算する.
	 * 
	 * 8bitの巡回冗長検査値(CRC-8).
	 * ユニコード文字については下位8bitのみを対象とする.
	 * 
	 * セキュリティには全く寄与しない.
	 * チェックサムやラウンドロビンなどに使用する.
	 * 
	 * @since 0.1.6
	 * @method at.pkgs.hash
	 * @param {String} source ハッシュ値計算対象文字列.
	 * @returns {Number} 0から255までのハッシュ値.
	 */
	this.hash = (function(_namespace_) {
		var table;

		table = [
			0x00, 0x85, 0x8F, 0x0A, 0x9B, 0x1E, 0x14, 0x91,
			0xB3, 0x36, 0x3C, 0xB9, 0x28, 0xAD, 0xA7, 0x22,
			0xE3, 0x66, 0x6C, 0xE9, 0x78, 0xFD, 0xF7, 0x72,
			0x50, 0xD5, 0xDF, 0x5A, 0xCB, 0x4E, 0x44, 0xC1,
			0x43, 0xC6, 0xCC, 0x49, 0xD8, 0x5D, 0x57, 0xD2,
			0xF0, 0x75, 0x7F, 0xFA, 0x6B, 0xEE, 0xE4, 0x61,
			0xA0, 0x25, 0x2F, 0xAA, 0x3B, 0xBE, 0xB4, 0x31,
			0x13, 0x96, 0x9C, 0x19, 0x88, 0x0D, 0x07, 0x82,
			0x86, 0x03, 0x09, 0x8C, 0x1D, 0x98, 0x92, 0x17,
			0x35, 0xB0, 0xBA, 0x3F, 0xAE, 0x2B, 0x21, 0xA4,
			0x65, 0xE0, 0xEA, 0x6F, 0xFE, 0x7B, 0x71, 0xF4,
			0xD6, 0x53, 0x59, 0xDC, 0x4D, 0xC8, 0xC2, 0x47,
			0xC5, 0x40, 0x4A, 0xCF, 0x5E, 0xDB, 0xD1, 0x54,
			0x76, 0xF3, 0xF9, 0x7C, 0xED, 0x68, 0x62, 0xE7,
			0x26, 0xA3, 0xA9, 0x2C, 0xBD, 0x38, 0x32, 0xB7,
			0x95, 0x10, 0x1A, 0x9F, 0x0E, 0x8B, 0x81, 0x04,
			0x89, 0x0C, 0x06, 0x83, 0x12, 0x97, 0x9D, 0x18,
			0x3A, 0xBF, 0xB5, 0x30, 0xA1, 0x24, 0x2E, 0xAB,
			0x6A, 0xEF, 0xE5, 0x60, 0xF1, 0x74, 0x7E, 0xFB,
			0xD9, 0x5C, 0x56, 0xD3, 0x42, 0xC7, 0xCD, 0x48,
			0xCA, 0x4F, 0x45, 0xC0, 0x51, 0xD4, 0xDE, 0x5B,
			0x79, 0xFC, 0xF6, 0x73, 0xE2, 0x67, 0x6D, 0xE8,
			0x29, 0xAC, 0xA6, 0x23, 0xB2, 0x37, 0x3D, 0xB8,
			0x9A, 0x1F, 0x15, 0x90, 0x01, 0x84, 0x8E, 0x0B,
			0x0F, 0x8A, 0x80, 0x05, 0x94, 0x11, 0x1B, 0x9E,
			0xBC, 0x39, 0x33, 0xB6, 0x27, 0xA2, 0xA8, 0x2D,
			0xEC, 0x69, 0x63, 0xE6, 0x77, 0xF2, 0xF8, 0x7D,
			0x5F, 0xDA, 0xD0, 0x55, 0xC4, 0x41, 0x4B, 0xCE,
			0x4C, 0xC9, 0xC3, 0x46, 0xD7, 0x52, 0x58, 0xDD,
			0xFF, 0x7A, 0x70, 0xF5, 0x64, 0xE1, 0xEB, 0x6E,
			0xAF, 0x2A, 0x20, 0xA5, 0x34, 0xB1, 0xBB, 0x3E,
			0x1C, 0x99, 0x93, 0x16, 0x87, 0x02, 0x08, 0x8D];
		return function(source) {
			var index;
			var value;

			value = 0;
			for (index = 0; index < source.length; index ++) {
				value ^= source.charCodeAt(index);
				value = table[value & 0xFF];
			}
			return value;
		};
	})(this);
	this.Object = (function(_namespace_) {
		var _class_;

		/**
		 * @since 0.1.0
		 * @class at.pkgs.Object
		 * @classdesc
		 *     継承に関する基本機能を提供するベースクラス.
		 *     チュートリアル: {@tutorial at.pkgs.Object}
		 * @param {at.pkgs.Object=} instance インスタンス.
		 */
		_class_ = function(instance) {};
		/**
		 * このクラスのサブクラスを作成する.
		 * 
		 * @since 0.1.0
		 * @memberof at.pkgs.Object.
		 * @name extend
		 * @function
		 * @param {Function} constructor コンストラクタ.
		 * @param {Object} prototype インスタンスメンバ.
		 * @param {Object=} statics クラスメンバ.
		 * @returns {Function} サブクラス.
		 */
		_class_.extend = function(constructor, prototype, statics) {
			var closure;
			var name;

			closure = new Function();
			closure.prototype = this.prototype;
			constructor.prototype = new closure();
			name = null;
			if (prototype)
				for (name in prototype) constructor.prototype[name] = prototype[name];
			constructor.prototype.self = constructor;
			constructor.prototype.parent = this.prototype;
			for (name in this) {
				if (name == 'prototype') continue;
				constructor[name] = this[name];
			}
			if (statics)
				for (name in statics) constructor[name] = statics[name];
			return constructor;
		};
		/**
		 * コンストラクタへの参照.
		 * 
		 * @since 0.1.0
		 * @readonly
		 * @memberof at.pkgs.Object#
		 * @name self
		 * @type {Function}
		 */
		_class_.prototype.self = _class_;
		/**
		 * 親クラスのプロトタイプへの参照.
		 * 
		 * @since 0.1.3
		 * @readonly
		 * @memberof at.pkgs.Object#
		 * @name parent
		 * @type {Object}
		 */
		_class_.prototype.parent = null;
		/**
		 * 指定された名称のプロパティを直接持つか取得する.
		 * 
		 * @since 0.1.5
		 * @memberof at.pkgs.Object#
		 * @function
		 * @name immediate
		 * @param {String} プロパティ名.
		 * @returns {Boolean} 持つならばtrue.
		 */
		_class_.prototype.immediate = function(name) {
			return this.self.prototype.hasOwnProperty(name);
		};
		return _class_;
	})(this);
	this.Log = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.0
			 * @class at.pkgs.Log
			 * @classdesc
			 *     ログクラス.
			 *     チュートリアル: {@tutorial at.pkgs.Log}
			 * @extends {at.pkgs.Object}
			 * @param {String} name ログ名称.
			 * @param {at.pkgs.Log=} instance インスタンス.
			 */
			function(name, instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.level = this.self.INFO;
				instance.name = name;
			}
		), { /* prototype */
			/**
			 * 出力対象となる最詳のログレベル.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @type {Number}
			 */
			level: null,
			/**
			 * ログ名称.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @type {String}
			 */
			name: null,
			/**
			 * 例外からコールスタックを取得する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {Error} error 例外.
			 * @returns {String|Boolean} コールスタックまたはfalse.
			 */
			stack: function(error) {
				var stack;

				if (!error.stack) return false; // <= IE9
				stack = error.stack.toString().match(/^.*$/gm);
				if (stack[0].match(/^Error/)) { // GC, IE10
					stack.shift();
				}
				else { // FX
					// do nothing
				}
				stack.shift();
				stack.shift();
				return stack.join('\n');
			},
			/**
			 * ログを出力する.
			 * 
			 * メッセージフォーマットパラメタを指定しない場合はログを出力せずに、ログの有効確認のみを行う.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param level ログレベル.
			 * @param {Function.Arguments=} parameters メッセージフォーマットパラメタ.
			 * @param {String=} trace コールスタック.
			 * @returns {Boolean} ログ出力が有効であればtrue.
			 */
			log: function(level, parameters, trace) {
				var message;

				if (this.level < level) return false;
				if (!this.self.append()) return false;
				if (parameters.length < 1) return true;
				message = '[' + this.self.LEVELS[level] + '] ';
				message += this.name + ': ';
				message += this.self.format.apply(this, parameters);
				try {
					if (!trace) throw new Error();
				}
				catch (error) {
					trace = this.stack(error);
				}
				return this.self.append(message, trace);
			},
			/**
			 * デバッグログを出力する.
			 * 
			 * メッセージを指定しない場合はログを出力せずに、ログの有効確認のみを行う.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {String=} template メッセージテンプレート.
			 * @param {...*} parameter テンプレートパラメタ.
			 * @returns {Boolean} ログ出力が有効であればtrue.
			 */
			debug: function(template, parameter) {
				return this.log(this.self.DEBUG, arguments);
			},
			/**
			 * 情報ログを出力する.
			 * 
			 * メッセージを指定しない場合はログを出力せずに、ログの有効確認のみを行う.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {String=} template メッセージテンプレート.
			 * @param {...*} parameter テンプレートパラメタ.
			 * @returns {Boolean} ログ出力が有効であればtrue.
			 */
			info: function(template, parameter) {
				return this.log(this.self.INFO, arguments);
			},
			/**
			 * エラーログを出力する.
			 * 
			 * メッセージを指定しない場合はログを出力せずに、ログの有効確認のみを行う.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {String=} template メッセージテンプレート.
			 * @param {...*} parameter テンプレートパラメタ.
			 * @returns {Boolean} ログ出力が有効であればtrue.
			 */
			error: function(template, parameter) {
				return this.log(this.self.ERROR, arguments);
			},
			/**
			 * 例外ログを出力し、例外を無視する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {Error} error 例外.
			 * @returns {at.pkgs.Log} ログ.
			 */
			_catch_: function(error) {
				this.log(this.self.ERROR, [ error.toString() ], error.stack);
				return this;
			},
			/**
			 * 例外ログを出力し、例外を再throwする.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {Error} error 例外.
			 * @throws {Error} パラメタの例外を再throwする.
			 */
			_throw_: function(error) {
				this._catch_(error);
				throw error;
			},
			/**
			 * 関数をラップし、例外ログの捕捉を有効にする.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log#
			 * @param {Function} callback ラップ対象の関数.
			 * @param {Boolean=} suppress 例外の再throwを行わない場合はtrueを指定.
			 * @param {Boolean=} inject ログインスタンスの注入を行う場合はtrueを指定.
			 * @returns {Function} ラップ匿名関数.
			 */
			trap: function(callback, suppress, inject) {
				var log;

				log = this;
				return function() {
					try {
						if (inject) callback.log = log;
						return callback.apply(this, arguments);
					}
					catch (error) {
						suppress ? log._catch_(error) : log._throw_(error);
					}
				};
			}
		}, { /* statics */
			/**
			 * ログレベル名称の配列.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @const
			 * @type {String[]}
			 */
			LEVELS: [ 'ERROR', 'INFO', 'DEBUG' ],
			/**
			 * エラーログレベル.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @const
			 * @type {Number}
			 */
			ERROR: 0,
			/**
			 * 情報ログレベル.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @const
			 * @type {Number}
			 */
			INFO: 1,
			/**
			 * デバッグログレベル.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @const
			 * @type {Number}
			 */
			DEBUG: 2,
			/**
			 * ログ名称をキーとするログインスタンスのマップ.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @type {Object}
			 */
			instances: {},
			/**
			 * ログ名称を指定してログを取得する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @param {String} name ログ名称.
			 * @returns {at.pkgs.Log} ログ.
			 */
			get: function(name) {
				if (!this.instances[name])
					this.instances[name] = new this(name);
				return this.instances[name];
			},
			/**
			 * ログ名称を指定してデバッグログを有効にする.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @param {String} name ログ名称.
			 * @returns {at.pkgs.Log} ログ.
			 */
			debug: function(name) {
				this.get(name).level = this.DEBUG;
				return this;
			},
			/**
			 * ログを書込む.
			 * 
			 * メッセージを指定しない場合はログを出力せずに、ログの有効確認のみを行う.
			 * このメソッドを上書きすることでログの出力先を変更可能.
			 * 標準ではconsole.logへ書き込みを行う.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @param {String=} message ローマット済のメッセージ.
			 * @param {?String=} trace コールスタック. 
			 * @returns {Boolean} ログ出力が有効であればtrue.
			 */
			append: function(message, trace) {
				if (!(_root_.console && _root_.console.log)) return false;
				if (arguments.length < 1) return true;
				_root_.console.log(message + (trace ? ('\n' + trace) : ''));
				return true;
			},
			/**
			 * メッセージをフォーマットする.
			 * 
			 * このメソッドを上書きすることでフォーマッタを変更可能.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.Log.
			 * @param {String} template ログ出力メソッドの第1引数.
			 * @param {...*} parameter ログ出力メソッドの第2引数以降.
			 * @returns フォーマット済みの文字列.
			 */
			format: function(template, parameter) {
				var parameters;

				if (!parameter) return template;
				parameters = arguments;
				return template.replace(
						/\{(\d*)?(:\w*)?\}/g,
						function(matched, index, name) {
							var value;

							index = index ? parseInt(index, 10) : 0;
							value = parameters[index + 1];
							return name ? value[name.substring(1)] : value;
						});
			}
		});
		return _class_;
	})(this);
	this.EventBinder = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.0
			 * @class at.pkgs.EventBinder
			 * @classdesc
			 *     イベントバインドクラス.
			 *     チュートリアル: {@tutorial at.pkgs.EventBinder}
			 * @extends {at.pkgs.Object}
			 * @param {at.pkgs.EventBinder=} instance インスタンス.
			 */
			function(instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.handlers = new Array();
			}
		), { /* prototype */
			/**
			 * イベントバインダで使用するイベントハンドラ.
			 * 
			 * イベント発火時の実行コンテキスト(this)にはイベントハンドラ自身が設定される.
			 * 
			 * @since 0.1.0
			 * @callback at.pkgs.EventBinder~EventHandler
			 * @param {...*} arguments イベント発火時に与えられたパラメタ.
			 * @returns {Boolean=} 後続のイベントハンドラを実行しない場合はfalse.
			 */
			/**
			 * バインドされたイベントハンドラの配列.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.EventBinder#
			 * @type {Function[]}
			 */
			handlers: null,
			/**
			 * イベントハンドラを登録する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.EventBinder#
			 * @param {at.pkgs.EventBinder~EventHandler} handler イベントハンドラ.
			 * @param {Boolean=} top ハンドラを末尾でなく先頭に追加する場合はtrueを指定.
			 * @returns {at.pkgs.EventBinder} イベントバインダ.
			 */
			bind: function(handler, top) {
				if (top) this.handlers.unshift(handler);
				else this.handlers.push(handler);
				return this;
			},
			/**
			 * イベントハンドラを抹消する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.EventBinder#
			 * @param {at.pkgs.EventBinder~EventHandler} handler イベントハンドラ.
			 * @returns {at.pkgs.EventBinder} イベントバインダ.
			 */
			unbind: function(handler) {
				var index;

				index = 0;
				while (index < this.handlers.length) {
					if (this.handlers[index] == handler)
						this.handlers.splice(index, 1);
					index ++;
				}
				return this;
			},
			/**
			 * 全てのイベントハンドラを抹消する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.EventBinder#
			 * @returns {at.pkgs.EventBinder} イベントバインダ.
			 */
			clear: function() {
				this.handlers.length = 0;
				return this;
			},
			/**
			 * イベントを発火(通知)する.
			 * 
			 * @since 0.1.0
			 * @memberof at.pkgs.EventBinder#
			 * @param {...*} arguments イベントハンドラに渡す引数.
			 * @returns {at.pkgs.EventBinder} イベントバインダ.
			 */
			fire: function() {
				var handlers;
				var index;
				var handler;

				handlers = this.handlers.concat();
				for (index = 0; index < handlers.length; index ++) {
					handler = handlers[index];
					if (handler.apply(handler, arguments) === false) break;
				}
				return this;
			}
		});
		return _class_;
	})(this);
	this.ObservableValue = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.EventBinder.extend((
			/**
			 * @since 0.1.5
			 * @class at.pkgs.ObservableValue
			 * @classdesc
			 *     値変更通知クラス.
			 *     チュートリアル: {@tutorial at.pkgs.EventBinder}
			 *     このクラスのイベントハンドラは第1引数に変更前の値、第2引数に変更後の値を受け取る.
			 * @extends {at.pkgs.EventBinder}
			 * @param {*=} value 初期値.
			 * @param {at.pkgs.ObservableValue=} instance インスタンス.
			 */
			function(value, instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.value = value;
			}
		), {
			/**
			 * 現在値.
			 * 
			 * @since 0.1.5
			 * @memberof at.pkgs.ObservableValue#
			 * @type {*}
			 */
			value: null,
			/**
			 * 値を設定する.
			 * 
			 * @since 0.1.5
			 * @memberof at.pkgs.ObservableValue#
			 * @param {*} value 新しい値.
			 */
			set: function(value) {
				var previous;

				previous = this.value;
				this.value = value;
				if (this.changed(previous, value))
					this.fire(previous, value);
			},
			/**
			 * 値を取得する.
			 * 
			 * @since 0.1.5
			 * @memberof at.pkgs.ObservableValue#
			 * @returns {*} 現在の値.
			 */
			get: function() {
				return this.value;
			},
			/**
			 * 値を比較する.
			 * 
			 * 継承するか、インスタンスの本メソッドを上書きすることで比較方法を変更可能.
			 * 
			 * @since 0.1.5
			 * @memberof at.pkgs.ObservableValue#
			 * @param {*} previous 以前の値.
			 * @param {*} current 現在の値.
			 * @returns {Boolean} 変化した場合はtrue;
			 */
			changed: function(previous, current) {
				return previous !== current;
			}
		});
		return _class_;
	})(this);
	this.OnceUpon = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.6
			 * @class at.pkgs.OnceUpon
			 * @classdesc
			 *     非同期処理結果キャッシュクラス.
			 *     
			 *     非同期処理を1回だけ実行し、複数回の呼出で初回実行時の結果を返す.
			 *     
			 *     チュートリアル: {@tutorial at.pkgs.OnceUpon}
			 * @extends {at.pkgs.Object}
			 * @param {at.pkgs.OnceUpon~Operation} operation 実行する処理.
			 * @param {at.pkgs.OnceUpon=} instance インスタンス.
			 */
			function(operation, instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.operation = operation;
				instance.binder = new _namespace_.EventBinder();
			}
		), { /* prototype */
			/**
			 * 非同期処理結果キャッシュクラスで実行する処理.
			 * 
			 * 処理の実行コンテキスト(this)にはキャッシュクラスが設定される.
			 * 実行結果はthis.set(value)を使用して設定する.
			 * 実行結果を設定しない場合は2度と呼び出されず、また結果取得コールバックも呼び出されない.
			 * 
			 * @since 0.1.6
			 * @callback at.pkgs.OnceUpon~Operation
			 * @returns {Object=} キャンセルなどを行う場合に使用するオブジェクト.
			 */
			/**
			 * 非同期実行結果取得コールバック.
			 * 
			 * 結果取得時の実行コンテキスト(this)にはコールバック自身が設定される.
			 * 
			 * @since 0.1.6
			 * @callback at.pkgs.OnceUpon~Callback
			 * @param {*} value 実行結果.
			 */
			/**
			 * 実行する処理.
			 * 
			 * 実行済みの場合はnull.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @type {Function}
			 */
			operation: null,
			/**
			 * 結果取得イベントバインダ.
			 * 
			 * 結果取得済みの場合はnull.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @type {at.pkgs.EventBinder}
			 */
			binder: null,
			/**
			 * 処理中の処理オブジェクト(処理開始時の返値).
			 * 
			 * 結果取得後のコールバック呼出し後はnull.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @type {*}
			 */
			process: null,
			/**
			 * 処理結果.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @type {*}
			 */
			value: null,
			/**
			 * 処理結果を設定する.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @param {*} value 処理結果.
			 */
			set: function(value) {
				var binder;

				if (!this.binder) return;
				this.value = value;
				binder = this.binder;
				this.binder = null;
				binder.fire(this.value);
				this.process = null;
			},
			/**
			 * 処理結果を取得する.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceUpon#
			 * @param {at.pkgs.OnceUpon~Callback=} callback コールバック.
			 * @returns {*} nullまたは実行結果.
			 */
			get: function(callback) {
				var operation;

				if (this.binder) {
					if (callback) this.binder.bind(callback);
					if (this.operation) {
						operation = this.operation;
						this.operation = null;
						this.process = operation.apply(this);
					}
				}
				else {
					if (callback) callback.call(callback, this.value);
				}
				return this.value;
			}
		});
		return _class_;
	})(this);
	this.OnceAfterAll = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.6
			 * @class at.pkgs.OnceAfterAll
			 * @classdesc
			 *     個別処理結果待ち合わせクラス.
			 *     
			 *     複数の個別処理の全数完了後に1回だけ処理を実行する.
			 *     
			 *     チュートリアル: {@tutorial at.pkgs.OnceAfterAll}
			 * @extends {at.pkgs.Object}
			 * @param {at.pkgs.OnceAfterAll=} instance インスタンス.
			 */
			function(instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.binder = new _namespace_.EventBinder();
				instance.jobs = 0;
				instance.succeed = true;
			}
		), { /* prototype */
			/**
			 * 個別処理結果待ち合わせクラスで完了後に実行する処理.
			 * 
			 * @since 0.1.6
			 * @callback at.pkgs.OnceAfterAll~Operation
			 * @param {Boolean} 処理結果フラグ.
			 */
			/**
			 * 処理完了イベントバインダ.
			 * 
			 * 実行済みの場合はnull.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 * @type {at.pkgs.EventBinder}
			 */			
			binder: null,
			/**
			 * 実行中の個別処理の数.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 * @type {Number}
			 */
			jobs: null,
			/**
			 * 処理結果フラグ.
			 * 
			 * 失敗を報告した個別処理が一つでもあればfalse.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 * @type {Function}
			 */
			succeed: null,
			/**
			 * 完了後の処理を追加する.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 * @param {at.pkgs.OnceAfterAll~Operation} operation 完了後に実行する処理.
			 */
			bind: function(handler) {
				this.binder.bind(handler);
			},
			/**
			 * 個別処理結果の開始を通知する.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 */
			enter: function() {
				if (this.binder) this.jobs ++;
			},
			/**
			 * 個別処理結果の完了を通知する.
			 * 
			 * @since 0.1.6
			 * @memberof at.pkgs.OnceAfterAll#
			 * @param {Boolean=} succeed falseを指定すると処理失敗となる.
			 */
			leave: function(succeed) {
				var binder;

				if (!this.binder) return;
				if (succeed === false) this.succeed = false;
				this.jobs --;
				if (this.jobs > 0) return;
				binder = this.binder;
				this.binder = null;
				binder.fire(this.succeed);
			}
		});
		return _class_;
	})(this);
	/**
	 * @since 0.1.4
	 * @namespace at.pkgs.escape
	 */
	this.escape = new (function() {
		this.empty = (function(_namespace_) {
			var _function_;

			/**
			 * null及びundefinedを空文字にエスケープする.
			 * 
			 * @since 0.1.4
			 * @method at.pkgs.escape.empty
			 * @param {*} source エスケープ対象.
			 * @returns {String} エスケープ済文字列.
			 */
			_function_ = function(source) {
				return (source === null || source === void 0) ? '' : source.toString();
			};
			return _function_;
		})(this);
		this.html = (function(_namespace_) {
			var _function_;

			/**
			 * HTML内のテキストとしてエスケープする.
			 * 
			 * @since 0.1.4
			 * @method at.pkgs.escape.html
			 * @param {*} source エスケープ対象.
			 * @returns {String} エスケープ済文字列.
			 */
			_function_ = function(source) {
				return _namespace_.empty(source).replace(/[&<>"']/g, function(matched) {
					switch (matched) {
					case '&' :
						return '&amp;';
					case '<' :
						return '&lt;';
					case '>' :
						return '&gt;';
					case '"' :
						return '&quot;';
					case "'" :
						return '&#039;';
					}
				});
			};
			return _function_;
		})(this);
	})();
	this.Template = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.4
			 * @class at.pkgs.Template
			 * @classdesc
			 *     テンプレートクラス.
			 *     チュートリアル: {@tutorial at.pkgs.Template}
			 * @extends {at.pkgs.Object}
			 * @param {String} source テンプレートソース.
			 * @param {at.pkgs.TemplateEngine} engine テンプレートエンジン.
			 * @param {at.pkgs.Template=} instance インスタンス.
			 */
			function(source, engine, instance) {
				instance = instance || this;
				this.parent.self(instance);
				instance.engine = engine;
				instance.texts = new Array();
				instance.prepare(source);
			}
		), { /* prototype */
			/**
			 * テンプレートエンジン.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @type {at.pkgs.TemplateEngine}
			 */
			engine: null,
			/**
			 * テキストノードの配列.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @type {String[]}
			 */
			texts: null,
			/**
			 * テンプレート実行スクリプト.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @type {String}
			 */
			code: null,
			/**
			 * テンプレートソースを処理し、実行可能にする.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @param {String} テンプレートソース.
			 */
			prepare: function(source) {
				var self;
				var codes;
				var last;

				self = this;
				codes = new Array();
				if (source === null || source === void 0 || !source.replace) throw new Error('invalid template source');
				last = 0;
				source.replace(this.engine.pattern, function(matched, code, code_, escape, escape_, raw, raw_, offset) {
					var expression;

					if (last != offset) {
						codes.push('_text_ += _this_.text(' + self.texts.length + ');');
						self.texts.push(source.slice(last, offset));
					}
					last = offset + matched.length;
					expression = code || escape || raw;
					if (code)
						codes.push(expression);
					if (escape)
						codes.push('_text_ += _this_.escape(' + expression + ');');
					if (raw)
						codes.push('_text_ += _this_.raw(' + expression + ');');
					if (code_ || escape_ || raw_) {
						if (source.charAt(last) == '\r') last ++;
						if (source.charAt(last) == '\n') last ++;
					}
					return matched;
				});
				this.code = codes.join('\n');
			},
			/**
			 * 指定されたインデックスのテキストを取得する.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @param {Number} index テキストノードインデックス.
			 * @returns {String} テキスト.
			 */
			text: function(index) {
				return this.texts[index];
			},
			/**
			 * 文字列をエスケープする.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @param {*} source エスケープ対象.
			 * @returns {String} エスケープ済文字列.
			 */
			escape: function(source) {
				return this.engine.escape(source);
			},
			/**
			 * 文字列化する.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @param {*} source 対象.
			 * @returns {String} 文字列.
			 */
			raw: function(source) {
				return this.engine.raw(source);
			},
			/**
			 * テンプレートを実行する.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.Template#
			 * @param {Object} _attributes_ テンプレートパラメタ.
			 * @returns {String} 出力文字列.
			 */
			render: function(_attributes_) {
				_attributes_ = _attributes_ || {};
				_attributes_._this_ = this;
				_attributes_._text_ = '';
				with (_attributes_) {
					eval(_this_.code);
				}
				return _attributes_._text_;
			}
		});
		return _class_;
	})(this);
	this.TemplateEngine = (function(_namespace_) {
		var _class_;

		_class_ = _namespace_.Object.extend((
			/**
			 * @since 0.1.4
			 * @class at.pkgs.TemplateEngine
			 * @classdesc
			 *     テンプレートエンジンクラス.
			 *     チュートリアル: {@tutorial at.pkgs.Template}
			 * @extends {at.pkgs.Object}
			 * @param {at.pkgs.TemplateEngine=} instance インスタンス.
			 */
			function(instance) {
				instance = instance || this;
				this.parent.self(instance);
			}
		), { /* prototype */
			/**
			 * @since 0.1.4
			 * @typedef {Function} at.pkgs.TemplateEngine~Renderer
			 * @param {Object} data テンプレートパラメタ.
			 * @returns {String} 出力文字列.
			 */
			/**
			 * テンプレートデリミタパターン.
			 * 
			 * `/スクリプトブロックパターン|エスケープ出力ブロックパターン|直接出力ブロックパターン|$/g`形式
			 * 
			 * インスタンスの本メソッドを上書きすることでデリミタを変更可能.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.TemplateEngine#
			 * @type {RegExp}
			 */
			pattern: /\{%([\s\S]*?)(%?)%\}|\{=([\s\S]*?)(=?)=\}|\{@([\s\S]*?)(@?)@\}|$/g,
			/**
			 * エスケープ関数.
			 * 
			 * インスタンスの本メソッドを上書きすることでエスケープ処理を変更可能.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.TemplateEngine#
			 * @method
			 * @param {*} source エスケープ対象.
			 * @returns {String} エスケープ済文字列.
			 */
			escape: _namespace_.escape.html,
			/**
			 * 直接出力関数.
			 * 
			 * インスタンスの本メソッドを上書きすることで直接出力処理を変更可能.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.TemplateEngine#
			 * @method
			 * @param {*} source 対象.
			 * @returns {String} 文字列.
			 */
			raw: _namespace_.escape.empty,
			/**
			 * テンプレートソースを処理しテンプレート実行関数を生成する.
			 * 
			 * @since 0.1.4
			 * @memberof at.pkgs.TemplateEngine#
			 * @param {String} source テンプレートソース.
			 * @returns {at.pkgs.TemplateEngine~Renderer} テンプレート実行関数.
			 */
			prepare: function(source) {
				var template;
				var renderer;

				template = new _namespace_.Template(source, this);
				renderer = function(data) {
					return template.render(data);
				};
				renderer.template = template;
				return renderer;
			}
		});
		/**
		 * デフォルトテンプレートエンジンインスタンス.
		 * 
		 * 本インスタンスの各メソッドを上書きすることでat.pkgs.template()の振舞いを変更可能.
		 * 
		 * @since 0.1.4
		 * @member at.pkgs.TemplateEngine.instance
		 * @type {at.pkgs.TemplateEngine}
		 */
		_class_.instance = new _class_();
		return _class_;
	})(this);
	this.template = (function(_namespace_) {
		var _function_;

		/**
		 * デフォルトテンプレートエンジンを使用してテンプレートを処理し、テンプレート実行関数を生成する.
		 * 
		 * チュートリアル: {@tutorial at.pkgs.Template}
		 * 
		 * @since 0.1.4
		 * @method at.pkgs.template
		 * @param {String} source テンプレートソース.
		 * @returns {at.pkgs.TemplateEngine~Renderer} テンプレート実行関数.
		 */
		_function_ = function(source) {
			return _namespace_.TemplateEngine.instance.prepare(source);
		};
		return _function_;
	})(this);
})(this);
