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


package com.metreeca._tool.client;

import com.metreeca._jeep.client.Option;
import com.metreeca._jeep.client.Options;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca._tile.client.plugins.Protocol;
import com.metreeca._tool.client.ports.ErrorPort;
import com.metreeca._tool.client.ports.ItemPort;
import com.metreeca._tool.client.ports.StorePort;
import com.metreeca._tool.client.views.Lock;
import com.metreeca._tool.shared.Item;

import com.google.gwt.core.client.EntryPoint;

import static com.metreeca._tile.client.Action.throttled;
import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.Window.window;


public abstract class Tool<T extends Item<T>> implements EntryPoint {

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


	public static final Option<String> Hash=option("", ""); // content to be shown (e.g. a shared document handle)

	public static final Option<Boolean> Embedded=option("embedded", false); // true if running in an iframe inside a host page


	protected static <T> Option<T> option(final String name, final T fallback) {

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
					@Override public void execute(final Event event) { root.<Bus<T, ?>>as().resize(); }
				}))

				.bind("beforeunload", new Action<Event>() {
					@Override public void execute(final Event event) { root.<Bus<T, ?>>as().close(false); }
				})

				.bind("unload", new Action<Event>() {
					@Override public void execute(final Event event) { root.<Bus<T, ?>>as().close(true); }
				});

		if ( !supported() ) {

			upgrade(root);

		} else if ( window().embedded() ) {

			embed(root);

		} else {

			load(root).<Bus<T, ?>>as().setup(run(new Options(window().hash())));

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

								load(root).<Bus<T, ?>>as()

										.setup(run(opts))
										.options(opts);

							} else if ( data.startsWith(options) ) { // direct message with driver options

								final Options opts=new Options(data.substring(options.length()));

								root.<Bus<T, ?>>as()

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


	protected abstract int supported(final Browser browser);

	protected abstract Setup<T> run(final Options options);


	private native boolean supported() /*-{

		var self=this;

		function supported(browser, version) {

			// enable browser-specific hacks

			var agent=browser.@com.metreeca._tool.client.Tool.Browser::name()()
					.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase(); // camel > hyphen

			var classes=$doc.documentElement.classList;

			classes.add(agent);
			classes.add(agent+"-"+version);

			// test compatibility

			return (self.@com.metreeca._tool.client.Tool::supported(Lcom/metreeca/_tool/client/Tool$Browser;)(browser) || undefined) <= Number(version);
		}

		var agent=$wnd.navigator.userAgent.toLowerCase();

		var edge=agent.match(/\bedge\/(\d+)/); // ;( test as first option as user agent matches also chrome/safari
		var explorer=agent.match(/\btrident\/.*\brv:(\d+)/);
		var firefox=agent.match(/\bfirefox\/(\d+)/);
		var chrome=agent.match(/\bchrome\/(\d+)/);
		var safari=agent.match(/\bversion\/(\d+)(?:\.\d+)*\s+safari\//);
		var safari_ios=agent.match(/\bversion\/(\d+)(?:\.\d+)*\s+mobile\/\S+\s+safari\//);

		return edge ? supported(@com.metreeca._tool.client.Tool.Browser::Edge, edge[1])
				: explorer ? supported(@com.metreeca._tool.client.Tool.Browser::Explorer, explorer[1])
						: firefox ? supported(@com.metreeca._tool.client.Tool.Browser::Firefox, firefox[1])
								: chrome ? supported(@com.metreeca._tool.client.Tool.Browser::Chrome, chrome[1])
										: safari ? supported(@com.metreeca._tool.client.Tool.Browser::Safari, safari[1])
												: safari_ios ? supported(@com.metreeca._tool.client.Tool.Browser::SafariIOS, safari_ios[1])
														: false;

	}-*/;


	public abstract static class Setup<T extends Item<T>> {

		private final int major; // major version
		private final int minor; // minor version
		private final int patch; // patch number
		private final int build; // build number

		private final Options options;


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


		public abstract Class<T> type(); // the item type managed by the app

		public abstract T item(final Options options); // a new item created from the given option values


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

	public abstract static class Bus<T extends Item<T>, B extends Bus<T, B>> extends Plugin {

		protected Bus() {}


		public final B setup(final Setup<T> setup) { return value("setup", setup, true); }

		public final B setup(final Action<Setup<T>> action) { return action("setup", action); }


		public final B error(final Throwable error) { return value("error", error, false); }

		public final B error(final Action<Throwable> action) { return action("error", action); }


		// true > a user-visible activity was initiated
		// false > a user-visible activity was completed
		// null > all user-visible activities were cancelled

		public final B activity(final Boolean activity) { return value("activity", activity, false); }

		public final B activity(final Action<Boolean> action) { return action("activity", action); }


		public final B resize() { return value("resize", null, false); }

		public final B resize(final Action<Void> action) {return action("resize", action); }


		public final B close(final boolean immediate) { return value("close", immediate, false); }

		public final B close(final Action<Boolean> action) { return action("close", action); }


		public final B reset(final boolean force) { return value("reset", force, false); }

		public final B reset(final Action<Boolean> action) { return action("reset", action); }


		public final B options(final Options options) { return value("options", options, true); }

		public final B options(final Action<Options> action) { return action("options", action); }


		protected final native B value(final String event, final Object value, final boolean persistent) /*-{

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

		protected final native B action(final String event, final Action<?> action) /*-{

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
