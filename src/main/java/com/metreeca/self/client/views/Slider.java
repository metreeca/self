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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.View.Orientation.Horizontal;
import static com.metreeca._tile.client.View.Orientation.Vertical;


public final class Slider extends View { // !!! refactor as plugin (?)

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Slider.css") TextResource skin();

	}


	private final Orientation orientation;

	private float lower;
	private float upper;

	private float minimum;
	private float maximum;
	private float range;

	private float delta;
	private float value;

	private final Tile track;
	private final Tile cursor;


	public Slider(final Orientation orientation) {

		if ( orientation == null ) {
			throw new NullPointerException("null orientation");
		}

		root($("<div/>")

				.skin(resources.skin().getText())

				.is((this.orientation=orientation).name().toLowerCase(), true)

				.append(track=$("<div/>").is("track", true)

						.click(new Action<Event>() {
							@Override public void execute(final Event e) {
								if ( e.targeting() && e.button("left") ) {
									if ( orientation == Horizontal ) {

										final int x=e.x();
										final float lower=cursor.left();
										final float upper=cursor.left()+cursor.width();

										if ( x < lower ) {

											delta=(maximum-minimum)*(x-lower)/track.width();
											value=(maximum-minimum)*x/track.width()+minimum;

											root().change();

										} else if ( x > upper ) {

											delta=(maximum-minimum)*(x-upper)/track.width();
											value=(maximum-minimum)*x/track.width()+minimum;

											root().change();

										}

									} else if ( orientation == Vertical ) {

										final int y=e.y();
										final float lower=cursor.top();
										final float upper=cursor.top()+cursor.height();

										if ( y < lower ) {

											delta=(maximum-minimum)*(y-lower)/track.height();
											value=(maximum-minimum)*y/track.height()+minimum;

											root().change();

										} else if ( y > upper ) {

											delta=(maximum-minimum)*(y-upper)/track.height();
											value=(maximum-minimum)*y/track.height()+minimum;

											root().change();

										}
									}
								}
							}
						})

						.append(cursor=$("<div/>").is("cursor", true)

								.drag(new Action.Drag() {

									@Override protected void dragstart(final Event e) {
										if ( enabled(e.button("left")) ) {
											e.mode("move").image($())
													.data(Slider.class, Slider.this);
										}
									}

									@Override protected void drag(final Event e) {
										if ( enabled() ) {

											if ( orientation == Horizontal ) {

												final float width=track.width();

												final float x=clip(e.x(track), 0, width);
												final float r=clip(last().x(track), 0, width);

												value=(maximum-minimum)*x/width;
												delta=(maximum-minimum)*(x-r)/width;

											} else if ( orientation == Vertical ) {

												final float height=track.height();

												final float y=clip(e.y(track), 0, height);
												final float r=clip(last().y(track), 0, height);

												value=(maximum-minimum)*y/height;
												delta=(maximum-minimum)*(y-r)/height;

											}

											root().change();
										}
									}

								}))))

				.window()

				.resize(new Action<Event>() {
					@Override public void execute(final Event e) { update(); }
				});
	}


	private float clip(final float value, final float lower, final float upper) {
		return Math.max(lower, Math.min(upper-1, value));
	}


	public float delta() {
		return delta;
	}

	public float value() {
		return value;
	}


	public Slider range(final float lower, final float upper, final float minimum, final float maximum) {

		if ( lower > upper ) {
			throw new IllegalArgumentException("illegal value ["+lower+" > "+upper+"]");
		}

		if ( minimum > maximum ) {
			throw new IllegalArgumentException("illegal range ["+minimum+" > "+maximum+"]");
		}

		if ( lower < minimum ) {
			throw new IllegalArgumentException("illegal lower ["+lower+" < "+minimum+"]");
		}

		if ( upper > maximum ) {
			throw new IllegalArgumentException("illegal upper ["+upper+" > "+maximum+"]");
		}

		this.lower=lower;
		this.upper=upper;

		this.minimum=minimum;
		this.maximum=maximum;
		this.range=maximum-minimum;

		return update();
	}


	private Slider update() {

		if ( orientation == Horizontal ) { // !!! min width
			cursor.left(range == 0 ? 0 : (lower-minimum)/range*track.width());
			cursor.right(range == 0 ? 0 : (maximum-upper)/range*track.width());
		} else if ( orientation == Vertical ) { // !!! min height
			cursor.top((range == 0 ? 0 : (lower-minimum)/range)*track.height());
			cursor.bottom(range == 0 ? 0 : (maximum-upper)/range*track.height());
		}

		return this;
	}
}
