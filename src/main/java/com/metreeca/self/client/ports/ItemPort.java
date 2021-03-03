/*
 * Copyright © 2013-2021 Metreeca srl. All rights reserved.
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
import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.plugins.Protocol;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.Self.Setup;
import com.metreeca.self.client.views.Catalog;
import com.metreeca.self.client.views.Dialog;
import com.metreeca.self.shared.Item;
import com.metreeca.self.shared.Options;
import com.metreeca.self.shared.forms.Export;
import com.metreeca.self.shared.forms.Import;

import com.google.gwt.core.client.GWT;

import java.util.Map;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca.self.shared.Item.UUID;


/**
 * <ul>
 * <li>handles file protocol</li>
 * <li>handles item XML import/export</li>
 * </ul>
 */
public final class ItemPort extends View {

	private static final String App=GWT.getHostPageBaseURL(); // the url of the current app
	private static final String Module=GWT.getModuleName(); // the name of the current module

	private static final Meta.Factory metaFactory=ClientMeta.factory();
	private static final Node.Factory nodeFactory=ClientNode.factory();

	private static final InfoCodec infoCodec=new InfoCodec(metaFactory);
	private static final NodeCodec nodeCodec=new NodeCodec(metaFactory, nodeFactory);


	private static final String RenameWarningKey=Module+".warning.rename"; // storage key for the 'rename exported file' warning

	private static final Map<String, String> archive=$().window().storage(true);

	public static void renameWarning() {
		if ( $("html").is("safari") && archive.get(RenameWarningKey) == null ) {
			new Dialog()

					.message("Rename Exported File")
					.details("Sorry for the inconvenience, "
							+"but Safari isn't yet able\nto save downloaded files with meaningful names: "
							+"until the issue is fixed, please rename them manually…")

					.action("Don't Warn me Again", new Action<Event>() {
						@Override public void execute(final Event e) {
							archive.put(RenameWarningKey, "disabled");
						}
					})

					.action("OK")

					.open();
		}
	}


	private Setup setup;

	private Item<?> item; // the last opened item


	public <T extends Item<T>> ItemPort() {
		root("<script/>")

				.bind(Protocol.File.New, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() ) { tidy(); }
					}
				})

				.bind(Protocol.File.Copy, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() ) { copy(); }
					}
				})

				.bind(Protocol.File.Open, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() ) { open(); }
					}
				})

				.bind(Protocol.File.Import, new Action<Event>() {
					@Override public void execute(final Event e) { if ( e.cancel() ) { mport(); } }
				})

				.bind(Protocol.File.Export, new Action<Event>() {
					@Override public void execute(final Event e) { if ( e.cancel() ) { xport(); } }
				})

				.bind(File.class, new Action<Event>() {
					@Override public void execute(final Event e) { if ( e.cancel() ) { mport(e.<File>data()); } }
				})


				.bind(Item.class, new Action<Event>() {
					@Override public void execute(final Event e) { item(e.<Item<?>>data()); }
				})


				.bind(Import.class, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( mport(e.<Import<?>>data()) ) { e.cancel(); }
					}
				})

				.bind(Export.class, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( xport(e.<Export<?>>data()) ) { e.cancel(); }
					}
				})

				.<Bus>as()

				.setup(new Action<Setup>() {
					@Override public void execute(final Setup setup) { setup(setup); }
				});
	}


	private Item<?> item() {
		return setup.item(new Options()).created();
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setup(final Setup setup) {
		this.setup=setup;
	}

	private void item(final Item<?> item) {
		this.item=item;
	}


	private void open() {
		new Catalog().type(setup.type()).open();
	}

	private void tidy() {
		root().async(item());
	}

	private void copy() {
		if ( item != null ) {
			root().async(infoCodec.<Item<?>>decode(infoCodec.encode(item).field("UUID", UUID()))
					.setLocked(false)
					.setLabel("")
					.created());
		}
	}


	private void mport() {
		root().window().open(new Action<JSArray<File>>() {
			@Override public void execute(final JSArray<File> files) {
				root().async(files.get(0)); // !!! multiple files
			}
		});
	}

	private void mport(final File file) {
		file.read(new File.Read() {

			@Override public void read(final File file, final String text) {
				if ( root().fire(new Import<Item<?>>(item != null ? item : item(), "*/*", text)) ) {
					report(file, "Unknown File Format", "The format of the file\nyou are trying to import\nis not recognized.");
				}
			}

			@Override public void failed(final File file, final String status) {
				report(file, "File Import Failed", "Reading file "+file.name()+" failed.\nThe browser says: '"+status+"'.");
			}


			private void report(final File file, final String message, final String details) {

				root().warning(getClass(), message);
				root().log(file);

				new Dialog()

						.message(message)
						.details(details)

						.action("OK")

						.open();

			}
		});
	}

	private <T extends Item<T>> boolean mport(final Import<?> mport) {
		if ( !mport.fulfilled()
				&& mport.target().getClass().equals(setup.type())
				&& mport.type("*/*")
				&& mport.data().contains("</bean>") // start tag may contain attributes
			/*&& mport.data().contains("</"+setup.type().getName()+'>') */ ) { // !!! ;( no type checks for loading legacy rover docs

			try {

				// no expected namespace: lenient parsing to accept document exported by legacy and test versions

				root().fire(nodeCodec.<T>decode(mport.data()));

			} catch ( final Throwable e ) {

				final String message="Corrupted Document";
				final String details="The file you are trying to import is corrupted and couldn't be decoded.";

				root().warning(getClass(), message, e); // !!! report file name / format

				new Dialog()

						.message(message)
						.details(details)

						.action("OK")

						.open();

			}

			return root().fire(mport.done());

		} else {
			return false;
		}
	}


	private void xport() {
		if ( item != null ) {
			root().async(new Export<>(item, "*/*"));
		}
	}

	private boolean xport(final Export<?> xport) {
		if ( !xport.fulfilled()
				&& xport.source().getClass().equals(setup.type())
				&& xport.type("*/*") ) {

			return file((Item)xport.source()) && root().fire(xport.done()); // export as file

		} else {
			return false;
		}
	}


	private <T extends Item<T>> boolean file(final T item) {

		final String mime="application/xml;charset=utf-8";
		final String name=item.getLabel().isEmpty() ? item.getUUID() : item.getLabel();
		final String extension=".xml";

		root().window().save(mime, name, extension, nodeCodec.encode(item, App));

		renameWarning();

		return true;
	}

}
