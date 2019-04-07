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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca._tool.client.views.Image;
import com.metreeca.self.shared.beans.Term;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.math.BigDecimal;

import static com.metreeca._tile.client.Tile.$;


public final class TermView extends View {

	public static final int LineLength=40; // maximum length of single line labels [chars]
	public static final int TooltipLength=4*LineLength; // maximum tooltip length [chars]

	public static final NumberFormat DecimalFormat=NumberFormat.getFormat("#,##0.00");
	public static final NumberFormat IntegralFormat=NumberFormat.getFormat("#,##0");
	public static final NumberFormat TechnicalFormat=NumberFormat.getFormat("#,##0"); // !!!

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("TermView.css") TextResource skin();

	}


	private Term term;

	private boolean raw; // true if the term must be displayed in turtle notation regardless of the content
	private boolean inline; // true if the term must be displayed on a single line regardless of the content


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public TermView term(final Term term) {

		this.term=term;

		return render();
	}


	public TermView raw(final boolean raw) {

		this.raw=raw;

		return render();
	}

	public TermView inline(final boolean inline) {

		this.inline=inline;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void open() {
		root().fire(term);
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private TermView render() { // rendering optimized for inner loops in TableFace

		if ( term != null ) {

			final String label=raw ? term.format() : term.label();

			final Action<Event> action=new Action<Event>() {
				@Override public void execute(final Event e) {
					if ( e.cancel() ) { open(); } // prevent browser from opening links
				}
			};

			final Action<Event> title=new Action<Event>() {
				@Override public void execute(final Event e) {
					e.current().title(label.length() <= TooltipLength && e.current().dw() > e.current().width() ? label : "");
				}
			};

			if ( term.isVisual() ) {

				root(new Image()

						.src(term.getText())
						.alt(term.getNotes()));

			} else if ( term.isExternal() ) {
				root("<span/>")

						.is("external", true)

						.append($("<a/>")
								.is("iri", term.getLabel().isEmpty())
								.attribute("href", term.getText()) // enable link copying
								.text(label)
								.mouseenter(title)
								.click(action))

						.append($("<a/>").is("external fa fa-external-link", true)
								.attribute("target", term.getText().startsWith("http://") || term.getText().startsWith("https://") ? "_blank" : "")
								.title(term.getText())
								.attribute("href", term.getText()));

			} else if ( term.isNamed() ) {

				root($("<a/>")
						.is("iri", term.getLabel().isEmpty())
						.attribute("href", term.getText()) // enable link copying
						.text(label)
						.mouseenter(title)
						.click(action));

			} else if ( term.getValue() instanceof Number ) {

				final Number value=(Number)term.getValue();

				final boolean decimal=value instanceof Float
						|| value instanceof Double
						|| value instanceof BigDecimal;

				root("<a/>").is("numeric", true)
						.attribute("href", "term:"+term.format()) // enable term copying
						.text((decimal ? DecimalFormat : IntegralFormat).format(value))
						.mouseenter(title)
						.click(action);

			} else if ( term.isTyped() ) {

				root("<a/>")
						.attribute("href", "term:"+term.format()) // enable term copying
						.text(label)
						.mouseenter(title)
						.click(action);

			} else if ( term.isBlank() ) {

				root("<span/>") // not navigable > no action
						.text(label)
						.mouseenter(title);

			} else if ( label.length() <= LineLength ) { // textual token

				root("<a/>")
						.attribute("href", "term:"+term.format()) // enable term copying
						.text(label)
						.mouseenter(title)
						.click(action);

			} else { // long text

				root("<span/>") // not navigable > no action
						.is("textual", true)
						.text(label)
						.mouseenter(title)
						.mouseleave(1000, new Action<Event>() { // reset scrollbars when cursor leaves text area
							@Override public void execute(final Event e) { e.current().scroll(0, 0); }
						});

			}

			root()

					.skin(resources.skin().getText())

					.is("inline", inline);

		}

		return this;
	}

}
