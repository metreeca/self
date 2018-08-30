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

package com.metreeca.self.client.views;

import com.metreeca._jeep.shared.Command;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.faces.Header;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class FieldsView extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("FieldsView.css") TextResource skin();

	}


	private boolean locked;

	private final Tile more;
	private final Tile add;


	private Report report;
	private Header header;

	private Map<Path, PathView> fields=Collections.emptyMap();


	public FieldsView() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append($("<menu/>")

						.append(more=$("<button/>").hide()

								.is("fa fa-ellipsis-v", true)
								.title("More fields…")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { more(); }
								})

								.drop(new Action.Drop() {
									@Override protected void dragenter(final Event e) {
										if ( e.data(PathView.class) != null ) { more(); }
									}
								}))

						.append(add=$("<button/>")

								.is("fa fa-plus", true)
								.title("Add field")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { insert(); }
								})))


				.drop(new PathDrop())


				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				})

				.bind(Header.class, new Action<Event>() {
					@Override public void execute(final Event e) { header(e.<Header>data()); }
				})

				.<Bus>as()

				.resize(new Action<Void>() {
					@Override public void execute(final Void v) { align(false); }
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean locked() {
		return locked;
	}

	public FieldsView locked(final boolean locked) {

		this.locked=locked;

		return render();
	}


	private Report report() {
		return report != null && !report.getEndpoint().isEmpty() ? report : null;
	}

	private FieldsView report(final Report report) {

		this.report=report;
		this.header=null;

		return render();
	}


	public Header header() {
		return header;
	}

	private FieldsView header(final Header header) {

		this.header=header;

		return align(true);
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void more() {

		final Tile menu=$("<menu/>");

		for (final Map.Entry<Path, PathView> entry : fields.entrySet()) {
			if ( !$(entry.getValue()).visible() ) {
				menu.append(new PathView().report(report).path(entry.getKey()));
			}
		}

		menu

				.is("sharp", true)
				.skin(":scope { max-width: 15em; padding: 0.25em; } :scope > * { width: auto !important; } :scope > * + * { margin-top: 0.25em; }")

				.drop(new PathDrop(true))

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { e.current().<Overlay>as().close(); }
				})

				.<Overlay>as()

				.labile(true)
				.open()
				.align(more.parent(), Align.Start, Align.After)

				.<Bus>as()

				.resize(new Action<Void>() {
					@Override public void execute(final Void v) { menu.<Overlay>as().close(); }
				});
	}

	private void insert() {
		$(new PathPicker().open(report.arrayed(), new Path())).change(new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.cancel() ) { insert(e.<Path>data()); }
			}
		});
	}

	private void insert(final Path path) {
		insert(path, -1);
	}

	private void insert(final Path path, final int target) {

		final Specs original=report.getSpecs();
		final Specs modified=original.copy().insertField(path, target);

		if ( target == 0 ) { // pivoting
			modified.center(modified.getFields().get(0));
		}

		root().fire(new Command.Change<Specs>(original, modified) {

			@Override protected boolean set(final Specs value) {
				return root().fire(report.updated().setSpecs(value));
			}

			@Override public String label() {
				return "Add field '"+path.label()+"'";
			}
		});
	}

	private void move(final Path path, final int target, final int source) {

		final Specs original=report.getSpecs();
		final Specs modified=original.copy().insertField(path, target);

		if ( target == 0 || source == 0 ) { // pivoting
			modified.center(modified.getFields().get(0));
		}

		root().fire(new Command.Change<Specs>(original, modified) {

			@Override protected boolean set(final Specs value) {
				return root().fire(report.updated().setSpecs(value));
			}

			@Override public String label() {
				return "Move field '"+path.label()+"'";
			}

		});
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private FieldsView render() {

		final Report report=report();

		if ( report != null ) {

			// create fields recycling existing ones (to preserve id for face synchronization)

			final Map<Path, PathView> fields=new LinkedHashMap<>();

			for (final Path path : report.getSpecs().getFields()) {

				PathView field=this.fields.get(path);

				if ( field == null ) {
					$(field=new PathView()).edit(new Action<Event>() {
						@Override public void execute(final Event e) { align(false); }
					});
				}

				fields.put(path, field);
			}

			// memo current fields

			this.fields=fields;

			// insert current fields

			final Tile root=root();
			final Tile tail=root.find(":scope > :last-child");

			root.clear();

			for (final Map.Entry<Path, PathView> entry : fields.entrySet()) {

				final Path path=entry.getKey();
				final PathView field=entry.getValue();

				$(field.report(report).path(path)).appendTo(root);
			}

			root.append(tail);

			add.enabled(!report.isLocked());
		}

		return align(true);
	}

	private FieldsView align(final boolean resize) {

		final Tile fields=root().find(":scope > :not(menu)");

		if ( resize ) {

			// compute natural fields size

			fields.show()
					.style("flex-shrink", "")
					.style("-webkit-flex-shrink", "")
					.style("min-width", "")
					.style("width", "");

			// reserve label space

			for (int i=0; i < fields.size(); ++i) {
				fields.node(i).style("min-width", fields.node(i).style("width", true));
			}

			// compute fitted fields size

			fields
					.style("flex-shrink", "1")
					.style("-webkit-flex-shrink", "1");

			if ( header != null ) {
				for (int i=0; i < fields.size(); ++i) {
					fields.node(i).style("width", header.width(i));
				}
			}
		}

		// toggle more menu

		fields.visible(true);
		more.visible(root().dw() > root().width());

		// hide overflowing fields

		for (int i=fields.size()-1; i >= 0 && root().dw() > root().width(); --i) {
			fields.node(i).hide();
		}

		// align face columns

		if ( header != null ) {
			for (int i=0; i < fields.size(); ++i) {
				header.width(i, fields.node(i).visible() ? fields.node(i).style("width", true) : "");
			}
		}

		return this;
	}


	private final class PathDrop extends Action.Drop {

		private final boolean more; // dropping on the more menu

		private Path path;

		private Tile tile; // the dragged tile
		private int origin; // < 0 : insertion / >= 0 : reordering (the original index of the tile)


		public PathDrop() {
			this(false);
		}

		public PathDrop(final boolean more) {
			this.more=more;
		}


		@Override protected void dragenter(final Event e) {

			final PathView view=e.data(PathView.class);

			if ( view != null && e.cancel() ) {

				e.mode("move");

				this.path=fields.containsKey(view.path()) ? view.path() : new Path() // copy if not already a field
						.setLabel(view.path().getLabel())
						.setSteps(view.path().getSteps())
						.setTransform(view.path().getTransform())
						.setSummary(view.path().getSummary());

				if ( $(view).parent().any(e.current()) ) { // reordering > move the original view

					this.tile=$(view);
					this.origin=tile.index();

				} else { // insertion > move a placeholder

					this.tile=$(new PathView().report(view.report()).path(path))
							.style("flex-shrink", "0") // don't collapse label
							.is("dragging", true); // emulates dragging on copy

					this.origin=-1;

				}
			}
		}

		@Override protected void dragover(final Event e) {
			if ( path != null && e.cancel() ) {

				final Tile current=e.mode("move").current();

				final int source=tile.index();
				final int target=target(e);

				if ( source < 0 ) { // insert if not already a child

					current.insert(tile, target);

					if ( !more ) {

						if ( header != null ) { header.insert(target, path); }

						align(false);
					}

				} else if ( target != source && target != source+1 ) { // insert if actually changing position

					final int index=source < target ? target-1 : target; // compensate index if moving right/upward

					current.insert(tile, index);

					if ( !more ) {

						if ( header != null ) { header.insert(index, source); }

						align(false);
					}

				}

			}
		}

		@Override protected void dragleave(final Event e) {
			if ( path != null && e.cancel() ) {

				final int source=tile.index();
				final int target=origin;

				if ( target < 0 ) { // insertion

					tile.remove(); // remove placeholder

					if ( !more ) {

						if ( header != null ) { header.remove(source); }

						align(false);
					}

				} else if ( source != target ) { // reordering

					e.current().insert(tile, target); // restore the original position

					if ( !more ) {

						if ( header != null ) { header.insert(target, source); }

						align(false);
					}
				}

				path=null;
				tile=null;
				origin=0;
			}
		}

		@Override protected void drop(final Event e) {
			if ( path != null && e.cancel() ) {

				final int offset=more ? root().find(":scope > :not([hidden]):not(:last-child)").size() : 0;
				final int target=tile.index()+offset;
				final int source=new ArrayList<>(fields.keySet()).indexOf(path);

				if ( source < 0 ) { // insertion

					insert(path, target);

				} else { // reordering

					final int index=more && source < offset ? target-1 : target; // compensate if moving to more menu

					move(path, index, source);

				}

				path=null;
				tile=null;
				origin=0;
			}
		}

		private int target(final Event e) { // return the insertion index for path dnd operations

			int index=0;
			float delta=Float.MAX_VALUE;

			final int offset=more ? e.y(true) : e.x(true);
			final Tile children=e.current().children();

			for (int i=0, size=more ? children.size() : children.size()-1; i < size; ++i) { // ignore tail menu

				final Box box=children.node(i).box();

				final float head=Math.abs(offset-(more ? box.top() : box.left()));

				if ( head < delta ) {
					index=i;
					delta=head;
				}

				final float tail=Math.abs(offset-(more ? box.bottom() : box.right()));

				if ( tail < delta ) {
					index=i+1;
					delta=tail;
				}
			}

			return index;
		}
	}

}
