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

package com.metreeca._tile.client.plugins;

import com.metreeca._tile.client.*;


public final class Protocol extends Plugin {

	public enum Home {About}

	public enum Exec {Undo, Redo}

	public enum File {New, Open, Copy, Save, Drop, Import, Export}

	public enum Search {Find, Next, Prev, Done}

	public enum Select {All, None, Delete}

	public enum Zoom {In, Out, Reset}

	public enum Move {Left, Right, Up, Down}


	private static interface Resources { // !!! ;(gwt) broken static constants on overlays

		public static final Action<Event> ContextMenu=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.ctrl() || e.alt() || e.target().editable() ) {
					e.stop(); // use ctrl/alt keys to access the native context menu
				} else if ( !e.capturing() ) { // handle nested targets
					e.cancel(); // hide native context menu
				}
			}
		};

		public static final Action<Event> ExecProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( !e.target().editable() ) {
					if ( e.key("cmd-z") ) {
						if ( !e.target().fire(Exec.Undo) ) { e.cancel(); }
					} else if ( e.key("cmd-shift-z", "cmd-y") ) {
						if ( !e.target().fire(Exec.Redo) ) { e.cancel(); }
					}
				}
			}
		}; // cmd-z / cmd-shift-z | cmd-y > undo/redo command

		public static final Action<Event> ManageProtocol=new Action<Event>() {
			@Override public void execute(final Event e) { // ;(chrome) cmd-n/w/t are reserved unless in app mode
				if ( e.key("cmd-n", "cmd-alt-n") ) {
					if ( !e.target().fire(File.New) ) { e.cancel(); }
				} else if ( e.key("cmd-o", "cmd-alt-o") ) {
					if ( !e.target().fire(File.Open) ) { e.cancel(); }
				} else if ( e.key("cmd-d", "cmd-alt-d") ) {
					if ( !e.target().fire(File.Copy) ) { e.cancel(); }
				} else if ( e.key("cmd-s", "cmd-alt-s") ) {
					if ( !e.target().fire(File.Save) ) { e.cancel(); }
				} else if ( e.key("cmd-delete", "cmd-backspace") ) {
					if ( !e.target().fire(File.Drop) ) { e.cancel(); }
				} else if ( e.key("cmd-alt-i") ) {
					if ( !e.target().fire(File.Import) ) { e.cancel(); }
				} else if ( e.key("cmd-alt-e") ) {
					if ( !e.target().fire(File.Export) ) { e.cancel(); }
				}
			}
		}; // cmd[-alt]-n / cmd[-alt]-o / cmd[-alt]-d / cmd[-alt]-s / cmd-del/back / cmd-alt-i / cmd-alt-e > manage files

		public static final Action<Event> SearchProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.key("cmd-f") ) {
					if ( !e.target().fire(Search.Find) ) { e.cancel(); }
				} else if ( e.key("cmd-g") ) {
					if ( !e.target().fire(Search.Next) ) { e.cancel(); }
				} else if ( e.key("cmd-shift-g") ) {
					if ( !e.target().fire(Search.Prev) ) { e.cancel(); }
				} else if ( e.key("esc") ) {
					if ( !e.target().fire(Search.Done) ) { e.cancel(); }
				}
			}
		}; // cmd-f / cmd-g / shift-cmd-g / esc > find/next/prev/done

		public static final Action<Event> SelectProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( !e.target().editable() ) {
					if ( e.key("cmd-a") ) {
						if ( !e.target().fire(Select.All) ) { e.cancel(); }
					} else if ( e.key("escape") ) {
						if ( !e.target().fire(Select.None) ) { e.cancel(); }
					} else if ( e.key("backspace", "delete", "cancel") ) {
						if ( !e.target().fire(Select.Delete) ) { e.cancel(); }
					}
				}
			}
		}; // cmd-a, esc, delete, backspace > alter selection

		public static final Action<Event> ZoomProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.key("meta-alt-equals", "ctrl-shift-equals") ) {
					if ( !e.target().fire(Zoom.In) ) { e.cancel(); }
				} else if ( e.key("meta-alt-minus", "ctrl-shift-minus", "meta-alt-subtract", "ctrl-shift-subtract") ) {
					if ( !e.target().fire(Zoom.Out) ) { e.cancel(); }
				} else if ( e.key("meta-alt-0", "ctrl-shift-0") ) {
					if ( !e.target().fire(Zoom.Reset) ) { e.cancel(); }
				}
			}
		}; // (meta-alt | ctrl-shift)-equal/minus/0 > alter zoom

		public static final Action<Event> MoveProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( !e.target().editable() ) {
					if ( e.key("left") ) {
						if ( !e.target().fire(Move.Left) ) { e.cancel(); }
					} else if ( e.key("right") ) {
						if ( !e.target().fire(Move.Right) ) { e.cancel(); }
					} else if ( e.key("up") ) {
						if ( !e.target().fire(Move.Up) ) { e.cancel(); }
					} else if ( e.key("down") ) {
						if ( !e.target().fire(Move.Down) ) { e.cancel(); }
					}
				}
			}
		}; // arrows > move

		public static final Action<Event> DropProtocol=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.type().equals("dragover") ) {

					if ( e.mime("Files") ) {
						e.cancel(); // accept drop
					}

				} else if ( e.type().equals("drop") && e.cancel() ) { // ignore nested drop targets

					for (final com.metreeca._tile.client.File file : e.files()) {
						if ( !e.target().fire(file) ) { e.cancel(); }
					}

				}
			}
		}; // drop files > process

	}


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Protocol() {}


	/**
	 * Configures context menus handling.
	 *
	 * @param custom if <code>true</code> the application manages its own custom menus; otherwise, native context menus
	 *               are enabled (native context menus are always accessible by holding the <code>alt</code> modifier
	 *               and within editable elements, in order to support spell-checking and other editing commands)
	 *
	 * @return this object
	 */
	public Protocol menu(final boolean custom) {
		return custom // observe both capturing and bubbling to handle nested targets
				? this.<Tile>as().bind("contextmenu", Resources.ContextMenu, true).bind("contextmenu", Resources.ContextMenu).<Protocol>as()
				: this.<Tile>as().drop("contextmenu", Resources.ContextMenu, true).drop("contextmenu", Resources.ContextMenu).<Protocol>as();
	}

	/**
	 * Configures command execution.
	 *
	 * @param custom if <code>true</code> undo/redo commands may be handled by the application; otherwise, they are
	 *               handled by the browser
	 *
	 * @return this object
	 */
	public Protocol exec(final boolean custom) {
		return custom
				? this.<Tile>as().bind("keydown", Resources.ExecProtocol, true).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.ExecProtocol, true).<Protocol>as();
	}

	/**
	 * Configures file management.
	 *
	 * @param custom if <code>true</code> data operations and file drops may be handled by the application; otherwise,
	 *               they are handled by the browser
	 *
	 * @return this object
	 */
	public Protocol file(final boolean custom) {
		return custom // only bubbling events for drop protocol to handle nested targets
				? this.<Tile>as().bind("keydown", Resources.ManageProtocol, true).bind("dragover drop", Resources.DropProtocol).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.ManageProtocol, true).drop("dragover drop", Resources.DropProtocol).<Protocol>as();
	}

	/**
	 * Configures search command handling.
	 *
	 * @param custom if <code>true</code> search commands may be handled by the application; otherwise, they are handled
	 *               by the browser
	 *
	 * @return this object
	 */
	public Protocol search(final boolean custom) {
		return custom
				? this.<Tile>as().bind("keydown", Resources.SearchProtocol, true).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.SearchProtocol, true).<Protocol>as();
	}

	/**
	 * Configures selection command handling.
	 *
	 * @param custom if <code>true</code> selection commands may be handled by the application; otherwise, they are
	 *               handled by the browser
	 *
	 * @return this object
	 */
	public Protocol select(final boolean custom) {
		return custom
				? this.<Tile>as().bind("keydown", Resources.SelectProtocol, true).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.SelectProtocol, true).<Protocol>as();
	}

	/**
	 * Configures zooming command handling.
	 *
	 * @param custom if <code>true</code> zooming commands may be handled by the application; otherwise, they are
	 *               handled by the browser
	 *
	 * @return this object
	 */
	public Protocol zoom(final boolean custom) {
		return custom
				? this.<Tile>as().bind("keydown", Resources.ZoomProtocol, true).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.ZoomProtocol, true).<Protocol>as();
	}

	/**
	 * Configures moving command handling.
	 *
	 * @param custom if <code>true</code> moving commands may be handled by the application; otherwise, they are handled
	 *               by the browser
	 *
	 * @return this object
	 */
	public Protocol move(final boolean custom) {
		return custom
				? this.<Tile>as().bind("keydown", Resources.MoveProtocol, true).<Protocol>as()
				: this.<Tile>as().drop("keydown", Resources.MoveProtocol, true).<Protocol>as();
	}

}
