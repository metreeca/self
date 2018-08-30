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

package com.metreeca.self.client.faces;

import com.metreeca._tile.client.*;
import com.metreeca.self.client.views.PathView;
import com.metreeca.self.shared.beans.Path;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Tile.$;


public final class Role extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Role.css") TextResource skin();

	}


	private boolean locked;
	private boolean multiple; // true for multiple series support

	private Tray<Path> tray=new Tray<Path>() {};

	private final List<Path> series=new ArrayList<>();


	public Role() {
		root("<ul/>")

				.skin(resources.skin().getText())

				.drop(new PathDrop());
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Role locked(final boolean locked) {

		this.locked=locked;

		return this;
	}

	public Role multiple(final boolean multiple) {

		this.multiple=multiple;

		return this;
	}

	public Role tray(final Tray<Path> tray) {

		if ( tray == null ) {
			throw new NullPointerException("null tray");
		}

		this.tray=tray;

		return this;
	}


	public Role series(final Path series) {
		return series(series != null ? Collections.singleton(series) : Collections.<Path>emptyList());
	}

	public Role series(final Collection<Path> series) {

		if ( series == null ) {
			throw new NullPointerException("null series");
		}

		if ( series.contains(null) ) {
			throw new NullPointerException("null series ["+series+"]");
		}

		this.series.clear();

		if ( multiple ) {
			this.series.addAll(series);
		} else if ( !series.isEmpty() ) {
			this.series.add(series.iterator().next());
		}

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Role render() {

		root().clear();

		for (final Path path : series) {
			root().append(new EntryView(path));
		}

		return this;
	}


	private final class EntryView extends View {

		private final Path path;


		private EntryView(final Path path) {

			this.path=path;

			root("<li/>")

					.append($("<span/>")

							.text(path.label()))

					.append($("<button/>")

							.enabled(!locked)
							.attribute("type", "button") // inside a form

							.is("fa fa-remove", true)
							.title("Hide series")

							.click(new Action<Event>() {
								@Override public void execute(final Event e) {
									tray.remove(path);
								}
							}))

					.drag(new Action.Drag() {
						@Override protected void dragstart(final Event e) {
							if ( locked ) { e.mode("none"); } else {
								e.mode("move")
										.data("text", path.label()) // ;(ie) text/plain not supported
										.data(EntryView.class, EntryView.this);
							}
						}
					});
		}


		public Path path() { return path; }
	}


	private final class PathDrop extends Action.Drop {

		private Tile tile; // the dragged tile
		private Path path; // the path contained in the dragged tile

		private Tile memo; // the original content


		@Override protected void dragenter(final Event e) {

			final PathView pv=e.data(PathView.class);
			final EntryView ev=e.data(EntryView.class);

			final Tile tile=(pv != null) ? $(pv) : (ev != null) ? $(ev) : null;
			final Path path=(pv != null) ? pv.path() : (ev != null) ? ev.path() : null;

			if ( tile != null && tray.accepts(path) && e.cancel() ) {

				e.mode("move");

				this.path=path;
				this.tile=tile.parent().all(e.current()) ? tile // a dragged child
						: series.contains(path) ? e.current().child(series.indexOf(path)) // insertion of existing path
						: $(new EntryView(path)); // insertion of new path > move a placeholder

				this.tile.is("dragging", true); // emulate dragging on placeholders
				this.memo=e.current().children();

			}
		}

		@Override protected void dragover(final Event e) {
			if ( tile != null && e.cancel() ) {

				e.mode("move");

				final int source=tile.index();
				final int target=target(e);

				// modify DOM if not already a child or if actually changing position to prevent DND issues

				if ( multiple ) {

					if ( source < 0 ) {
						e.current().insert(tile, target);
					} else if ( target != source && target != source+1 ) {
						e.current().insert(tile, source < target ? target-1 : target); // compensate index if moving downward
					}

				} else {

					if ( source < 0 ) {
						e.current().clear().append(tile);
					}

				}
			}
		}

		@Override protected void dragleave(final Event e) {
			if ( memo != null && e.cancel() ) {

				e.current().clear().append(memo);

				done();
			}
		}

		@Override protected void drop(final Event e) {
			if ( tile != null && e.cancel() ) {

				tray.insert(path, tile.index());

				done();
			}
		}


		private void done() {
			tile=null;
			path=null;
			memo=null;
		}

		private int target(final Event e) { // return the insertion index for dnd operations

			int index=0;
			float delta=Float.MAX_VALUE;

			final int y=e.y(true);
			final Tile children=e.current().children();

			for (int i=0, size=children.size(); i < size; ++i) {

				final Box box=children.node(i).box();

				final float top=Math.abs(y-box.top());

				if ( top < delta ) {
					index=i;
					delta=top;
				}

				final float bottom=Math.abs(y-box.bottom());

				if ( bottom < delta ) {
					index=i+1;
					delta=bottom;
				}
			}

			return index;
		}
	}
}
