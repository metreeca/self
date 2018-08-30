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

package com.metreeca._tile.client;

import com.metreeca._tile.client.js.JSHandler;

import com.google.gwt.core.client.JavaScriptObject;

import static com.metreeca._tile.client.Tile.$;


public abstract class Action<E> {

	public static native Action<Event> leading(final int period, final Action<Event> action) /*-{

		var executed=0;

		return period >= 0 && action && @com.metreeca._tile.client.Action::wrap(*)($entry(function (e) {

			var current=e.timeStamp || Date.now(); // ;(firefox) some event don't have a timestamp

			try {

				if ( current > executed+period ) {  // new event sequence
					action.@com.metreeca._tile.client.Action::execute(*)(e);
				}

			} finally {
				executed=current; // extend the current event sequence
			}

		}));

	}-*/;

	public static native Action<Event> trailing(final int period, final Action<Event> action) /*-{

		var event
		var scheduled=0;

		function test(planned) {
			return $entry(function () {
				if ( scheduled === planned ) {
					action.@com.metreeca._tile.client.Action::execute(*)(event)
				} else {
					$wnd.setTimeout(test(scheduled), scheduled-planned);
				}
			});
		}

		return period >= 0 && action && @com.metreeca._tile.client.Action::wrap(*)(function (e) {

			var current=e.timeStamp || Date.now(); // ;(firefox) some event don't have a timestamp
			var planned=current+period;

			if ( current > scheduled ) {
				$wnd.setTimeout(test(planned), period);
			}

			event=e;
			scheduled=planned;

		});

	}-*/;

	public static native Action<Event> throttled(final int period, final Action<Event> action) /*-{

		var scheduled=0;

		return period >= 0 && action && @com.metreeca._tile.client.Action::wrap(*)($entry(function (e) {

			var current=e.timeStamp || Date.now(); // ;(firefox) some event don't have a timestamp

			if ( current > scheduled+period ) { // new event sequence

				action.@com.metreeca._tile.client.Action::execute(*)(e); // execute immediately

				return current; // mark the current time as the event sequence origin

			} else if ( current > scheduled ) { // unscheduled event in the current sequence

				$wnd.setTimeout(function () {

					action.@com.metreeca._tile.client.Action::execute(*)(e);

				}, scheduled+period-current); // schedule the next execution

				return scheduled+period; // extend the current event sequence

			} else { // scheduled event

				return scheduled;

			}
		}));

	}-*/;


	private static Action<Event> wrap(final JSHandler<Event> handler) {

		if ( handler == null ) {
			throw new NullPointerException("null handler");
		}

		return new Action<Event>() {
			@Override public void execute(final Event event) { handler.execute(event); }
		};
	}


	private static native JavaScriptObject listener(final Action<?> action) /*-{
		return $entry(function (e) { // event may be null (eg for GoogleMaps) > protect callback
			action.@com.metreeca._tile.client.Action::execute(Ljava/lang/Object;)(e || {});
		});
	}-*/;


	@SuppressWarnings("UnusedDeclaration") // see Tile.bind()/drop()/schedule
	private final JavaScriptObject listener=listener(this);


	private boolean enabled=true;


	public abstract void execute(final E e);


	public final boolean enabled() {
		return enabled;
	}

	public final boolean enabled(final boolean enabled) {
		return this.enabled=enabled;
	}


	public abstract static class Drag extends Action<Event> {

		private static final int HoldOnset=300; // hold onset [ms]


		private boolean active;
		private boolean holding;

		private Event first; // the dragstart event
		private Event last; // the last dragstart/drag event


