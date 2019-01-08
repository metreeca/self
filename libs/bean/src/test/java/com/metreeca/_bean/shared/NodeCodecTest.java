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

package com.metreeca._bean.shared;

import com.metreeca._bean.server.ServerMeta;
import com.metreeca._bean.server.ServerNode;


public final class NodeCodecTest extends BeanCodecTest {

	private final NodeCodec codec=new NodeCodec(ServerMeta.factory(), ServerNode.factory());


	@Override protected <T> T clone(final T object) {
		return codec.decode(codec.encode(object)
				.replace(">", ">\n").replace("</", "\n</")); // simulate xml reformatting
	}

}
