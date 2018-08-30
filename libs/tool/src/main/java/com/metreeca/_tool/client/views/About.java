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

package com.metreeca._tool.client.views;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.View;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca._tool.client.Tool.Bus;
import com.metreeca._tool.client.Tool.Setup;
import com.metreeca._tool.shared.Item;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class About extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("About.xml") TextResource html();

		@Source("About.css") TextResource skin();

	}


	public About() {
		root(resources.html().getText())

				.skin(resources.skin().getText())

				.<Overlay>as().modal(true).shading(true);
	}


	public <T extends Item<T>> About open() {

		root().<Bus<T, ?>>as().setup(new Action<Setup<T>>() {
			@Override public void execute(final Setup<T> setup) {

				root().find(".major").text(String.valueOf(setup.major()));
				root().find(".minor").text(String.valueOf(setup.minor()));
				root().find(".patch").text(String.valueOf(setup.patch()));
				root().find(".build").text(String.valueOf(setup.build()));
				root().find(".year").text(String.valueOf(setup.build()/10_000));

				root().<Overlay>as().open().align($("html"), Align.Center, Align.Center);
			}
		});

		return this;
	}

	public About close() {

		root().<Overlay>as().close(true);

		return this;
	}

}