		private final Action<Event> grabbed=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().is("holding", holding=true);
			}
		};


		private boolean active() {
			return active;
		}

		private boolean active(final boolean active) {
			return this.active=active;
		}


		protected final boolean holding() {
			return holding;
		}

		protected final Event first() {
			return first != null ? first : (first=(Event)JavaScriptObject.createObject());
		}

		protected final Event last() {
			return last != null ? last : (last=(Event)JavaScriptObject.createObject());
		}


		@Override public final void execute(final Event e) {
			if ( e.type().equals("mousedown") ) {

				e.stop().current().delay(HoldOnset, grabbed); // ignore nested drag sources

			} else if ( e.type().equals("mouseup") || e.type().equals("mouseout") ) {

				e.current().cancel(grabbed).is("holding", holding=false);

			} else if ( e.targeting() ) { // ignore nested drag sources

				if ( e.type().equals("dragstart") ) {

					e.current().cancel(grabbed);

					try {
						dragstart(first=last=e);
					} finally {
						if ( active(!e.cancelled()) ) { // accepted drag
							e.target().is("dragging", true);
						} else { // rejected drag
							e.current().is("holding", holding=false);
						}
					}

				} else if ( e.type().equals("drag") ) {

					try {
						if ( active() ) { drag(e); }
					} finally {
						last=e;
					}

				} else if ( e.type().equals("dragend") ) {

					try {
						if ( active() ) { dragend(e); }
					} finally {
						holding=false;
						first=null;
						last=null;
						e.target().is("holding dragging", false);
						active(true);
					}

				}

			}
		}


		protected void dragstart(final Event e) {}

		protected void drag(final Event e) {}

		protected void dragend(final Event e) {}

	}

	public abstract static class Drop extends Action<Event> {

		@Override public final native void execute(final Event e) /*-{

			var drop=e.currentTarget.$drop || (e.currentTarget.$drop={});

			if ( e.type === "dragenter" ) {

				try {

					if ( !drop.target ) { // first dragenter event

						this.@com.metreeca._tile.client.Action.Drop::dragenter(Lcom/metreeca/_tile/client/Event;)(e);

					} else { // ignore events on children and replay the first response

						if ( drop.defaultPrevented ) { e.preventDefault(); }

						e.dataTransfer.dropEffect=drop.dropEffect;

						// ;(ie) effectAllowed causes exception on file drop (connect.microsoft.com/IE/feedback/details/811625/)

						if ( Array.prototype.indexOf.call(e.dataTransfer.types, "Files") < 0 ) { // ;(chrome) types is an Array
							e.dataTransfer.effectAllowed=drop.effectAllowed;
						}

					}

				} finally {

					if ( !drop.target ) { // memo response on first dragenter event

						drop.defaultPrevented=e.defaultPrevented;
						drop.dropEffect=e.dataTransfer.dropEffect;

						// ;(ie) effectAllowed causes exception on file drop (connect.microsoft.com/IE/feedback/details/811625/)

						if ( Array.prototype.indexOf.call(e.dataTransfer.types, "Files") < 0 ) { // ;(chrome) types is an Array
							drop.effectAllowed=e.dataTransfer.effectAllowed;
						}

					}

					drop.target=e.target; // keep track of the last dragenter target

					if ( drop.defaultPrevented ) { // accepted drop
						e.currentTarget.classList.add("dropping");
					} else {
						e.currentTarget.classList.remove("dropping"); // ;(ie) toggle(property, boolean) not supported
					}

				}

			} else if ( e.type === "dragover" ) {

				try {

					this.@com.metreeca._tile.client.Action.Drop::dragover(Lcom/metreeca/_tile/client/Event;)(e);

				} finally {

					if ( e.defaultPrevented ) { // accepted drop
						e.currentTarget.classList.add("dropping");
					} else {
						e.currentTarget.classList.remove("dropping"); // ;(ie) toggle(property, boolean) not supported
					}

				}

			} else if ( e.type === "dragleave" ) {

				if ( drop.target === e.target ) { // real dragleave event: otherwise ignore events on children
					try {

						this.@com.metreeca._tile.client.Action.Drop::dragleave(Lcom/metreeca/_tile/client/Event;)(e);

					} finally {

						e.currentTarget.classList.remove("dropping");

						delete e.currentTarget.$drop;

					}
				}

			} else if ( e.type === "drop" ) {

				try {

					com.metreeca.next.handlers
					com.metreeca.next.handlers

					this.@com.metreeca._tile.client.Action.Drop::drop(Lcom/metreeca/_tile/client/Event;)(e);

				} finally {

					e.currentTarget.classList.remove("dropping");

					delete e.currentTarget.$drop;

				}

			}
		}-*/;


		protected void dragenter(final Event e) {}

		protected void dragover(final Event e) {}

		protected void dragleave(final Event e) {}

		protected void drop(final Event e) {}

	}


	/**
	 * Merge activity bursts using a retriggerable monostable.
	 */
	public abstract static class Monitor extends Action<Boolean> {

		private static final int Grace=250; // grace period for signaling activity changes [ms]

		private static final Tile root=$(":root");


		private int pending; // pending activities


		@Override public void execute(final Boolean active) {
			if ( Boolean.TRUE.equals(active) && ++pending > 0 ) {
				schedule(true);
			} else if ( Boolean.FALSE.equals(active) && --pending == 0 ) {
				schedule(false);
			} else {
				try { schedule(false); } finally { pending=0; }
			}
		}

		private void schedule(final boolean active) { // merge activity bursts using a retriggerable monostable
			root.delay(Grace, new Action<Event>() {
				@Override public void execute(final Event e) {
					if ( active && pending > 0 ) {
						active(true);
					} else if ( !active && pending == 0 ) {
						active(false);
					}
				}
			});
		}

		protected abstract void active(final boolean active);

	}

}
