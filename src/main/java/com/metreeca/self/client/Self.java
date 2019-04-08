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


package com.metreeca.self.client;

import com.metreeca._jeep.client.Option;
import com.metreeca._jeep.client.Options;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca._tile.client.plugins.Protocol;
import com.metreeca.self.client.ports.ErrorPort;
import com.metreeca.self.client.ports.ItemPort;
import com.metreeca.self.client.ports.StorePort;
import com.metreeca.self.client.views.Lock;
import com.metreeca.self.shared.Item;
import com.metreeca.self.client.ports.ReportPort;
import com.metreeca.self.client.ports.SPARQLPort;
import com.metreeca.self.client.ports.ShapePort;
import com.metreeca.self.client.views.ReportPage;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.sparql.Query;

import com.google.gwt.core.client.EntryPoint;

import static com.metreeca._tile.client.Action.throttled;
import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.Window.window;


public final class Self implements EntryPoint {

	public enum Browser {

		Firefox {
			@Override protected String upgrade() {
				return "http://www.mozilla.org/firefox/new/";
			}
		},

		Chrome {
			@Override protected String upgrade() { return "https://www.google.com/chrome/"; }
		},

		Edge {
			@Override protected String upgrade() { return "https://www.microsoft.com/en-us/download/details.aspx?id=48126"; }

			@Override public String toString() { return "Microsoft Edge"; }
		},

		Explorer {
			@Override protected String upgrade() { return "http://windows.microsoft.com/internet-explorer/"; }

			@Override public String toString() { return "Internet Explorer"; }
		},

		Safari {
			@Override protected String upgrade() { return "https://www.apple.com/safari/"; }

			@Override public String toString() { return "Safari/OSX"; }
		},

		SafariIOS {
			@Override protected String upgrade() { return "https://www.apple.com/ios/"; }

			@Override public String toString() { return "Safari/iOS"; }
		};

		protected abstract String upgrade(); // return the url of the upgrade site

	}

	public enum Mode {App, Catalog, Navigator, Visualizer}

	public enum Toolbar {Top, Bottom, None}

	public enum Fields {Top, None}

	public enum Facets {Right, Left, None}


	public static final Option<String> Hash=option("", ""); // content to be shown (e.g. a shared document handle)

	public static final Option<Boolean> Embedded=option("embedded", false); // true if running in an iframe inside a host page
	public static final Option<String> Endpoint=option("endpoint", "");
	public static final Option<String> Resource=option("resource", "");
	public static final Option<String> Collection=option("collection", "");
	public static final Option<String> Label=option("label", "");


	private static <T> Option<T> option(final String name, final T fallback) {

		if ( name == null ) {
			throw new NullPointerException("null name");
		}

		if ( fallback == null ) {
			throw new NullPointerException("null fallback");
		}

		return new Option<T>() {

			@Override public String name() { return name; }

			@Override public T fallback() { return fallback; }

		};
	}


	@Override public void onModuleLoad() {

		final String build=window().storage(true).get("self.build");

		if ( build == null ) {  // ;( remove legacy serialized beans from session/local storage
			window().storage(false).clear();
			window().storage(true).clear();
		}


		final Tile root=$("body").title(""); // remove the loading indicator

		window() // manage and broadcast window events

				.bind("resize", throttled(500, new Action<Event>() {
					@Override public void execute(final Event event) { root.<Bus>as().resize(); }
				}))

				.bind("beforeunload", new Action<Event>() {
					@Override public void execute(final Event event) { root.<Bus>as().close(false); }
				})

				.bind("unload", new Action<Event>() {
					@Override public void execute(final Event event) { root.<Bus>as().close(true); }
				});

		if ( !supported() ) {

			upgrade(root);

		} else if ( window().embedded() ) {

			embed(root);

		} else {

			load(root).<Bus>as().setup(run(new Options(window().hash())));

		}

	}


