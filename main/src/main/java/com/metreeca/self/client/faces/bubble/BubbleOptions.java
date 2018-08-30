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

package com.metreeca.self.client.faces.bubble;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;


final class BubbleOptions extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("BubbleOptions.xml") TextResource form();

	}


	private BubbleModel model=new BubbleModel();


	public BubbleOptions() {
		root(resources.form().getText())

				.change(new Action<Event>() {
					@Override public void execute(final Event e) { // !!! factor

						final Tile target=e.target();

						final String name=target.name();
						final String value=target.value();

						final Boolean checked=target.property("checked");

						if ( checked != null ) {
							option(name, checked);
						} else {
							option(name, value);
						}
					}
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BubbleOptions model(final BubbleModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void option(final String name, final boolean value) {
		root().async(model.lens(true).setSetup(model.options().set(name, value).json()));
	}

	private void option(final String name, final String value) {
		root().async(model.lens(true).setSetup(model.options().set(name, value).json()));
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private BubbleOptions render() {

		if ( model != null ) {

			final JSObject options=model.options();

			root().find("input[type=checkbox]").each(new Lambda<Tile>() {
				@Override public Tile apply(final Tile tile) {

					return tile.checked("true".equals(options.get(tile.name(), BubbleModel.defs.string(tile.name()))));

				}
			});

		}

		return this;
	}

}
