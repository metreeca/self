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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Editable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class Input extends View { // !!! factor as plugin

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Input.css") TextResource skin();

	}


	private boolean immediate; // if true the change event is fired automatically after the user stops typing

	private final Tile label;
	private final Tile text;
	private final Tile clear;


	private String value=""; // the last reported value


	public Input() {
		this("text");
	}

	public Input(final String type) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		root($("<span/>")

				.skin(resources.skin().getText())

				.append(label=$("<label/>").visible(false)

						.is("fa fa-search", true))

				.append(text=$("<input/>")

						.attribute("type", type)

						.focus(new Action<Event>() {
							@Override public native void execute(final Event e) /*-{

								e.target.selectionStart=0;
								e.target.selectionEnd=e.target.value.length;

							}-*/;
						})

						.input(new Action<Event>() {
							@Override public void execute(final Event e) { render(); }
						})

						.change(new Action<Event>() {
							@Override public void execute(final Event e) {
								if ( !modified(e.target().value()) ) {
									e.stop().cancel();
								}
							}
						}))

				.append(clear=$("<button/>").visible(false)

						.title("Clear")
						.is("fa fa-times-circle", true)

						.attribute("tabindex", "-1")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) {
								clear();
								text.change();
							}
						})))

				// ;(ie) root is focused after clicking the clear button, causing RangeView to wait for further user input

				.bind("focus", new Action<Event>() {
					@Override public void execute(final Event e) { e.current().blur(); }
				});
	}


	private boolean modified(final String value) {
		try {
			return !this.value.equals(value);
		} finally {
			this.value=value;
		}
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean immediate() {
		return immediate;
	}

	public Input immediate(final boolean immediate) {

		this.immediate=immediate;

		return render();
	}


	public boolean enabled() {
		return text.enabled();
	}

	public Input enabled(final boolean enabled) {

		text.enabled(enabled);

		return render();
	}


	public boolean search() {
		return root().is("search");
	}

	public Input search(final boolean search) {

		root().is("search", search);

		return render();
	}


	public String placeholder() {
		return text.attribute("placeholder");
	}

	public Input placeholder(final String placeholder) {

		text.attribute("placeholder", placeholder);

		return render();
	}


	public String pattern() {
		return text.attribute("pattern");
	}

	public Input pattern(final String pattern) {

		text.attribute("pattern", pattern);

		return render();
	}


	public String value() {
		return text.valid() ? text.value() : "";
	}

	public Input value(final String value) {

		text.value(this.value=value == null ? "" : value);

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public Input focus() {

		text.focus();

		return this;
	}

	public Input blur() {

		text.blur();

		return this;
	}

	public Input clear() {

		text.value("");

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Input render() {

		label.visible(search()).attribute("for", text.id());
		text.<Editable>as().immediate(immediate);
		clear.visible(text.enabled() && !text.value().isEmpty());

		return this;
	}
}
