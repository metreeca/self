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

package com.metreeca.self.shared.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class CSVReader {

	private boolean labeled;

	private char separator=',';
	private char delimiter='"';


	public boolean isLabeled() {
		return labeled;
	}

	public CSVReader setLabeled(final boolean labeled) {

		this.labeled=labeled;

		return this;
	}


	public char getSeparator() {
		return separator;
	}

	public CSVReader setSeparator(final char separator) {

		this.separator=separator;

		return this;
	}


	public char getDelimiter() {
		return delimiter;
	}

	public CSVReader setDelimiter(final char delimiter) {

		this.delimiter=delimiter;

		return this;
	}


	public <T extends TableHandler> T read(final String text, final T handler) throws IOException {

		if ( text == null ) {
			throw new NullPointerException("null text");
		}

		if ( handler == null ) {
			throw new NullPointerException("null handler");
		}

		final Scanner scanner=new Scanner(text);

		handler.start();
		handler.table(null, labeled ? scanner.empty().values() : null);

		int records=0;

		while ( !scanner.empty().eot() ) { handler.record(records++, scanner.values()); }

		handler.size(records);
		handler.end();

		return handler;
	}


	private final class Scanner {

		private final String text;

		private int next;
		private int line;
		private int offset;

		private final StringBuilder token=new StringBuilder();


		public Scanner(final String text) {
			this.text=text;
		}


		private Scanner empty() {

			do {} while ( read('\r') || read('\n') );

			return this;
		}

		private List<String> values() throws IOException {

			final List<String> values=new ArrayList<String>();

			do { values.add(value()); } while ( read(separator) );

			return values;
		}

		private String value() throws IOException {

			if ( read(delimiter) ) {
				while ( true ) {
					if ( eot() ) {
						throw error("unexpected end of text within quoted field");
					} else if ( read(delimiter) ) {
						if ( read(delimiter) ) { token.append('"'); } else { break; }
					} else {
						token.append(read());
					}
				}
			} else {
				while ( !(eot() || peek(separator) || peek('\r') || peek('\n')) ) {
					token.append(read());
				}
			}

			try {
				return token.toString();
			} finally {
				token.setLength(0);
			}
		}


		private boolean eot() {
			return next == text.length();
		}

		private boolean peek(final char c) {
			return next < text.length() && text.charAt(next) == c;
		}

		private boolean read(final char c) {
			return peek(c) && read() == c;
		}


		private char read() {

			final char c=text.charAt(next++);

			if ( c == '\n' ) {
				line++;
				offset=0;
			} else {
				offset++;
			}

			return c;
		}


		private IOException error(final String message) {
			return new IOException("("+(line+1)+":"+(offset+1)+") "+message);
		}
	}
}
