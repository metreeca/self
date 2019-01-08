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

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public abstract class MetaTest {

	private final Meta.Factory factory=factory();


	protected abstract Meta.Factory factory();


	//// Default Constructors //////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testUseDefaultConstructor() {
		assertNotNull("default constructor", factory.meta(DefaultConstructor.class).create());
	}


	@Test(expected=IllegalArgumentException.class) public void testCheckAnnotation() {
		factory.meta(NoAnnotation.class);
	}

	@Test(expected=IllegalArgumentException.class) public void testRejectMissingConstructor() {
		factory.meta(MissingConstructor.class);
	}

	@Test(expected=IllegalArgumentException.class) public void testRejectHiddenConstructor() {
		factory.meta(HiddenConstructor.class);
	}


	public static final class NoAnnotation {}

	@Bean public static final class DefaultConstructor {}

	@Bean public static final class MissingConstructor {

		public MissingConstructor(final int id) {}

	}

	@Bean public static final class HiddenConstructor {

		private HiddenConstructor() {}

	}


	//// Key Constructors //////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testUseDeclaredConstructor() {

		final Map<String, Object> fields=new HashMap<String, Object>();
		fields.put("keyA", 1);
		fields.put("keyB", 2);

		final Meta<KeyConstructor> meta=factory.meta(KeyConstructor.class);
		final Object bean=meta.create(fields);

		final Map<String, Class<?>> key=new LinkedHashMap<String, Class<?>>();
		key.put("keyA", int.class);
		key.put("keyB", int.class);

		assertEquals("expose key fields", key, meta.key());
		assertTrue("use declared constructor", bean instanceof KeyConstructor);
		assertEquals("set declared fields", 1, ((KeyConstructor)bean).getKeyA());
		assertEquals("set declared fields", 2, ((KeyConstructor)bean).getKeyB());
	}

	@Test public void testUseFallbackConstructor() {

		final Meta<KeyConstructor> meta=factory.meta(KeyConstructor.class);
		final Object bean=meta.create();

		assertTrue("use declared constructor", bean instanceof KeyConstructor);
		assertEquals("set key fields", -1, ((KeyConstructor)bean).getKeyA());
		assertEquals("set key fields", -2, ((KeyConstructor)bean).getKeyB());
	}


	@Test(expected=IllegalArgumentException.class) public void testRejectMissingDeclaredConstructor() {
		factory.meta(MissingKeyConstructor.class);
	}

	@Test(expected=IllegalArgumentException.class) public void testMissingKeyGettersConstructor() {
		factory.meta(MissingKeyGetters.class);
	}

	@Test(expected=IllegalArgumentException.class) public void testUnpairedDeclaredConstructor() {
		factory.meta(UnpairedKeyConstructor.class);
	}

	@Test(expected=IllegalArgumentException.class) public void testWritableKeyFields() {
		factory.meta(WritableKey.class);
	}


	@Bean({"keyA", "keyB"}) public static final class KeyConstructor {

		private final int keyA;
		private final int keyB;

		public KeyConstructor() {
			this(-1, -2);
		}

		public KeyConstructor(final int keyA, final int keyB) {
			this.keyA=keyA;
			this.keyB=keyB;
		}

		public int getKeyA() {
			return keyA;
		}

		public int getKeyB() {
			return keyB;
		}
	}

	@Bean("id") public static final class MissingKeyConstructor {}

	@Bean("id") public static final class MissingKeyGetters {

		public MissingKeyGetters(final String id) {}

	}

	@Bean("id") public static final class UnpairedKeyConstructor {

		public UnpairedKeyConstructor(final String id) {}

		public int getId() { return 0; }
	}

	@Bean("id") public static final class WritableKey {

		private int id;

		public WritableKey(final int id) {
			this.id=id;
		}

		public int getId() {
			return id;
		}

		public void setId(final int id) {
			this.id=id;
		}
	}


	//// Fields ////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testUsePairedAccessors() {

		final Meta<PairedAccessors> meta=factory.meta(PairedAccessors.class);

		final Object bean=meta.create();
		meta.set(bean, "paired", 100);

		assertEquals("list paired accessors", Collections.singletonMap("paired", int.class), meta.fields());
		assertEquals("use paired accessors", 100, meta.get(bean, "paired"));
	}

	@Test public void testUseRelaxedCollectionSetters() {

		final Meta<RelaxedSetters> meta=factory.meta(RelaxedSetters.class);
		final List<Integer> value=Arrays.asList(1, 2, 3);

		final Object bean=meta.create();
		meta.set(bean, "collection", value);

		assertSame("list relaxed collection accessors", List.class, meta.fields().get("collection"));
		assertEquals("use relaxed collection accessors", value, meta.get(bean, "collection"));
	}

	@Test public void testUseRelaxedIterableSetters() {

		final Meta<RelaxedSetters> meta=factory.meta(RelaxedSetters.class);
		final List<Integer> value=Arrays.asList(1, 2, 3);

		final Object bean=meta.create();
		meta.set(bean, "iterable", value);

		assertSame("list relaxed iterable accessors", List.class, meta.fields().get("iterable"));
		assertEquals("use relaxed iterable accessors", value, meta.get(bean, "iterable"));
	}

	@Test public void testUseRelaxedMapSetters() {

		final Meta<RelaxedSetters> meta=factory.meta(RelaxedSetters.class);
		final Map<Integer, String> value=Collections.singletonMap(1, "one");

		final Object bean=meta.create();
		meta.set(bean, "map", value);

		assertSame("list relaxed map accessors", SortedMap.class, meta.fields().get("map"));
		assertEquals("use relaxed map accessors", value, meta.get(bean, "map"));
	}

	@Test public void testIgnoreAncillarySetters() {

		final Meta<?> meta=factory.meta(AncillarySetters.class);

		final Object bean=meta.create();
		meta.set(bean, "paired", 100);

		assertEquals("list paired accessors", Collections.singletonMap("paired", int.class), meta.fields());
		assertEquals("use paired accessors", 100, meta.get(bean, "paired"));
	}

	@Test public void testCasedFields() {

		final Meta<?> meta=factory.meta(CasedFields.class);

		assertEquals("handle field case", new HashSet<String>(Arrays.asList("z", "zzz", "ZZZ")), meta.fields().keySet());
	}


	@Test(expected=IllegalArgumentException.class) public void testRejectUnpairedAccessors() {
		factory.meta(UnpairedAccessors.class);
	}

	@Test(expected=UnsupportedOperationException.class) public void testRejectUnsupportedCollections() {
		factory.meta(UnsupportedCollection.class);
	}

	@Test(expected=UnsupportedOperationException.class) public void testRejectUnsupportedMaps() {
		factory.meta(UnsupportedMap.class);
	}


	@Bean public static final class PairedAccessors {

		private int paired;
		private int unpaired;


		public int getPaired() {
			return paired;
		}

		public void setPaired(final int field) {
			this.paired=field;
		}

		public int getUnpaired() {
			return unpaired;
		}
	}

	@Bean public static final class RelaxedSetters {

		private final List<Integer> collection=new ArrayList<Integer>();
		private final List<Integer> iterable=new ArrayList<Integer>();


		private SortedMap<Integer, String> map=new TreeMap<Integer, String>();


		public List<Integer> getCollection() {
			return Collections.unmodifiableList(collection);
		}

		public void setCollection(final Collection<Integer> collection) {
			this.collection.clear();
			this.collection.addAll(collection);
		}


		public List<Integer> getIterable() {
			return Collections.unmodifiableList(iterable);
		}

		public void setIterable(final Iterable<Integer> iterable) {

			this.iterable.clear();

			for (final Integer integer : iterable) {
				this.iterable.add(integer);
			}
		}


		public SortedMap<Integer, String> getMap() {
			return Collections.unmodifiableSortedMap(map);
		}

		public void setMap(final Map<Integer, String> map) {
			this.map.clear();
			this.map.putAll(map);
		}
	}

	@Bean public static final class AncillarySetters {

		private int paired;


		public int getPaired() {
			return paired;
		}

		public void setPaired(final int field) {
			this.paired=field;
		}

		public void setPaired(final String field) {
			this.paired=Integer.parseInt(field);
		}
	}

	@Bean public static final class CasedFields {

		private int z, zzz, ZZZ;

		public int getZ() {
			return z;
		}

		public void setZ(final int z) {
			this.z=z;
		}

		public int getZzz() {
			return zzz;
		}

		public void setZzz(final int zzz) {
			this.zzz=zzz;
		}

		public int getZZZ() {
			return ZZZ;
		}

		public void setZZZ(final int ZZZ) {
			this.ZZZ=ZZZ;
		}
	}

	@Bean public static final class UnpairedAccessors {

		public List<String> getList() { return null; }

		public void setList(final ArrayList<String> list) { }

	}

	@Bean public static final class UnsupportedCollection {

		public ArrayList<String> getList() { return null; }

		public void setList(final ArrayList<String> list) { }

	}

	@Bean public static final class UnsupportedMap {

		public HashMap<String, Integer> getMap() { return null; }

		public void setMap(final HashMap<String, Integer> map) { }

	}


	//// Primitives /////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleEnums() {
		assertSame("handle enum classes", BeanEnum.class, factory.meta(BeanEnum.class).type());
	}


	@Test(expected=IllegalArgumentException.class) public void testCheckAnnotationArgs() {
		factory.meta(BadAnnotation.class);
	}

	@Test(expected=MetaException.class) public void testRefuseToCreateEnums() {
		factory.meta(BeanEnum.class).create();
	}


	@Bean public enum BeanEnum {One, Two, Three}

	@Bean("id") enum BadAnnotation {}
}
