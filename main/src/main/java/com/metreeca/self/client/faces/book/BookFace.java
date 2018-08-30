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

package com.metreeca.self.client.faces.book;

import com.metreeca._bean.shared.Bean;
import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.Tile;
import com.metreeca._tool.client.forms.State;
import com.metreeca._tool.client.views.Image;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.client.views.TermView;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.*;
import com.metreeca.self.shared.forms.Leaves;
import com.metreeca.self.shared.forms.Tuples;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Action.trailing;
import static com.metreeca._tile.client.Tile.$;
import static com.metreeca.self.shared.beans.Constraint.Options;
import static com.metreeca.self.shared.beans.Term.named;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;


public final class BookFace extends Face<BookFace> {

	private static final int Visible=10; // length threshold for collapsible panels
	private static final int Detail=3; // length limit for leaves lists


	private static final List<Term> HiddenTerms=asList( // support resources
			named("http://apps.metreeca.it/rover") // legacy reference dataset for top-level resources
	);

	private static final List<Term> HiddenLinks=asList( // header fields and other hidden properties
			RDFS.RDFSLabel, RDFS.RDFSComment
	);

	private static final List<Term> ResourcesLinks=asList( // external resources
			VoID.VoIDSparqlEndpoint,
			DCTerms.DCTermsReferences
	);

	private static final List<Term> AnnotationLinks=asList( // housekeeping predicates
			RDFS.RDFSSeeAlso, RDFS.RDFSIsDefinedBy,
			FOAF.FOAFPrimaryTopic, FOAF.FOAFIsPrimaryTopicOF,
			Prov.ProvWasDerivedFrom
	);

	private static final List<Term> AliasesLink=singletonList( // aliasing predicates
			OWL.OWLSameAs
	);

	private static final List<Term> GeoLinks=asList(  // georeferencing predicates
			Geo.GeoLat, Geo.GeoLong, Geo.GeoAlt, Geo.GeoLatLong, Geo.GeoLocation, Geo.GeoGeometry,
			named("http://www.georss.org/georss/point")
	);

	private static final List<Term> TimeLinks=singletonList( // timekeeping predicates
			Geo.GeoTime
	);


