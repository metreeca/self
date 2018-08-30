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

final class AgentWK extends Agent {

	public AgentWK() {
		transition();
		animation();
		issue23695();
		issue66547();
	}


	@Override public native int x(final Event event, final Tile tile) /*-{

		var frame=tile ? tile[0] : event.currentTarget || event.target; // ;( no currentTarget on drag event

		if ( !frame ) {
			return event.clientX || 0;
		} else {
			return $wnd.webkitConvertPointFromPageToNode(frame, new WebKitPoint(event.pageX, event.pageY)).x;
		}
	}-*/;

	@Override public native int y(final Event event, final Tile tile) /*-{

		var frame=tile ? tile[0] : event.currentTarget || event.target; // ;( no currentTarget on drag event

		if ( !frame ) {
			return event.clientY || 0;
		} else {
			return $wnd.webkitConvertPointFromPageToNode(frame, new WebKitPoint(event.pageX, event.pageY)).y;
		}
	}-*/;


	//// Patches ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private native void transition() /*-{
		// !!! ;(safari) convert webkitTransitionEnd to transitionend
	}-*/;

	private native void animation() /*-{
		$doc.addEventListener("webkitAnimationEnd", function () {
			throw "implement animationend support"; // !!! verify support
		})
	}-*/;

	/**
	 * dataTransfer.getData unusable on same domain of a dragstart (https://bugs.webkit.org/show_bug.cgi?id=23695) HTML5
	 * drag and drop not working when getData is used (https://bugs.webkit.org/show_bug.cgi?id=58206#c7)
	 */
	private void issue23695() {}

	/**
	 * relatedTarget is not populated for dragenter / dragleave events
	 *
	 * https://bugs.webkit.org/show_bug.cgi?id=66547 https://code.google.com/p/chromium/issues/detail?id=159534
	 */
	private void issue66547() {}

}
