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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.View;
import com.metreeca.self.client.faces.auto.AutoFace;
import com.metreeca.self.client.faces.bar.BarChart;
import com.metreeca.self.client.faces.board.BoardFace;
import com.metreeca.self.client.faces.book.BookFace;
import com.metreeca.self.client.faces.bubble.BubbleChart;
import com.metreeca.self.client.faces.line.LineChart;
import com.metreeca.self.client.faces.marker.MarkerMap;
import com.metreeca.self.client.faces.pie.PieChart;
import com.metreeca.self.client.faces.table.TableFace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class SpecsView extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("SpecsView.css") TextResource skin();

	}


	private final AutoFace auto;

	private final TableFace table;
	private final BoardFace board;
	private final BookFace book;

	private final PieChart pie;
	private final BarChart bar;
	private final LineChart line;
	private final BubbleChart bubble;

	private final MarkerMap marker;

	private final FieldsView fields;
	private final FacetsView facets;


	public SpecsView() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append($("<section/>")

						.append(fields=new FieldsView())

						.append($("<div/>").is("port", true)

								.append(auto=new AutoFace())

								.append(table=new TableFace())
								.append(board=new BoardFace())
								.append(book=new BookFace())

								.append(pie=new PieChart())
								.append(bar=new BarChart())
								.append(line=new LineChart())
								.append(bubble=new BubbleChart())

								.append(marker=new MarkerMap())))

				.append($("<aside/>")

						.append(facets=new FacetsView()));
	}


	public SpecsView locked(final boolean locked) {

		auto.locked(locked);

		table.locked(locked);
		board.locked(locked);
		book.locked(locked);

		pie.locked(locked);
		bar.locked(locked);
		line.locked(locked);
		bubble.locked(locked);

		marker.locked(locked);

		fields.locked(locked);
		facets.locked(locked);

		return this;
	}

}