	private static final Leaves NoLeaves=new Leaves().setEntries(Collections.<Term, List<Term>>emptyMap()).done();

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("BookFace.css") TextResource skin();

	}


	private Report report;
	private Tuples tuples;
	private Leaves leaves;

	private final List<Term> terms=new ArrayList<>();

	private final Tile head;
	private final Tile port;

	private final Tile page;
	private final Tile prev;
	private final Tile next;


	public BookFace() {
		root().is("placeholder", true)

				.skin(resources.skin().getText())


				.append(head=$("<header/>")

						.append(page=$("<span/>").is("page", true))

						.append(prev=$("<button/>").is("prev", true)

								.title("Previous item")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { step(-1); }
								}))

						.append(next=$("<button/>").is("next", true)

								.title("Next item")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { step(1); }
								})))

				.append(port=$("<div/>") // handles scrolling

						.bind("scroll", trailing(500, new Action<Event>() {
							@Override public void execute(final Event e) { scroll(e.current().dy()); }
						})))

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected BookFace self() {
		return this;
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report;
	}

	private BookFace report(final Report report) {

		if ( report != null && this.report != null && !this.report.equals(report) ) { report(null); }

		this.report=(report != null && report.getLens() instanceof Report.Book) ? report : null;
		this.tuples=null;
		this.leaves=null;

		this.terms.clear();

		return render();
	}


	private Tuples tuples() {

		if ( report() != null && tuples == null ) {
			root().async(tuples=new Tuples()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report.getSpecs())
					.setLabel(true) // keep ordering consistent with TableFace

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) { tuples(tuples); }
					}));
		}

		return tuples != null && tuples.fulfilled() ? tuples : null;
	}

	private BookFace tuples(final Tuples tuples) {

		this.tuples=tuples;
		this.leaves=null;

		final Collection<Term> terms=new LinkedHashSet<>(); // deduplicate preserving order

		for (final List<Term> tuple : tuples.getEntries()) {
			if ( !tuple.isEmpty() ) { // no fields
				terms.add(tuple.get(0)); // !!! support other fields
			}
		}

		this.terms.addAll(terms);

		return render();
	}


	private Leaves leaves() {

		if ( report() != null && tuples() != null && leaves == null ) {
			if ( terms.isEmpty() ) { leaves=NoLeaves; } else {

				final Term term=terms.get(index()); // !!! support other fields

				root().async(leaves=new Leaves()

						.setEndpoint(report.getEndpoint())
						.setSpecs(new Specs().insertPath(new Path().setConstraint(new Options(term.isVerso() ? term.reverse() : term))))
						.setDetail(Detail+1) // insert an additional item to detect truncated sets
						.setLabel(true)
						.setPoint(true)

						.then(new Handler<Leaves>() {
							@Override public void value(final Leaves leaves) { leaves(leaves); }
						}));
			}
		}

		return leaves != null && leaves.fulfilled() ? leaves : null;
	}

	private BookFace leaves(final Leaves leaves) {

		this.leaves=leaves;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private BookFace step(final int delta) {

		index(index()+delta); // memo the target index

		this.leaves=null;

		return render();
	}

	private void open(final Term term, final Term link) {
		root().async(report.getSpecs().open(term, link));
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected BookFace render() {

		final Report report=report();
		final Tuples tuples=tuples();
		final Leaves leaves=leaves();

		if ( report == null ) {

			root().hide().clear();

		} else if ( tuples == null || leaves == null ) {

			root().show(); // show placeholder

		} else {

			final long start=currentTimeMillis();

			root().clear();

			if ( !terms.isEmpty() ) {

				final BookData entries=new BookData(leaves.getEntries());

				final int index=index();
				final int items=terms.size();

				// retrieve annotations from leaves rather than from term >> tuples query may be skipped for singletons

				final Term term=terms.get(index).copy(); // modify a local copy

				term.setLabel(entries.text(RDFS.RDFSLabel));
				term.setNotes(entries.text(RDFS.RDFSComment));
				term.setImage(entries.data(Specs.Images));

				// classify leaves (order gives classification priority)

				for (final Term t : HiddenTerms) { entries.hidden(t); }
				for (final Term l : HiddenLinks) { entries.hidden(l); }
				for (final Term l : AliasesLink) { entries.symmetric(l); }

				final Map<Term, List<Term>> scheme=scheme(entries);
				final Map<Term, List<Term>> concept=concept(entries);
				final Map<Term, List<Term>> related=related(entries);
				final Map<Term, List<Term>> broader=broader(entries);
				final Map<Term, List<Term>> narrower=narrower(entries);

				final Map<Term, List<Term>> resources=resources(entries);
				final Map<Term, List<Term>> annotations=annotations(entries);
				final Map<Term, List<Term>> aliases=aliases(entries);

				final Map<Term, List<Term>> primary=primary(entries); // at end to make textual terms available to more specific tests

				final Map<Term, List<Term>> spatial=spatial(entries); // test before numeric to support numeric coordinates
				final Map<Term, List<Term>> temporal=temporal(entries);
				final Map<Term, List<Term>> numeric=numeric(entries);
				final Map<Term, List<Term>> textual=textual(entries);

				final Map<Term, List<Term>> slots=slots(entries);
				final Map<Term, List<Term>> roles=roles(entries);

				root()

						.append(head.clear()

								.append(new TermView().term(term))

								.append($("<menu/>").when(items > 1)

										.append(page.text("")) // currently unused
										.append(prev.enabled(index > 0))
										.append(next.enabled(index < items-1))))

						.append(port.clear()

								.append(term.getImage().isEmpty() ? null : new Image().src(term.getImage()))

								.append($("<p/>")

										.text(term.getNotes())

										.mouseleave(1000, new Action<Event>() { // reset scrollbars when cursor leaves text area
											@Override public void execute(final Event e) { e.current().scroll(0, 0); }
										}))

								.append($("<section/>").is("primary", true) // no captions: section merges with header info

										.append(entries(term, primary, false)))

								.append($("<section/>").is("literals", true)

										.append(panel("Facts", term, textual, false))
										.append(panel("Figures", term, numeric, false))
										.append(panel("Dates+Times", term, temporal, false))
										.append(panel("Places", term, spatial, false)))

								.append($("<section/>").is("skos", true)

										.append(panel("Concept Scheme", term, scheme, false))
										.append(panel("Concept", term, concept, false))
										.append(panel("Related", term, related, false)))

								.append($("<section/>").is("hierarchy", true)

										.append(panel("Broader", term, broader, false))
										.append(panel("Narrower", term, narrower, false)))

								.append($("<section/>").is("connections", true)

										.append(panel("Roles", term, roles, false))
										.append(panel("Links", term, slots, false)))

								.append($("<section/>").is("resources", true)

										.append(panel("Resources", term, resources, true))
										.append(panel("Annotations", term, annotations, true))
										.append(panel("Aliases", term, aliases, true))));

			}

			port.scroll(0.0f, scroll()); // recover scrolling offset from browser history

			final long stop=currentTimeMillis();

			$().info(BookFace.class, "rendered in "+(stop-start)+" ms");

		}

		return this;
	}


	private Tile panel(final String label, final Term term, final Map<Term, List<Term>> edges, final boolean raw) {
		return entries(term, edges, raw).insert(caption(label, edges.size() > Visible));
	}

	private Tile caption(final String label, final boolean collapsible) {
		return $("<caption/>")

				.is("collapsed", collapsible)

				.append($("<span/>").text(label))
				.append(collapsible ? toggle() : $());
	}

	private Tile toggle() {
		return $("<button/>")

				.is("fa fa-chevron-down", true)
				.title("Show more…")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) {

						final Tile toggle=e.current();
						final Tile panel=toggle.parent();

						final boolean collapsed=!panel.is("collapsed");

						toggle
								.is("fa-chevron-down", collapsed)
								.is("fa-chevron-up", !collapsed)
								.title(collapsed ? "Show more…" : "Show less…");

						panel.is("collapsed", collapsed);

					}
				});
	}

	private Tile entries(final Term term, final Map<Term, List<Term>> edges, final boolean raw) {
		if ( edges.isEmpty() ) { return $(); } else {

			final Tile tbody=$("<tbody/>");

			for (final Map.Entry<Term, List<Term>> entry : edges.entrySet()) {

				final Term link=entry.getKey();
				final List<Term> terms=entry.getValue();

				if ( !terms.isEmpty() ) {
					tbody.append(entry(term, link, terms, raw));
				}
			}

			return $("<table/>").append(tbody

					.mouseleave(1000, new Action<Event>() { // after a grace period to allow grabbing the scrollbar
						@Override public void execute(final Event e) { e.current().scroll(0, 0); }
					}));
		}
	}

	private Tile entry(final Term term, final Term link, final List<Term> terms, final boolean raw) {
		return terms == null ? $() : $("<tr/>")
				.append($("<td/>").append(link(link)))
				.append($("<td/>")
						.attribute("colspan", terms.size() > 1 || report.isLocked() ? "" : "2")
						.append(list(term, link, terms, raw)))
				.append(terms.size() > 1 && !report.isLocked() ? $("<td/>").append(terms(term, link, terms)) : $());
	}

	private Tile link(final Term term) {

		final Tile link=$("<span/>").is("link", true);

		if ( term != null ) {
			if ( term.isVerso() ) {
				link
						.append($("<span/>").text("is"))
						.append(new TermView().term(term.reverse())) // reversed to remove leading ^
						.append($("<span/>").text("of"));
			} else {
				link
						.append(new TermView().term(term));
			}
		}

		return link;
	}

	private Tile list(final Term term, final Term link, final List<Term> terms, final boolean raw) {

		final Tile list=$("<ul/>");

		for (int i=0, size=min(terms.size(), Detail); i < size; i++) {
			list.append($("<li/>").is("link", true).append(new TermView().raw(raw).term(terms.get(i))));
		}

		if ( terms.size() > Detail ) {
			list.append($("<li><a title='More items'/></li>")

					.click(new Action<Event>() {
						@Override public void execute(final Event e) { open(term, link); }
					}));
		}

		return list;
	}

	private Tile terms(final Term term, final Term link, final Collection<Term> terms) {
		return $("<button/>")

				.title("Open set")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) {
						open(term, link);
					}
				});
	}


	//// Sections //////////////////////////////////////////////////////////////////////////////////////////////////////

	private Map<Term, List<Term>> primary(final BookData entries) {

		entries.hidden(Sesame.SesameDirectType);

		final Map<Term, List<Term>> primary=new LinkedHashMap<>();

		for (final Term link : asList( // insert types

				RDF.RDFType

		)) { primary.putAll(entries.collections(link)); }

		primary.putAll(entries.resources(new BookData.Filter() {

			@Override public boolean test(final Term link, final Term term) {
				return term.isLiteral()
						&& term.getText().length() > TermView.LineLength
						&& Specs.Langs.contains(term.getLang()); // include only preferred languages
			}

		}));

		return primary;
	}


	private Map<Term, List<Term>> spatial(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) {
				return term.isSpatial() || GeoLinks.contains(link);
			}
		});
	}

	private Map<Term, List<Term>> temporal(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) {
				return term.isTemporal() || TimeLinks.contains(link);
			}
		});
	}

	private Map<Term, List<Term>> numeric(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) { return term.isNumeric(); }
		});
	}

	private Map<Term, List<Term>> textual(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) {
				return term.isTagged() ? Specs.Langs.contains(term.getLang()) : term.isLiteral();
			}
		});
	}


	private Map<Term, List<Term>> scheme(final BookData entries) {

		entries.inverse(SKOS.SKOSHasTopConcept, SKOS.SKOSTopConceptOf);

		final Map<Term, List<Term>> scheme=new LinkedHashMap<>();

		for (final Term link : asList(

				SKOS.SKOSHasTopConcept,
				SKOS.SKOSInScheme.reverse()

		)) { scheme.putAll(entries.resources(link)); }

		return scheme;
	}

	private Map<Term, List<Term>> concept(final BookData entries) { // Class/SKOS concept properties

		entries.inverse(SKOS.SKOSTopConceptOf, SKOS.SKOSHasTopConcept);

		final Map<Term, List<Term>> concept=new LinkedHashMap<>();

		for (final Term link : asList(

				RDFS.RDFSDomain,
				RDFS.RDFSRange,

				SKOS.SKOSTopConceptOf,
				SKOS.SKOSInScheme,

				SKOS.SKOSPrefLabel,
				SKOS.SKOSAltLabel,
				SKOS.SKOSHiddenLabel,

				SKOS.SKOSNotation,
				SKOS.SKOSDefinition,

				SKOS.SKOSNote,
				SKOS.SKOSScopeNote,
				SKOS.SKOSHistoryNote,
				SKOS.SKOSEditorialNote,
				SKOS.SKOSChangeNote,

				SKOS.SKOSExample,

				SKOS.SKOSExactMatch,
				SKOS.SKOSCloseMatch

		)) { concept.putAll(entries.resources(link)); }

		return concept;
	}

	private Map<Term, List<Term>> related(final BookData entries) {

		entries.symmetric(SKOS.SKOSRelated);
		entries.symmetric(SKOS.SKOSRelatedMatch);

		final Map<Term, List<Term>> related=new LinkedHashMap<>();

		for (final Term link : asList(

				SKOS.SKOSRelated,
				SKOS.SKOSRelatedMatch

		)) { related.putAll(entries.resources(link)); }

		return related;
	}

	private Map<Term, List<Term>> broader(final BookData entries) {

		entries.hidden(SKOS.SKOSBroaderTransitive);
		entries.inverse(SKOS.SKOSBroader, SKOS.SKOSNarrower);
		entries.inverse(SKOS.SKOSBroadMatch, SKOS.SKOSNarrowMatch);

		final Map<Term, List<Term>> broader=new LinkedHashMap<>();

		for (final Term link : asList(

				RDFS.RDFSSubClassOf,
				RDFS.RDFSSubPropertyOf,

				SKOS.SKOSBroader,
				SKOS.SKOSBroadMatch

		)) { broader.putAll(entries.resources(link)); }

		return broader;
	}

	private Map<Term, List<Term>> narrower(final BookData entries) {

		entries.hidden(SKOS.SKOSNarrowerTransitive);
		entries.inverse(SKOS.SKOSNarrower, SKOS.SKOSBroader);
		entries.inverse(SKOS.SKOSNarrowMatch, SKOS.SKOSBroadMatch);

		final Map<Term, List<Term>> narrower=new LinkedHashMap<>();

		for (final Term link : asList(

				RDFS.RDFSSubClassOf.reverse(),
				RDFS.RDFSSubPropertyOf.reverse(),

				SKOS.SKOSNarrower,
				SKOS.SKOSNarrowMatch

		)) { narrower.putAll(entries.resources(link)); }

		return narrower;
	}


	private Map<Term, List<Term>> resources(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) { return ResourcesLinks.contains(link); }
		});
	}

	private Map<Term, List<Term>> annotations(final BookData entries) {
		return entries

				.inverse(FOAF.FOAFIsPrimaryTopicOF, FOAF.FOAFPrimaryTopic)

				.resources(new BookData.Filter() {
					@Override public boolean test(final Term link, final Term term) { return AnnotationLinks.contains(link); }
				});
	}

	private Map<Term, List<Term>> aliases(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) { return AliasesLink.contains(link); }
		});
	}


	private Map<Term, List<Term>> slots(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) { return link.isRecto() && term.isRecto(); }
		});
	}

	private Map<Term, List<Term>> roles(final BookData entries) {
		return entries.resources(new BookData.Filter() {
			@Override public boolean test(final Term link, final Term term) { return link.isVerso() && term.isRecto(); }
		});
	}


	//// Memo //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private int index() {
		return report.memo() != null ? report.<Memo>memo().getIndex() : 0;
	}

	private void index(final int index) {
		root().fire(new State(report.memo(new Memo(index, 0))));
	}


	private float scroll() {
		return report.memo() != null ? report.<Memo>memo().getScroll() : 0;
	}

	private void scroll(final float scroll) {
		if ( report != null && abs(scroll()-scroll) > 0 ) { // scroll event before the report is set (e.g. when backtracking from history)
			root().fire(new State(report.memo(new Memo(index(), scroll))));
		}
	}


	@Bean({"index", "scroll"}) public static final class Memo {

		private final int index;
		private final float scroll;


		public Memo(final int index, final float scroll) {
			this.index=index;
			this.scroll=scroll;
		}


		public int getIndex() {
			return index;
		}

		public float getScroll() {
			return scroll;
		}

	}

}
