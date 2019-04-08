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

var core=require("metreeca-core");

var d3=require("d3");

module.exports=function tool(defaults, update, render) {

	update=update || function () {}; // called to update the setup as tool.update(setup, tool)
	render=render || function () {}; // called to handle a D3 selection as tool.render(nodes, tool)

	var setup={}; // the user provided setup
	var queue=[]; // pending scheduler timeouts


	function merge(target /*, source... */) { // merge sources into target

		for (var i=1; i < arguments.length; ++i) {

			var source=arguments[i];

			if ( core.isObject(source) ) {

				target=core.isObject(target) ? target : {};

				for (var p in source) {
					if ( source.hasOwnProperty(p) ) { target[p]=merge(target[p], source[p]) }
				}

			} else {
				target=source; // don't clone arrays as they may be extended (eg by jquery/zepto)
			}
		}

		return target;
	}


	return (function self(target) {

		if ( target === undefined ) { // get the setup

			return merge({}, setup);

		} else if ( target === null ) { // reset to defaults

			return update.call(self, merge({}, defaults || {}, (setup={})), self) || self;

		} else if ( core.isObject(target) || target === self ) { // merge setup

			return update.call(self, merge({}, defaults || {}, merge(setup, target)), self) || self;

		} else if ( target instanceof d3.selection ) { // render in D3 selection

			return render.call(self, target, self) || self;

		} else if ( core.isNode(target) ) { // render in node

			return render.call(self, d3.select(target), self) || self;

		} else if ( core.isNodeList(target) ) { // render in nodeList

			return render.call(self, d3.selectAll(target), self) || self;

		} else if ( core.isFunction(target) ) { // execute function as tool.target()

			return target.call(self, self) || self;

		} else if ( core.isNumber(target) ) { // schedule calls
			if ( target > 0 ) { // return a new scheduler

				function scheduler() { // (target...)

					if ( arguments.length ) { // record targets

						for (var i=0; i < arguments.length; ++i) {
							scheduler.targets.push(arguments[i]);
						}

					} else { // no arguments > process targets

						queue.splice(queue.indexOf(scheduler.timeout, 1)); // no longer pending > remove
						scheduler.targets.forEach(self); // process targets

					}

					return scheduler;
				}

				scheduler.targets=[];
				scheduler.timeout=setTimeout(scheduler, target);

				queue.push(scheduler.timeout);

				return scheduler;

			} else { // clear pending schedulers

				while ( queue.length ) { clearTimeout(queue.shift()); }

				return self;
			}

		} else {

			return self;

		}

	})({});

};
