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

import com.metreeca._tile.client.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class Overlay extends Plugin {

	public enum Align { // !!! make relative to target, if target is parent

		None {
			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile);
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile);
			}
		},

		Start {
			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).left(target.left(true));
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).top(target.top(true));
			}
		},

		End {
			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).right(target.right(true));
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).bottom(target.bottom(true));
			}
		},

		Fill { // !!! stay centered if resized

			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).left(target.left(true)).width(target.width(true));
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).top(target.top(true)).height(target.height(true));
			}
		},

		Center { // !!! stay centered if resized

			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).left(Math.max(0, target.left(true)+target.width(true)/2-tile.width(true)/2));
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).top(Math.max(0, target.top(true)+target.height(true)/2-tile.height(true)/2));
			}
		},

		Before {
			@Override public Tile x(final Tile target, final Tile tile) {

				final float tileWidth=tile.width();

				final float targetLeft=target.left(true);
				final float targetAnchor=targetLeft-1;

				return targetAnchor-tileWidth >= 0
						? horizontal(tile).left(targetAnchor-tileWidth)
						: tile.left(0); // fit inside viewport without sharp corners
			}

			@Override public Tile y(final Tile target, final Tile tile) {

				final float tileHeight=tile.height();

				final float targetTop=target.top(true);
				final float targetAnchor=targetTop-1;

				return targetAnchor-tileHeight >= 0
						? vertical(tile).top(targetAnchor-tileHeight)
						: tile.top(0); // fit inside viewport without sharp corners
			}
		},

		After {
			@Override public Tile x(final Tile target, final Tile tile) {

				final float rootWidth=$(":root").width(true);
				final float tileWidth=tile.width();

				final float targetLeft=target.left(true);
				final float targetWidth=target.width(true);
				final float targetAnchor=targetLeft+targetWidth+1;

				return targetAnchor+tileWidth <= rootWidth
						? horizontal(tile).left(targetAnchor)
						: tile.left(rootWidth-tileWidth); // fit inside viewport without sharp corners
			}

			@Override public Tile y(final Tile target, final Tile tile) {

				final float rootHeight=$(":root").height(true);
				final float tileHeight=tile.height();

				final float targetTop=target.top(true);
				final float targetHeight=target.height(true);
				final float targetAnchor=targetTop+targetHeight+1;

				return targetAnchor+tileHeight <= rootHeight
						? vertical(tile).top(targetAnchor)
						: tile.top(rootHeight-tileHeight); // fit inside viewport without sharp corners
			}
		},

		Aside { // after or before if trailing space is not enough

			private static final float AsideAllowance=1.1f; // allowance factor for aside alignment

			@Override public Tile x(final Tile target, final Tile tile) {

				final float rootWidth=$(":root").width(true);
				final float tileWidth=AsideAllowance*tile.width();

				final float targetLeft=target.left(true);
				final float targetWidth=target.width(true);

				final float before=targetLeft-1-tileWidth;
				final float after=rootWidth-(targetLeft+targetWidth+1+tileWidth);

				return after >= 0 || after >= before ? After.y(target, tile) : Before.y(target, tile);
			}

			@Override public Tile y(final Tile target, final Tile tile) {

				final float rootHeight=$(":root").height(true);
				final float tileHeight=AsideAllowance*tile.height();

				final float targetTop=target.top(true);
				final float targetHeight=target.height(true);

				final float before=targetTop-1-tileHeight;
				final float after=rootHeight-(targetTop+targetHeight+1+tileHeight);

				return after >= 0 || after >= before ? After.y(target, tile) : Before.y(target, tile);
			}

		},

		Leading { // relative to target center

			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).right(target.right(true)+target.width(true)/2+1);
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).bottom(target.bottom(true)+target.height(true)/2+1);
			}
		},

		Trailing { // relative to target center

			@Override public Tile x(final Tile target, final Tile tile) {
				return horizontal(tile).left(target.left(true)+target.width(true)/2+1);
			}

			@Override public Tile y(final Tile target, final Tile tile) {
				return vertical(tile).top(target.top(true)+target.height(true)/2+1);
			}
		};


		public abstract Tile x(final Tile target, final Tile tile);

		public abstract Tile y(final Tile target, final Tile tile);


		protected final Tile horizontal(final Tile tile) {

			for (final Align align : Align.values()) {
				tile.is("h-"+align.name().toLowerCase(), align == this);
			}

			return tile;
		}

		protected final Tile vertical(final Tile tile) {

			for (final Align align : Align.values()) {
				tile.is("v-"+align.name().toLowerCase(), align == this);
			}

			return tile;
		}
	}


	public static Overlay overlays() {
		return overlays(false);
	}

	public static Overlay overlays(final boolean force) {
		return (force ? $(".overlay") : $(".overlay:not(.sticky)")).as();
	}


	private static final int Delay=500;


	public static interface Resources extends ClientBundle {

		@Source("Overlay.css") public TextResource skin();


		public static final Resources resources=GWT.create(Resources.class); // !!! ;(gwt) broken static in overlay types

		public static final String CloseEvents="keydown mousedown mousemove click dragenter dragover dragleave drop";

		public static final Action<Event> Close=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.key("esc") || e.type().equals("mousedown") && e.targeting() ) {

					e.stop().cancel();

					$("body > .overlay:not(.sticky)").<Overlay>as().close();

				} else if ( (e.type().startsWith("drag") || e.type().equals("drop")) && $("body > .modal.overlay").size() > 0 ) {

					// prevent drops outside modal dialogs
					// cancel dragenter/over to accept drop, then cancel drop to prevent browser default…
					// cancelling drop without accepting dragenter/over won't prevent the browser from opening files…

					e.mode("none").cancel();

				} else if ( (e.type().equals("keydown") || e.type().equals("click")) && !e.path().is("overlay") ) {

					$("body > .labile.overlay").<Overlay>as().close();

				} else {

					$("body > .humanized.overlay").<Overlay>as().close();

				}
			}
		};


		public static final int DragPadding=5; // padding for dragging boundaries [px]

		public static final Action.Drag Drag=new Action.Drag() {

			private int fx;
			private int fy;

			@Override protected void dragstart(final Event e) {
				if ( enabled(e.button("left")) ) {

					e.mode("move").data("$overlay", e.current().id()).image($());

					fx=e.x();
					fy=e.y();
				}
			}

			@Override protected void drag(final Event e) {
				if ( enabled() ) {

					final Tile body=$("body");
					final Tile tile=e.current();

					tile
							.left(Math.max(DragPadding, Math.min(body.width(true)-DragPadding-tile.width(true), e.x(body)-fx)))
							.top(Math.max(DragPadding, Math.min(body.height(true)-DragPadding-tile.height(true), e.y(body)-fy)));
				}
			}

		};

	}


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Overlay() {}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Overlay modal(final boolean modal) { // will prevent interaction with body
		return this.<Tile>as().is("modal", modal).<Overlay>as().render();
	}

	public Overlay sticky(final boolean sticky) { // won't close on esc/mousedown
		return this.<Tile>as().is("sticky", sticky).<Overlay>as().render();
	}

	public Overlay labile(final boolean labile) { // will close on keydown/click
		return this.<Tile>as().is("labile", labile).<Overlay>as().render();
	}

	public Overlay humanized(final boolean humanized) { // will close on any user gesture
		return this.<Tile>as().is("humanized", humanized).<Overlay>as().render();
	}


	public Overlay shading(final boolean shading) { // shade body content
		return this.<Tile>as().is("shading", shading).<Overlay>as().render();
	}

	public Overlay draggable(final boolean draggable) { // repositionable by user
		if ( draggable ) {
			return this.<Tile>as().drag(Resources.Drag).<Overlay>as().render();
		} else {
			throw new UnsupportedOperationException("to be implemented"); // !!! tbi
		}
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////


	public boolean visible() {
		return this.<Tile>as().parent().size() > 0;
	}

	public Overlay visible(final Boolean visible) {
		return this.<Tile>as().each(new Lambda<Tile>() {
			@Override public Tile apply(final Tile tile) {

				if ( visible == Boolean.TRUE || visible == null && tile.parent().size() == 0 ) {
					tile.<Overlay>as().open();
				} else if ( visible == Boolean.FALSE || visible == null && tile.parent().size() > 0 ) {
					tile.<Overlay>as().close();
				}

				return tile;
			}
		}).as();
	}


	public Overlay open() {
		return open(null);
	}

	public Overlay open(final Tile master) {
		return this.<Tile>as().each(new Lambda<Tile>() {

			@Override public Tile apply(final Tile tile) {
				return tile.fire("open", master) ? tile.<Overlay>as().wire().<Tile>as() : tile;
			}

		}).<Overlay>as().render();
	}


	public Overlay close() {
		return close(false, null);
	}

	public Overlay close(final boolean force) {
		return close(force, null);
	}

	public Overlay close(final boolean force, final Tile master) {
		return this.<Tile>as().each(new Lambda<Tile>() {

			@Override public Tile apply(final Tile tile) {
				return force || tile.fire("close", master) ? tile.<Overlay>as().unwire().<Tile>as() : tile;
			}

		}).<Overlay>as().render();
	}


	public Overlay align(final Align horizontal, final Align vertical) {
		return align($("body"), horizontal, vertical);
	}

	public Overlay align(final Tile reference, final Align horizontal, final Align vertical) {

		if ( reference == null ) {
			throw new NullPointerException("null reference");
		}

		if ( horizontal == null ) {
			throw new NullPointerException("null horizontal");
		}

		if ( vertical == null ) {
			throw new NullPointerException("null vertical");
		}

		return vertical.y(reference, horizontal.x(reference, this.<Tile>as())).as();
	}

	public Overlay align(final float x, final float y) { // client coords // !!! review

		final Tile tile=as();

		final float width=$("body").width();
		final float height=$("body").height();

		final float l=/*target.left(true)+*/x; // !!! relative coords are handy but break for transformed elements
		final float w=tile.width();

		final float t=/*target.top(true)*/+y; // !!! relative coords are handy but break for transformed elements
		final float h=tile.height();

		return tile
				.left(l+w < width ? l : l-w)
				.top(t+h < height ? t : t-h)
				.as();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Overlay wire() {
		return unwire().<Tile>as()

				.skin(Resources.resources.skin().getText())

				.is("overlay", true)

				.set("$overlay-tabindex", this.<Tile>as().attribute("tabindex"))
				.attribute("tabindex", "-1")

				.appendTo($("body")) // ;(ie) don't append to html: input field break down
				.show()

				.as();
	}

	private Overlay unwire() {
		return this.<Tile>as()

				.hide()
				.remove()

				.is("overlay", false)
				.attribute("tabindex", this.<Tile>as().get("$overlay-tabindex", ""))

				.left("")
				.right("")
				.top("")
				.bottom("")
				.width("")
				.height("")

				.as();
	}


	private Overlay render() {

		$("body")

				.is("modal", $("body > .overlay.modal").size() > 0)
				.is("shading", $("body > .overlay.shading").size() > 0);

		if ( $("body > .overlay").size() > 0 ) {

			$("body").delay(Delay, new Action<Event>() { // after a refractory period
				@Override public void execute(final Event e) {
					$("body").bind(Resources.CloseEvents, Resources.Close, true);
				}
			});

		} else {

			$("body").drop(Resources.CloseEvents, Resources.Close, true);

		}

		return this;
	}
}
