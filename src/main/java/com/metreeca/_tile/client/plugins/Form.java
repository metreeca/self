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

package com.metreeca._tile.client.plugins;

import com.metreeca._tile.client.*;


public final class Form extends Plugin { // !!! write methods should operate for each form in tile

	@SuppressWarnings("ProtectedMemberInFinalClass") protected Form() {}


	public Form wire() {
		return this.<Tile>as().each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {

				tile

						.edit(new Action<Event>() {
							@Override public void execute(final Event e) { e.current().<Form>as().edited(true); }
						})

						.reset(new Action<Event>() {
							@Override public void execute(final Event e) { e.current().<Overlay>as().close(true); }
						})

						.submit(new Action<Event>() {
							@Override public void execute(final Event e) {

								e.cancel(); // prevent browser submission
								e.target().window().focus().blur(); // e.g. to force close combo boxes

							}
						})

						.bind("open", new Action<Event>() { // opening as Overlay
							@Override public void execute(final Event e) {
								e.current().async(new Action<Event>() { // after attachment
									@Override public void execute(final Event e) {
										size(e.current().find("textarea"));
									}
								});
							}
						})

						.bind("close", new Action<Event>() { // closing as Overlay
							@Override public void execute(final Event e) {
								if ( !e.current().valid() ) { e.cancel(); }
							}
						})

						.<Overlay>as()

						.modal(true)
						.shading(true);


				tile.find("input")

						.attribute("autocomplete", "off") // ;(ie)

						.bind("focus", new Action<Event>() { // select all on focus
							@Override public native void execute(final Event e) /*-{
								$wnd.setTimeout(function () {
									e.target.selectionStart=0;
									e.target.selectionEnd=e.target.value.length;
								}, 10); // async to select after click effect
							}-*/;
						});

				tile.find("textarea")

						.input(new Action<Event>() {
							@Override public void execute(final Event e) { size(e.current()); }
						}); // input > auto-size

				return tile.async(new Action<Event>() { // possibly after form is attached to see it already validated
					@Override public void execute(final Event e) {
						e.current().<Form>as().edited(false);
					}
				});

			}

		}).as();
	}


	public boolean forced() { // execute submit action even if not edited
		return this.<Tile>as().get("$forced", false);
	}

	public Form forced(final boolean forced) {
		return this.<Tile>as().set("$forced", forced).as();
	}


	public boolean locked() { // not editable
		return this.<Tile>as().get("$locked", false);
	}

	public Form locked(final boolean locked) {

		this.<Tile>as().find("input, textarea, select, button:not([type=submit])").enabled(!locked);

		// !!! hide empty fields/sections when locked

		return this.<Tile>as().set("$locked", locked).as();
	}


	public boolean edited() {
		return this.<Tile>as().get("$edited", false);
	}

	public Form edited(final boolean edited) { // !!! l10n

		final Tile tile=as();
		final boolean valid=tile.valid();

		tile.find("button[type=reset]").title("Cancel").enabled(edited);
		tile.find("button[type=submit]").title(edited || tile.<Form>as().forced() || !valid ? "Save" : "Close").enabled(valid);

		return tile.set("$edited", edited).as();
	}


	public Form submit(final Action<Event> action) {
		return this.<Tile>as().submit(new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.current().valid() ) {

					e.current().<Overlay>as().close();

					final Form form=e.current().as();

					if ( form.forced() || form.edited() ) { action.execute(e); }
				}
			}
		}).as();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Tile size(final Tile tile) {
		return tile.each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {
				return tile
						.style("height", "")
						.style("height", tile.dh()+"px")
						.style("overflow", tile.dh() > tile.height() ? "auto" : "hidden"); // ;(ie) scrollbar glitches on editing
			}
		});
	}
}
