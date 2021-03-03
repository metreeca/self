/*
 * Copyright © 2013-2021 Metreeca srl. All rights reserved.
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

public interface Node {

	public String name();


	public String attribute(final String name);

	public Node attribute(final String name, final String value);


	public String text();

	public Node text(final String text);


	public int size();

	public Node node(final int index);

	public Node node(final Node node);


	public static interface Factory {

		public Node node(final String name);


		public Node parse(final String text);

		public String write(final Node node);

	}
}
