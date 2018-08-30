/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._bean.shared;


import com.metreeca._bean.shared.Bean.Info;


public final class InfoCodec {

	private final InfoEncoder infoEncoder;
	private final InfoDecoder infoDecoder;


	public InfoCodec(final Meta.Factory meta) {

		if ( meta == null ) {
			throw new NullPointerException("null meta factory");
		}

		infoEncoder=new InfoEncoder(meta);
		infoDecoder=new InfoDecoder(meta);
	}


	public Info encode(final Object object) {

		if ( object == null ) {
			throw new NullPointerException("null object");
		}

		return infoEncoder.encode(object);
	}

	public <T> T decode(final Info info) {

		if ( info == null ) {
			throw new NullPointerException("null info");
		}

		return infoDecoder.decode(info);
	}
}
