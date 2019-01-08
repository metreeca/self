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

package com.metreeca._bean.xlator;

import com.metreeca._bean.client.ClientMeta;
import com.metreeca._bean.server.ServerMeta;
import com.metreeca._bean.shared.Bean;
import com.metreeca._bean.shared.Meta;

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


public final class MetaGenerator extends Generator {

	private static final Map<Class<?>, Class<?>> primitives=new HashMap<Class<?>, Class<?>>();

	static {
		primitives.put(boolean.class, Boolean.class);
		primitives.put(char.class, Character.class);
		primitives.put(byte.class, Byte.class);
		primitives.put(short.class, Short.class);
		primitives.put(int.class, Integer.class);
		primitives.put(long.class, Long.class);
		primitives.put(float.class, Float.class);
		primitives.put(double.class, Double.class);
	}

	private static Class<?> convert(final Class<?> type) {
		return type.isPrimitive() ? primitives.get(type) : type;
	}


	@Override public String generate(
			final TreeLogger logger, final GeneratorContext context, final String target
	) throws UnableToCompleteException {

		try {

			final TypeOracle oracle=context.getTypeOracle();
			final JClassType type=oracle.getType(target);

			final JClassType clazz=oracle.getType(ClientMeta.class.getName());
			final String pkg=clazz.getPackage().getName();
			final String name=clazz.getSimpleSourceName()+type.getSimpleSourceName();

			final PrintWriter writer=context.tryCreate(logger, pkg, name);

			if ( writer != null ) {

				writer.format("package %s;\n\n", pkg);

				writer.format("import %s;\n", Map.class.getName());
				writer.format("import %s;\n\n", Bean.class.getName());

				writer.format("final class %s extends ClientMeta.Factory {{\n\n", name);

				final Meta.Factory factory=ServerMeta.factory();

				for (final JClassType bean : oracle.getTypes()) {
					if ( bean.isAnnotationPresent(Bean.class) ) {

						final Meta<?> meta=factory.meta(bean.getQualifiedBinaryName());

						// open

						writer.format("\tmeta(\"%s\", new ClientMeta(%s.class",
								bean.getQualifiedBinaryName(), bean.getQualifiedSourceName());

						writer.print(", ");
						creator(writer, meta);

						writer.print(", ");
						encoder(writer, meta);

						writer.print(", ");
						decoder(writer, meta);

						writer.print(") {{\n\n");

						// key fields

						for (final Map.Entry<String, Class<?>> entry : meta.key().entrySet()) {

							writer.format("\t\tkey(\"%s\", %s.class, ",
									entry.getKey(), source(entry.getValue()));

							getter(writer, meta, entry.getKey(), entry.getValue());

							writer.format(");\n\n");
						}

						// plain fields

						for (final Map.Entry<String, Class<?>> entry : meta.fields().entrySet()) {

							writer.format("\t\tfield(\"%s\", %s.class, ",
									entry.getKey(), source(entry.getValue()));

							getter(writer, meta, entry.getKey(), entry.getValue());

							writer.format(", ");

							setter(writer, meta, entry.getKey(), entry.getValue());

							writer.format(");\n\n");
						}

						// done

						writer.format("\t}});\n\n");

					}
				}

				for (final Map.Entry<String, String> alias : ServerMeta.aliases().entrySet()) {
					writer.format("\talias(\"%s\", \"%s\");\n\n", alias.getKey(), alias.getValue());
				}

				writer.println("}}\n");

				context.commit(logger, writer);
			}

			return pkg+"."+name;

		} catch ( final NotFoundException e ) {

			logger.log(TreeLogger.Type.ERROR, target, e);

			throw new UnableToCompleteException();
		}
	}


	private void creator(final PrintWriter writer, final Meta<?> meta) {
		if ( meta.type().isEnum() ) {

			writer.print("null");

		} else {

			writer.format("new ClientMeta.Creator() {\n\n");

			writer.format("\t\t@Override public Object create(final Map<String, Object> values) {\n");
			writer.format("\t\t\treturn ");

			if ( hasDefault(meta.type()) && !meta.key().isEmpty() ) {
				writer.format("values.isEmpty() ? new %s() : ", source(meta.type()));
			}

			writer.format("new %s(", source(meta.type()));

			int index=0;

			for (final Map.Entry<String, Class<?>> entry : meta.key().entrySet()) {

				if ( index++ > 0 ) {
					writer.append(", ");
				}

				writer.format("(%s)values.get(\"%s\")",
						source(convert(entry.getValue())), entry.getKey());
			}

			writer.format(");\n");
			writer.format("\t\t}\n\n");

			writer.print("\t}");
		}
	}

