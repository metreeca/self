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

package com.metreeca._bean.server;

import com.metreeca._bean.shared.Node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public final class ServerNode implements Node {

	private static final DocumentBuilder builder=builder();
	private static final Transformer transformer=transformer();


	private static DocumentBuilder builder() {
		try {

			final DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);

			return factory.newDocumentBuilder();

		} catch ( final ParserConfigurationException e ) {
			throw new UnsupportedOperationException("unable to configure document builder", e);
		}
	}

	private static Transformer transformer() {
		try {

			return TransformerFactory.newInstance().newTransformer();

		} catch ( final TransformerConfigurationException e ) {
			throw new UnsupportedOperationException("unable to configure transformer", e);
		}
	}


	private final Element element;


	private ServerNode(final Element element) {
		this.element=element;
	}


	@Override public String name() {
		return element.getTagName();
	}


	@Override public String attribute(final String name) {

		final String value=element.getAttribute(name);

		return value != null ? value : "";
	}

	@Override public Node attribute(final String name, final String value) {

		if ( value == null || value.isEmpty() ) {
			element.removeAttribute(name);
		} else {
			element.setAttribute(name, value);
		}

		return this;
	}

	@Override public String text() {

		final NodeList nodes=element.getChildNodes();

		final org.w3c.dom.Node node=nodes.item(0);

		return nodes.getLength() == 1 && node.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? node.getNodeValue() : "";
	}

	@Override public Node text(final String text) {

		while ( element.getChildNodes().getLength() > 0 ) {
			element.removeChild(element.getChildNodes().item(0));
		}

		element.appendChild(element.getOwnerDocument().createTextNode(text));

		return this;
	}


	@Override public int size() {

		int size=0;

		final NodeList nodes=element.getChildNodes();

		for (int i=0, length=nodes.getLength(); i < length; ++i) {
			if ( nodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) { ++size; }
		}

		return size;
	}

	@Override public Node node(final int index) {

		final NodeList nodes=element.getChildNodes();

		for (int i=0, n=0, length=nodes.getLength(); i < length; ++i) {

			final org.w3c.dom.Node node=nodes.item(i);

			if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) { ++n; }

			if ( n == index+1 ) {
				return new ServerNode((Element)node);
			}
		}

		return null;
	}

	@Override public Node node(final Node node) {

		element.appendChild(((ServerNode)node).element);

		return this;
	}


	public static Factory factory() {
		return new Factory() {

			private final Document document=builder.newDocument();


			@Override public Node node(final String name) {
				return new ServerNode(document.createElement(name));
			}

			@Override public Node parse(final String text) {
				try {
					return new ServerNode(builder.parse(new InputSource(new StringReader(text))).getDocumentElement());
				} catch ( final SAXException e ) {
					throw new IllegalArgumentException("syntax error ["+e+"]");
				} catch ( final IOException e ) {
					throw new IOError(e);
				}
			}

			@Override public String write(final Node node) {

				transformer.setOutputProperty(OutputKeys.INDENT, "no");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

				final StringWriter writer=new StringWriter();

				try {
					transformer.transform(new DOMSource(((ServerNode)node).element), new StreamResult(writer));
				} catch ( final TransformerException unexpected ) {
					throw new UnsupportedOperationException("unable to transform document", unexpected);
				}

				return writer.toString();
			}
		};
	}
}
