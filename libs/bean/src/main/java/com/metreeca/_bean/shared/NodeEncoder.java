/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._bean.shared;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


final class NodeEncoder {

	private final Node.Factory factory;

	private int id;

	private final Map<Bean.Info, Node> nodes=new HashMap<>();


	NodeEncoder(final Node.Factory factory) {
		this.factory=factory;
	}


	public String encode(final Bean.Info info, final String namespace) {
		try {

			return factory.write(factory.node("bean").attribute("xmlns", namespace).node(bean(info)));

		} finally {
			id=0;
			nodes.clear();
		}
	}


	private Node object(final Object object) {
		return object instanceof Bean.Info ? bean((Bean.Info)object)

				: object instanceof Boolean ? factory.node("boolean").text(String.valueOf(object))
				: object instanceof Character ? factory.node("char").text(String.valueOf(object))
				: object instanceof Byte ? factory.node("byte").text(String.valueOf(object))
				: object instanceof Short ? factory.node("short").text(String.valueOf(object))
				: object instanceof Integer ? factory.node("int").text(String.valueOf(object))
				: object instanceof Long ? factory.node("long").text(String.valueOf(object))
				: object instanceof Float ? factory.node("float").text(String.valueOf(object))
				: object instanceof Double ? factory.node("double").text(String.valueOf(object))

				: object instanceof BigInteger ? factory.node("integer").text(String.valueOf(object))
				: object instanceof BigDecimal ? factory.node("decimal").text(String.valueOf(object))
				: object instanceof String ? factory.node("string").text(text(String.valueOf(object)))
				: object instanceof Date ? factory.node("date").text(String.valueOf(((Date)object).getTime()))

				: object instanceof List ? collection((Iterable<?>)object, "list")
				: object instanceof SortedSet ? collection((Iterable<?>)object, "sorted-set")
				: object instanceof Set ? collection((Iterable<?>)object, "set")
				: object instanceof Collection ? collection((Iterable<?>)object, "collection")

				: object instanceof SortedMap ? map((Map<?, ?>)object, "sorted-map")
				: object instanceof Map ? map((Map<?, ?>)object, "map")

				: null;
	}


	private Node collection(final Iterable<?> items, final String name) {

		final Node node=factory.node(name);

		for (final Object item : items) {
			node.node(object(item));
		}

		return node;
	}

	private Node map(final Map<?, ?> entries, final String name) {

		final Node node=factory.node(name);

		for (final Map.Entry<?, ?> entry : entries.entrySet()) {
			node.node(factory.node("entry")
					.node(object(entry.getKey()))
					.node(object(entry.getValue())));
		}

		return node;
	}


	private Node bean(final Bean.Info info) {
		if ( nodes.containsKey(info) ) {

			final Node node=nodes.get(info);

			if ( node.attribute("id").isEmpty() ) {
				node.attribute("id", String.valueOf(id++));
			}

			return factory.node(name(info.type())).attribute("id", node.attribute("id"));

		} else if ( info.mode().equals("") ) { // regular bean

			final Node node=factory.node(name(info.type()));

			if ( info.version() > 0 ) {
				node.attribute("version", String.valueOf(info.version()));
			}

			nodes.put(info, node);

			for (final Map.Entry<String, Object> entry : info.fields().entrySet()) {
				node.node(factory.node(entry.getKey()).node(object(entry.getValue())));
			}

			return node;

		} else if ( info.mode().equals("class") ) { // class literal

			return factory.node("class").text(name(info.type()));

		} else { // enum value

			return factory.node("enum").text(name(info.type())+"@"+info.mode());

		}
	}


	private String name(final String type) {
		return type.replace("$", "..");
	}

	private String text(final CharSequence text) {

		final StringBuilder builder=new StringBuilder();

		for (int n=0, l=text.length(); n < l; ++n) {

			final char c=text.charAt(n);

			if ( c <= 0x0F ) {
				builder.append("\\0").append(Integer.toHexString(c).toUpperCase());
			} else if ( c <= 0x20 || c >= 0x7F && c <= 0x9F ) {
				builder.append("\\").append(Integer.toHexString(c).toUpperCase());
			} else if ( c == '\\' ) {
				builder.append("\\");
			} else {
				builder.append(c);
			}
		}

		return builder.toString();
	}
}
