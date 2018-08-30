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

package com.metreeca._tile.client.js;

import com.google.gwt.core.client.JavaScriptObject;


public abstract class JSDate extends JavaScriptObject {

	public static native JSDate now() /*-{
		return new Date(Date.now());
	}-*/;

	public static native JSDate date(final String date) /*-{
		return new Date(date ? Date.parse(date) : new Date(Date.now()));
	}-*/;


	protected JSDate() {}


	public final native int year() /*-{ return this.getFullYear(); }-*/;

	public final native int month() /*-{ return this.getMonth(); }-*/;

	public final native int date() /*-{ return this.getDate(); }-*/;

}
