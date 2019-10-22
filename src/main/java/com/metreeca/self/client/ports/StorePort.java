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

package com.metreeca.self.client.ports;

import com.metreeca._bean.client.ClientMeta;
import com.metreeca._bean.client.ClientNode;
import com.metreeca._bean.shared.*;
import com.metreeca._tile.client.*;
import com.metreeca.self.client.Self;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.views.Dialog;
import com.metreeca.self.shared.Item;
import com.metreeca.self.shared.Item.Lens;
import com.metreeca.self.shared.Options;
import com.metreeca.self.shared.forms.Items;
import com.metreeca.self.shared.forms.State;

import com.google.gwt.core.client.GWT;

import java.util.HashMap;
import java.util.Map;

import static com.metreeca._tile.client.Window.window;
import static com.metreeca.self.client.Self.Setup;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;


/**
 * <ul> <li>synchronizes item state and address bar</li> <li>persists item state</li> </ul>
 */
public final class StorePort extends View {

	private static final int FlushPeriod=30*1000; // [ms]
	private static final int Version=20170826; // stored metadata layout version number

	private static final String Module=GWT.getModuleName(); // the name of the current module

	private static final String BuildKey=Module+".build"; // storage key for the build number of the last opened version
	private static final String UUIDKey=Module+".item"; // session key for the uuid of the last opened item

	private static final String Meta=Module+".info-";
	private static final String Data=Module+".item-";

	private static final Window window=window();

	private static final Meta.Factory metaFactory=ClientMeta.factory();
	private static final Node.Factory nodeFactory=ClientNode.factory();

	private static final InfoCodec infoCodec=new InfoCodec(metaFactory);
	private static final NodeCodec nodeCodec=new NodeCodec(metaFactory, nodeFactory);


	private static native String decodeURIComponent(final String uri) /*-{

		return $wnd.decodeURIComponent(uri);

	}-*/;

	private static native String decodeBase64(final String base64) /*-{

		return $wnd.atob(base64);

	}-*/;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Setup setup;

	private long synced; // the timestamp of the last synchronization

	private final Map<String, Item<?>> cache=new HashMap<>(); // uuid > item
	private final Map<String, Item<?>> queue=new HashMap<>(); // uuid > item

	private final Map<String, String> archive=window.storage(true);
	private final Map<String, String> session=window.storage(false);

	private boolean reporting; // true if already reporting errors to the user (to prevent multiple messages)


