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

package com.metreeca.self.client.sparql;

import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Table;

import com.google.gwt.core.client.JavaScriptObject;

import static com.metreeca.self.shared.beans.Term.*;


/**
 * <a href="https://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query Results JSON Format</a> client-side parser.
 */
public final class JSONTable extends JavaScriptObject implements Table { // !!! refactor

	protected JSONTable() {}


	public static native Table table(final String results) /*-{
		return $wnd.JSON.parse(results.replace(/\\U([0-9A-Fa-f]{8})/g, function ($0, $1) {

			// ;(virtuso) fix illegal long unicode escape sequences (see https://github.com/openlink/virtuoso-opensource/issues/303)

			var c=parseInt($1, 16)-0x010000;
			var h=(c>>10)+0xD800;
			var l=(c&0x3FF)+0xDC00;

			return String.fromCharCode(h, l)

		}));
	}-*/;


	@Override public native int rows() /*-{

		return this.results.bindings.length;

	}-*/;

	@Override public Term term(final int row, final String key) {

		final Cell cell=cell(row, key);
		final Term term=cell.term();

		return term == null ? null : term

				.setLabel(cell(row, key+"_label").text())
				.setNotes(cell(row, key+"_notes").text())
				.setImage(cell(row, key+"_image").text())

				.setLat(cell(row, key+"_lat").number())
				.setLng(cell(row, key+"_lng").number());
	}


	private native Cell cell(final int row, final String key) /*-{
		return this.results.bindings[row][key] || {};
	}-*/;


	private static final class Cell extends JavaScriptObject {

		@SuppressWarnings("ProtectedMemberInFinalClass") protected Cell() {}


		public native String type() /*-{
			return this.type;
		}-*/;

		public native String data() /*-{
			return this.datatype;
		}-*/;

		public native String lang() /*-{
			return this["xml:lang"];
		}-*/;

		public native String text() /*-{
			return this.value || ""; // !!! review fallback
		}-*/;

		public native float number() /*-{
			return parseFloat(this.value);
		}-*/;


		public Term term() {

			final String text=text();
			final String type=type();
			final String data=data();
			final String lang=lang();

			return type == null ? null
					: type.equals("uri") ? named(text)
					: type.equals("bnode") ? blank(text)
					: data != null ? typed(text, data)
					: lang != null ? plain(text, lang)
					: plain(text);
		}

	}

}
