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

package com.metreeca._tile.client.plugins;


import com.metreeca._tile.client.Lambda;
import com.metreeca._tile.client.Plugin;
import com.metreeca._tile.client.Tile;


public final class Selectable extends Plugin {

	public static final String Tabs="    ";


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Selectable() {}


	public native String line() /*-{ // !!! support for editable nodes
		if ( this[0] && this[0].value // ;(ff) selectionStart on checkboxes/radios causes an exception
				&& this[0].type !== 'checkbox' && this[0].type !== 'radio' && this[0].selectionStart ) {

			var value=this[0].value;
			var caret=this[0].selectionStart;
			var start=value.substring(0, caret).lastIndexOf('\n')+1;

			return value.substring(start, caret);

		}
		else {
			return ""
		}
	}-*/;


	public native int first() /*-{ // !!! support for editable nodes
		// ;(ff) selectionStart on checkboxes/radios causes an exception
		return this[0] && this[0].type !== 'checkbox' && this[0].type !== 'radio' && this[0].selectionStart || 0;
	}-*/;

	public native int last() /*-{ // !!! support for editable nodes
		// ;(ff) selectionStart on checkboxes/radios causes an exception
		return this[0] && this[0].type !== 'checkbox' && this[0].type !== 'radio' && this[0].selectionEnd || 0;
	}-*/;


	public native Selectable select() /*-{

		var selection=$wnd.getSelection();

		selection.removeAllRanges();

		for (var i=0; i < this.length; ++i) { // ;(ff) selectionStart on checkboxes/radios causes an exception
			if ( this[i].type !== 'checkbox' && this[i].type !== 'radio' && this[i].selectionStart ) {

				var target=this[i];

				var top=target.scrollTop;
				var left=target.scrollLeft;

				target.selectionStart=0;
				target.selectionEnd=target.value.length;
				target.scrollTop=top;
				target.scrollLeft=left;

			} else {

				var range=$doc.createRange();

				range.selectNodeContents(this[i]);
				selection.addRange(range);
			}
		}

		return this;
	}-*/;

	public Selectable select(final int caret) {
		return select(caret, caret);
	}

	public native Selectable select(final int first, final int last) /*-{ // !!! support for editable nodes

		for (var i=0; i < this.length; ++i) { // ;(ff) selectionStart on checkboxes/radios causes an exception
			if ( this[i].type !== 'checkbox' && this[i].type !== 'radio' && this[i].selectionStart ) {

				var top=this[i].scrollTop;
				var left=this[i].scrollLeft;

				this[i].selectionStart=first;
				this[i].selectionEnd=last;
				this[i].scrollTop=top;
				this[i].scrollLeft=left;
			}
		}

		return this;
	}-*/;


	public native Selectable insert(final String text) /*-{ // !!! rename // !!! support for editable nodes

		text=text || "";

		for (var i=0; i < this.length; ++i) { // ;(ff) selectionStart on checkboxes/radios causes an exception
			if ( this[i].type !== 'checkbox' && this[i].type !== 'radio' && this[i].selectionStart ) {

				var top=this[i].scrollTop;
				var left=this[i].scrollLeft;

				var first=this[i].selectionStart;
				var last=this[i].selectionEnd;
				var value=this[i].value;

				this[i].value=value.substring(0, first)+text+value.substring(last);

				this[i].selectionStart=first+text.length;
				this[i].selectionEnd=this[i].selectionStart;
				this[i].scrollTop=top;
				this[i].scrollLeft=left;
			}
		}

		return this;
	}-*/;

	public native Selectable delete(final int length) /*-{ // !!! support for editable nodes

		for (var i=0; i < this.length; ++i) { // ;(ff) selectionStart on checkboxes/radios causes an exception
			if ( this[i].type !== 'checkbox' && this[i].type !== 'radio' && this[i].selectionStart ) {

				var top=this[i].scrollTop;
				var left=this[i].scrollLeft;

				var first=Math.max(0, this[i].selectionStart-length);
				var last=this[i].selectionEnd;
				var value=this[i].value;

				this[i].value=value.substring(0, first)+value.substring(last, value.length);

				this[i].selectionStart=first;
				this[i].selectionEnd=this[i].selectionStart;
				this[i].scrollTop=top;
			}
		}

		return this;
	}-*/;


	public Selectable indent() {
		return this.<Tile>as().each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {

				final Selectable selectable=tile.as();

				final String line=selectable.line();
				final StringBuilder spaces=new StringBuilder();

				for (int n=0; n < line.length() && line.charAt(n) == ' '; ++n) {
					spaces.append(' ');
				}

				final String value=tile.value();
				final int first=selectable.first();

				if ( first > 0 && first < value.length()
						&& value.substring(first-1, first+1).equals("{}") ) {

					selectable.insert("\n"+spaces+Tabs+"\n"+spaces);
					selectable.select(first+spaces.length()+Tabs.length()+1);

				} else if ( first > 0 && value.charAt(first-1) == '{' ) {
					selectable.insert("\n"+spaces+Tabs);
				} else {
					selectable.insert("\n"+spaces);
				}

				return tile;
			}
		}).as();
	}


	public native Selectable clear() /*-{

		$wnd.getSelection().removeAllRanges();

		return this;
	}-*/;

}
