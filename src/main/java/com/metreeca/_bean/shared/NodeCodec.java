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


public final class NodeCodec {

	private final InfoEncoder infoEncoder;
	private final InfoDecoder infoDecoder;

	private final NodeEncoder nodeEncoder;
	private final NodeDecoder nodeDecoder;


	public NodeCodec(final Meta.Factory meta, final Node.Factory node) {

		if ( meta == null ) {
			throw new NullPointerException("null meta factory");
		}

		if ( node == null ) {
			throw new NullPointerException("null node factory");
		}

		infoEncoder=new InfoEncoder(meta);
		infoDecoder=new InfoDecoder(meta);

		nodeEncoder=new NodeEncoder(node);
		nodeDecoder=new NodeDecoder(node);
	}


	public String encode(final Object object) {
		return encode(object, "");
	}

	public String encode(final Object object, final String namespace) {

		if ( object == null ) {
			throw new NullPointerException("null object");
		}

		if ( namespace == null ) {
			throw new NullPointerException("null namespace");
		}

		return nodeEncoder.encode(infoEncoder.encode(object), namespace);
	}


	public <T> T decode(final String text) {return decode(text, "");}

	public <T> T decode(final String text, final String namespace) {

		if ( text == null ) {
			throw new NullPointerException("null text");
		}

		if ( namespace == null ) {
			throw new NullPointerException("null namespace");
		}

		return infoDecoder.decode(nodeDecoder.decode(text, namespace));
	}

}
