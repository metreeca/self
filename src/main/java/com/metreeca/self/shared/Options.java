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

package com.metreeca.self.shared;

import com.google.gwt.http.client.URL;

import java.util.LinkedHashMap;
import java.util.Map;


public final class Options {

	private final Map<String, String> fields=new LinkedHashMap<>();


	public Options() {}

	public Options(final String form) {

		if ( form == null ) {
			throw new NullPointerException("null form");
		}

		if ( !form.isEmpty() ) {
			for (final String entry : form.split("&")) {

				final int equal=entry.indexOf('=');

				final String name=URL.decodeQueryString((equal >= 0) ? entry.substring(0, equal).toLowerCase() : "");
				final String value=URL.decodeQueryString((equal >= 0) ? entry.substring(equal+1) : entry);

				fields.put(name.trim(), value.trim());

			}
		}

	}


	public Options options(final Options options) {

		if ( options == null ) {
			throw new NullPointerException("null options");
		}

		fields.putAll(options.fields);

		return this;
	}


	public boolean flag(final Option<Boolean> option) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		final String value=fields.get(option.name());

		return value != null ? Boolean.valueOf(value) : option.fallback();
	}

	public Options flag(final Option<Boolean> option, final Boolean value) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		if ( value != null ) {
			fields.put(option.name(), String.valueOf(value));
		} else {
			fields.remove(option.name());
		}

		return this;
	}


	public String string(final Option<String> option) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		final String value=fields.get(option.name());

		return value != null ? value : option.fallback();
	}

	public Options string(final Option<String> option, final String value) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		if ( value != null ) {
			fields.put(option.name(), value);
		} else {
			fields.remove(option.name());
		}

		return this;
	}


	public <T extends Enum<T>> T token(final Class<T> option) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		T value=first(option);

		final String string=fields.get(name(option));

		for (final T constant : option.getEnumConstants()) {
			if ( constant.name().equalsIgnoreCase(string) ) { value=constant; }
		}

		return value;
	}

	public <T extends Enum<T>> Options token(final Class<T> option, final T value) {

		if ( option == null ) {
			throw new NullPointerException("null option");
		}

		if ( value != null ) {
			fields.put(name(option), value.name().toLowerCase());
		} else {
			fields.remove(name(option));
		}

		return this;
	}


	private <T extends Enum<T>> T first(final Class<T> option) {

		final T[] constants=option.getEnumConstants();

		return constants.length > 0 ? constants[0] : null;
	}

	private <T extends Enum<T>> String name(final Class<T> option) {
		return option.getSimpleName().toLowerCase();
	}


	@Override public String toString() {

		final StringBuilder form=new StringBuilder(fields.size()*100);

		for (final Map.Entry<String, String> field : fields.entrySet()) {

			final String name=field.getKey();
			final String value=field.getValue();

			if ( form.length() > 0 ) { form.append('&'); }
			if ( !name.isEmpty() ) { form.append(URL.encodeQueryString(name)).append('='); }
			if ( !value.isEmpty() ) { form.append(URL.encodeQueryString(value)); }

		}

		return form.toString();
	}

}
