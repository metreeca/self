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

package com.metreeca._tool.client.views;

import com.metreeca._jeep.shared.Command;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Editable;
import com.metreeca._tile.client.plugins.Menu;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca._tile.client.plugins.Protocol.Exec;
import com.metreeca._tile.client.plugins.Protocol.Home;
import com.metreeca._tool.client.Tool.Bus;
import com.metreeca._tool.shared.Item;
import com.metreeca._tool.shared.Item.Lens;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.Map;
import java.util.Objects;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;
import static com.metreeca._tile.client.plugins.Protocol.File;
import static com.metreeca._tool.client.Tool.Embedded;
import static com.metreeca._tool.client.Tool.Setup;


public abstract class Page<T extends Item<T>> extends View {

	private static final int ExecStack=100; // undo/redo stack depth
	private static final int SplashDelay=1500; // splash screen duration [ms]

	private static final String ActivationKey=GWT.getModuleName()+".activated"; // storage key for the activation flag


	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Page.css") TextResource skin();

	}


	private Setup<T> setup;

	private T item; // the open item
	private boolean locked; // if true, editing functions are disabled

	private final Command.Manager manager=new Command.Manager(ExecStack);

	private final Map<String, String> session=root().window().storage(false);
	private final Map<String, String> storage=root().window().storage(true);

	private final Tile header;
	private final Tile section;

	private final Tile logo=$(new Logo());
	private final Tile label=$label();
	private final Tile undo=$undo();
	private final Tile redo=$redo();
	private final Tile lock=$lock();
	private final Tile save=$save();
	private final Tile drop=$drop();


	protected Page() {

		root("<div/>")

				.skin(resources.skin().getText())

				.append(header=$("<header/>"))
				.append(section=$("<section/>"))


				.bind(Item.class, new Action<Event>() {
					@Override public void execute(final Event e) { item(e.<T>data()); }
				})

				.bind(Lens.class, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() && !item.isLocked() ) { lens(e.<Lens<T>>data()); }
					}
				})


				.bind(Home.About, new Action<Event>() {
					@Override public void execute(final Event event) {
						if ( event.cancel() ) { about(); }
					}
				})


				.bind(Exec.Undo, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() && !item.isLocked() ) { undone(); }
					}
				})

				.bind(Exec.Redo, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() && !item.isLocked() ) { redone(); }
					}
				})

				.bind(Command.class, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.cancel() && !item.isLocked() ) { command(e.<Command>data()); }
					}
				})


				.bind(File.Save, new Action<Event>() {
					@Override public void execute(final Event e) { if ( e.cancel() && !item.isLocked() ) { save(); } }
				})

				.bind(File.Drop, new Action<Event>() {
					@Override public void execute(final Event e) { if ( e.cancel() && !item.isLocked() ) { drop(); } }
				})

				.<Bus<T, ?>>as()

				.setup(new Action<Setup<T>>() {
					@Override public void execute(final Setup<T> setup) { setup(setup); }
				});

	}


	protected boolean locked() {
		return locked;
	}


	protected T item() {
		return item;
	}


	//// Widgets ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private Tile $powered() {
		return $("<button/>").is("powered", true)

				.text("Powered by Metreeca")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { about(); }
				});
	}

	private Tile $label() {
		return $("<div/>").is("label", true) // ;(ff) <div/> because of broken cnp on <h1/span contenteditable/>

				.<Editable>as().wire()

				.<Tile>as()

				.keydown(new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.key("any-enter", "any-return") ) { e.cancel(); } // single line
					}
				})

				.change(new Action<Event>() {
					@Override public void execute(final Event e) {
						label(e.target().<Editable>as().text(true));
					}
				})

				.menu(new Action<Event>() {
					@Override public void execute(final Event e) {
						menu().open().align(e.x(true), e.y(true));
					}
				});
	}

	private Tile $undo() {
		return $("<button/>")

				.is("fa fa-undo", true)

				.enabled(false)

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(Exec.Undo); }
				});
	}

	private Tile $redo() {
		return $("<button/>")

				.is("fa fa-undo fa-flip-horizontal", true)

				.enabled(false)

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(Exec.Redo); }
				});
	}

	private Tile $lock() {
		return $("<button/>")

				.is("fa", true)

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { lock(); }
				})

				.bind(Item.class, new Action<Event>() {
					@Override public void execute(final Event e) {

						final boolean locked=e.<T>data().isLocked();

						e.current()
								.is("fa-lock", locked)
								.is("fa-unlock", !locked)
								.title(locked ? "Unlock" : "Lock");

					}
				});
	}

	private Tile $save() {
		return $("<command/>")

				.attribute("label", "Save…")

				.enabled(false)

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.Save); }
				});
	}

	private Tile $drop() {
		return $("<command label='Trash'/>")

				.attribute("label", "Trash")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.Drop); }
				});
	}


	//// Placeholders //////////////////////////////////////////////////////////////////////////////////////////////////

	protected Page tools(final Tile tools) {

		header.clear()

				.append(setup.flag(Embedded) ? $powered() : logo)
				.append(setup.flag(Embedded) ? $() : label)
				.append(tools);

		return this;
	}

	protected Page body(final Tile body) {

		section.clear().append(body);

		return this;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected Overlay info(final T t) {
		return $().as(); // !!! abstract ItemInfo from ModelInfo
	}

	protected Menu menu() {
		return $().as();
	}

	protected Menu manage() {
		return $("<menu/>")

				.append(opening())
				.append(archiving())
				.append(porting())

				.append(printing())

				.as();
	}


	//// Standard Tools ////////////////////////////////////////////////////////////////////////////////////////////////

	protected Tile locking() {
		return lock;
	}

	protected Tile configuring() {
		return $("<button/>")

				.is("fa fa-gear", true)
				.title("Configure")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { inspect(); }
				});
	}

	protected Tile managing() {
		return $("<button/>")

				.is("fa fa-folder-open", true)
				.title("Manage")

				.click(new Action<Event>() {
					@Override public void execute(final Event e) {
						manage().open().align(e.current(), Align.End, Align.After);
					}
				});
	}


	//// Standard Menu Sections ////////////////////////////////////////////////////////////////////////////////////////

	protected Tile renaming() {
		return $("<section/>").unless(locked)

				.append($("<command/>").text("Rename…").click(new Action<Event>() {
					@Override public void execute(final Event e) { rename(); }
				}));
	}

	protected Tile inspecting() {
		return $("<section/>")

				.append($("<command label='Get Info…'/>").click(new Action<Event>() {
					@Override public void execute(final Event e) { inspect(); }
				}));
	}


	protected Tile editing() {
		return $("<section/>")

				.append(undo)
				.append(redo);
	}

	protected Tile opening() {
		return $("<section/>")

				.append($("<command label='New'/>").unless(locked).click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.New); }
				}))

				.append($("<command label='Open…'/>").click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.Open); }
				}));
	}

	protected Tile archiving() {
		return $("<section/>")

				.append($("<command/>")

						.text("Copy")

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { e.current().fire(File.Copy); }
						}))

				.append(save)
				.append(drop);
	}

	protected Tile porting() {
		return $("<section/>")

				.append($("<command label='Import…'/>").click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.Import); }
				}))

				.append($("<command label='Export…'/>").click(new Action<Event>() {
					@Override public void execute(final Event e) { e.current().fire(File.Export); }
				}));
	}

	protected Tile printing() {
		return $("<section/>").when(false) // !!!

				.append($("<command label='Print…'/>").click(new Action<Event>() {
					@Override public void execute(final Event e) {
						// !!!
					}
				}));
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setup(final Setup<T> setup) {

		if ( this.setup == null && !setup.flag(Embedded) ) { // show agreement/about if initializing and not embedded

			//if ( storage.get(ActivationKey) == null ) { // first visit
			//
			//	agreement();
			//
			//} else

			if ( session.get(ActivationKey) == null ) { // first visit in this session

				splash();

			}

		}

		this.setup=setup;

		render();
	}

	private void item(final T item) {

		if ( !Objects.equals(item, this.item) ) {
			this.item=item;
			manager.clear();
		}

		this.locked=item.isLocked();

		render();
	}

	private void lens(final Lens<T> lens) {
		root().async(item().arrayed().lens(lens));
	}


	private void command(final Command command) {

		manager.exec(command);

		render();
	}

	private void undone() {

		manager.undo();

		render();
	}

	private void redone() {

		manager.redo();

		render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void splash() {
		$(new About().open())

				.delay(SplashDelay, new Action<Event>() { // fade away after a delay
					@Override public void execute(final Event e) {
						e.current()

								.style("opacity", "0") // start opacity transition

								.bind("transitionend", new Action<Event>() {
									@Override public void execute(final Event e) {
										e.current().<Overlay>as().close(true);
									}
								});
					}
				});

		activate(session);
	}

	private void activate(final Map<String, String> storage) {
		storage.put(ActivationKey, "true");
	}


	private void about() {
		new About().open();
	}

	protected void lock() {
		root().async(item().isLocked() ? item().setLocked(false).arrayed() : item().arrayed().setLocked(true));
	}

	protected void inspect() {
		info(item()).modal(true).open().align(Align.Center, Align.Center);
	}

	protected void rename() {
		if ( item.arrayed() != null ) {
			label.<Editable>as().editable(true);
		}
	}

	protected void label(final String label) {
		root().fire(new Command.Change<String>(item.getLabel(), label) {

			@Override protected boolean set(final String value) {
				return root().fire(item.updated().setLabel(value));
			}

			@Override public String label() {
				return "rename document";
			}

		});
	}


	protected void save() {
		if ( item.getLabel().isEmpty() ) { rename(); }
	}

	protected void drop() {
		if ( !item.getLabel().isEmpty() ) { label(""); }
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Page<T> render() {

		if ( item != null && setup != null ) {

			label.enabled(!locked).text(item.getLabel()).attribute("placeholder", "Metreeca");

			undo.enabled(!locked && manager.undoable()).title(("Undo "+manager.undoing().toLowerCase()).trim());
			redo.enabled(!locked && manager.redoable()).title(("Redo "+manager.redoing().toLowerCase()).trim());

			final boolean anonymous=item.isAnonymous();

			save.enabled(!locked && anonymous);
			drop.enabled(!locked && !anonymous);

			// global css driver

			$("html").is("locked", locked);

		}

		return this;
	}

}
