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

package com.metreeca._tile.client.js;

import com.google.gwt.core.client.JavaScriptObject;


public abstract class JSObject extends JavaScriptObject { // !!! refactor

	public static native JSObject object() /*-{ return {}; }-*/;

	public static native JSObject object(final String json) /*-{ return $wnd.JSON.parse(json); }-*/;


	protected JSObject() {}


	public final native JSArray<String> keys() /*-{
		return Object.keys(this);
	}-*/;


	public final native float number(final String property) /*-{
		return this[property];
	}-*/;

	public final native int integer(final String property) /*-{
		return this[property] || 0;
	}-*/;

	public final String string(final String property) { return get(property, (String)null); }


	public final native boolean get(final String property, final boolean fallback) /*-{
		return this[property] === undefined ? fallback : this[property];
	}-*/;

	public final native JSObject set(final String property, final boolean value) /*-{

		(property || "").match(/[^\.]+/g).reduce(function (o, p, i, a) {

			return i+1 === a.length ? (o[p]=value) : o[p] === undefined ? (o[p]={}) : o[p];

		}, this);

		return this;
	}-*/;


	public final native float get(final String property, final float fallback) /*-{
		return this[property] === undefined ? fallback : this[property];
	}-*/;

	public final native JSObject set(final String property, final float value) /*-{

		(property || "").match(/[^\.]+/g).reduce(function (o, p, i, a) {

			return i+1 === a.length ? (o[p]=value) : o[p] === undefined ? (o[p]={}) : o[p];

		}, this);

		return this;
	}-*/;


	public final native int get(final String property, final int fallback) /*-{
		return this[property] === undefined ? fallback : this[property];
	}-*/;

	public final native JSObject set(final String property, final int value) /*-{

		(property || "").match(/[^\.]+/g).reduce(function (o, p, i, a) {

			return i+1 === a.length ? (o[p]=value) : o[p] === undefined ? (o[p]={}) : o[p];

		}, this);

		return this;
	}-*/;


	public final native String get(final String property, final String fallback) /*-{

		var value=(property || "").match(/[^\.]+/g).reduce(function (o, p) { return o && o[p] }, this);

		return value === undefined || value === null ? fallback : String(value);
	}-*/;

	public final native JSObject set(final String property, final String value) /*-{

		(property || "").match(/[^\.]+/g).reduce(function (o, p, i, a) {

			return i+1 === a.length ? (o[p]=value) : o[p] === undefined ? (o[p]={}) : o[p];

		}, this);

		return this;

	}-*/;


	public final native JSObject get(final String property, final JavaScriptObject fallback) /*-{

		var value=(property || "").match(/[^\.]+/g).reduce(function (o, p) { return o && o[p] }, this);

		return value === undefined || value === null ? fallback : value;

	}-*/;

	public final native JSObject set(final String property, final JavaScriptObject value) /*-{

		(property || "").match(/[^\.]+/g).reduce(function (o, p, i, a) {

			return i+1 === a.length ? (o[p]=value) : o[p] === undefined ? (o[p]={}) : o[p];

		}, this);

		return this;

	}-*/;


	public final native String json() /*-{
		return $wnd.JSON.stringify(this);
	}-*/;

	public final native String json(final boolean indent) /*-{
		return $wnd.JSON.stringify(this, undefined, indent ? 2 : 0);
	}-*/;

}
