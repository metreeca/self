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

import org.junit.Test;

import java.util.*;

import static com.metreeca._bean.shared.Bean.Info;

import static org.junit.Assert.assertEquals;


public final class BeanInfoTest {

	private Info info(final Object bean) {
		return new InfoEncoder(ServerMeta.factory()).encode(bean);
	}


	@Test public void testVisitBeanInfo() {

		final Work a=new Work("a");
		final Work b=new Work("b");

		a.setBean(b);
		b.setBean(a);

		final Set<Info> set=info(a).visit();
		final Info[] infos=set.toArray(new Info[set.size()]);

		assertEquals("visit bean info", 2, infos.length);
		assertEquals("visit bean info", infos[1], infos[0].field("bean"));
		assertEquals("visit bean info", infos[0], infos[1].field("bean"));
	}

	@Test public void testVisitCollections() {

		final Work a=new Work("a");
		final Work b=new Work("b");
		final Work c=new Work("c");

		a.setList(Arrays.asList(b, c));

		final Set<Info> infos=info(a).visit();

		final Collection<String> expected=new HashSet<String>(Arrays.asList("a", "b", "c"));
		final Collection<String> actual=new HashSet<String>();

		for (final Info info : infos) {
			actual.add(info.string("id"));
		}

		assertEquals("visit beans in lists", expected, actual);
	}

	@Test public void testVisitMaps() {

		final Work a=new Work("a");
		final Work b=new Work("b");
		final Work c=new Work("c");

		final Map<Integer, Work> map=new HashMap<Integer, Work>();
		map.put(1, b);
		map.put(2, c);

		a.setMap(map);

		final Set<Info> infos=info(a).visit();

		final Collection<String> expected=new HashSet<String>(Arrays.asList("a", "b", "c"));
		final Collection<String> actual=new HashSet<String>();

		for (final Info info : infos) {
			actual.add(info.string("id"));
		}

		assertEquals("visit beans in lists", expected, actual);
	}


	@Bean("id") public static final class Work {

		private final String id;

		private Work bean;

		private List<Work> list;
		private Map<Integer, Work> map;


		public Work(final String id) {
			this.id=id;
		}


		public String getId() {
			return id;
		}


		public Work getBean() {
			return bean;
		}

		public Work setBean(final Work bean) {

			this.bean=bean;

			return this;
		}


		public List<Work> getList() {
			return list;
		}

		public Work setList(final List<Work> list) {

			this.list=list;

			return this;
		}


		public Map<Integer, Work> getMap() {
			return map;
		}

		public Work setMap(final Map<Integer, Work> map) {

			this.map=map;

			return this;
		}
	}

}
