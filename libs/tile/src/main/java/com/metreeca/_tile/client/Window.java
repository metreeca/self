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

package com.metreeca._tile.client;


import com.metreeca._tile.client.js.JSArray;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

import java.util.Map;


public final class Window extends JavaScriptObject {

	public static native Window window() /*-{ return $wnd; }-*/;

	private static final boolean development=!GWT.getModuleBaseURL().equals(GWT.getModuleBaseForStaticFiles());


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Window() {}


	//// State /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean development() { // GWT.isProdMode() doesn't recognize SuperDevMode
		return development;
	}

	public native boolean embedded() /*-{

		return this !== this.parent

	}-*/;


	public native Window parent() /*-{

		return this.parent

	}-*/;


	public native String title() /*-{
		return this.document.title;
	}-*/;

	public native Window title(final String title) /*-{

		this.document.title=title;

		return this;
	}-*/;


	public native String location() /*-{
		return this.location.href;
	}-*/;

	public Window location(final String location) {
		return location(location, false);
	}

	public native Window location(final String location, final boolean replace) /*-{

		if ( location ) {
			if ( replace ) {
				this.location.replace(location);
			} else {
				this.location.assign(location);
			}
		}

		return this;

	}-*/;


	public native String path() /*-{
		return this.location.pathname;
	}-*/;

	public native String hash() /*-{
		return this.location.hash.substring(1);
	}-*/;


	/**
	 * @return the current focus element
	 */
	public native Tile focus() /*-{
		return [this.document.activeElement];
	}-*/;


	public Window reload() {
		return reload(false);
	}

	public native Window reload(final boolean force) /*-{ // force reloading from server

		this.location.reload(force);

		return this;

	}-*/;


	//// Cookies ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public native String cookie(final String name) /*-{

		var cookies={};

		$doc.cookie.replace(/([^=;\s]+)="?([^=;\s"]+)/g, function ($0, $1, $2) { cookies[$1]=$2 })

		return decodeURIComponent(cookies[encodeURIComponent(name)] || "");

	}-*/;

	public native Window cookie(final String name, final String value) /*-{

		if ( name ) { $doc.cookie=encodeURIComponent(name)+"="+encodeURIComponent(value);}

		return this;

	}-*/;


	//// History ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public native String state() /*-{

		return this.history.state || "";

	}-*/;


	public native Window state(final String state, final String title, final String location) /*-{

		this.history.replaceState(state, title, location);

		return this;

	}-*/;

	public native Window pushstate(final String state, final String title, final String location) /*-{

		this.history.pushState(state, title, location);

		return this;

	}-*/;


	public Window popstate(final Action<Event> action) {
		return bind("popstate", action);
	}

	public Window hashchange(final Action<Event> action) {
		return bind("hashchange", action);
	}


	//// Messages //////////////////////////////////////////////////////////////////////////////////////////////////////

	public Window message(final Action<Event> action) {
		return bind("message", action);
	}

	public native Window message(final Object message, final String target) /*-{

		return this.postMessage(message, target || "*");

	}-*/;


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Window resize(final Action<Event> action) { // !!! remove
		return bind("resize", action);
	}


	public native Window bind(final String types, final Action<Event> action) /*-{

		if ( types && action ) {
			types.split(/\s+/).forEach(function (type) {
				this.addEventListener(type, action.@com.metreeca._tile.client.Action::listener);
			}, this);
		}

		return this;

	}-*/;

	public native Window drop(final String types, final Action<Event> action) /*-{

		if ( types && action ) {
			types.split(/\s+/).forEach(function (type) {
				this.removeEventListener(type, action.@com.metreeca._tile.client.Action::listener);
			}, this);
		}

		return this;

	}-*/;


	//// Storage ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public Map<String, String> storage(final boolean persistent) {
		return Storage.create(persistent).map();
	}


	//// Dialogs////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Window open(final Action<JSArray<File>> action) {
		return open(action, false);
	}

	public native Window open(final Action<JSArray<File>> action, final boolean multiple) /*-{

		var input=this.document.createElement('input');

		input.setAttribute("type", "file");
		input.setAttribute("multiple", multiple ? "multiple" : "");

		input.addEventListener("change", $entry(function (e) {

			action.@com.metreeca._tile.client.Action::execute(*)(e.target.files);

		}));

		try { // ;(ie) won't work unless attached to the dom

			this.document.head.appendChild(input);

			input.click();

		} finally {

			this.document.head.removeChild(input);

		}

		return this;

	}-*/;

	public native Window save(final String mime, final String name, final String extension, final String data) /*-{

		// !!! support binary data

		this.$saveAs=this.$saveAs

				|| this.navigator.msSaveBlob && function (blob, name) { // ie
					return this.navigator.msSaveBlob(blob, name);
				}

				|| "download" in this.document.createElement("a") && function (blob, name) { // chrome/firefox

					var link=this.document.createElement("a");

					link.href=URL.createObjectURL(blob);
					link.download=name;

					try {
						this.document.head.appendChild(link); // ;(ff) won't start download unless attached
						link.click();
					} finally {
						this.document.head.removeChild(link);
						URL.revokeObjectURL(link.save); // !!! timeout?
					}

				}

				|| function (blob, name) { // safari

					var reader=new FileReader();

					reader.onload=(function () {
						this.location.href=reader.result
								.replace(/data:[^;]*;/, "data:octet/stream;"); // force download
					}).bind(this);

					reader.readAsDataURL(blob);

				};

		this.$saveAs(new Blob([data], { type: mime || "octet/stream" }), (name || "Untitled")+extension);

		return this;

	}-*/;


	//// Sounds ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Window tick() {
		return play(GWT.getModuleBaseURL()+"sounds/tick.wav");
	}


	public native Window play(final String url) /*-{

		if ( url ) {
			new Audio(url).play();
		}

		return this;

	}-*/;

}
