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

package com.metreeca._tile.client;

final class AgentFF extends Agent {

	public AgentFF() {
		keypress();
		issue505521();
		patch20130605();
		issue860329();
	}


	@Override public native int x(final Event event, final Tile tile) /*-{

		var frame=tile ? tile[0] : event.currentTarget || event.target; // ;( no currentTarget on drag event

		// $clientX written by issue505521() for drag events

		if ( !frame ) {
			return event.clientX || event.$clientX || 0;
		} else { // !!! fix/reject for transformed elements
			return frame.scrollLeft+(event.clientX || event.$clientX || 0)-Math.round(frame.getBoundingClientRect().left);
		}
	}-*/;

	@Override public native int y(final Event event, final Tile tile) /*-{

		var frame=tile ? tile[0] : event.currentTarget || event.target; // ;( no currentTarget on drag event

		// $clientY written by issue505521() for drag events

		if ( !frame ) {
			return event.clientY || event.$clientY || 0;
		} else { // !!! fix/reject for transformed elements
			return frame.scrollTop+(event.clientY || event.$clientY || 0)-Math.round(frame.getBoundingClientRect().top);
		}
	}-*/;


	// -- apparently fixed in 16+ --
	//@Override public native void closing(final Action action) /*-{
	//
	//	// ;(FF) doesn't support custom messages on beforeunload dialogs
	//	// (see http://stackoverflow.com/a/9866486/739773)
	//	// (see https://bugzilla.mozilla.org/show_bug.cgi?id=641509)
	//
	//	var done=false;
	//
	//	$wnd.addEventListener("beforeunload", $entry(function (e) {
	//
	//		// ;(FF) fires beforeunload twice when closing the last tab in a window
	//		// (see https://bugzilla.mozilla.org/show_bug.cgi?id=531199)
	//		// (see https://bugzilla.mozilla.org/show_bug.cgi?id=369955)
	//
	//		if ( !done ) {
	//
	//			done=true;
	//
	//			var event={};
	//
	//			action.@com.metreeca._tile.client.Action::execute(Ljava/lang/Object;)(event);
	//
	//			return event.returnValue; // see Event.mode()
	//		}
	//
	//	}));
	//
	//}-*/;


	//// Patches ///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Kill keypress events for functional keys
	 */
	private native void keypress() /*-{ // !!! review

		$doc.addEventListener("keypress", function (e) {
			if ( e.charCode === 0 ) { e.stopPropagation(); }
		}, true);

	}-*/;

	/**
	 * Set screen coordinates during drag event (https://bugzilla.mozilla.org/show_bug.cgi?id=505521)
	 */
	private native void issue505521() /*-{

		var x, y; // the coordinates of the last dragstart/over event

		var drag=function (e) { // e.clientX/Y are read only
			e.$clientX=x;
			e.$clientY=y;
		};

		var dragover=function (e) {
			x=e.clientX;
			y=e.clientY;
		};

		$doc.addEventListener("dragstart", function (e) {

			$doc.addEventListener("drag", drag, true);
			$doc.addEventListener("dragover", dragover, true);

			// initialize coordinates for the first drag event fired before dragover)

			x=e.clientX;
			y=e.clientY;

		}, true);

		$doc.addEventListener("dragend", function (e) {

			$doc.removeEventListener("drag", drag, true);
			$doc.removeEventListener("dragover", dragover, true);

			e.$clientX=x;
			e.$clientY=y;

		}, true);

	}-*/;


	/**
	 * Dispatching events on a disabled control causes an unexpected error.
	 *
	 * <pre>
	 *
	 *      b=document.createElement("button");
	 *      e=document.createEvent("CustomEvent");
	 *      e.initCustomEvent("test", true, true, null);
	 *
	 *      b.dispatchEvent(e); // pass
	 *
	 *      b.disabled=true; b.dispatchEvent(e); // fail (Error: Unexpected error)
	 *
	 * </pre>
	 */
	private native void patch20130605() /*-{

		//var original = $wnd.Element.prototype.dispatchEvent;
		//
		//$wnd.Element.prototype.dispatchEvent=function (e) {
		//	return this.disabled ? true : original(e);
		//}

	}-*/;


	/*
	 * <input> won't scroll its text to the start on blur.
	 *
	 * https://bugzilla.mozilla.org/show_bug.cgi?id=860329
	 */
	private native void issue860329() /*-{ // ;(ie) causes focus loops

		//$wnd.addEventListener("blur", function (e) {
		//
		//	if ( e.target.tagName === "INPUT" ) {
		//		e.target.selectionStart=0;
		//		e.target.selectionEnd=0;
		//	}
		//
		//}, true);

	}-*/;

}
