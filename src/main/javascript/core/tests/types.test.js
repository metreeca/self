/*
 * Copyright © 2013-2021 Metreeca srl. All rights reserved.
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

var core=require("src/main/node.js/core");


describe("type test", function () {

	var types={

		"undefined": { value: undefined, tests: [core.isNil] },
		"null": { value: null, tests: [core.isNil] },

		"boolean literal": { value: true, tests: [core.isBoolean] },
		"number literal": { value: 123, tests: [core.isNumber] },
		"string literal": { value: "xyz", tests: [core.isString] },

		"boolean wrapper": { value: new Boolean(true), tests: [core.isBoolean] },
		"number wrapper": { value: new Number(123), tests: [core.isNumber] },
		"string wrapper": { value: new String("xyz"), tests: [core.isString] },

		"array": { value: [1, 2, 3], tests: [core.isArray, core.isList] },
		"array-like": { value: { 1: 1, 2: 2, 3: 3, length: 3 }, tests: [core.isList, core.isObject] },

		"object": { value: {}, tests: [core.isObject] },
		"function": { value: function () {}, tests: [core.isFunction] },

		"Node": { value: window.document, tests: [core.isNode] },
		"NodeList": { value: window.document.querySelectorAll("body"), tests: [core.isList, core.isNodeList] },
		"HTMLCollection": { value: document.documentElement.children, tests: [core.isList, core.isNodeList] },
		"Event": { value: new window.CustomEvent("test"), tests: [core.isEvent] }
	};

	var tests=[]; // collect type tests

	for (var t in types) {
		if ( types.hasOwnProperty(t) ) {
			for (var i=0; i < types[t].tests.length; ++i) {

				var type=types[t];
				var test=type.tests[i];

				if ( tests.indexOf(test) < 0 ) {
					tests.push(test);
				}
			}
		}
	}

	for (var i=0; i < tests.length; ++i) { // build type-specific specs

		var test=tests[i];

		describe(/function (\w+)/.exec(test)[1]+"()", function () {

			for (var t in types) {
				if ( types.hasOwnProperty(t) ) {
					should(test, t, types[t].value, types[t].tests.indexOf(test) >= 0);
				}
			}

		});
	}

	function should(test, type, value, expected) {
		if ( expected ) {
			it("accepts "+type, function () { expect(test(value)).toBe(true) });
		} else {
			it("rejects "+type, function () { expect(test(value)).toBe(false); });
		}
	}

});

describe("type test isList()", function () {

	it("accepts empty lists", function () {
		expect(core.isList({ length: 0 })).toBe(true);
	});

	it("rejects negative lengths", function () {
		expect(core.isList({ length: -1 })).toBe(false);
	});

});
