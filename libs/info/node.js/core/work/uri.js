/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

var core=require("metreeca-core");


module.exports=core.type({

	new: function URI(specs) {

		specs=core.isNil(specs) ? {} : core.isString(specs) ? parse(specs) : specs;

		this.scheme=specs.scheme || "";
		this.authority=specs.authority || "";
		this.path=specs.path || "";
		this.query=specs.query || "";
		this.fragment=specs.fragment || "";

		this.specs=this.scheme+this.authority+this.path+this.query+this.fragment; // !!! normalize path for comparison

	},


	resolve: function (uri) { // see http://tools.ietf.org/html/rfc3986#section-5.4
		if ( uri instanceof this.constructor ) {

			return new this.constructor({

				scheme: uri.scheme ? uri.scheme : this.scheme,

				authority: uri.scheme || uri.authority ? uri.authority : this.authority,

				query: uri.scheme || uri.authority || uri.path || uri.query ? uri.query : this.query,

				path: clean(uri.scheme || uri.authority ? uri.path // !!! review/refactor
						: !uri.path ? this.path
								: uri.path.charAt(0) === "/" ? uri.path
										: this.path ? this.path.substr(0, this.path.lastIndexOf("/")+1)+uri.path
												: "/"+uri.path),

				fragment: uri.fragment

			});

		} else {
			return this.resolve(new this.constructor(uri));
		}
	},

	relativize: function (uri) { // see http://tools.ietf.org/html/rfc3986#section-5.4
		if ( uri instanceof this.constructor ) {

			var bpath=clean(this.path);
			var bslash=bpath.lastIndexOf("/");
			var bhead=bslash >= 0 ? bpath.substring(0, bslash+1) : bpath;

			var upath=clean(uri.path);
			var utail=upath === bpath ? "" : upath.startsWith(bhead) ? upath.substring(bhead.length) : upath;

			return uri.scheme !== this.scheme ? uri : new this.constructor({
				authority: uri.authority !== this.authority ? uri.authority : "",
				path: utail,
				query: uri.query !== this.query ? uri.query : "",
				fragment: uri.fragment
			});

		} else {
			return this.relativize(new this.constructor(uri));
		}
	},


	toString: function () {
		return this.specs;
	}

});


function parse(specs) { // see <http://tools.ietf.org/html/rfc3986#appendix-B>

	var match=specs.match(/^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?$/);

	if ( !match ) {
		throw new URIError("malformed URI ["+specs+"]");
	}

	return {

		scheme: match[1],
		authority: match[3],
		path: match[5],
		query: match[6],
		fragment: match[8]

	};
}

function clean(path) {
	return path.split("/").reduce(function (value, item, index, array) {

		if ( item === ".." ) {
			value.pop();
			if ( value.length === 0 || index === array.length-1 ) { value.push(""); }
		} else if ( item !== "." ) {
			value.push(item);
		} else if ( index === array.length-1 ) {
			value.push("");
		}

		return value;

	}, []).join("/");
}