	private void embed(final Tile root) { // wait for driver to send options

		final String metreeca="metreeca"; // prefix for messages sent to/from the driver

		final String identity=metreeca+":identity:"+Item.UUID()+":"; // targeted driver activation message
		final String options=metreeca+":options:"; // generic driver configuration message
		final String escape=metreeca+":escape"; // generic driver configuration message

		window()

				.message(new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.data() instanceof String ) {

							final String data=e.data();

							if ( data.startsWith(identity) ) { // broadcast message with initial driver options

								final Options opts=new Options(data.substring(identity.length()));

								load(root).<Bus>as()

										.setup(run(opts))
										.options(opts);

							} else if ( data.startsWith(options) ) { // direct message with driver options

								final Options opts=new Options(data.substring(options.length()));

								root.<Bus>as()

										.options(opts);

							} else if ( data.equals(escape) ) { // user interaction outside embedding iframe

								Overlay.overlays().close();

							}
						}
					}
				})

				.parent()

				.message(identity, "*"); // ask driver for options
	}

	private Tile load(final Tile root) {
		return root

				.append(new ErrorPort()) // user reporting
				.append(new ItemPort()) // item codec
				.append(new StorePort()) // item storage

				.append(new Lock()) // activity reporting

				.<Protocol>as()

				.exec(true) // custom undo/redo
				.file(true) // custom file management
				.search(true) // custom search
				.select(true) // custom selection
				.zoom(true) // custom zooming
				.move(true) // custom moving

				.as();
	}

	private Tile upgrade(final Tile root) {

		final Tile browsers=$("<ul/>");

		for (final Browser browser : Browser.values()) {

			final int version=supported(browser);

			if ( version > 0 ) {
				browsers.append($("<li/>").append($("<a/>")
						.attribute("href", browser.upgrade()).text(browser+" "+version+"+")));
			}
		}

		return root.append($("<div/>").is("warning", true)

				.skin(":scope > ul > li { list-style: none; } ")

				.append($("<p>You browser is not supported!<br/>Switch or upgrade to:</p>"))
				.append(browsers));
	}


	private int supported(final Browser browser) {
		return browser == Browser.Chrome ? 63
				: browser == Browser.Firefox ? 57
				: browser == Browser.Safari ? 10
				: browser == Browser.Edge ? 15
				: browser == Browser.Explorer ? 11
				: 0;
	}

	private Setup run(final Options options) {

		$("body")

				.append(new ReportPort()) // report codec
				.append(new ShapePort()) // query engine
				.append(new SPARQLPort()) // sparql client

				.append(new ReportPage());


		//// probes ////////////////////////////////////////////////////////////////////////////////////////////////////

		final boolean embedded=window().embedded();


		//// default options ///////////////////////////////////////////////////////////////////////////////////////////

		final Mode mode=options.token(Mode.class);

		final boolean catalog=mode == Mode.Catalog;
		final boolean navigator=mode == Mode.Navigator;
		final boolean visualizer=mode == Mode.Visualizer;

		final Options defaults=new Options()

				.token(Toolbar.class, embedded ? Toolbar.Bottom : null)

				.token(Fields.class, navigator || visualizer ? Fields.None : null)
				.token(Facets.class, navigator || visualizer ? Facets.None : null);


		//// system-controlled options /////////////////////////////////////////////////////////////////////////////////

		final Options system=new Options()

				.options(defaults)
				.options(options)

				.flag(Embedded, embedded);


		//// global css drivers ////////////////////////////////////////////////////////////////////////////////////////

		$("html").is("embedded", embedded)

				.is(Mode.class, false).is(system.token(Mode.class), true)
				.is(Toolbar.class, false).is(system.token(Toolbar.class), true)
				.is(Fields.class, false).is(system.token(Fields.class), true)
				.is(Facets.class, false).is(system.token(Facets.class), true);

		return new Setup(system);

	}


	private native boolean supported() /*-{

		var self=this;

		function supported(browser, version) {

			// enable browser-specific hacks

			var agent=browser.@com.metreeca.self.client.Self.Browser::name()()
					.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase(); // camel > hyphen

			var classes=$doc.documentElement.classList;

			classes.add(agent);
			classes.add(agent+"-"+version);

			// test compatibility

			return (self.@com.metreeca.self.client.Self::supported(Lcom/metreeca/self/client/Self$Browser;)(browser) || undefined) <= Number(version);
		}

		var agent=$wnd.navigator.userAgent.toLowerCase();

		var edge=agent.match(/\bedge\/(\d+)/); // ;( test as first option as user agent matches also chrome/safari
		var explorer=agent.match(/\btrident\/.*\brv:(\d+)/);
		var firefox=agent.match(/\bfirefox\/(\d+)/);
		var chrome=agent.match(/\bchrome\/(\d+)/);
		var safari=agent.match(/\bversion\/(\d+)(?:\.\d+)*\s+safari\//);
		var safari_ios=agent.match(/\bversion\/(\d+)(?:\.\d+)*\s+mobile\/\S+\s+safari\//);

		return edge ? supported(@com.metreeca.self.client.Self.Browser::Edge, edge[1])
				: explorer ? supported(@com.metreeca.self.client.Self.Browser::Explorer, explorer[1])
						: firefox ? supported(@com.metreeca.self.client.Self.Browser::Firefox, firefox[1])
								: chrome ? supported(@com.metreeca.self.client.Self.Browser::Chrome, chrome[1])
										: safari ? supported(@com.metreeca.self.client.Self.Browser::Safari, safari[1])
												: safari_ios ? supported(@com.metreeca.self.client.Self.Browser::SafariIOS, safari_ios[1])
														: false;

	}-*/;


	public static final class Setup {

		private final int major; // major version
		private final int minor; // minor version
		private final int patch; // patch number
		private final int build; // build number

		private final Options options;


		protected Setup(final Options options) {
			this(Report.Major, Report.Minor, Report.Patch, Report.Build, options);
		}

		protected Setup(final int major, final int minor, final int patch, final int build, final Options options) {

			if ( major < 0 ) {
				throw new IllegalArgumentException("illegal major version ["+major+"]");
			}

			if ( minor < 0 ) {
				throw new IllegalArgumentException("illegal minor version ["+minor+"]");
			}

			if ( patch < 0 ) {
				throw new IllegalArgumentException("illegal patch number ["+patch+"]");
			}

			if ( build < 0 ) {
				throw new IllegalArgumentException("illegal build number ["+build+"]");
			}

			if ( options == null ) {
				throw new NullPointerException("null options");
			}

			this.major=major;
			this.minor=minor;
			this.patch=patch;
			this.build=build;

			this.options=options;
		}


		public Class<Report> type() {  // the item type managed by the app
			return Report.class;
		}

		public Report item(final Options options) { // a new item created from the given option values

			final String endpoint=options.string(Endpoint);

			final String resource=options.string(Resource);
			final String collection=options.string(Collection);
			final String label=options.string(Label);

			final Report report=new Report().setEndpoint(endpoint);

			if ( !resource.isEmpty() ) {
				report.setSpecs(Specs.Resource(resource, label));
			}

			if ( !collection.isEmpty() ) {
				report.setSpecs(Specs.Collection(collection, label));
			}

			return report;
		}


		public int major() { return major; }

		public int minor() { return minor; }

		public int patch() { return patch; }

		public int build() { return build; }


		public boolean flag(final Option<Boolean> option) {

			if ( option == null ) {
				throw new NullPointerException("null option");
			}

			return options.flag(option);
		}

		public String string(final Option<String> option) {

			if ( option == null ) {
				throw new NullPointerException("null option");
			}

			return options.string(option);

		}

		public <O extends Enum<O>> O token(final Class<O> option) {

			if ( option == null ) {
				throw new NullPointerException("null option");
			}

			return options.token(option);

		}

		public <O extends Enum<O>> boolean token(final O option) {

			if ( option == null ) {
				throw new NullPointerException("null option");
			}

			return token(option.getDeclaringClass()) == option;
		}

	}

	public static final class Bus extends Plugin {

		protected Bus() {}


		public Bus setup(final Setup setup) { return value("setup", setup, true); }

		public Bus setup(final Action<Setup> action) { return action("setup", action); }


		public Bus error(final Throwable error) { return value("error", error, false); }

		public Bus error(final Action<Throwable> action) { return action("error", action); }


		// true > a user-visible activity was initiated
		// false > a user-visible activity was completed
		// null > all user-visible activities were cancelled

		public Bus activity(final Boolean activity) { return value("activity", activity, false); }

		public Bus activity(final Action<Boolean> action) { return action("activity", action); }


		public Bus resize() { return value("resize", null, false); }

		public Bus resize(final Action<Void> action) {return action("resize", action); }


		public Bus close(final boolean immediate) { return value("close", immediate, false); }

		public Bus close(final Action<Boolean> action) { return action("close", action); }


		public Bus reset(final boolean force) { return value("reset", force, false); }

		public Bus reset(final Action<Boolean> action) { return action("reset", action); }


		public Bus options(final Options options) { return value("options", options, true); }

		public Bus options(final Action<Options> action) { return action("options", action); }



		public Bus query(final Query query) { return value("query", query, false); }

		public Bus query(final Action<Query> action) { return action("query", action); }


		public Bus sampling(final Boolean sampling) { return value("sampling", sampling, true); }

		public Bus sampling(final Action<Boolean> action) { return action("sampling", action); }


		public Bus slicing(final Boolean slicing) { return value("slicing", slicing, true); }

		public Bus slicing(final Action<Boolean> action) { return action("slicing", action); }


		private native Bus value(final String event, final Object value, final boolean persistent) /*-{

			if ( persistent ) { // before dispatching to handle re-entrant calls
				($wnd.$nmtcToolBusStates || ($wnd.$nmtcToolBusStates={}))[event]=value;
			}

			var listeners=$doc.querySelectorAll(".bus-listener");

			for (var i=0; i < listeners.length; ++i) {

				var events=listeners[i].$nmtcToolBusEvents || (listeners[i].$nmtcToolBusEvents={});
				var actions=events[event] || (events[event]=[]);

				for (var j=0; j < actions.length; ++j) {
					actions[j].@com.metreeca._tile.client.Action::execute(*)(value);
				}

			}

			return this;

		}-*/;

		private native Bus action(final String event, final Action<?> action) /*-{

			if ( action ) {
				for (var i=0; i < this.length; ++i) {

					this[i].classList.add("bus-listener");

					var events=this[i].$nmtcToolBusEvents || (this[i].$nmtcToolBusEvents={});
					var actions=events[event] || (events[event]=[]);

					actions.push(action);

					var state=($wnd.$nmtcToolBusStates || ($wnd.$nmtcToolBusStates={}))[event];

					if ( state !== undefined ) {
						action.@com.metreeca._tile.client.Action::execute(*)(state);
					}

				}
			}

			return this;

		}-*/;

	}

}
