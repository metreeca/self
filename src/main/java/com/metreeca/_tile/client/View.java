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

package com.metreeca._tile.client;

import static com.metreeca._tile.client.Tile.$;


@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
public abstract class View {

	public enum Orientation {Horizontal, Vertical}


	private Tile root=$();


	public <T extends Plugin> T as() {
		return $(this).as();
	}


	protected final Tile root() {
		return root;
	}

	protected final Tile root(final String root) {
		return root($(root));
	}

	protected final Tile root(final View root) {
		return root($(root));
	}

	protected final Tile root(final Tile root) {
		return this.root=tag($(root))
				.set((Class<View>)getClass(), this)
				.replace(this.root.set(getClass(), null));
	}


	private Tile tag(final Tile tile) {

		for (Class<?> type=getClass(); !type.equals(Object.class); type=type.getSuperclass()) {
			tile.is(hyphenate(name(type.getName())), true);
		}

		return tile;
	}


	private native String name(final String name) /*-{
		return (name.match(/([_a-zA-Z]\w+?)(View)?$/) || [])[1]; // ignore anonymous inner classes
	}-*/;

	private native String hyphenate(final String name) /*-{
		return name && name.replace(/(.)([A-Z][a-z]|[A-Z]$)/g, "$1-$2").toLowerCase();
	}-*/;

	private native String camelize(final String name) /*-{
		return name && name.replace(/-([a-zA-Z])/g, function (s, x) {return x.toUpperCase(); });
	}-*/;

}
