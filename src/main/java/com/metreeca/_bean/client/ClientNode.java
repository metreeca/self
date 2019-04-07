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

package com.metreeca._bean.client;

import com.metreeca._bean.shared.Node;

import com.google.gwt.core.client.JavaScriptObject;


public final class ClientNode extends JavaScriptObject implements Node {

	@SuppressWarnings("ProtectedMemberInFinalClass") protected ClientNode() {}


	@Override public native String name() /*-{
		return this.tagName;
	}-*/;


	@Override public native String attribute(final String name) /*-{
		return this.getAttribute(name) || "";
	}-*/;

	@Override public native Node attribute(final String name, final String value) /*-{

		if ( value ) {
			this.setAttribute(name, value);
		} else {
			this.removeAttribute(name);
		}

		return this;
	}-*/;

	@Override public native String text() /*-{

		var nodes=this.childNodes;
		var node=nodes[0];

		return nodes.length == 1 && node.nodeType == node.TEXT_NODE ? node.nodeValue : "";
	}-*/;

	@Override public native Node text(final String text) /*-{

		while ( this.childNodes.length ) {
			this.removeChild(this.childNodes[0]);
		}

		this.appendChild(this.ownerDocument.createTextNode(text));

		return this;
	}-*/;


	@Override public native int size() /*-{

		var size=0;

		var nodes=this.childNodes;

		for (var i=0, length=nodes.length; i < length; ++i) {
			if ( nodes.item(i).nodeType === Node.ELEMENT_NODE ) { ++size; }
		}

		return size;

	}-*/;

	@Override public native Node node(final int index) /*-{

		var nodes=this.childNodes;

		for (var i=0, n=0, length=nodes.length; i < length; ++i) {

			var node=nodes.item(i);

			if ( node.nodeType === Node.ELEMENT_NODE ) { ++n; }

			if ( n == index+1 ) {
				return node
			}
		}

		return null;

	}-*/;

	@Override public native Node node(final Node node) /*-{

		this.appendChild(node);

		return this;

	}-*/;


	public static Factory factory() {
		return ClientFactory.create();
	}


	private static final class ClientFactory extends JavaScriptObject implements Factory {

		public static native ClientFactory create() /*-{
			return {
				document: $doc.implementation.createDocument(null, null, null),
				parser: new DOMParser(),
				writer: new XMLSerializer()
			};
		}-*/;


		@SuppressWarnings("ProtectedMemberInFinalClass") protected ClientFactory() {}


		@Override public native Node node(final String name) /*-{
			return this.document.createElement(name);
		}-*/;

		@Override public native Node parse(final String text) /*-{
			return this.parser.parseFromString(text, "application/xml").documentElement; // !!! error handling
		}-*/;

		@Override public native String write(final Node node) /*-{
			return this.writer.serializeToString(node); // !!! error handling
		}-*/;

	}
}