	private void encoder(final PrintWriter writer, final Meta<?> meta) {
		if ( hasEncoder(meta.type()) ) {

			writer.format("new ClientMeta.Coder() {\n\n");

			writer.format("\t\t@Override public void code(final Bean.Info info) {\n");
			writer.format("\t\t\t%s.encode(info);\n", meta.type().getCanonicalName());
			writer.format("\t\t}\n\n");

			writer.print("\t}");

		} else {
			writer.write("null");
		}
	}

	private void decoder(final PrintWriter writer, final Meta<?> meta) {
		if ( hasEncoder(meta.type()) ) {

			writer.format("new ClientMeta.Coder() {\n\n");

			writer.format("\t\t@Override public void code(final Bean.Info info) {\n");
			writer.format("\t\t\t%s.decode(info);\n", meta.type().getCanonicalName());
			writer.format("\t\t}\n\n");

			writer.print("\t}");

		} else {
			writer.write("null");
		}
	}

	private void getter(final PrintWriter writer, final Meta<?> meta, final String field, final Class<?> type) {

		writer.format("new ClientMeta.Getter() {\n\n");

		writer.format("\t\t\t@Override public Object get(final Object bean) {\n");

		writer.format("\t\t\t\treturn ((%s)bean).%s%s();\n",
				source(meta.type()),
				isBoolean(type) && isVariant(meta.type(), field) ? "is" : "get",
				upper(field));

		writer.format("\t\t\t}\n\n");

		writer.print("\t\t}");
	}

	private void setter(final PrintWriter writer, final Meta<?> meta, final String field, final Class<?> type) {

		writer.format("new ClientMeta.Setter() {\n\n");

		writer.format("\t\t\t@Override public void set(final Object bean, final Object value) {\n");

		writer.format("\t\t\t\t((%s)bean).set%s((%s)value);\n",
				source(meta.type()),
				upper(field),
				source(wrap(type)));

		writer.format("\t\t\t}\n\n");

		writer.print("\t\t}");
	}

	private Class<?> wrap(final Class<?> type) {
		return !type.isPrimitive() ? type
				: type.equals(boolean.class) ? Boolean.class
				: type.equals(char.class) ? Character.class
				: type.equals(byte.class) ? Byte.class
				: type.equals(int.class) ? Integer.class
				: type.equals(short.class) ? Short.class
				: type.equals(long.class) ? Long.class
				: type.equals(float.class) ? Float.class
				: type.equals(double.class) ? Double.class
				: Void.TYPE;
	}


	private boolean hasDefault(final Class<?> type) {
		try {
			return type.getConstructor() != null;
		} catch ( final NoSuchMethodException ignored ) {
			return false;
		}
	}

	private boolean hasEncoder(final Class<?> type) {
		try {

			final int modifiers=type.getMethod("encode", Bean.Info.class).getModifiers();

			return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);

		} catch ( final NoSuchMethodException ignored ) {
			return false;
		}
	}

	private boolean hasDecoder(final Class<?> type) {
		try {

			final int modifiers=type.getMethod("decode", Bean.Info.class).getModifiers();

			return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);

		} catch ( final NoSuchMethodException ignored ) {
			return false;
		}
	}

	private boolean isBoolean(final Class<?> type) {
		return type.equals(boolean.class) || type.equals(Boolean.class);
	}

	private boolean isVariant(final Class<?> type, final String field) {
		try {

			type.getMethod("is"+upper(field));

			return true;

		} catch ( final NoSuchMethodException ignored ) {
			return false;
		}
	}


	private String source(final Class<?> type) {
		return type.getName().replace('$', '.');
	}

	private String upper(final String field) {
		return Character.toUpperCase(field.charAt(0))+field.substring(1);
	}
}