	public <T extends Item<T>> StorePort() {
		root("<script/>")

				.bind(State.class, new Action<Event>() { // !!! migrate to bus
					@Override public void execute(final Event e) { memo(e.<State>data()); }
				})

				.bind(Item.class, new Action<Event>() { // !!! migrate to bus
					@Override public void execute(final Event e) { item(e.<Item<?>>data()); }
				})

				.bind(Items.class, new Action<Event>() { // !!! migrate to bus
					@Override public void execute(final Event e) { items(e.<Items>data()); }
				})


				.repeat(FlushPeriod, new Action<Event>() {
					@Override public void execute(final Event e) { store(); }
				})


				.<Bus>as()

				.setup(new Action<Setup>() {
					@Override public void execute(final Setup setup) { setup(setup); }
				})

				.options(new Action<Options>() {
					@Override public void execute(final Options options) { opts(options); }
				})

				.reset(new Action<Boolean>() {
					@Override public void execute(final Boolean force) { reset(force); }
				})

				.close(new Action<Boolean>() {
					@Override public void execute(final Boolean immediate) { store(); }
				})


				.<Tile>as().window() // !!! leakage > migrate to bus

				.popstate(new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( window.hash().isEmpty() ) { data(window.state()); }
					}
				})

				.hashchange(new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( !window.hash().isEmpty() ) { opts(new Options(window.hash())); }
					}
				});
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void reset(final Boolean force) {
		if ( force ) {

			clear();

			root().async(new Action<Event>() {
				@Override public void execute(final Event event) {
					root().window().reload(true);
				}
			});

		} else {

			new Dialog()

					.message("Reset storage? Really?")
					.details("You are about to reset\nthe local storage of the application: this operation can't be undone…")

					.action("Reset", new Action<Event>() {
						@Override public void execute(final Event e) { reset(true); }
					})

					.action("Cancel")

					.open();

		}
	}

	private void setup(final Setup setup) {

		if ( this.setup == null ) { // initializing > synchronize content to location
			root().async(new Action<Event>() {
				@Override public void execute(final Event event) { // after setup is completed by all listeners
					if ( !window.state().isEmpty() ) {

						data(window.state());

					} else if ( !window.embedded() ) { // if embedded wait for driver instructions

						final String hash=window.hash();

						if ( hash.isEmpty() ) {

							uuid(session.get(UUIDKey)); // try to reopen the last session item

						} else {

							opts(new Options(hash));

						}

					}
				}
			});
		}

		this.setup=setup;
	}


	private void memo(final State memo) {
		state(memo.item(), memo.push());
	}

	private void item(final Item<?> item) {

		final Item<?> fetch=fetch(item.getUUID());
		final boolean rollback=fetch != null && item.getArrayed() < fetch.getArrayed();

		if ( item instanceof Info ) { // meta item (eg when opening an item retrieved from Items)

			if ( fetch == null ) {
				throw new IllegalStateException("unknown info uuid ["+item.getUUID()+"]");
			}

			root().async(fetch);

		} else if ( rollback && !window.embedded() ) { // version rollback (eg when importing)

			rollback(item, fetch);

		} else {

			store(item, false);
			state(item, false);

		}
	}

	private void items(final Items items) {
		if ( !items.fulfilled() ) {

			for (final Item<?> item : items.getItems()) {
				store(item, false);
			}

			for (final Item<?> item : fetch()) {
				items.insertItem(item);
			}

			root().async(items.done());
		}
	}


	//// Inputs ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void uuid(final String uuid) {

		final Item<?> item=(uuid != null && !uuid.isEmpty()) ? fetch(uuid) : null;

		root().async(item != null ? item : setup.item(new Options()).created());
	}

	private void data(final String data) {

		final Item<?> item=data.isEmpty() ? null : item(data);
		final Item<?> fetch=data.isEmpty() ? null : fetch(item.getUUID());

		// ignore data if fetch is null (after resetting the system or removing an item from the storage the
		// history may still contain previous states for that item, i.e. fetch is null and data is not empty)

		if ( item == null || fetch == null ) { // not stored or already deleted >> create a new item

			root().async(setup.item(new Options()).created());

		} else {

			root().async(item // restore metadata from the latest version
					.setLabel(fetch.getLabel())
					.setNotes(fetch.getNotes())
					.setUpdated(fetch.getUpdated())
					.setArrayed(max(currentTimeMillis(), synced+1))); // force syncing avoiding locks

		}

	}

	private <T extends Item<T>> void opts(final Options opts) {

		final String hash=opts.string(Self.Hash);

		if ( hash.isEmpty() ) { // a definition form

			root().async(setup.item(opts).created());

		} else {

			try { // encodeURIComponent(btoa())-encoded report

				root().async(nodeCodec.<T>decode(decodeBase64(decodeURIComponent(hash))));

			} catch ( final Exception e ) { // malformed document

				warning("Document Not Found", new RuntimeException("malformed document", e),
						"The link you are trying to open was not generated by this app or was corrupted…");

				root().warning(getClass(), "malformed document", e);

			}

		}
	}


	//// State /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void state(final Item<?> item, final boolean push) {

		final String label=item.getLabel();
		final String state=item.getState();
		final String title=(state.isEmpty() ? "" : state+" | ")+(label.isEmpty() ? "Metreeca" : label);

		final String data=data(item);
		final String path=window.path(); // clears query/hash (using '.' would cause page reloads in embedded mode)

		final boolean open=!window.state().isEmpty() && !window.state().contains(item.getUUID()); // new item

		if ( push || open ) {
			window.pushstate(data, title, path);
		} else {
			window.state(data, title, path);
		}

		window.title(title);
		session.put(UUIDKey, item.getUUID()); // record UUID for future session use

	}


	private String data(final Item<?> item) {

		final long start=currentTimeMillis();

		final Map<Lens<?>, Object> memos=new HashMap<>();

		for (final Lens<? extends Item<?>> lens : item.getLenses()) {

			final Object memo=lens.memo();

			if ( memo != null ) { // ;( guards against unexpected javascript internal errors
				memos.put(lens, memo);
			}
		}

		final String data=nodeCodec.encode(new Memo(item, memos));

		final long stop=currentTimeMillis();

		info("encoded "+label(item)+" to history in "+(stop-start)+" ms");

		return data;
	}

	private Item<?> item(final String data) {

		final long start=currentTimeMillis();

		final Memo memo=nodeCodec.decode(data);

		for (final Map.Entry<Lens<?>, Object> entry : memo.getMemos().entrySet()) {
			entry.getKey().memo(entry.getValue());
		}

		final Item<?> item=memo.getItem();

		final long stop=currentTimeMillis();

		info("decoded "+label(item)+" from history in "+(stop-start)+" ms");

		return item;
	}


	//// Store /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Item<?> fetch(final String uuid) {

		sync(); // prefetch to prime sync timestamp // !!! avoid decoding all infos (mark UUIDs with null?)

		final Item<?> cached=cache.get(uuid);

		if ( cached == null ) {

			return null;

		} else if ( cached instanceof Info ) { // !!! handle exceptions

			final String key=Data+uuid;
			final String data=archive.containsKey(key) ? archive.get(key) : session.get(key);

			if ( data != null ) {

				final long start=currentTimeMillis();

				final Item<?> item=nodeCodec.decode(data);

				final long stop=currentTimeMillis();

				info("fetched "+label(item)+" in "+(stop-start)+" ms");

				cache.put(uuid, info(item, cached));

				return item;

			} else {
				return cached; // !!! missing item: return what?
			}

		} else {
			return cached;
		}
	}

	private StorePort store(final Item<?> item, final boolean force) {

		final String uuid=item.getUUID();
		final boolean cached=cache.containsKey(uuid);

		if ( force || !cached || item.getUpdated() >= synced || item.getArrayed() >= synced ) {

			queue.put(uuid, item);

			if ( item instanceof Info && cached ) {
				info(cache.get(uuid), item); // update cached item
			} else {
				cache.put(uuid, item);
			}
		}

		return this;
	}


	private Iterable<Item<?>> fetch() {

		sync();

		return cache.values();
	}

	private StorePort store() {
		try {

			sync(); // prefetch to prime sync timestamp // !!! avoid decoding all infos (mark UUIDs with null?)

			for (final Item<?> item : queue.values()) {

				final long start=currentTimeMillis();

				final String uuid=item.getUUID();

				final String meta=Meta+uuid;
				final String data=Data+uuid;

				final boolean named=!item.getLabel().isEmpty();

				final Map<String, String> store=named ? archive : session;
				final Map<String, String> clear=named ? session : archive;

				if ( item instanceof Info ) {

					store.put(meta, nodeCodec.encode(item));

				} else {

					store.put(meta, nodeCodec.encode(info(item)));
					store.put(data, nodeCodec.encode(item));

				}

				clear.remove(meta);
				clear.remove(data);

				final long stop=currentTimeMillis();

				info("stored "+label(item)+" in "+(stop-start)+" ms");
			}

			queue.clear();

			synced=currentTimeMillis();

		} catch ( final RuntimeException e ) { // !!! report exceeded quota
			error("Unable to Store Report", e,
					"The browser was unable\nto write pending changes\nto the local storage: "
							+"make sure to save a backup copy of your work using the 'Export…' command.");
		}

		return this;
	}

	private StorePort clear() {

		session.clear();
		archive.clear();

		cache.clear();
		queue.clear();

		synced=0;

		return this;
	}


	private void sync() {
		if ( synced == 0 ) {

			sync(session);
			sync(archive);

			synced=currentTimeMillis();
		}
	}

	private void sync(final Map<String, String> storage) {
		for (final Map.Entry<String, String> entry : update(storage).entrySet()) {
			if ( entry.getKey().startsWith(Meta) ) {

				final Item<?> item=nodeCodec.decode(entry.getValue());

				cache.put(item.getUUID(), item);
			}
		}
	}


	//// Versioning ////////////////////////////////////////////////////////////////////////////////////////////////////

	private Map<String, String> update(final Map<String, String> storage) {

		final String meta=storage.get("meta"); // recover legacy version info (version < 0.30)

		if ( meta != null ) {
			storage.put(BuildKey, meta);
		}

		final int build=Item.Build(storage.get(BuildKey));

		if ( build > 0 && build < setup.build() ) { // !!! handle build > Build (roll-back to a previous release)

			info("upgrading storage from build "+build);

			if ( build <= 20130228 ) { _20130228(storage); } // 0.20
			if ( build <= 20140724 ) { _20140724(storage); } // 0.29

		}

		storage.put(BuildKey, String.valueOf(setup.build()));

		return storage;
	}


	private void _20130228(final Map<String, String> storage) {
		storage.clear(); // changed codecs
	}

	private void _20140724(final Map<String, String> storage) {

		final Map<String, String> memo=new HashMap<>(storage);

		storage.clear();

		for (final Map.Entry<String, String> entry : memo.entrySet()) { // renamed storage keys

			final String key=entry.getKey();
			final String value=entry.getValue();

			if ( key.endsWith(".activated") ) { // preserve activation

				storage.put(key, value);

			}

			if ( key.startsWith("info-") || key.startsWith("item-") ) { // rename documents

				if ( value.contains("com.metreeca.finder.") ) { storage.put("finder."+key, value); }
				if ( value.contains("com.metreeca.rover.") ) { storage.put("rover."+key, value); }

			}
		}
	}


	//// Reporting /////////////////////////////////////////////////////////////////////////////////////////////////////

	private String label(final Item<?> item) {

		final String type=item.getType();
		final String kind=type.substring(type.lastIndexOf('.')+1).toLowerCase();
		final String name=item.getLabel().isEmpty() ? item.getUUID() : item.getLabel();

		return kind+(item instanceof Info ? " info" : "")+' '+"["+name+']';
	}


	private void info(final String message) {
		root().info(getClass(), message);
	}

	private void warning(final String message, final Throwable cause, final String details) {
		dismiss(message, details);
	}

	private void error(final String message, final Throwable cause, final String details) {
		root().error(StorePort.class, message.toLowerCase(), cause);
		dismiss(message, details);
	}


	private void dismiss(final String message, final String details) {
		if ( !reporting ) {

			new Dialog()

					.message(message)
					.details(details)

					.action("OK", new Action<Event>() {
						@Override public void execute(final Event e) { reporting=false; }
					})

					.open();

			reporting=true;
		}
	}

	private void rollback(final Item<?> item, final Item<?> fetch) {
		new Dialog()

				.message("Document Rollback")

				.details("The archive contains a newer version of this item:\n"
						+"do you want to replace it with the one you’re importing?")

				.action("Cancel", new Action<Event>() {
					@Override public void execute(final Event e) {
						root().async(fetch); // reload current document
					}
				})

				.action("Replace", new Action<Event>() {
					@Override public void execute(final Event e) {
						store(item, true); // force store on rollback
						state(item, false);
					}
				})

				.open();
	}


	//// Metadata //////////////////////////////////////////////////////////////////////////////////////////////////////

	public Info info(final Item<?> item) {
		return new Info(item.getUUID())

				.setType(item.getType())

				.setCreated(item.getCreated())
				.setUpdated(item.getUpdated())
				.setArrayed(item.getArrayed())

				.setLabel(item.getLabel())
				.setNotes(item.getNotes());
	}

	public Item<?> info(final Item<?> item, final Item<?> info) {
		return item

				.setCreated(info.getCreated())
				.setUpdated(info.getUpdated())
				.setArrayed(info.getArrayed())

				.setLabel(info.getLabel())
				.setNotes(info.getNotes());
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean({"item", "memos"}) public static final class Memo {

		private final Item<?> item;
		private final Map<Lens<?>, Object> memos;


		public Memo(final Item<?> item, final Map<Lens<?>, Object> memos) {
			this.item=item;
			this.memos=memos;
		}


		public Item<?> getItem() {
			return item;
		}

		public Map<Lens<?>, Object> getMemos() {
			return memos;
		}

	}

	@Bean("UUID") public static final class Info extends Item<Info> { // beans must be public

		private String type;


		public Info(final String uuid) { super(uuid); }


		@Override protected Info self() {
			return this;
		}


		@Override public String getType() {
			return type;
		}

		public Info setType(final String type) {

			if ( type == null ) {
				throw new NullPointerException("null type");
			}

			this.type=type;

			return this;
		}


		public static void encode(final Bean.Info info) {
			info.version(Version);
		}

		public static void decode(final Bean.Info info) {

			if ( info.version() < 20130521 ) { _20130521(info); } // 0.23
			if ( info.version() < 20170826 ) { _20170826(info); } // 0.43

			info.version(Version);
		}


		private static void _20130521(final Bean.Info info) {

			final Object type=info.field("type");

			if ( type instanceof Bean.Info ) {
				info.field("type", ((Bean.Info)type).type()); // convert type from class literal to string
			}
		}

		private static void _20170826(final Bean.Info info) {
			for (final Bean.Info i : info) {
				i.type(i.type().replace("net.metreeca.tool.", "com.metreeca._tool.")); // migrated to com.metreeca._tool pkg
			}
		}

	}

}
