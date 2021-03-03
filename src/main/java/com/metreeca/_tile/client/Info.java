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

package com.metreeca._tile.client;

import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.js.JSObject;

import com.google.gwt.core.client.*;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;


/**
 * Infographics API
 */
public final class Info extends JavaScriptObject {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Info.js") TextResource info();

	}


	public static native Info create(final String type, final JSObject defs) /*-{
		return {
			type: type, // tool dotted name relative to info
			defs: defs, // default setup
			tool: {}  // to be set to the actual tool during the first call to draw()
		};
	}-*/;


	protected Info() {}


	public native String type() /*-{
		return this.type;
	}-*/;


	public native JSObject setup() /*-{

		return typeof this.tool === "function" ? this.tool() : Object.create(this.tool);

	}-*/;

	public native Info setup(final JSObject setup) /*-{

		if ( typeof this.tool === "function" ) {

			this.tool(setup);

		} else {

			for (var p in setup) { if ( setup.hasOwnProperty(p) ) { this.tool[p]=setup[p]; } }

		}

		return this;

	}-*/;


	public native Info controls(final Handler<Tile> handler) /*-{

		var listener=(handler === null) ? null : $entry(function (d, i) {
			return handler.@com.metreeca._tile.client.Info.Handler::handle(Lcom/metreeca/_tile/client/js/JSObject;I)(d, i);
		});

		if ( typeof this.tool === "function" ) {

			this.tool({ controls: listener });

		} else {

			this.tool.controls=listener;

		}

		return this;
	}-*/;


	public Info detail(final Handler<Tile> handler) {
		return handler("detail", handler);
	}

	public Info action(final Handler<Boolean> handler) {
		return handler("action", handler);
	}

	public native Info handler(final String event, final Handler<?> handler) /*-{

		var listener=(handler === null) ? null : $entry(function (d, i) {
			return handler.@com.metreeca._tile.client.Info.Handler::handle(Lcom/metreeca/_tile/client/js/JSObject;I)(
					d !== undefined ? d : {}, i !== undefined ? i : 0
			);
		});

		if ( typeof this.tool === "function" ) {

			var setup={};

			setup[event]=listener;

			this.tool(setup);

		} else {

			this.tool[event]=listener;

		}

		return this;

	}-*/;


	public Info draw(final Tile tile, final JSArray<JSObject> data) {

		GWT.runAsync(new RunAsyncCallback() {

			private native boolean isLoaded() /*-{ return $wnd.info; }-*/;

			@Override public void onSuccess() {

				if ( !isLoaded() ) {
					ScriptInjector
							.fromString(resources.info().getText())
							.setWindow(ScriptInjector.TOP_WINDOW)
							.inject();
				}

				_draw(tile, data);

			}

			@Override public void onFailure(final Throwable reason) {

				final GWT.UncaughtExceptionHandler handler=GWT.getUncaughtExceptionHandler();

				if ( handler != null ) {
					handler.onUncaughtException(reason);
				}
			}

		});

		return this;

	}


	private native void _draw(final Tile tile, final JSArray<JSObject> data) /*-{
		if ( tile && data ) {

			var target=$wnd.info.d3.selectAll(tile);

			target.data(target.map(function () { return data; }));

			if ( !(typeof this.tool === "function") ) { // to be initialized
				this.tool=this.type
						.match(/[^\.]+/g)// split into path components
						.reduce(function (object, property) { return object[property]; }, $wnd.info) // traverse path
						(this.defs) // create
						(this.tool); // configure
			}

			this.tool(target);

		}
	}-*/;


	public static interface Handler<T> {

		public T handle(final JSObject data, final int index);

	}

}
