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

var tool=require("../../units/base/tool");

describe("tool", function () {

	var test=tool({}, function () { this.updated="updated!" }, function () {});

	describe("()", function () {

		it("returns the user-defined setup", function () {
			expect(test()).toEqual({});
			expect(test({ defined: "defined!" })()).toEqual({ defined: "defined!" });
		});

	});

	describe("(null)", function () {

		it("resets the user-defined setup", function () {
			expect(test({ defined: "defined!" })(null)()).toEqual({});
		});

	});

});
