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

package com.metreeca._tile.client.plugins;

import com.metreeca._tile.client.*;


public final class Editable extends Plugin { // !!! handle contenteditable/input/textarea

	private static final int AutoDelay=750; // grace period for immediate change events [ms]


	@SuppressWarnings("InterfaceNeverImplemented") public static interface Resources {

		public static final String MouseEvents="mousemove mousedown mouseup click dblclick";

		public static final Action<Event> Dblclick=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.button("left") && e.current().enabled() && e.cancel() ) {
					e.current().<Editable>as().editable(true);
				}
			}
		};

		public static final Action<Event> Keydown=new Action<Event>() {
			@Override public void execute(final Event e) {

				final Tile editable=e.current();

				if ( e.key("enter", "return") ) {
					e.cancel();
					e.current().<Editable>as().editable(false);
				} else if ( e.key("escape") ) {
					e.cancel();
					editable.<Editable>as().text(editable.get("$original", "")).editable(false);
				}
			}
		};

		public static final Action<Event> Blur=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().<Editable>as().editable(false);
			}
		};


		public static final Action<Event> Immediate=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().cancel(Change).delay(AutoDelay, Change);
			}
		};

		public static final Action<Event> Change=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().change();
			}
		};


		public static final Action<Event> Paste=new Action<Event>() {
			@Override public void execute(final Event e) { e.current().async(Resources.Flatten); }
		}; // !!! review async > should be executed before rendering

		public static final Action<Event> Flatten=new Action<Event>() {
			@Override public void execute(final Event e) { flatten(e.current()); }
		};

		public static final Action<Event> Trap=new Action<Event>() {
			@Override public void execute(final Event e) { e.stop(); }
		};
	}

	private static native Tile flatten(final Tile tile) /*-{

		for (var i=0; i < tile.length; ++i) {

			var root=tile[i];
			var nodes=root.childNodes;

			var next=0;

			while ( next < nodes.length ) {

				var node=nodes[next];

				if ( node.nodeType === Node.TEXT_NODE ) { ++next; } else {

					var children=node.childNodes;

					for (var k=0; k < children.length; ++k) {
						root.insertBefore(children[k], node);
					}

					root.removeChild(node);
				}
			}
		}

		return tile;

	}-*/;


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Editable() {}


	public Editable wire() {
		return this.<Tile>as()

				// as resources >> idempotent

				.dblclick(Resources.Dblclick)
				.keydown(Resources.Keydown)
				.blur(Resources.Blur)

				.bind("paste", Resources.Paste) // ;(ff) fix pasting issues after select-all

				.skin("\n"
						+":scope[contenteditable] {\n"
						+"\ttext-overflow: clip !important;\n"
						+"}\n"
						+"\n"
						+":scope:not([contenteditable])[placeholder]:empty::before {\n"
						+"\tcontent: attr(placeholder);\n"
						+"\tcolor: #999;\n"
						+"}\n"
						+"\n"
						+":scope[contenteditable]:empty::before {\n"
						+"\tcontent: '\\200B';  /* ;(ff) fix collapsing editable divs */\n"
						+"}")

				.as();
	}


	public boolean editable() {
		return Boolean.TRUE.equals(this.<Tile>as().property("isContentEditable"));
	}

	public Editable editable(final boolean editable) {

		final Tile self=as();

		if ( self.enabled() ) {
			if ( editable && Boolean.FALSE.equals(self.property("isContentEditable")) ) {

				self.attribute("contenteditable", "true")
						.set("$original", self.text())
						.bind(Resources.MouseEvents, Resources.Trap)
						.async(new Action<Event>() { // try to focus even if made editable before attaching to the dom
							@Override public void execute(final Event e) {
								e.target().focus().<Selectable>as().select().as();
							}
						});

			} else if ( !editable && Boolean.TRUE.equals(self.property("isContentEditable")) ) {

				final String modified=self.text();
				final String original=self.get("$original", "");

				self.blur() // ;(ff) before resetting contenteditable to avoid stray selection (causes multiple change events)
						.attribute("contenteditable", null)
						.set("$original", "")
						.drop(Resources.MouseEvents, Resources.Trap)
						.scroll(0, 0)
						.<Selectable>as().clear().as();

				if ( !modified.equals(original) ) {
					self.change();
				}
			}
		}

		return this;
	}

	public Editable immediate(final boolean immediate) { // !!! to Tile?

		final Tile self=as();

		if ( immediate ) {
			self.bind("input", Resources.Immediate);
		} else {
			self.drop("input", Resources.Immediate);
		}

		return this;
	}


	public native String text() /*-{ // !!! to Tile.text()?

		function text(nodes) {

			var value="";

			for (var i=0; nodes[i]; i++) {

				var child=nodes[i];

				if ( child.nodeName === "DIV" || child.nodeName === "P" || child.nodeName === "BR" ) {
					value+="\n";
				}

				if ( child.nodeType === Node.TEXT_NODE || child.nodeType === Node.CDATA_SECTION_NODE ) {
					value+=child.nodeValue;
				} else if ( child.nodeType !== Node.COMMENT_NODE ) {
					value+=text(child.childNodes);
				}
			}

			return value;
		}

		return this[0] ? text(this[0].childNodes) : "";

	}-*/;

	public String text(final boolean normalized) {
		return normalized ? normalize(text()) : text();
	}

	public Editable text(final String text) { // !!! remove? Change is triggered only after input events
		return this.<Tile>as().text(text).cancel(Resources.Change).as(); // prevent immediate change event on programmatic change
	}

}
