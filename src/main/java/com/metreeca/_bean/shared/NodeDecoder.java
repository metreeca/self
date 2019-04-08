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

package com.metreeca._bean.shared;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.metreeca._bean.shared.Bean.Info;


final class NodeDecoder {

	private final Node.Factory factory;

	private final Map<String, Info> infos=new HashMap<>();


	NodeDecoder(final Node.Factory factory) {
		this.factory=factory;
	}


	public Info decode(final String text, final String namespace) {
		try {

			final Node root=factory.parse(text);

			final String name=root.name();
			final String xmlns=root.attribute("xmlns");

			if ( !name.equals("bean") ) {
				throw new IllegalArgumentException("illegal root element ["+name+"]");
			}

			if ( root.size() != 1 ) {
				throw new IllegalArgumentException("malformed root element ["+name+"]");
			}

			if ( !namespace.isEmpty() && !namespace.equals(xmlns) ) {
				throw new IllegalArgumentException("mismatched namespace [expected: "+namespace+"; actual: "+xmlns+"]");
			}

			return bean(root.node(0));

		} finally {
			infos.clear();
		}
	}


	private Object object(final Node node) {
		return node == null ? null

				: node.name().equals("boolean") ? _boolean(node)
				: node.name().equals("char") ? _char(node)
				: node.name().equals("byte") ? _byte(node)
				: node.name().equals("short") ? _short(node)
				: node.name().equals("int") ? _int(node)
				: node.name().equals("long") ? _long(node)
				: node.name().equals("float") ? _float(node)
				: node.name().equals("double") ? _double(node)

				: node.name().equals("integer") ? integer(node)
				: node.name().equals("decimal") ? decimal(node)
				: node.name().equals("string") ? string(node)
				: node.name().equals("date") ? date(node)

				: node.name().equals("list") ? collection(node, new ArrayList<Object>())
				: node.name().equals("sorted-set") ? collection(node, new TreeSet<Object>())
				: node.name().equals("set") ? collection(node, new LinkedHashSet<Object>())
				: node.name().equals("collection") ? collection(node, new ArrayList<Object>())

				: node.name().equals("sorted-map") ? map(node, new TreeMap<Object, Object>())
				: node.name().equals("map") ? map(node, new LinkedHashMap<Object, Object>())

				: node.name().equals("class") ? _class(node)
				: node.name().equals("enum") ? _enum(node)

				: bean(node);
	}


	private Boolean _boolean(final Node node) {
		return Boolean.parseBoolean(text(node.text()));
	}

	private Character _char(final Node node) {

		final String text=text(node.text());

		return text.length() == 1 ? text.charAt(0) : null;
	}

	private Byte _byte(final Node node) {
		try {
			return Byte.parseByte(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Short _short(final Node node) {
		try {
			return Short.parseShort(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Integer _int(final Node node) {
		try {
			return Integer.parseInt(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Long _long(final Node node) {
		try {
			return Long.parseLong(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Float _float(final Node node) {
		try {
			return Float.parseFloat(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Double _double(final Node node) {
		try {
			return Double.parseDouble(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}


	private BigInteger integer(final Node node) {
		try {
			return new BigInteger(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private BigDecimal decimal(final Node node) {
		try {
			return new BigDecimal(text(node.text()));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private String string(final Node node) {
		return text(node.text());
	}

	private Date date(final Node node) {
		try {
			return new Date(Long.parseLong(text(node.text())));
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}


	private Collection<?> collection(final Node node, final Collection<Object> collection) {

		for (int i=0, size=node.size(); i < size; ++i) {
			collection.add(object(node.node(i)));
		}

		return collection;
	}

	private Map<?, ?> map(final Node node, final Map<Object, Object> collection) {

		for (int i=0, size=node.size(); i < size; ++i) {

			final Node entry=node.node(i);

			if ( !entry.name().equals("entry") || entry.size() != 2 ) {
				throw new IllegalArgumentException("malformed entry node ["+entry.name()+"/"+entry.size()+"]");
			}

			collection.put(object(entry.node(0)), object(entry.node(1)));
		}

		return collection;
	}


	private Info _class(final Node node) {
		return new Info().type(name(text(node.text()))).mode("class");
	}

	private Info _enum(final Node node) {

		final String text=text(node.text());
		final int first=text.indexOf('@');
		final int last=text.lastIndexOf('@');

		if ( first < 0 || first != last ) {
			throw new IllegalArgumentException("malformed enum value ["+text+"]");
		}

		return new Info().type(name(text.substring(0, first))).mode(text.substring(first+1));
	}


	private Info bean(final Node node) {

		if ( !node.text().isEmpty() ) { // legacy enum values (removed with 0.23)
			return new Info().type(name(node.name())).mode(text(node.text()));
		}

		final String id=node.attribute("id");

		if ( infos.containsKey(id) ) {

			final Info info=infos.get(id);

			if ( !name(node.name()).equals(info.type()) ) {
				throw new IllegalArgumentException("incompatible type for reference node ["+node.name()+"/"+id+"]");
			}

			return info;

		} else {

			final Info info=new Info().type(name(node.name()));

			if ( !node.attribute("version").isEmpty() ) {
				try {
					info.version(Integer.parseInt(node.attribute("version")));
				} catch ( final NumberFormatException ignored ) { }
			}

			if ( !id.isEmpty() ) {
				infos.put(id, info);
			}

			for (int i=0, size=node.size(); i < size; ++i) {

				final Node child=node.node(i);

				if ( child.size() != 1 ) {
					throw new IllegalArgumentException("malformed bean field ["+child.name()+"/"+child.size()+"]");
				}

				info.field(name(child.name()), object(child.node(0)));
			}

			return info;

		}
	}


	private String name(final String name) {
		return name.replace("..", "$");
	}

	private String text(final CharSequence text) {

		final StringBuilder builder=new StringBuilder();

		for (int n=0, l=text.length(); n < l; ++n) {

			final char c=text.charAt(n);

			if ( c == '\\' ) {

				final int x=Math.max(0, Character.digit(++n < l ? text.charAt(n) : '0', 16));
				final int y=Math.max(0, Character.digit(++n < l ? text.charAt(n) : '0', 16));

				builder.append((char)(16*x+y));

			} else if ( !ctrl(c) || (c == 0x20 || c == '\n') && n > 0 && n+1 < l && !ctrl(text.charAt(n+1)) ) {
				builder.append(c); // try to preserve legacy whitespace
			}
		}

		return builder.toString();
	}

	private boolean ctrl(final char c) {
		return c <= 0x20 || c >= 0x7F && c <= 0x9F;
	}
}
