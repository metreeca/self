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

// A D3.js plugin for overlaying content to a selection

var base=require("../base");

var d3=require("d3");

module.exports=function overlay(selection, generator) {

	selection=base.nodes(selection);
	generator=base.lambda(generator);

	if ( selection && generator ) {
		selection.each(function (datum, index) {

			var overlay=base.nodes(generator.call(this, datum, index));
			var target=base.nodes(this); // !!! if overlay is html and this is svg search the enclosing html element

			// make sure the target is a positioning container

			if ( target.style("position") === "static" ) {
				target.style("position", "relative");
			}

			// position overlay according to its css top/bottom/left/right properties

			overlay.style("position", "absolute");

			target.append(function () { return overlay.node(); });

		});
	}

	return selection;

};
