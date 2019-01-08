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

package com.metreeca._tile.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.HashMap;
import java.util.Map;

import static com.metreeca._tile.client.Agent.Agent;


@SuppressWarnings({"OverlyComplexClass", "ClassWithTooManyMethods"})
public final class Tile extends Plugin {

	static {
		polyfillIEElementRemove();
	}

	private static native void polyfillIEElementRemove() /*-{ // ;(IE11)

		if ( !$wnd.Element.prototype.remove ) {

			Object.defineProperty($wnd.Element.prototype, 'remove', {
				value: function () {
					if ( this.parentNode !== null ) { this.parentNode.removeChild(this) }
				}
			});
		}

	}-*/;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final String NBSP="\u00A0"; // non-breaking space
	public static final String ZWSP="\u200B"; // zero-width space


	private static interface Resources { // !!! ;(gwt) broken static constants on overlays

		public static final Agent Agent=Agent();

		public static final Tile $=(Tile)createArray();

		public static final Map<String, String> skins=new HashMap<String, String>();


		public static final Action<Event> Relay=new Action<Event>() {
			@Override public void execute(final Event e) {

				final Object event=e.data();

				if ( event instanceof Enum<?> ) {
					if ( !e.current().fire(name((Enum<?>)event), event, true) ) { e.cancel(); }
				}

				for (Class<?> clazz=(event == null) ? null : event.getClass(); clazz != null; clazz=clazz.getSuperclass()) {
					if ( !e.current().fire(name(clazz), event, true) ) { e.cancel(); }
				}
			}
		};

	}


	private static interface Hover { // !!! ;(gwt) broken static constants on overlays

		public static final int Delay=500; // onset delay [ms]

		public static final String Event="hover";

		public static final String Triggers="mousemove";
		public static final String Stoppers="mouseout mousedown";


