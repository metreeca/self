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

package com.metreeca.self.client.faces.table;


import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.*;
import com.metreeca._tool.client.forms.State;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.client.faces.Header;
import com.metreeca.self.client.views.TermView;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.XSD;
import com.metreeca.self.shared.forms.Tuples;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.metreeca._tile.client.Tile.$;

import static com.google.gwt.core.client.GWT.create;

import static java.lang.Math.*;


public final class TableFace extends Face<TableFace> {

	private static final Resources resources=create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("TableFace.css") TextResource skin();

	}


	private Report report;
	private Tuples tuples;

	private final Tile frame; // manages virtual space for progressive rendering
	private final Tile table; // absolutely positioned inside frame

	private List<Path> fields=Collections.emptyList(); // the current field list

	private final List<String> widths=new ArrayList<>(); // computed css field widths [<float>px] (empty if not shown)

	private float total; // the running total for row height estimate
	private float count; // the running count for row height estimate


	private final Action<Event> scroll=new Action<Event>() {
		@Override public void execute(final Event e) {
			if ( enabled() ) {
				e.current()
						.animate(adapt) // schedule table filling at next animation frame
						.cancel(memoize).delay(500, memoize); // schedule memoization as soon as scrolling is done
			}
		}
	};

	private final Action<Tile> adapt=new Action<Tile>() {
		@Override public void execute(final Tile tile) { rows(); }
	};

	private final Action<Event> memoize=new Action<Event>() {
		@Override public void execute(final Event e) { scroll(e.current().dy()); }
	};


	public TableFace() {
		root().is("placeholder", true)

				.skin(resources.skin().getText())

				.append(frame=$("<div/>")

						.append(table=$("<table/>").is("striped", true)))


				.bind("scroll", scroll)


				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected TableFace self() {
		return this;
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report != null && !report.getEndpoint().isEmpty() ? report : null;
	}

	private TableFace report(final Report report) {

		if ( report != null && this.report != null && !this.report.equals(report) ) { report(null); }

		this.report=(report != null && report.getLens() instanceof Report.Table) ? report : null;
		this.tuples=null;

		return render();
	}


	private Tuples tuples() {

		final Report report=report();

		if ( report != null && tuples == null ) {
			root().async(this.tuples=new Tuples()

					.setEndpoint(this.report.getEndpoint())
					.setSpecs(report.getSpecs())
					.setLabel(true)

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) { render(); }
					}));
		}

		return tuples != null && tuples.fulfilled() ? tuples : null;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected TableFace render() {

		final Report report=report();
		final Tuples tuples=tuples();

		if ( report == null ) {

			root().hide().clear();

		} else if ( tuples == null ) {

			scroll.enabled(false); // ignore system-generated scroll events

			root().show(); // show placeholder

			lock(); // freeze row height to prevent vertical accordion effects when removing tall cells
			cols(); // update cols after field insertion/deletion/rearrangement
			head(); // align to header

		} else {

			final long start=System.currentTimeMillis();

			root().clear();

			frame.style("height", "0");
			table.style("top", "0").clear();

			total=0;
			count=0;

			if ( !tuples.getEntries().isEmpty() ) {

				root().append(frame);

				cols();
				rows();
				head(); // after rows() has filled and sized the table
				rows(); // again as head() may hide fields changing row height

			}

			root().scroll(0.0f, scroll());  // recover scrolling offset from browser history

			final long stop=System.currentTimeMillis();

			$().info(TableFace.class, "rendered in "+(stop-start)+" ms");

			scroll.enabled(true); // react to user-generated scroll events

		}

		return this;
	}


	private void lock() {
		table.children().each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {
				return tile.style("height", tile.height()+"px"); // ;(ie) use offset height as computed height for table rows is always 'auto'
			}
		});
	}

	private boolean head() {

		// compute preferred column widths

		table.find("td").style("width", "");

		for (int c=0, cs=fields.size(); c < cs; ++c) {

			float width=0.0f;

			for (int r=0, rs=table.children().size(); r < rs; ++r) {
				width=max(width, table.child(r).child(c).width()+1); // ;(ie) compensate rounding errors
			}

			widths.set(c, width+"px");

		}

		return root().fire(new Header() { // managed by FieldsView

			@Override public String width(final int index) { // report preferred column widths
				return widths.get(index);
			}

			@Override public void width(final int index, final String width) { // align to header fields

				widths.set(index, width);
				table.children().child(index)
						.style("width", width)
						.style("display", width.isEmpty() ? "none" : "");

			}

			@Override public void insert(final int target, final Path path) {

				lock(); // freeze row height to prevent vertical accordion effects (will be cleared on redraw)

				fields.add(target, path);
				widths.add(target, "auto");
				table.children().insert($("<td/>"), target);
			}

			@Override public void insert(final int target, final int source) {

				lock(); // freeze row height to prevent vertical accordion effects (will be cleared on redraw)

				fields.add(target, fields.remove(source));
				widths.add(target, widths.remove(source));
				table.children().each(new Lambda<Tile>() {
					@Override public Tile apply(final Tile tile) {
						return tile.insert(tile.child(source), target);
					}
				});
			}

			@Override public void remove(final int target) {

				lock(); // freeze row height to prevent vertical accordion effects (will be cleared on redraw)

				fields.remove(target);
				widths.remove(target);
				table.children().child(target).remove();
			}

		});
	}

	private TableFace cols() { // update table columns after report fields are modified/rearranged

		final Report report=report();

		if ( report != null ) {

			// memo current fields/widths

			final List<Path> fields=new ArrayList<>(report.getSpecs().getFields());

			// compute field permutation

			final int[] indexes=new int[fields.size()];

			boolean rearranged=fields.size() != this.fields.size();

			this.widths.clear();

			for (int i=0; i < fields.size(); i++) {
				rearranged|=i != (indexes[i]=this.fields.indexOf(fields.get(i)));
				this.widths.add("auto");
			}

			// update fields

			this.fields=fields;

			// rearrange columns

			if ( rearranged ) {
				table.children().each(new Lambda<Tile>() {
					@Override public Tile apply(final Tile row) {

						final Tile children=row.children();

						row.clear();

						for (final int index : indexes) {
							row.append(index >= 0 ? children.node(index) : $("<td/>"));
						}

						return row;
					}
				});
			}
		}

		return this;
	}

	private TableFace rows() { // display rows in the current sliding window (optimized for slow scrolling)

		final Tuples tuples=tuples();

		if ( tuples != null ) {

			final List<List<Term>> entries=tuples.getEntries();

			final int rows=entries.size();

			// measure viewport

			final float portHeight=root().box().height();
			final float portTop=root().dy();
			final float portBottom=portTop+portHeight;

			// estimate average row height

			total+=table.box().height();
			count+=table.children().size();

			final float average=(count > 0) ? total/count : portHeight/100;

			// estimate visible row range

			final int lower=max((int)floor(portTop/average), 0); // inclusive
			final int upper=min((int)ceil(portBottom/average), rows); // exclusive

			// render visible table slice

			table.clear();

			for (int index=lower; index < upper; ++index) {
				table.append(row(index, fields, entries));
			}

			// estimate virtual space and position the table adjusting at top/bottom for possible errors

			final float total=average*rows;
			final float virtual=total-table.height();

			frame.style("height", total+"px");

			final int hidden=rows-(upper-lower);

			table.style("top", (hidden > 0 ? virtual*lower/hidden : 0)+"px");
			table.is("odd", lower%2 != 0); // adapt striping pattern

		}

		return this;
	}

	private Tile row(final int index, final List<Path> fields, final List<List<Term>> entries) {

		final List<Term> previous=(index > 0) ? entries.get(index-1) : Collections.<Term>emptyList();
		final List<Term> entry=entries.get(index);

		final Tile row=$("<tr/>");

		boolean repeated=true;

		for (int i=0; i < fields.size(); ++i) {

			final Tile cell=$("<td/>").style("width", widths.get(i));

			if ( widths.get(i).isEmpty() ) { // dom ops > execute only if required
				cell.style("display", "none"); // ;( hidden attribute  possibly broken on td elements with display: inline-block
			}

			final Term term=entry.get(i);

			if ( term != null ) {

				final boolean year=fields.get(i).getTransform() == Path.Transform.Year;

				final Term term1=year ? Term.typed(term.getText(), XSD.XSDGYear) : term;
				final Tile tile=$(new TermView().term(term1)); // !!! review

				repeated&=previous.size() > i && term.equals(previous.get(i));

				if ( repeated ) { // dom ops > execute only if required
					tile.is("shadow", true);
				}

				cell.append(tile);

			}

			row.append(cell);

		}

		return row;
	}


	//// Memo //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private float scroll() {
		return report.memo() == null ? 0 : report.<Float>memo();
	}

	private void scroll(final float scroll) {
		if ( report != null ) { // scroll event before the report is set (e.g. when backtracking from history)
			root().fire(new State(report.memo(scroll)));
		}
	}

}
