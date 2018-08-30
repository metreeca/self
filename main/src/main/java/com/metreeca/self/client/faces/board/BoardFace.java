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

package com.metreeca.self.client.faces.board;


import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.Tile;
import com.metreeca._tool.client.forms.State;
import com.metreeca._tool.client.views.Image;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.client.views.TermView;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Action.throttled;
import static com.metreeca._tile.client.Action.trailing;
import static com.metreeca._tile.client.Plugin.real;
import static com.metreeca._tile.client.Tile.$;

import static java.lang.Math.*;


public final class BoardFace extends Face<BoardFace> {

	private static final Resources resources=GWT.create(Resources.class);

	private static final String placeholder=resources.icon().getText();


	public static interface Resources extends ClientBundle {

		@Source("BoardFace.css") TextResource skin();

		@Source("BoardFace.img") TextResource icon();

	}


	private Report report;
	private Tuples tuples;

	private final List<Term> terms=new ArrayList<>();


	private int cols; // current cols
	private int rows; // current rows
	private float size; // the card size

	private int lower; // the first displayed term (inclusive)
	private int upper; // the last displayed term (exclusive)

	private final Tile frame; // virtual frame for progressive rendering


	public BoardFace() {
		root().is("placeholder", true)

				.skin(resources.skin().getText())

				.append(frame=$("<div/>"))


				.bind("scroll", throttled(50, new Action<Event>() {
					@Override public void execute(final Event e) { fill(); }
				}))

				.bind("scroll", trailing(250, new Action<Event>() {
					@Override public void execute(final Event e) { draw(); }
				}))

				.bind("scroll", trailing(500, new Action<Event>() {
					@Override public void execute(final Event e) { scroll(e.current().dy()); }
				}))


				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected BoardFace self() {
		return this;
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report != null && !report.getEndpoint().isEmpty() ? report : null;
	}

	private BoardFace report(final Report report) {

		if ( report != null && this.report != null && !this.report.equals(report) ) { report(null); }

		this.report=(report != null && report.getLens() instanceof Report.Board) ? report : null;
		this.tuples=null;

		this.terms.clear();

		return render();
	}


	private Tuples tuples() {

		if ( report() != null && tuples == null ) {
			root().async(tuples=new Tuples()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report.getSpecs())
					.setLabel(true)
					.setImage(true)

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) { tuples(tuples); }
					}));
		}

		return tuples != null && tuples.fulfilled() ? tuples : null;
	}

	private BoardFace tuples(final Tuples tuples) {

		this.tuples=tuples;

		final Collection<Term> terms=new LinkedHashSet<>(); // deduplicate preserving order

		for (final List<Term> tuple : tuples.getEntries()) {
			if ( !tuple.isEmpty() ) {
				terms.add(tuple.get(0)); // !!! support other fields
			}
		}

		this.terms.addAll(terms);

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected BoardFace render() {

		final Report report=report();
		final Tuples tuples=tuples();

		if ( report == null ) {

			root().hide().clear();

		} else if ( tuples == null ) {

			root().show(); // show placeholder

		} else {

			final long start=System.currentTimeMillis();

			frame.appendTo(root())

					.clear()
					.height(0)
					.height(size())
					.height(size()) // again in case the vertical scrollbar appeared reducing width

					.parent()
					.scroll(0, scroll()); // recover scrolling offset from browser history

			$(":scope").bind("scroll", new Action<Event>() { // ignore the next system-generated scroll event // !!! review
				@Override public void execute(final Event e) { e.stop().current().drop(e.type(), this, true); }
			}, true);

			fill();
			draw();

			final long stop=System.currentTimeMillis();

			$().info(BoardFace.class, "rendered in "+(stop-start)+" ms");

		}

		return this;
	}


	private float size() {

		final float width=root().client().width()-1; // -1 prevents rounding errors // ;(ie) getBoundingClientRect includes scrollbar
		final float preferred=10*real(root().style("font-size", true));

		cols=max(1, round(width/preferred));
		rows=terms.size()/cols+(terms.size()%cols > 0 ? 1 : 0);

		lower=0;
		upper=0;

		return rows*(size=width/cols);
	}

	private void fill() { // add tiles

		final float scroll=max(root().dy(), 0); // webkit may overscroll into negative offsets

		final int lower=cols*(int)floor(scroll/size);
		final int upper=min(cols*(int)ceil((scroll+root().height(true))/size), terms.size());

		for (int n=min(max(lower-this.lower, 0), this.upper-this.lower); n > 0; --n) { frame.first().remove(); }
		for (int n=min(max(this.upper-upper, 0), this.upper-this.lower); n > 0; --n) { frame.last().remove(); }

		for (int n=min(this.lower, upper); n > lower; --n) { frame.insert(card(terms.get(n-1), size)); }
		for (int n=max(this.upper, lower); n < upper; ++n) { frame.append(card(terms.get(n), size)); }

		final float offset=size*lower/cols;
		final float height=size*rows;

		frame
				.style("padding-top", offset+"px")
				.style("height", height+"px");

		this.lower=lower;
		this.upper=upper;

	}

	private void draw() { // add picture as soon as tiles are visible

		for (int i=lower; i < upper; ++i) {

			final String image=terms.get(i).getImage();

			if ( !image.isEmpty() ) {
				frame.child(i-lower).child(0).get(Image.class).src(image);
			}

		}

	}


	private Tile card(final Term term, final float size) {
		return $("<div/>")

				.style("width", size+"px")
				.style("height", size+"px") // keep cards square: pictures are centered vertically (see .css)

				.append($(new Image(placeholder)) // initial image, to be updated when the card becomes visible

						.bind("click", new Action<Event>() {
							@Override public void execute(final Event e) { if ( e.cancel() ) { open(term); } }
						}))

				.append(new TermView().term(term));
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