		public static final Action<Event> Start=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().cancel(Fire).delay(Delay, Fire);
			}
		};

		public static final Action<Event> Clear=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().cancel(Fire);
			}
		};

		public static final Action<Event> Fire=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.target().fire(Event); // !!! as mouse event (coordinates+button+keys)
			}
		};

	}

	private static interface Hold { // !!! ;(gwt) broken static constants on overlays

		public static final int Delay=500; // onset delay [ms]

		public static final String Event="hold";

		public static final String Triggers="mousedown";
		public static final String Stoppers="mouseout mousemove mouseup";


		public static final Action<Event> Start=new Action<Event>() {
			@Override public void execute(final Event e) {
				e.current().cancel(Fire).delay(Delay, Fire);
			}
		};

		public static final Action<Event> Clear=new Action<Event>() {
			@Override public void execute(final Event e) {

				e.current().cancel(Fire);

				if ( e.type().equals("mouseout") ) {
					$(":scope").drop("click", Click, true); // no click if moved outside element
				}
			}
		};

		public static final Action<Event> Fire=new Action<Event>() {
			@Override public void execute(final Event e) {
				if ( e.target().fire(Event) ) { // !!! as mouse event (coordinates+button+keys)
					$(":scope").bind("click", Click, true); // suppress the next click
				}
			}
		};

		public static final Action<Event> Click=new Action<Event>() {
			@Override public void execute(final Event e) {
				try {
					e.stop().cancel();
				} finally {
					e.current().drop(e.type(), this, true);
				}
			}
		};

	}


	public static Tile $() {
		return Resources.$;
	}

	public static Tile $(final String specs) {
		return specs == null || specs.isEmpty() ? Resources.$ : specs.startsWith("<") ? create(specs) : select(specs, null);
	}


	public static Tile $(final Plugin plugin) {
		return plugin == null ? Resources.$ : plugin.as();
	}

	public static Tile $(final View view) {
		return view == null ? Resources.$ : view.root();
	}

	public static Tile $(final Plugin... plugins) {
		if ( plugins == null ) { return Resources.$; } else {

			final Tile tile=(Tile)createArray();

			for (final Plugin plugin : plugins) {
				if ( plugin != null ) {

					final JsArray<JavaScriptObject> root=plugin.cast();

					for (int i=0, l=root.length(); i < l; ++i) {
						tile.<JsArray<JavaScriptObject>>cast().push(root.get(i));
					}
				}
			}

			return tile;
		}
	}

	public static Tile $(final Iterable<? extends View> views) {
		if ( views == null ) { return Resources.$; } else {

			final Tile tile=(Tile)createArray();

			for (final View view : views) {
				if ( view != null ) {

					final JsArray<JavaScriptObject> root=view.root().cast();

					for (int i=0, l=root.length(); i < l; ++i) {
						tile.<JsArray<JavaScriptObject>>cast().push(root.get(i));
					}
				}
			}

			return tile;
		}
	}


	private static native Tile create(final String nodes) /*-{

		function parse(html) {

			html=(html || "").replace(/(\s*<\?xml[^>]*>)?(\s*<!doctype[^>]*>)?(\s*)/i, '');

			var namespaces={
				'xmlns': 'http://www.w3.org/1999/xhtml',
				'xmlns:svg': 'http://www.w3.org/2000/svg',
				'xmlns:xlink': 'http://www.w3.org/1999/xlink'
			};

			var attributes='';

			for (var n in namespaces) {
				if ( namespaces.hasOwnProperty(n) ) { attributes+=" "+n+"='"+namespaces[n]+"'"; }
			}

			return ($wnd.$parser || ($wnd.$parser=new DOMParser()))
					.parseFromString('<body'+attributes+'>'+html+'</body>', 'text/xml').documentElement
		}

		function prune(node) { // get rid of formatting whitespaces

			var children=node.childNodes;

			for (var i=children.length-1; i >= 0; --i) { // concurrent deletion > scan backward

				var child=children[i];

				if ( child.nodeType === Node.TEXT_NODE ) {
					if ( /^\s*$/.exec(child.textContent) ) { node.removeChild(child); }
				} else {
					prune(child);
				}
			}

			return node;
		}

		var simple=/^<(\w+)\/>$/.exec(nodes);

		if ( simple ) {

			return [$doc.createElement(simple[1])];

		} else {

			var tile=[];
			var root=prune(parse(nodes));

			while ( root.firstChild !== null ) {
				if ( root.firstChild.nodeType === Node.ELEMENT_NODE ) {
					tile.push($doc.adoptNode(root.firstChild));
				} else {
					root.removeChild(root.firstChild);
				}
			}

			return tile;
		}
	}-*/;

	private static native Tile select(final String selector, final Plugin base) /*-{

		function select(selector, root) {
			if ( selector === ":scope" ) { // (:scope) self selector

				return [root];

			} else if ( selector.match(/^:scope\b/) ) { // (:scope ,|>|+|~) relative selector

				var id=root.id;
				var parent=root.parentNode;

				if ( !id ) { root.id="__root__"; }
				if ( !parent ) { $doc.documentElement.appendChild(root); }

				try {

					return (parent || $doc).querySelectorAll('#'+root.id+selector.substr(6));

				} finally {
					if ( !id ) { root.id=""; }
					if ( !parent ) { $doc.documentElement.removeChild(root); }
				}

			} else {

				return root.querySelectorAll(selector);

			}
		}

		var tile;

		if ( !selector ) {

			tile=[];

		} else if ( base === null ) {

			tile=select(selector, $doc);

		} else if ( base.length == 1 ) {

			tile=select(selector, base[0]);

		} else {

			tile=[];

			for (var i=0; i < base.length; ++i) {
				tile.push.apply(tile, select(selector, base[i]));
			}

		}

		return tile;

	}-*/;


	private static String name(final Class<?> type) {
		return type == null ? null : type.getName();
	}

	private static String name(final Enum<?> value) {
		return value == null ? null : value.getClass().getName()+'@'+value.name();
	}


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Tile() {}


	//// Nodes /////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the number of nodes in this tile
	 */
	public native int size() /*-{
		return this.length || 0;
	}-*/;

	/**
	 * Retrieves a node of this tile.
	 *
	 * @param index the index of the node to be retrieved
	 *
	 * @return a new tile containing the node found in this tile at the specified <code>index</code> or an empty tile if
	 * <code>index</code> is less than zero or greater or equal to the size of this tile
	 */
	public native Tile node(final int index) /*-{
		return this[index] && [this[index]] || [];
	}-*/;


	public Tile at(final int x, final int y) {
		return Resources.Agent.at(this, x, y);
	}


	public Tile copy() {
		return copy(false);
	}

	public native Tile copy(final boolean shallow) /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {

			var clone=this[i].cloneNode(!shallow);

			tile.push(clone);

			// clear ids

			clone.removeAttribute("id");

			if ( !shallow ) {

				var ids=clone.querySelectorAll("[id]");

				for (var j=0; j < ids.length; ++j) {
					ids[j].removeAttribute("id");
				}
			}
		}

		return tile;

	}-*/;


	//// Navigation ////////////////////////////////////////////////////////////////////////////////////////////////////

	public native Window window() /*-{ return $wnd; }-*/;


	public Tile find(final String selector) {
		return select(selector, this);
	}


	public native Tile path() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {
			for (var self=this[i]; self && self.nodeType === Node.ELEMENT_NODE; self=self.parentNode) { tile.push(self); }
		}

		return tile;

	}-*/;

	public native Tile parent() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {

			var parent=this[i].parentNode;

			if ( parent && parent.nodeType === Node.ELEMENT_NODE ) {
				tile.push(parent);
			}
		}

		return tile;
	}-*/;

	public native Tile prev() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {
			if ( this[i].previousElementSibling ) { tile.push(this[i].previousElementSibling); }
		}

		return tile;
	}-*/;

	public native Tile next() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {
			if ( this[i].nextElementSibling ) { tile.push(this[i].nextElementSibling); }
		}

		return tile;
	}-*/;

	public native Tile first() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {
			if ( this[i].firstElementChild ) { tile.push(this[i].firstElementChild); }
		}

		return tile;

	}-*/;

	public native Tile last() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {
			if ( this[i].lastElementChild ) { tile.push(this[i].lastElementChild); }
		}

		return tile;

	}-*/;

	public native Tile child(final int index) /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {

			var children=this[i].childNodes;

			for (var j=0, elements=0; j < children.length; ++j) {
				if ( children[j].nodeType === Node.ELEMENT_NODE && elements++ == index ) {
					tile.push(children[j]);
				}
			}
		}

		return tile;

	}-*/;

	public native Tile siblings() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {

			var siblings=this[i].parentNode && this[i].parentNode.childNodes;

			if ( siblings ) {
				for (var j=0; j < siblings.length; ++j) {
					if ( siblings[j] !== this[i] && siblings[j].nodeType === Node.ELEMENT_NODE ) {
						tile.push(siblings[j]);
					}
				}
			}
		}

		return tile;
	}-*/;

	public native Tile children() /*-{

		var tile=[];

		for (var i=0; i < this.length; ++i) {

			var children=this[i].childNodes;

			for (var j=0; j < children.length; ++j) {
				if ( children[j].nodeType === Node.ELEMENT_NODE ) {
					tile.push(children[j]);
				}
			}
		}

		return tile;
	}-*/;

	public native Tile tree() /*-{

		var tile=[];

		function visit(node) {

			tile.push(node);

			var children=node.childNodes;

			for (var j=0; j < children.length; ++j) {
				if ( children[j].nodeType === Node.ELEMENT_NODE ) { visit(children[j]); }
			}
		}

		for (var i=0; i < this.length; ++i) { visit(this[i]); }

		return tile;
	}-*/;

	public native Tile leafs() /*-{

		var tile=[];

		function visit(node) {

			var children=node.childNodes;

			if ( children.length === 0 ) {
				tile.push(node);
			} else {
				for (var j=0; j < children.length; ++j) {
					if ( children[j].nodeType === Node.ELEMENT_NODE ) { visit(children[j]); }
				}
			}
		}

		for (var i=0; i < this.length; ++i) { visit(this[i]); }

		return tile;
	}-*/;


	public native Tile and(final Plugin tile) /*-{

		var and=[];

		if ( tile ) {
			for (var i=0; i < this.length; ++i) {
				for (var j=0; j < tile.length; ++j) {
					if ( this[i] === tile[j] ) {
						and.push(this[i]);
					}
				}
			}
		}

		return and;
	}-*/;

	public native Tile or(final Plugin tile) /*-{

		var or=[];

		for (var i=0; i < this.length; ++i) {
			or.push(this[i]);
		}

		if ( tile ) {
			for (var j=0; j < tile.length; ++j) {
				if ( or.indexOf(tile[j]) < 0 ) {
					or.push(tile[j]);
				}
			}
		}

		return or;
	}-*/;

	public native Tile not(final Plugin tile) /*-{

		var not=[];

		for (var i=0; i < this.length; ++i) {
			not.push(this[i]);
		}

		if ( tile ) {
			for (var j=0; j < tile.length; ++j) {
				if ( (i=not.indexOf(tile[j])) >= 0 ) {
					not.splice(i, 1);
				}
			}
		}

		return not;
	}-*/;

	public native Tile xor(final Plugin tile) /*-{

		var xor=[];

		for (var i=0; i < this.length; ++i) {
			xor.push(this[i]);
		}

		if ( tile ) {
			for (var j=0; j < tile.length; ++j) {
				if ( (i=xor.indexOf(tile[j])) >= 0 ) {
					xor.splice(i, 1);
				} else {
					xor.push(tile[j]);
				}
			}
		}

		return xor;
	}-*/;


	public native Tile when(final boolean test) /*-{
		return test ? this : [];
	}-*/;

	public Tile when(final String selector) {
		return find(selector).size() > 0 ? this : $();
	}

	// !!! when(Lambda)


	public native Tile unless(final boolean test) /*-{
		return !test ? this : [];
	}-*/;

	public Tile unless(final String selector) {
		return find(selector).size() == 0 ? this : $();
	}

	// !!! unless(Lambda)


	public native Tile with(final Lambda<Tile> lambda) /*-{
		return lambda && lambda.@com.metreeca._tile.client.Lambda::apply(Ljava/lang/Object;)(this) || [];
	}-*/;

	public native Tile each(final Lambda<Tile> lambda) /*-{

		var tile=[];

		if ( lambda ) {
			for (var i=0; i < this.length; ++i) {

				var items=lambda.@com.metreeca._tile.client.Lambda::apply(Ljava/lang/Object;)([this[i]]);

				if ( items !== null ) {
					for (var j=0; j < items.length; ++j) {
						tile.push(items[j]);
					}
				}
			}
		}

		return tile;
	}-*/;


	//// Testing ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public native boolean contains(final Plugin tile) /*-{

		function nodes_nodes(parents, children) {

			for (var i=0; i < children.length; ++i) {
				if ( !nodes_node(parents, children[i]) ) { return false; }
			}

			return true;
		}

		function nodes_node(parents, child) {

			for (var i=0; i < parents.length; ++i) {
				if ( node_node(parents[i], child) ) { return true; }
			}

			return false;
		}

		function node_node(parent, child) {

			for (var node=child; node !== null; node=node.parentNode) {
				if ( node === parent ) { return true; }
			}

			return false;
		}

		return !tile || nodes_nodes(this, tile);
	}-*/;


	public native boolean all(final Plugin tile) /*-{

		if ( tile ) {
			for (var j=0; j < tile.length; ++j) {
				if ( Array.prototype.indexOf.call(this, tile[j]) < 0 ) { return false; }
			}
		}

		return true;
	}-*/;

	public native boolean all(final Lambda<Tile> lambda) /*-{

		if ( lambda ) {
			for (var i=0; i < this.length; ++i) {

				var items=lambda.@com.metreeca._tile.client.Lambda::apply(Ljava/lang/Object;)([this[i]]);

				if ( !items || items.length === 0 ) { return false; }
			}
		}

		return true;
	}-*/;


	public native boolean any(final Plugin tile) /*-{

		if ( tile ) {
			for (var j=0; j < tile.length; ++j) {
				if ( Array.prototype.indexOf.call(this, tile[j]) >= 0 ) { return true; }
			}
		}

		return false;
	}-*/;

	public native boolean any(final Lambda<Tile> lambda) /*-{

		if ( lambda ) {

			for (var i=0; i < this.length; ++i) {

				var items=lambda.@com.metreeca._tile.client.Lambda::apply(Ljava/lang/Object;)([this[i]]);

				if ( items && items.length > 0 ) { return true; }
			}
		}

		return false;
	}-*/;


	public boolean none(final Plugin tile) {
		return !any(tile);
	}

	public boolean none(final Lambda<Tile> lambda) {
		return !any(lambda);
	}


	//// Structure /////////////////////////////////////////////////////////////////////////////////////////////////////

	public native Tile remove() /*-{

		for (var i=0; i < this.length; ++i) {
			this[i].remove();
		}

		return this;
	}-*/;


	public native Tile clear() /*-{

		for (var i=0; i < this.length; ++i) {
			while ( this[i].lastChild ) {
				this[i].lastChild.remove();
			}
		}

		return this;

	}-*/;

	public Tile clear(final String selector) {

		find(selector).remove();

		return this;
	}


	public Tile append(final View view) {
		return append($(view));
	}

	public native Tile append(final Plugin tile) /*-{

		if ( tile !== null ) {
			for (var i=0; i < this.length; ++i) {
				for (var j=0; j < tile.length; ++j) {
					this[i].appendChild(this.length > 1 ? tile[j].cloneNode(true) : tile[j]);
				}
			}
		}

		return this;
	}-*/;

	public Tile appendTo(final View view) {
		return appendTo($(view));
	}

	public Tile appendTo(final Plugin tile) {

		$(tile).append(this);

		return this;
	}


	public Tile insert(final View view) {
		return insert($(view));
	}

	public Tile insert(final Plugin tile) {
		return insert(tile, 0);
	}

	public Tile insert(final View view, final int index) {
		return insert($(view), index);
	}

	public native Tile insert(final Plugin tile, final int index) /*-{

		function nth(nodes, index) { // return the index of the nth children element

			for (var target=0, count=0; target < nodes.length; ++target) {
				if ( nodes[target].nodeType === Node.ELEMENT_NODE ) {
					if ( count === index ) { return target; } else { ++count; }
				}
			}

			return nodes.length;
		}

		if ( tile !== null ) {
			for (var i=0; i < this.length; ++i) {
				for (var j=0; j < tile.length; ++j) {

					var node=tile[j];

					node.remove(); // detach before computing index to handle self-insertion

					this[i].insertBefore(
							this.length > 1 ? node.cloneNode(true) : node,
							this[i].childNodes[nth(this[i].childNodes, index)+j]);
				}
			}
		}

		return this;
	}-*/;

	public Tile insertInto(final View view) {
		return insertInto($(view));
	}

	public Tile insertInto(final Plugin tile) {

		$(tile).insert(this);

		return this;
	}

	public Tile insertInto(final View view, final int index) {
		return insertInto($(view), index);
	}

	public Tile insertInto(final Plugin tile, final int index) {

		$(tile).insert(this, index);

		return this;
	}


	public Tile replace(final View view) {
		return replace($(view));
	}

	public Tile replace(final Plugin tile) {

		swap(tile);

		return this;
	}

	public Tile replaceWith(final View view) {
		return replaceWith($(view));
	}

	public Tile replaceWith(final Plugin tile) {

		$(tile).replace(this);

		return this;
	}


	public Tile wrap(final View view) {
		return wrap($(view));
	}

	public Tile wrap(final Plugin tile) {

		swap(tile).leafs().append(tile);

		return this;
	}

	public Tile wrapWith(final View view) {
		return wrapWith($(view));
	}

	public Tile wrapWith(final Plugin tile) {

		$(tile).wrap(this);

		return this;
	}


	private native Tile swap(final Plugin tile) /*-{

		var replacements=[];

		if ( tile ) {

			for (var j=0; j < tile.length; ++j) {

				var context=tile[j].parentNode;

				for (var i=0; i < this.length; ++i) {

					var replacement=tile.length > 1 ? this[i].cloneNode(true) : this[i];

					if ( context && replacement !== tile[j] ) {
						context.insertBefore(replacement, tile[j]);
						context.removeChild(tile[j]);
					}

					replacements.push(replacement);
				}
			}

		}

		return replacements;

	}-*/;


	//// Element ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public native int index() /*-{
		if ( this[0] && this[0].parentNode ) {

			var index=0;

			for (var e=this[0]; e=e.previousSibling;) { ++index; }

			return index;

		} else {
			return -1;
		}
	}-*/;


	public native String id() /*-{
		return this[0] && (this[0].id || (this[0].id="id"+($wnd.$id ? ++$wnd.$id : ($wnd.$id=1)))) || "";
	}-*/;


	public String title() {
		return attribute("title");
	}

	public Tile title(final String title) {
		return attribute("title", title);
	}


	public native String text() /*-{
		return this[0] && this[0].textContent || "";
	}-*/;

	public String text(final boolean normalized) {
		return normalized ? normalize(text()) : text();
	}

	public native Tile text(final String text) /*-{

		text=text || "";

		for (var i=0; i < this.length; ++i) {
			this[i].textContent=text;
		}

		return this;
	}-*/;


	public native String html() /*-{
		return this[0] && this[0].innerHTML || "";
	}-*/;

	public native Tile html(final String html) /*-{

		html=html || "";

		for (var i=0; i < this.length; ++i) {
			this[i].innerHTML=html;
		}

		return this;
	}-*/;


	public boolean enabled() { // !!! all/any?
		return attribute("disabled") == null; // no property if not an input
	}

	public Tile enabled(final boolean enabled) {  // !!! blur if disabled while focused
		return attribute("disabled", enabled ? null : "disabled");
	}


	public boolean selected() { // !!! all/any?
		return Boolean.TRUE.equals(property("selected")) || attribute("selected") != null; // property only on options
	}

	public Tile selected(final boolean selected) {
		return property("selected") != null ? property("selected", selected) : attribute("selected", selected ? "selected" : null);
	}


	public boolean checked() { // !!! all/any?
		return Boolean.TRUE.equals(property("checked")) || attribute("checked") != null; // property only on inputs
	}

	public Tile checked(final boolean checked) {
		return property("checked") != null ? property("checked", checked) : attribute("checked", checked ? "checked" : null);
	}


	public boolean editable() {  // !!! all/any? // !!! generic editable test?
		return Boolean.TRUE.equals(property("isContentEditable")) // handle contenteditable='false|inherit'
				|| this.<String>property("tagName").equalsIgnoreCase("input") // !!! exclude checkbox/radio/button
				|| this.<String>property("tagName").equalsIgnoreCase("textarea");
	}

	public Tile editable(final boolean editable) {
		return attribute("contenteditable", editable ? "true" : null);
	}


	public native String name() /*-{
		return this[0] && this[0].name || "";
	}-*/;

	public native Tile name(final String name) /*-{

		name=name || "";

		for (var i=0; i < this.length; ++i) {
			this[i].name=name;
		}

		return this;
	}-*/;


	public native String value() /*-{
		return this[0] && this[0].value || "";
	}-*/;

	public String value(final boolean normalized) {
		return normalized ? normalize(value()) : value();
	}

	public native Tile value(final String value) /*-{

		value=value || "";

		for (var i=0; i < this.length; ++i) {
			this[i].value=value;
		}

		return this;
	}-*/;


	public native String attribute(final String name)/*-{

		name=name || "";

		return this[0] && this[0].getAttribute && this[0].getAttribute(name); // no attributes on document

	}-*/;

	public native Tile attribute(final String name, final String value)/*-{

		name=name || "";

		for (var i=0; i < this.length; ++i) {
			if ( this[i].getAttribute ) { // no attributes on document
				if ( value ) {
					this[i].setAttribute(name, value);
				} else {
					this[i].removeAttribute(name);
				}
			}
		}

		return this;
	}-*/;

	public Tile attribute(final String name, final boolean value) {
		return attribute(name, value ? name : "");
	}

	public Tile attribute(final String name, final int value) {
		return attribute(name, String.valueOf(value));
	}

	public Tile attribute(final String name, final float value) {
		return attribute(name, String.valueOf(value));
	}


	public native <V> V property(final String name)/*-{

		name=name || "";

		var value=this[0] && this[0][name];
		var type=typeof value;

		return type === 'undefined' ? null
				: type === 'object' || type === 'string' ? value
						: type === 'boolean' && value === true ? @java.lang.Boolean::TRUE
								: type === 'boolean' && value === false ? @java.lang.Boolean::FALSE
										: new Object(value);
	}-*/;

	public native <V> Tile property(final String name, final V value)/*-{

		name=name || "";

		for (var i=0; i < this.length; ++i) {
			this[i][name]
					=@java.lang.Boolean::TRUE.@java.lang.Object::equals(Ljava/lang/Object;)(value) ? true
					: @java.lang.Boolean::FALSE.@java.lang.Object::equals(Ljava/lang/Object;)(value) ? false
							: value;
		}

		return this;
	}-*/;

	public Tile property(final String name, final int value) {
		return property(name, String.valueOf(value));
	}

	public Tile property(final String name, final float value) {
		return property(name, String.valueOf(value));
	}


	// !!! review

	public <V> V get(final String key) {

		final V value=get(key, null);

		if ( value == null ) {
			throw new IllegalStateException("undefined value ["+key+"]");
		}

		return value;
	}

	public native <V> V get(final String key, final V fallback) /*-{

		key=key || "";

		return this[0] && (this[0].$data=this[0].$data || {})[key] || fallback;

	}-*/;

	public native Tile set(final String key, final Object value) /*-{

		key=key || "";

		for (var i=0; i < this.length; ++i) {
			if ( value ) {
				(this[i].$data=this[i].$data || {})[key]=value;
			} else {
				delete (this[i].$data=this[i].$data || {})[key];
			}
		}

		return this;
	}-*/;


	public <V> V get(final Class<V> type) {
		return (V)get(name(type));
	}

	public <V> V get(final Class<V> type, final V fallback) {
		return get(name(type), fallback);
	}

	public <V> Tile set(final Class<V> type, final V value) {
		return set(name(type), value);
	}


	//// Classes ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public native boolean is(final String classes) /*-{

		classes=classes && classes.match(/[^,\s]+/g) || [];

		for (var i=0; i < this.length; ++i) {
			for (var c=0; c < classes.length; ++c) {
				if ( this[i].classList ) { // ;( no classList on SVG nodes
					if ( this[i].classList.contains(classes[c]) ) {
						return true;
					}
				} else if ( this[i].getAttribute  // no attributes on document
						&& (this[i].getAttribute('class') || '').search(new RegExp('\\b'+classes[c]+'\\b')) >= 0 ) {
					return true;
				}
			}
		}

		return false;

	}-*/;

	public Tile is(final String classes, final Boolean state) {
		return state == Boolean.TRUE ? add(classes) : state == Boolean.FALSE ? remove(classes) : toggle(classes);
	}


	private native Tile add(final String classes) /*-{

		classes=classes && classes.match(/[^,\s]+/g) || [];

		for (var i=0; i < this.length; ++i) {
			for (var c=0; c < classes.length; ++c) {
				if ( this[i].classList ) { // ;( no classList on SVG nodes
					this[i].classList.add(classes[c]);
				} else if ( this[i].getAttribute // no attributes on document
						&& (this[i].getAttribute('class') || '').search(new RegExp('\\b'+classes[c]+'\\b')) < 0 ) {
					this[i].setAttribute('class', (this[i].getAttribute('class') || '')+' '+classes[c]);
				}
			}
		}

		return this;
	}-*/;

	private native Tile remove(final String classes) /*-{

		classes=classes && classes.match(/[^,\s]+/g) || [];

		for (var i=0; i < this.length; ++i) {
			for (var c=0; c < classes.length; ++c) {
				if ( this[i].classList ) { // ;( no classList on SVG nodes
					this[i].classList.remove(classes[c]);
				} else if ( this[i].getAttribute ) { // no attributes on document
					this[i].setAttribute('class', (this[i].getAttribute('class') || '').replace(new RegExp('\\s+\\b'+classes[c]+'\\b'), ''))
				}
			}
		}

		return this;
	}-*/;

	private native Tile toggle(final String classes) /*-{

		classes=classes && classes.match(/[^,\s]+/g) || [];

		for (var i=0; i < this.length; ++i) {
			for (var c=0; c < classes.length; ++c) {
				if ( this[i].classList ) { // ;( no classList on SVG nodes
					this[i].classList.toggle(classes[c]);
				} else if ( this[i].getAttribute // no attributes on document
						&& (this[i].getAttribute('class') || '').search(new RegExp('\\b'+classes[c]+'\\b')) < 0 ) {
					this[i].setAttribute('class', (this[i].getAttribute('class') || '')+' '+classes[c]);
				} else if ( this[i].getAttribute ) { // no attributes on document
					this[i].setAttribute('class', (this[i].getAttribute('class') || '').replace(new RegExp('\\s+\\b'+classes[c]+'\\b'), ''));
				}
			}
		}

		return this;
	}-*/;


	public <T extends Enum<T>> Tile is(final Class<T> type, final Boolean state) {

		final String prefix=type.getSimpleName().toLowerCase();

		for (final T value : type.getEnumConstants()) {
			is(prefix+"-"+value.name().toLowerCase(), state);
		}

		return this;
	}

	public <T extends Enum<T>> Tile is(final T token, final Boolean state) {

		final Class<T> type=token.getDeclaringClass();

		final String prefix=type.getSimpleName().toLowerCase();

		is(prefix+"-"+token.name().toLowerCase(), state);

		return this;
	}


	//// Style /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Tile style(final Plugin tile) {
		return style(tile, false);
	}

	public native Tile style(final Plugin tile, final boolean computed) /*-{

		if ( tile && tile[0] ) {

			// ;(ff) $wnd.getComputedStyle may be null fr hidden elements

			var master=tile[0];
			var style=computed ? ($wnd.getComputedStyle(master, null) || {}) : master.style;

			for (var i=0; i < this.length; ++i) {
				for (var j=0; j < style.length; ++j) {

					var name=style.item(j);
					var value=style.getPropertyValue(name);
					var priority=style.getPropertyPriority(name);

					this[i].style.setProperty(name, value, priority);

					// ;(ff/wk) computed style reports content width/height ignoring 'box:sizing: border-box'

					if ( style.boxSizing === "border-box" || style.MozBoxSizing === "border-box" ) {

						var box=master.getBoundingClientRect();

						this[i].style.width=box.width+"px";
						this[i].style.height=box.height+"px";
					}
				}
			}
		}

		return this;

	}-*/;


	public String style(final String property) {
		return style(property, false);
	}

	public native String style(final String property, final boolean computed) /*-{

		property=(property || "").replace(/-([a-z]?)/g, function (s, s1) { return s1.toUpperCase() });

		// ;(ff) $wnd.getComputedStyle may be null fr hidden elements

		return this[0] && (computed ? ($wnd.getComputedStyle(this[0], null) || {})[property] : this[0].style[property]) || "";

	}-*/;

	public native Tile style(final String property, final String value) /*-{

		property=(property || "").replace(/-([a-z]?)/g, function (s, s1) { return s1.toUpperCase() });
		value=value || "";

		for (var i=0; i < this.length; ++i) {
			this[i].style[property]=value;
		}

		return this;
	}-*/;


	public Tile overlay(final String property, final Integer value) {
		return overlay(property, value == null ? null : String.valueOf(value));
	}

	public native Tile overlay(final String property, final String value) /*-{

		property=(property || "").replace(/-([a-z]?)/g, function (s, s1) { return s1.toUpperCase() });

		for (var i=0; i < this.length; ++i) {

			if ( !this[i].$style ) {
				this[i].$style={};
			}

			if ( this[i].$style[property] === undefined ) {
				this[i].$style[property]=this[i].style[property];
			}

			if ( value !== null ) {
				this[i].style[property]=value;
			}
		}

		return this;
	}-*/;

	public native Tile restore(final String property) /*-{

		property=(property || "").replace(/-([a-z]?)/g, function (s, s1) { return s1.toUpperCase() });

		for (var i=0; i < this.length; ++i) {

			if ( this[i].$style ) {

				if ( this[i].$style[property] !== undefined ) {
					this[i].style[property]=this[i].$style[property];
				}

				delete this[i].$style[property];
			}
		}

		return this;
	}-*/;

	public native Tile discard(final String property) /*-{

		property=(property || "").replace(/-([a-z]?)/g, function (s, s1) { return s1.toUpperCase() });

		for (var i=0; i < this.length; ++i) {
			if ( this[i].$style ) {
				delete this[i].$style[property];
			}
		}

		return this;
	}-*/;


	public Tile skin(final String css) {
		return skin(css, false);
	}

	public Tile skin(final String css, final boolean important) {
		if ( css != null ) {

			String id=Resources.skins.get(css);

			if ( id == null ) {

				Resources.skins.put(css, id="skin"+(Resources.skins.size()+1));

				$(important ? "body" : "head").append($("<style/>")
						.attribute("id", id)
						.attribute("type", "text/css")
						.text(prefix(css).replaceAll(":scope\\b", "."+id)));
			}

			return is(id, true);

		} else {
			return this;
		}
	}


	public native boolean visible() /*-{

		for (var i=0; i < this.length; ++i) {
			if ( !this[i].hidden && this[i].style.display !== "none" ) { return true; }
		}

		return false;

	}-*/;

	public native Tile visible(final Boolean visible) /*-{

		for (var i=0; i < this.length; ++i) {

			var node=this[i];

			var hidden=node.hidden || node.style.display === "none";

			if ( visible === @java.lang.Boolean::TRUE || visible === null && hidden ) {

				if ( node.tile$display !== undefined ) {  // restore display value (idempotent)
					try { node.style.display=node.tile$display; } finally { delete node.tile$display; }
				}

				node.hidden=false;

			} else {

				if ( node.tile$display === undefined ) { // stash display value (idempotent)
					node.tile$display=node.style.display;
				}

				node.hidden=true;
				node.style.display="none";

			}
		}

		return this;

	}-*/;


	public Tile hide() { return visible(false); }

	public Tile show() { return visible(true); }


	//// Geometry //////////////////////////////////////////////////////////////////////////////////////////////////////

	public native Box box() /*-{
		return this[0] && this[0].getBoundingClientRect() || {};
	}-*/;

	public native Box client() /*-{
		return this[0] && { width: this[0].clientWidth, height: this[0].clientHeight } || {};
	}-*/;


	public float x() {
		return x(false);
	}

	public float x(final boolean absolute) { // !!! native
		return left(absolute)+width(absolute)/2;
	}

	// !!! x setter


	public float y() {
		return y(false);
	}

	public float y(final boolean absolute) { // !!! native
		return top(absolute)+height(absolute)/2;
	}

	// !!! y setter


	public float top() {
		return top(false);
	}

	public native float top(final boolean absolute) /*-{ // !!! handle transforms
		if ( absolute ) {

			return this[0] && this[0].getBoundingClientRect().top || 0;

		} else { // no offset on SVG nodes
			return this[0] && (this[0].getBBox ? this[0].getBBox().y : this[0].offsetTop) || 0;
		}
	}-*/;

	public Tile top(final float top) {
		return top(top+"px");
	}

	public Tile top(final String top) {
		return style("top", top);
	}


	public float bottom() {
		return bottom(false);
	}

	public native float bottom(final boolean absolute) /*-{ // !!! handle transforms
		if ( absolute ) {

			return this[0] && $doc.documentElement.getBoundingClientRect().height-this[0].getBoundingClientRect().bottom || 0;

		} else { // no offset on SVG nodes
			return this[0] && (this[0].getBBox
							? this[0].parentNode ? this[0].parentNode.getBBox().height-(this[0].getBBox().y+this[0].getBBox().height) : 0
							: this[0].offsetParent ? this[0].offsetParent.offsetHeight-(this[0].offsetTop+this[0].offsetHeight) : 0
			) || 0;
		}
	}-*/;

	public Tile bottom(final float bottom) {
		return bottom(bottom+"px");
	}

	public Tile bottom(final String bottom) {
		return style("bottom", bottom);
	}


	public float left() {
		return left(false);
	}

	public native float left(final boolean absolute) /*-{ // !!! handle transforms
		if ( absolute ) {

			return this[0] && this[0].getBoundingClientRect().left || 0;

		} else { // no offset on SVG nodes
			return this[0] && (this[0].getBBox ? this[0].getBBox().x : this[0].offsetLeft) || 0;
		}
	}-*/;

	public Tile left(final float left) {
		return left(left+"px");
	}

	public Tile left(final String left) {
		return style("left", left);
	}


	public float right() {
		return right(false);
	}

	public native float right(final boolean absolute) /*-{ // !!! handle transforms
		if ( absolute ) {

			return this[0] && $doc.documentElement.getBoundingClientRect().width-this[0].getBoundingClientRect().right || 0;

		} else { // no offset on SVG nodes
			return this[0] && (this[0].getBBox
							? this[0].parentNode ? this[0].parentNode.getBBox().width-(this[0].getBBox().x+this[0].getBBox().width) : 0
							: this[0].offsetParent ? this[0].offsetParent.offsetWidth-(this[0].offsetLeft+this[0].offsetWidth) : 0
			) || 0;
		}
	}-*/;

	public Tile right(final float right) {
		return right(right+"px");
	}

	public Tile right(final String right) {
		return style("right", right);
	}


	public float width() {
		return width(false);
	}

	public native float width(final boolean absolute) /*-{
		if ( absolute ) {

			return this[0] && this[0].getBoundingClientRect().width || 0;

		} else {  // no offsetWidth on SVG nodes
			return this[0] && (this[0].getBBox ? this[0].getBBox().width : this[0].offsetWidth) || 0;
		}
	}-*/;

	public Tile width(final float width) {
		return width(width+"px");
	}

	public Tile width(final String width) {
		return style("width", width);
	}


	public float height() {
		return height(false);
	}

	public native float height(final boolean absolute) /*-{
		if ( absolute ) {

			return this[0] && this[0].getBoundingClientRect().height || 0;

		} else { // no offsetHeight on SVG nodes
			return this[0] && (this[0].getBBox ? this[0].getBBox().height : this[0].offsetHeight) || 0;
		}
	}-*/;

	public Tile height(final float height) {
		return height(height+"px");
	}

	public Tile height(final String height) {
		return style("height", height);
	}


	public native boolean scrolled() /*-{

		for (var i=0; i < this.length; ++i) {
			if ( this[i].scrollWidth > this[i].clientWidth || this[i].scrollHeight > this[i].clientHeight ) {
				return true;
			}
		}

		return false;
	}-*/;

	public native float dx() /*-{
		return this[0] && this[0].scrollLeft || 0;
	}-*/;

	public native float dy() /*-{
		return this[0] && this[0].scrollTop || 0;
	}-*/;

	public native float dw() /*-{ // no scrollWidth on SVG nodes
		return this[0] && (this[0].getBBox ? this[0].getBBox().height : this[0].scrollWidth) || 0;
	}-*/;

	public native float dh() /*-{ // no scrollHeight on SVG nodes
		return this[0] && (this[0].getBBox ? this[0].getBBox().height : this[0].scrollHeight) || 0;
	}-*/;


	public Tile scroll(final float left, final float top) {
		return scroll(left, top, false);
	}

	public native Tile scroll(final float left, final float top, final boolean relative) /*-{

		for (var i=0; i < this.length; ++i) {
			if ( relative ) {
				this[i].scrollLeft=Math.max(0, this[i].scrollLeft+left);
				this[i].scrollTop=Math.max(0, this[i].scrollTop+top);
			} else {
				this[i].scrollLeft=(left >= 0) ? left : this[i].scrollWidth-left+1;
				this[i].scrollTop=(top >= 0) ? top : this[i].scrollHeight-top+1;
			}
		}

		return this;
	}-*/;

	public Tile scroll(final Action<Event> action) {
		return bind("scroll", action);
	}


	//// Schedule //////////////////////////////////////////////////////////////////////////////////////////////////////

	public Tile async(final Action<Event> action) {
		return delay(0, action);
	}


	public native Tile delay(final int delay, final Action<Event> action) /*-{

		if ( delay >= 0 && action ) {
			for (var i=0; i < this.length; ++i) {
				(function (target) {

					var timeouts=target.tile$timeouts || (target.tile$timeouts={});

					var id=$wnd.setTimeout($entry(function () {

						try {

							(action.@com.metreeca._tile.client.Action::listener)({
								type: 'delay',
								target: target
							}); // pseudo-event

						} finally {
							delete timeouts[id];
						}

					}), delay);

					timeouts[id]=action.@com.metreeca._tile.client.Action::listener; // used by cancel

				})(this[i]);
			}
		}

		return this;

	}-*/;

	public native Tile repeat(final int period, final Action<Event> action) /*-{

		if ( period >= 0 && action ) {
			for (var i=0; i < this.length; ++i) {
				(function (target) {

					var intervals=target.tile$intervals || (target.tile$intervals={});

					var id=$wnd.setInterval($entry(function () {

						var event={
							type: 'repeat',
							target: target
						}; // pseudo-event

						(action.@com.metreeca._tile.client.Action::listener)(event);

						if ( event.defaultPrevented ) {
							try { $wnd.clearInterval(id); } finally { delete intervals[id]; }
						}

					}), period);

					intervals[id]=action.@com.metreeca._tile.client.Action::listener; // used by cancel

				})(this[i]);
			}
		}

		return this;

	}-*/;

	public native Tile animate(final Action<Tile> action) /*-{

		if ( action ) {
			for (var i=0; i < this.length; ++i) {
				(function (target) {

					var signature=action.toString(); // action unique identity used as a key for the wrapper
					var animations=target.tile$animations || (target.tile$animations={});

					if ( Object.keys(animations).every(function (p) { return animations[p] !== signature }) ) {

						var id=$wnd.requestAnimationFrame($entry(function (time) {
							try {

								action.@com.metreeca._tile.client.Action::execute(*)(target); // !!! pass the timestamp

							} finally {

								delete animations[id];

							}
						}));

						animations[id]=signature; // used by cancel

					}

				})(this[i]);
			}
		}

		return this;

	}-*/;


	public native Tile cancel(final Action<Event> action) /*-{

		if ( action ) {
			for (var i=0; i < this.length; ++i) {

				var signature=action.toString(); // action unique identity used as a key for the wrapper

				var timeouts=this[i].tile$timeouts || (this[i].tile$timeouts={});
				var intervals=this[i].tile$intervals || (this[i].tile$intervals={});
				var animations=this[i].tile$animations || (this[i].tile$animations={});

				for (var id in timeouts) {
					if ( timeouts.hasOwnProperty(id) ) {
						if ( timeouts[id] === action.@com.metreeca._tile.client.Action::listener ) {
							try { $wnd.clearTimeout(id); } finally { delete timeouts[id]; }
						}
					}
				}

				for (var id in intervals) {
					if ( intervals.hasOwnProperty(id) ) {
						if ( intervals[id] === action.@com.metreeca._tile.client.Action::listener ) {
							try { $wnd.clearInterval(id); } finally { delete intervals[id]; }
						}
					}
				}

				for (var id in animations) {
					if ( animations.hasOwnProperty(id) ) {
						if ( animations[id] === signature ) {
							try { $wnd.cancelRequestAnimationFrame(id); } finally { delete animations[id]; }
						}
					}
				}

			}
		}

		return this;
	}-*/;

	public native Tile cancel() /*-{

		for (var i=0; i < this.length; ++i) {

			var timeouts=this[i].tile$timeouts || (this[i].tile$timeouts={});
			var intervals=this[i].tile$intervals || (this[i].tile$intervals={});
			var animations=this[i].tile$animations || (this[i].tile$animations={});

			for (var id in timeouts) {
				if ( timeouts.hasOwnProperty(id) ) {
					try { $wnd.clearTimeout(id); } finally { delete timeouts[id]; }
				}
			}

			for (var id in intervals) {
				if ( intervals.hasOwnProperty(id) ) {
					try { $wnd.clearInterval(id); } finally { delete intervals[id]; }
				}
			}

			for (var id in animations) {
				if ( animations.hasOwnProperty(id) ) {
					try { $wnd.cancelRequestAnimationFrame(id); } finally { delete animations[id]; }
				}
			}

		}

		return this;
	}-*/;


	//// Focus /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public native boolean focused() /*-{

		for (var i=0; i < this.length; ++i) {
			if ( $doc.activeElement === this[i] ) { return true; }
		}

		return false;
	}-*/;


	public native Tile focus() /*-{

		if ( this[0] && this[0] !== $doc.activeElement ) {

			this[0].focus();

			//var event=($doc).createEvent("HTMLEvents");
			//event.initEvent("focus", false, false);
			//
			//if ( this[0].dispatchEvent(event) ) {
			//	this[0].focus(); // ;( no event on programmatic focus // !!! ;(wk) as well?
			//}
		}

		return this;
	}-*/;

	public Tile focus(final Action<Event> action) {
		return bind("focus", action, true); // focus events don't bubble > enable delegation
	}


	public native Tile blur() /*-{

		for (var i=0; i < this.length; ++i) {
			if ( this[i] === $doc.activeElement ) {

				this[i].blur();

				//var event=($doc).createEvent("HTMLEvents");
				//event.initEvent("blur", false, false);
				//
				//if ( this[0].dispatchEvent(event) ) {
				//	this[0].blur(); // ;( no event on programmatic blur // !!! ;(wk) as well?
				//}

				return this;
			}
		}

		return this;
	}-*/;

	public Tile blur(final Action<Event> action) {
		return bind("blur", action, true); // blur events don't bubble > enable delegation
	}


	//// Keyboard //////////////////////////////////////////////////////////////////////////////////////////////////////

	public Tile keydown(final Action<Event> action) {
		return bind("keydown", action);
	}

	public Tile keyup(final Action<Event> action) {
		return bind("keyup", action);
	}

	public Tile keypress(final Action<Event> action) {
		return bind("keypress", action);
	}


	//// Mouse /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public native Tile click() /*-{

		for (var i=0; i < this.length; ++i) { this[i].click(); }

		return this;
	}-*/;


	public Tile mousemove(final Action<Event> action) {
		return bind("mousemove", action);
	}

	public Tile mouseenter(final Action<Event> action) {
		return bind("mouseenter", action);
	}

	public Tile mouseleave(final Action<Event> action) {
		return bind("mouseleave", action);
	}

	public native Tile mouseenter(final int delay, final Action<Event> action) /*-{

		function mouseenter(node) {

			var timeout;

			node.addEventListener("mouseenter", function (e) {
				timeout=$wnd.setTimeout($entry(function () {
					action.@com.metreeca._tile.client.Action::execute(*)(e);
				}), delay);
			});

			node.addEventListener("mouseleave", function () {
				$wnd.clearTimeout(timeout);
			});

		}

		if ( delay >= 0 && action ) {
			for (var i=0; i < this.length; ++i) { mouseenter(this[i]); }
		}

		return this;

	}-*/;

	public native Tile mouseleave(final int delay, final Action<Event> action) /*-{

		function mouseleave(node) {

			var timeout;

			node.addEventListener("mouseleave", function (e) {
				timeout=$wnd.setTimeout($entry(function () {
					action.@com.metreeca._tile.client.Action::execute(*)(e);
				}), delay);
			});

			node.addEventListener("mouseenter", function () {
				$wnd.clearTimeout(timeout);
			});

		}

		if ( delay >= 0 && action ) {
			for (var i=0; i < this.length; ++i) { mouseleave(this[i]); }
		}

		return this;

	}-*/;

	public Tile mouseover(final Action<Event> action) {
		return bind("mouseover", action);
	}

	public Tile mouseout(final Action<Event> action) {
		return bind("mouseout", action);
	}

	public Tile mousedown(final Action<Event> action) {
		return bind("mousedown", action);
	}

	public Tile mouseup(final Action<Event> action) {
		return bind("mouseup", action);
	}

	public Tile menu(final Action<Event> action) {
		return bind("contextmenu", action);
	}

	public Tile click(final Action<Event> action) {
		return bind("click", action);
	}

	public Tile dblclick(final Action<Event> action) {
		return bind("dblclick", action);
	}

	public Tile wheel(final Action<Event> action) {
		return bind("mousewheel DOMMouseScroll", action); // !!! review after DOM level 3 are implemented
	}


	public Tile hover(final Action<Event> action) {
		return bind(Hover.Event, action)

				.bind(Hover.Triggers, Hover.Start)
				.bind(Hover.Stoppers, Hover.Clear);
	}

	public Tile hold(final Action<Event> action) {
		return bind(Hold.Event, action)

				.bind(Hold.Triggers, Hold.Start)
				.bind(Hold.Stoppers, Hold.Clear);
	}


	public Tile drag(final Action.Drag action) {
		return attribute("draggable", "true")

				.bind("mousedown mouseup mouseout dragstart dragend drag", action)

				.focus(new Action<Event>() { // disable dragging while editing nested elements
					@Override public void execute(final Event e) {
						e.current().attribute("draggable", e.target().editable() ? "" : "true");
					}
				})

				.blur(new Action<Event>() { // re-enable dragging after editing is completed
					@Override public void execute(final Event e) {
						e.current().attribute("draggable", "true");
					}
				});
	}

	public Tile drop(final Action.Drop action) {
		return attribute("droppable", "true")

				.bind("dragenter dragover dragleave drop", action);
	}


	//// Input /////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@code true} if all the nodes of the tile supporting input validation are valid; <code>false</code>
	 * otherwise
	 */
	public native boolean valid() /*-{

		for (var i=0; i < this.length; ++i) {
			if ( this[i].checkValidity && this[i].checkValidity() === false ) { return false; }
		}

		return true;
	}-*/;


	public native Tile validity(final String message) /*-{

		for (var i=0; i < this.length; ++i) {
			this[i].setCustomValidity && this[i].setCustomValidity(message || "");
		}

		return this;

	}-*/;


	public Tile edit(final Action<Event> action) {
		return input(action).change(action);
	}


	public native Tile input() /*-{

		for (var i=0; i < this.length; ++i) {

			var event=$doc.createEvent("HTMLEvents");
			event.initEvent("input", true, true);

			this[i].dispatchEvent(event);
		}

		return this;
	}-*/;

	public Tile input(final Action<Event> action) {
		return bind("input", action);
	}


	public native Tile change() /*-{

		for (var i=0; i < this.length; ++i) {

			var event=$doc.createEvent("HTMLEvents");
			event.initEvent("change", true, true);

			this[i].dispatchEvent(event);
		}

		return this;

	}-*/;

	public Tile change(final Action<Event> action) {
		return bind("change", action);
	}


	public native Tile submit() /*-{

		for (var i=0; i < this.length; ++i) {

			var event=$doc.createEvent("HTMLEvents");
			event.initEvent("submit", true, true);

			this[i].dispatchEvent(event);
		}

		return this;
	}-*/;

	public Tile submit(final Action<Event> action) {
		return bind("submit", action);
	}


	public native Tile reset() /*-{

		for (var i=0; i < this.length; ++i) {

			var event=$doc.createEvent("HTMLEvents");
			event.initEvent("reset", true, true);

			this[i].dispatchEvent(event);
		}

		return this;
	}-*/;

	public Tile reset(final Action<Event> action) {
		return bind("reset", action);
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Tile bind(final String types, final Action<Event> action) {
		return bind(types, action, false);
	}

	public native Tile bind(final String types, final Action<Event> action, final boolean capture) /*-{

		if ( types && action ) {

			types=types.match(/[^,\s]+/g);

			for (var t=0; t < types.length; ++t) {

				var type=types[t];

				for (var i=0; i < this.length; ++i) {
					this[i].addEventListener(type, action.@com.metreeca._tile.client.Action::listener, capture);
				}
			}
		}

		return this;

	}-*/;


	public Tile drop(final String types, final Action<Event> action) {
		return drop(types, action, false);
	}

	public native Tile drop(final String types, final Action<Event> action, final boolean capture) /*-{

		if ( types && action ) {

			types=types.match(/[^,\s]+/g);

			for (var t=0; t < types.length; ++t) {
				for (var i=0; i < this.length; ++i) {
					this[i].removeEventListener(types[t], action.@com.metreeca._tile.client.Action::listener, capture);
				}
			}
		}

		return this;
	}-*/;


	public boolean fire(final String type) {
		return fire(type, null, false);
	}

	public boolean fire(final String type, final Object data) {
		return fire(type, data, false);
	}

	public native boolean fire(final String type, final Object data, final boolean local) /*-{

		var defaults=true;

		if ( type ) {
			for (var i=0; i < this.length; ++i) {
				if ( !this[i].disabled ) {

					// ;(ff) firing events on disable inputs is problematic (http://stackoverflow.com/questions/3100319/event-on-a-disabled-input)
					// ;(ff) and breaks if the element is not attached (https://bugzilla.mozilla.org/show_bug.cgi?id=889376)

					var event=$doc.createEvent("CustomEvent");
					event.initCustomEvent(type, !local, true, data);

					defaults=this[i].dispatchEvent(event) && defaults; // 'and' in this order to dispatch unconditionally}
				}
			}
		}

		return defaults;
	}-*/;


	//// Event Bus /////////////////////////////////////////////////////////////////////////////////////////////////////

	public Tile bind(final Class<?> type, final Action<Event> action) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		if ( type.isInterface() ) {
			throw new UnsupportedOperationException("interface event type ["+type+"]");
		}

		return bind(name(type), action).is("eventbus-listener", true).bind("eventbus-event", Resources.Relay);
	}

	public Tile drop(final Class<?> type, final Action<Event> action) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		if ( type.isInterface() ) {
			throw new UnsupportedOperationException("interface event type ["+type+"]");
		}

		return drop(name(type), action);
	}


	public Tile bind(final Enum<?> event, final Action<Event> action) {

		if ( event == null ) {
			throw new NullPointerException("null event");
		}

		return bind(name(event), action).is("eventbus-listener", true).bind("eventbus-event", Resources.Relay);
	}

	public Tile drop(final Enum<?> event, final Action<Event> action) {

		if ( event == null ) {
			throw new NullPointerException("null event");
		}

		return drop(name(event), action);
	}


	public Tile async(final Object event) {
		return async(new Action<Event>() {
			@Override public void execute(final Event e) { fire(event); }
		});
	}


	public native boolean fire(final Object event) /*-{

		var defaults=true;

		if ( event ) {

			var listeners=$doc.querySelectorAll(".eventbus-listener");

			for (var i=0; i < this.length; ++i) {

				// identify the target path (before firing, least the event detach the target from the dom)

				var path=[];

				for (var node=this[i]; node; node=node.parentNode) {
					path.push(node);
				}

				// dispatch to the target

				var e=$doc.createEvent("CustomEvent");
				e.initCustomEvent("eventbus-event", true, true, event); // bubbling

				var dispatch=this[i].dispatchEvent(e);

				// dispatch to bus listeners outside the target path, unless cancelled while bubbling

				if ( dispatch ) {
					for (var j=0; j < listeners.length; ++j) {
						if ( path.indexOf(listeners[j]) < 0 ) {

							var e=$doc.createEvent("CustomEvent");
							e.initCustomEvent("eventbus-event", false, true, event); // no bubbling

							dispatch=listeners[j].dispatchEvent(e) && dispatch; // 'and' in this order to dispatch unconditionally
						}
					}
				}

				defaults=dispatch && defaults;
			}
		}

		return defaults;

	}-*/;

}
