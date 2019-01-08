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

var tool=require("../base/tool");

module.exports=function area(setup) {
	return tool(
			setup,

			function update(setup) {

				this.left=Math.max(0, Number(setup.left) || 0); // left margin { >= 0 } ([0..1] >> % of target area)
				this.right=Math.max(0, Number(setup.right) || 0); // right margin { >= 0 } ([0..1] >> % of target area)
				this.top=Math.max(0, Number(setup.top) || 0); // top margin { >= 0 } ([0..1] >> % of target area)
				this.bottom=Math.max(0, Number(setup.bottom) || 0); // bottom margin { >= 0 } ([0..1] >> % of target area)

				this.maxWidth=Math.max(0, Number(setup.maxWidth) || 0); // maximum width { >= 0 } (0 >> no limit)
				this.maxHeight=Math.max(0, Number(setup.maxHeight) || 0); // maximum height { >= 0} (0 >> no limit)

				this.aspect=Math.max(0, Number(setup.aspect) || 0); // aspect ratio { >= 0 } (0 >> same as target area)
				this.exact=Boolean(setup.exact) || false; // exact pixel alignment (true >> align area to fractional values)

			},

			function render(nodes) {

				var node=nodes.node() || {};
				var area={};

				var outerWidth=node.clientWidth || 0;
				var outerHeight=node.clientHeight || 0;

				var leftMargin=value(this.left, outerWidth);
				var rightMargin=value(this.right, outerWidth);
				var topMargin=value(this.top, outerHeight);
				var bottomMargin=value(this.bottom, outerHeight);

				var innerWidth=clip(outerWidth-leftMargin-rightMargin, 0, this.maxWidth || NaN);
				var innerHeight=clip(outerHeight-topMargin-bottomMargin, 0, this.maxHeight || NaN);

				var actualWidth=(this.aspect === 0) ? innerWidth : Math.min(innerWidth, innerWidth*this.aspect);
				var actualHeight=(this.aspect === 0) ? innerHeight : Math.min(innerHeight, innerHeight/this.aspect);

				area.portWidth=outerWidth;
				area.portHeight=outerHeight;

				area.width=exact(actualWidth);
				area.height=exact(actualHeight);

				area.left=exact(leftMargin+(innerWidth-area.width)/2); // center in the inner area
				area.top=exact(topMargin+(innerHeight-area.height)/2); // center in the inner area

				area.right=area.portWidth-(area.left+area.width); // no crisping
				area.bottom=area.portHeight-(area.top+area.height); // no crisping

				area.aspect=area.width/area.height; // +Infinity allowed

				return area;


				function value(value, range) {
					return value > 1 ? value : value*range;
				}

				function exact(value) {
					return this.exact ? value : Math.floor(value);
				}

				function clip(value, lower, upper) {
					return !isNaN(lower) && value < lower ? lower : !isNaN(upper) && value > upper ? upper : value;
				}

			});
};
