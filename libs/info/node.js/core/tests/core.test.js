/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

var core=require("../units/core");


describe("the unique id generator id()", function () {

	it("returns a numeric value greater than 0", function () {
		expect(core.id()).toBeGreaterThan(0);
	});

	it("returns a unique value on each call", function () {
		expect(core.id()).not.toEqual(core.id())
	});

});

describe("the object merger copy()", function () {

	it("handles nil arguments", function () {
		expect(core.copy(null, undefined)).toEqual({});
	});

});

describe("the deep equality test equals()", function () {

	var classes=[ // equality classes

		// nils

		[undefined, null],

		// booleans

		[true, new Boolean(true)],
		[false, new Boolean(false)],

		// numbers

		[0, 0, new Number(0)],
		[123, 123, new Number(123)],


		// strings

		["", "", new String("")],
		["xyz", "xyz", new String("xyz")],

		// arrays and array-like objects

		[
			[],
			[]
		],

		[
			["a", "b", "c"],
			["a", "b", "c"],
			{ 0: "a", 1: "b", 2: "c", length: 3 }
		],

		[
			[1, 2, 3],
			[1, 2, 3],
			{ 0: 1, 1: 2, 2: 3, length: 3 }
		],

		[
			[1, ["a", "b", "c"], 3],
			[1, ["a", "b", "c"], 3],
			{ 0: 1, 1: ["a", "b", "c"], 2: 3, length: 3 }
		],

		// objects

		[
			{},
			{}
		],

		[
			{ a: 1, b: 2, c: 3 },
			{ a: 1, b: 2, c: 3 }
		],

		[
			{ a: "a", b: "b", c: "c" },
			{ a: "a", b: "b", c: "c" }
		],

		[
			{ a: "a", b: ["a", "b", "c"], c: "c" },
			{ a: "a", b: ["a", "b", "c"], c: "c" }
		],

		// functions

		[function () {}],
		[function () {}],

		// nodes

		[document.documentElement],
		[document.head],

		// node lists and html collections

		[
			document.querySelectorAll("html > *"),
			document.querySelectorAll("html > *"),
			document.documentElement.children
		],

		[
			document.querySelectorAll("head > *"),
			document.querySelectorAll("head > *"),
			document.head.children
		],

		// events

		[new CustomEvent("x")],
		[new CustomEvent("y")]

	];

	for (var sources=0; sources < classes.length; ++sources) {
		for (var targets=0; targets < classes.length; ++targets) {

			for (var source=0; source < classes[sources].length; ++source) {
				for (var target=0; target < classes[targets].length; ++target) {

					test(sources === targets, classes[sources][source], classes[targets][target]);

				}
			}
		}
	}

	function test(expected, x, y) {
		it("returns "+expected+" for ("+report(x)+", "+report(y)+")", function () {
			expect(core.equals(x, y)).toBe(expected);
		})
	}

	function report(value) {
		return core.isNode(value) || core.isNodeList(value) || core.isEvent(value) ?
				String(value) : JSON.stringify(value);
	}

});
