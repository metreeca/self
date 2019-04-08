/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca/Self.
 * If not, see <http://www.gnu.org/licenses/>.
 */

(function () { // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith#Polyfill

	if ( !String.prototype.endsWith ) {
		(function () {
			'use strict'; // needed to support `apply`/`call` with `undefined`/`null`
			var defineProperty=(function () {
				// IE 8 only supports `Object.defineProperty` on DOM elements
				try {
					var object={};
					var $defineProperty=Object.defineProperty;
					var result=$defineProperty(object, object, object) && $defineProperty;
				} catch ( error ) {}
				return result;
			}());
			var toString={}.toString;
			var endsWith=function (search) {
				if ( this == null ) {
					throw TypeError();
				}
				var string=String(this);
				if ( search && toString.call(search) == '[object RegExp]' ) {
					throw TypeError();
				}
				var stringLength=string.length;
				var searchString=String(search);
				var searchLength=searchString.length;
				var pos=stringLength;
				if ( arguments.length > 1 ) {
					var position=arguments[1];
					if ( position !== undefined ) {
						// `ToInteger`
						pos=position ? Number(position) : 0;
						if ( pos != pos ) { // better `isNaN`
							pos=0;
						}
					}
				}
				var end=Math.min(Math.max(pos, 0), stringLength);
				var start=end-searchLength;
				if ( start < 0 ) {
					return false;
				}
				var index=-1;
				while ( ++index < searchLength ) {
					if ( string.charCodeAt(start+index) != searchString.charCodeAt(index) ) {
						return false;
					}
				}
				return true;
			};
			if ( defineProperty ) {
				defineProperty(String.prototype, 'endsWith', {
					'value': endsWith,
					'configurable': true,
					'writable': true
				});
			} else {
				String.prototype.endsWith=endsWith;
			}
		}());
	}

})();
