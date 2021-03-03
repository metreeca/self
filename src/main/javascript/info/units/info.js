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

require("src/main/node.js/info/units/info.css");

module.exports={

	chart: {

		pie: require("src/main/node.js/info/units/chart/pie/pie"),
		bar: require("src/main/node.js/info/units/chart/bar/bar"),
		line: require("src/main/node.js/info/units/chart/line/line"),
		bubble: require("src/main/node.js/info/units/chart/bubble/bubble")

	},

	map: {

		marker: require("src/main/node.js/info/units/map/marker/marker")

	},

	d3: require("d3"),
	L: require("leaflet")

};


function inject() { // !!! refactor
	document.body.appendChild(document.adoptNode(new DOMParser()
			.parseFromString(require("raw!src/main/node.js/info/units/base/defs.svg"), "text/xml").documentElement));
}

if ( document.readyState !== "loading" ) {
	inject();
} else {
	document.addEventListener("DOMContentLoaded", inject);
}
