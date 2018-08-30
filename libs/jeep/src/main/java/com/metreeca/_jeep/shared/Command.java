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

package com.metreeca._jeep.shared;

import java.util.ArrayList;
import java.util.List;


public abstract class Command {

	public abstract boolean exec();

	public boolean undo() { return false; }

	public boolean redo() { return exec(); }


	public void done() {}

	public void undone() { done(); }

	public void redone() { done(); }


	public String label() { return ""; }


	@Override public String toString() { return label(); }


	public static final class Manager {

		private final int limit;

		private final List<Command> undo=new ArrayList<Command>();
		private final List<Command> redo=new ArrayList<Command>();


		public Manager() {
			this(0);
		}

		public Manager(final int limit) {

			if ( limit < 0 ) {
				throw new IllegalArgumentException("illegal limit ["+limit+"]");
			}

			this.limit=limit;
		}


		public boolean undoable() {
			return !undo.isEmpty();
		}

		public boolean redoable() {
			return !redo.isEmpty();
		}


		public String undoing() {
			return undo.isEmpty() ? "" : undo.get(0).label();
		}

		public String redoing() {
			return redo.isEmpty() ? "" : redo.get(0).label();
		}


		public Manager exec(final Command command) {

			if ( command == null ) {
				throw new NullPointerException("null command");
			}

			if ( command.exec() ) {

				if ( limit > 0 && undo.size() == limit ) {
					undo.remove(limit-1);
				}

				undo.add(0, command);
				redo.clear();
			}

			return this;
		}

		public Manager undo() {

			if ( !undo.isEmpty() ) {

				final Command command=undo.get(0);

				if ( command.undo() ) {
					try {
						command.undone();
					} finally {
						undo.remove(0);
						redo.add(0, command);
					}
				}

			}

			return this;
		}

		public Manager redo() {

			if ( !redo.isEmpty() ) {

				final Command command=redo.get(0);

				if ( command.redo() ) {
					try {
						command.redone();
					} finally {
						redo.remove(0);
						undo.add(0, command);
					}
				}

			}

			return this;
		}

		public Manager clear() {

			undo.clear();
			redo.clear();

			return this;
		}
	}

	public abstract static class Change<T> extends Command {

		private final T original;
		private final T modified;


		protected Change(final T original, final T modified) {
			this.original=original;
			this.modified=modified;
		}


		public T original() {
			return original;
		}

		public T modified() {
			return modified;
		}


		@Override public boolean exec() {

			final boolean changed=!equals(original, modified);

			if ( changed ) { set(modified); }

			return changed;
		}

		@Override public boolean undo() {
			return set(original);
		}

		@Override public boolean redo() {
			return set(modified);
		}


		protected abstract boolean set(final T value);


		private boolean equals(final Object object, final Object reference) {
			return object == null ? reference == null : object.equals(reference);
		}
	}

}
