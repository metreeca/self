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

import com.metreeca._bean.client.ClientMeta;
import com.metreeca._bean.client.ClientNode;
import com.metreeca._bean.shared.Bean;
import com.metreeca._bean.shared.NodeCodec;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.Tile;
import com.metreeca._tile.client.plugins.Menu;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca._tool.client.forms.Export;
import com.metreeca._tool.client.forms.State;
import com.metreeca._tool.client.views.Page;
import com.metreeca._tool.shared.Item;
import com.metreeca._tool.shared.Item.Lens;
import com.metreeca.self.client.Self;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.Self.Mode;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;
import static com.metreeca._tool.client.Tool.Embedded;
import static com.metreeca._tool.client.Tool.Setup;


public final class ReportPage extends Page<Report> {

	private static final int EndpointLimit=5; // endpoints cache size limit

	private static final String EndpointsKey=GWT.getModuleName()+".endpoints"; // storage key for the list of known endpoints

	private static final NodeCodec codec=new NodeCodec(ClientMeta.factory(), ClientNode.factory());
	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("ReportPage.css") TextResource skin();

	}


	private Setup<Report> setup;
	private Cache<String> endpoints;

	private final Map<String, String> storage=root().window().storage(true);


	private final Tile faces=$faces();


	public ReportPage() {
		root()

				.skin(resources.skin().getText())


				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				})

				.bind(Specs.class, new Action<Event>() {
					@Override public void execute(final Event e) { specs(e.<Specs>data()); }
				})

				.bind(Path.class, new Action<Event>() {
					@Override public void execute(final Event e) { path(e.<Path>data()); }
				})

				.bind(Term.class, new Action<Event>() {
					@Override public void execute(final Event e) { term(e.<Term>data()); }
				})

				.<Bus>as()

				.setup(new Action<Setup<Report>>() {
					@Override public void execute(final Setup<Report> setup) { setup(setup); }
				});
	}


	//// Placeholders //////////////////////////////////////////////////////////////////////////////////////////////////

	private Tile $tools(final Setup<Report> setup) {
		return $("<menu/>")

				.append(setup.flag(Embedded) ? $() : editing())

				.append(setup.token(Mode.Visualizer) ? $() : faces)

				.append(setup.flag(Embedded) ? $() : $("<section/>")

						.append(locking())
						.append(configuring())
						.append(managing()));
	}

	private Tile $faces() {
		return $("<section/>")

				.append($("<button/>").is("fa table-face", true).title("Table")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Table()); }
						}))

				.append($("<button/>").is("fa board-face", true).title("Board")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Board()); }
						}))

				.append($("<button/>").is("fa book-face", true).title("Book")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Book()); }
						}))

				.append($("<button/>").is("fa charts-face", true).title("Charts")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) {
								$charts().open().align(e.current(),
										Align.Start,
										setup.token(Self.Toolbar.Bottom) ? Align.Before : Align.After);
							}

						}))

				.append($("<button/>").is("fa marker-map", true).title("Map")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Marker()); }
						}));
	}

	private Menu $charts() {
		return $("<menu/>")

				.style("margin-left", "-0.35em") // align with toolbar icon

				.append($("<button/>").is("fa pie-chart", true)

						.text("Pie")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Pie()); }
						}))

				.append($("<button/>").is("fa bar-chart", true)

						.text("Bars")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Bar()); }
						}))

				.append($("<button/>").is("fa line-chart", true)

						.text("Lines")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Line()); }
						}))

				.append($("<button/>").is("fa bubble-chart", true)

						.text("Bubbles")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { show(new Report.Bubble()); }
						}))

				.as();
	}


	private Tile $body() {
		return $(new SpecsView().locked(locked()));
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected Overlay info(final Report report) {
		return new ReportInfo()
				.locked(locked())
				.endpoints(endpoints.getItems())
				.report(report)
				.as();
	}


	@Override protected Menu manage() {
		return $("<menu/>")

				.append(opening())
				.append(connecting())
				.append(archiving())
				.append(porting())

				.append(exporting())

				.as();
	}

	private Tile connecting() {
		return $("<section/>")

				.append($("<command/>").text("Connect To…").click(new Action<Event>() {
					@Override public void execute(final Event e) {
						new EndpointInfo().endpoints(endpoints.getItems()).open();
					}
				}));
	}

	private Tile exporting() {
		return $("<section/>")

				.append($("<command/>").text("Export As CSV…").click(new Action<Event>() {
					@Override public void execute(final Event e) {
						e.current().async(new Export<>(item(), "text/csv"));
					}
				}));
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setup(final Setup<Report> setup) {

		this.setup=setup;

		// configure interface

		tools($tools(setup));
		body($body());

		// recover endpoint cache

		if ( endpoints == null ) {

			final String endpoints=storage.get(EndpointsKey);

			this.endpoints=(endpoints == null
					? new Cache<String>()
					: codec.<Cache<String>>decode(endpoints)).setLimit(EndpointLimit).trim();
		}
	}

	private void report(final Report report) {

		faces.children().enabled(!report.isLocked());

		final String endpoint=report.getEndpoint();

		// update endpoint cache

		if ( !endpoint.isEmpty() && endpoints.insertItem(endpoint) ) {
			storage.put(EndpointsKey, codec.encode(endpoints));
		}

		// newly created report with no assigned endpoint > assign one

		if ( report.getCreated() == report.getUpdated() && endpoint.isEmpty() && !locked() ) {
			root().async(new Action<Event>() { // avoid concurrent report modification
				@Override public void execute(final Event event) {

					if ( endpoints.getSize() == 0 ) { // no known endpoints: ask user
						new EndpointInfo().open();
					} else { // use the last seen endpoint
						root().fire(report.updated().setEndpoint(endpoints.getItem(0)));
					}

				}
			});
		}
	}


	private void term(final Term term) {
		if ( !item().isLocked() ) {
			specs(item().getSpecs().open(term));
		}
	}

	private void path(final Path path) {
		if ( !item().isLocked() ) {
			specs(item().getSpecs().open(path));
		}
	}

	private void specs(final Specs specs) {
		if ( !item().isLocked() ) {
			root().fire(new State(item(), true)); // push the current state to history (see StorePort)
			root().async(item().updated().setSpecs(specs).setLens(new Item.Auto<Report>()));
		}
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void show(final Lens<Report> lens) {
		root().fire(lens);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * LRU cache.
	 *
	 * @param <T> the type of the cached items
	 */
	@Bean public static final class Cache<T> { // !!! factor // !!! share with jeep.shared.Cache

		private int limit; // by default 0 (no limit)

		private final List<T> items=new ArrayList<>();


		public int getLimit() {
			return limit;
		}

		public Cache<T> setLimit(final int limit) {

			if ( limit < 0 ) {
				throw new IllegalArgumentException("illegal limit ["+limit+"]");
			}

			this.limit=limit;

			return this;
		}


		public int getSize() {
			return items.size();
		}


		public T getItem(final int index) {
			return items.get(index);
		}

		public boolean insertItem(final T item) {

			if ( item == null ) {
				throw new NullPointerException("null item");
			}

			final int index=items.indexOf(item);

			items.remove(item);
			items.add(0, item);

			trim();

			return index != 0; // true if the cache was modified
		}

		public boolean removeItem(final T item) {

			if ( item == null ) {
				throw new NullPointerException("null item");
			}

			return items.remove(item); // true if the cache was modified
		}


		public List<T> getItems() {
			return Collections.unmodifiableList(items);
		}

		public Cache<T> setItems(final List<T> items) {

			if ( items == null ) {
				throw new NullPointerException("null items");
			}

			this.items.clear();

			for (final T item : items) {

				if ( item == null ) {
					throw new NullPointerException("null item ["+items+"]");
				}

				this.items.add(item);
			}

			return this;
		}


		public Cache<T> trim() {

			while ( limit > 0 && items.size() > limit ) {
				items.remove(limit);
			}

			return this;
		}


		//// Versioning ////////////////////////////////////////////////////////////////////////////////////////////////

		private static final int Layout=20170826; // layout version


		public static void encode(final Bean.Info info) {
			info.version(20170826);
		}

		public static void decode(final Bean.Info info) {

			if ( info.version() < 20170826 ) { _20170826(info); } // 0.43

			info.version(Layout);
		}

		private static void _20170826(final Bean.Info info) {
			for (final Bean.Info i : info) {
				i.type(i.type().replace("net.metreeca.", "com.metreeca.")); // migrated to com.metreeca.self pkg
			}
		}
	}

}
