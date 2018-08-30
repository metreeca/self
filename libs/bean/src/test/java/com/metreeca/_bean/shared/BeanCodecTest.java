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

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public abstract class BeanCodecTest {

	protected abstract <T> T clone(final T object);


	private Map<Integer, String> map() {

		final Map<Integer, String> map=new HashMap<Integer, String>();
		map.put(0, "zero");
		map.put(1, "uno");
		map.put(2, "due");
		map.put(3, "tre");

		return map;
	}


	//// Checks ////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test(expected=IllegalArgumentException.class) public void testCheckNullRoot() {
		clone(null);
	}


	//// Object Graph //////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testSelfLoops() {

		final Beans a=new Beans();

		a.setObject(a);

		final Beans x=clone(a);

		assertEquals("handle self loops", x, x.getObject());
	}

	@Test public void testBeanLoops() {

		final Beans a=new Beans();
		final Beans b=new Beans();

		a.setObject(b);
		b.setObject(a);

		final Beans x=clone(a);

		assertSame("handle reference loops", x, ((Beans)x.getObject()).getObject());
	}

	@Test public void testNestedBeanLoops() {

		final Beans a=new Beans();
		final Beans b=new Beans();

		a.setObject(b);
		b.setObject(b);

		final Beans x=clone(a);

		assertSame("handle nested reference loops", x.getObject(), ((Beans)x.getObject()).getObject());
	}

	@Test public void testListLoops() {

		final Beans a=new Beans();

		a.setObject(Arrays.asList(a));

		final Beans x=clone(a);

		assertSame("handle list loops", x, ((List<Containers>)x.getObject()).get(0));
	}

	@Test public void testCollectionLoops() {

		final Beans a=new Beans();

		a.setObject(Collections.unmodifiableCollection(Arrays.asList(a)));

		final Beans x=clone(a);

		assertSame("handle collection loops", x, ((Collection<Containers>)x.getObject()).iterator().next());
	}

	@Test public void testMapLoops() {

		final Beans a=new Beans();

		final HashMap<Integer, Beans> map=new HashMap<Integer, Beans>();
		map.put(1, a);

		a.setObject(map);

		final Beans x=clone(a);

		assertSame("handle map loops", x, ((Map<Integer, Beans>)x.getObject()).values().iterator().next());
	}


	//// Primitives ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleBooleans() {

		final Primitives expected=new Primitives().setBoolean(true);
		final Primitives actual=clone(expected);

		assertEquals("handle boolean values", expected.isBoolean(), actual.isBoolean());
	}

	@Test public void testHandleChars() {

		final Primitives expected=new Primitives().setChar('c');
		final Primitives actual=clone(expected);

		assertEquals("handle char values", expected.getChar(), actual.getChar());
	}

	@Test public void testHandleBytes() {

		final Primitives expected=new Primitives().setByte((byte)123);
		final Primitives actual=clone(expected);

		assertEquals("handle byte values", expected.getByte(), actual.getByte());
	}

	@Test public void testHandleShorts() {

		final Primitives expected=new Primitives().setShort((short)123);
		final Primitives actual=clone(expected);

		assertEquals("handle short values", expected.getShort(), actual.getShort());
	}

	@Test public void testHandleInts() {

		final Primitives expected=new Primitives().setInt(123);
		final Primitives actual=clone(expected);

		assertEquals("handle int values", expected.getInt(), actual.getInt());
	}

	@Test public void testHandleLongs() {

		final Primitives expected=new Primitives().setLong(123L);
		final Primitives actual=clone(expected);

		assertEquals("handle long values", expected.getLong(), actual.getLong());
	}

	@Test public void testHandleFloats() {

		final Primitives expected=new Primitives().setFloat(123.0f);
		final Primitives actual=clone(expected);

		assertEquals("handle float values", expected.getFloat(), actual.getFloat(), 0);
	}

	@Test public void testHandleFloatSpecialValues() {
		assertEquals("handle float NaN", Float.NaN, clone(new Primitives().setFloat(Float.NaN)).getFloat(), 0);
		assertEquals("handle float +Inf", Float.POSITIVE_INFINITY, clone(new Primitives().setFloat(Float.POSITIVE_INFINITY)).getFloat(), 0);
		assertEquals("handle float -Inf", Float.NEGATIVE_INFINITY, clone(new Primitives().setFloat(Float.NEGATIVE_INFINITY)).getFloat(), 0);
	}

	@Test public void testHandleDoubles() {

		final Primitives expected=new Primitives().setDouble(123.0);
		final Primitives actual=clone(expected);

		assertEquals("handle double values", expected.getDouble(), actual.getDouble(), 0);
	}

	@Test public void testHandleDoubleSpecialValues() {
		assertEquals("handle double NaN", Double.NaN, clone(new Primitives().setDouble(Double.NaN)).getDouble(), 0);
		assertEquals("handle double +Inf", Double.POSITIVE_INFINITY, clone(new Primitives().setDouble(Double.POSITIVE_INFINITY)).getDouble(), 0);
		assertEquals("handle double -Inf", Double.NEGATIVE_INFINITY, clone(new Primitives().setDouble(Double.NEGATIVE_INFINITY)).getDouble(), 0);
	}


	//// Literals //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleIntegers() {

		final Literals expected=new Literals().setInteger(BigInteger.TEN);
		final Literals actual=clone(expected);

		assertEquals("handle integer values", expected.getInteger(), actual.getInteger());
	}

	@Test public void testHandleDecimals() {

		final Literals expected=new Literals().setDecimal(BigDecimal.TEN);
		final Literals actual=clone(expected);

		assertEquals("handle decimal values", expected.getDecimal(), actual.getDecimal());
	}

	@Test public void testHandleStrings() {

		final Literals expected=new Literals().setString("a string with spaces\n");
		final Literals actual=clone(expected);

		assertEquals("handle string values", expected.getString(), actual.getString());
	}

	@Test public void testHandleDates() {

		final Literals expected=new Literals().setDate(new Date());
		final Literals actual=clone(expected);

		assertEquals("handle date values", expected.getDate(), actual.getDate());
	}

	@Test public void testHandleTypes() {

		final Literals expected=new Literals().setType(Beans.class);
		final Literals actual=clone(expected);

		assertSame("handle type values", expected.getType(), actual.getType());
	}


	//// Collections ///////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleLists() {

		final Containers expected=new Containers().setList(Arrays.asList(0, 1, 2, 3));
		final Containers actual=clone(expected);

		assertEquals("handle list fields", expected.getList(), actual.getList());
	}

	@Test public void testHandleSets() {

		final Containers expected=new Containers().setSet(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3)));
		final Containers actual=clone(expected);

		assertEquals("handle set fields", expected.getSet(), actual.getSet());
	}

	@Test public void testHandleSortedSets() {

		final Containers expected=new Containers().setSortedSet(new TreeSet<Integer>(Arrays.asList(0, 1, 2, 3)));
		final Containers actual=clone(expected);

		assertEquals("handle sorted set fields", expected.getSortedSet(), actual.getSortedSet());
	}

	@Test public void testHandleCollections() {

		final Containers expected=new Containers().setCollection(Collections.unmodifiableCollection(Arrays.asList(0, 1, 2, 3)));
		final Containers actual=clone(expected);

		assertEquals("handle collection fields",
				new ArrayList<Integer>(expected.getCollection()), new ArrayList<Integer>(actual.getCollection()));
	}


	////// Maps ////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleMaps() {

		final Containers expected=new Containers().setMap(new HashMap<Integer, String>(map()));
		final Containers actual=clone(expected);

		assertEquals("handle map fields", expected.getMap(), actual.getMap());
	}

	@Test public void testHandleSortedMaps() {

		final Containers expected=new Containers().setSortedMap(new TreeMap<Integer, String>(map()));
		final Containers actual=clone(expected);

		assertEquals("handle sorted map fields", expected.getSortedMap(), actual.getSortedMap());
	}


	//// Enums /////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleSimpleEnums() {

		final Enums expected=new Enums().setSimple(Enums.Simple.Three);
		final Enums actual=clone(expected);

		assertEquals("handle simple enumerated fields", expected.getSimple(), actual.getSimple());
	}

	@Test public void testHandleComplexEnums() {

		final Enums expected=new Enums().setComplex(Enums.Complex.Three);
		final Enums actual=clone(expected);

		assertEquals("handle complex enumerated fields", expected.getComplex(), actual.getComplex());
	}


	//// Beans /////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleNulls() {

		final Beans expected=new Beans().setObject(null);
		final Beans actual=clone(expected);

		assertEquals("handle null values", expected.getObject(), actual.getObject());
	}

	@Test public void testHandleKeys() {

		final Value expected=new Value(123);
		final Value actual=clone(expected);

		assertEquals("handle keys", expected, actual);
	}

	@Test public void testHandleDefaultValuesInKeys() {

		final Value expected=new Value(0);
		final Value actual=clone(expected);

		assertEquals("handle default values in keys", expected, actual);
	}

	@Test public void testHandleFilteredBeans() {

		final Beans expected=new Beans().setObject(new Custom());
		final Beans actual=clone(expected);

		assertTrue("handle custom encoding", ((Custom)actual.getObject()).encoding);
		assertTrue("handle custom decoding", ((Custom)actual.getObject()).decoding);
	}


	//// Special Cases /////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandleTriStateFlags() {

		final Specials expected=new Specials().setFlag(false);
		final Specials actual=clone(expected);

		assertEquals("handle tri-state flags", expected.getFlag(), actual.getFlag());
	}

	@Test public void testHandleRelaxedCollectionSetter() {

		final Specials expected=new Specials().setCollection(Arrays.asList(0, 1, 2, 3));
		final Specials actual=clone(expected);

		assertEquals("handle relaxed collection setters", expected.getCollection(), actual.getCollection());
	}

	@Test public void testHandleRelaxedIterableSetter() {

		final Specials expected=new Specials().setIterable(Arrays.asList(0, 1, 2, 3));
		final Specials actual=clone(expected);

		assertEquals("handle relaxed iterable setters", expected.getIterable(), actual.getIterable());
	}

	@Test public void testHandleRelaxedMapSetter() {

		final Specials expected=new Specials().setMap(Collections.singletonMap(1, "one"));
		final Specials actual=clone(expected);

		assertEquals("handle relaxed map setters", expected.getMap(), actual.getMap());
	}


	//// Harness ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean public static final class Primitives {

		private boolean _boolean;
		private char _char;
		private byte _byte;
		private int _short;
		private int _int;
		private long _long;
		private float _float;
		private double _double;


		public boolean isBoolean() {
			return _boolean;
		}

		public Primitives setBoolean(final boolean _boolean) {

			this._boolean=_boolean;

			return this;
		}


		public char getChar() {
			return _char;
		}

		public Primitives setChar(final char _char) {

			this._char=_char;

			return this;
		}


		public byte getByte() {
			return _byte;
		}

		public Primitives setByte(final byte _byte) {

			this._byte=_byte;

			return this;
		}


		public int getShort() {
			return _short;
		}

		public Primitives setShort(final int _short) {

			this._short=_short;

			return this;
		}


		public int getInt() {
			return _int;
		}

		public Primitives setInt(final int _int) {

			this._int=_int;

			return this;
		}


		public long getLong() {
			return _long;
		}

		public Primitives setLong(final long _long) {
			this._long=_long;

			return this;
		}


		public float getFloat() {
			return _float;
		}

		public Primitives setFloat(final float _float) {

			this._float=_float;

			return this;
		}


		public double getDouble() {
			return _double;
		}

		public Primitives setDouble(final double _double) {

			this._double=_double;

			return this;
		}

	}

	@Bean public static final class Literals {

		private BigInteger integer=BigInteger.ZERO;
		private BigDecimal decimal=BigDecimal.ZERO;

		private String string="";
		private Date date=new Date(0);

		private Class<?> type;


		public BigInteger getInteger() {
			return integer;
		}

		public Literals setInteger(final BigInteger integer) {

			this.integer=integer;

			return this;
		}


		public BigDecimal getDecimal() {
			return decimal;
		}

		public Literals setDecimal(final BigDecimal decimal) {

			this.decimal=decimal;

			return this;
		}


		public String getString() {
			return string;
		}

		public Literals setString(final String string) {

			this.string=string;

			return this;
		}


		public Date getDate() {
			return date == null ? null : new Date(date.getTime());
		}

		public Literals setDate(final Date date) {

			this.date=new Date(date.getTime());

			return this;
		}


		public Class<?> getType() {
			return type;
		}

		public Literals setType(final Class<?> type) {

			this.type=type;

			return this;
		}
	}

	@Bean public static final class Containers {

		private final List<Integer> list=new ArrayList<Integer>();
		private final Set<Integer> set=new HashSet<Integer>();
		private final SortedSet<Integer> sortedSet=new TreeSet<Integer>();
		private final Collection<Integer> collection=new ArrayList<Integer>();

		private final Map<Integer, String> map=new HashMap<Integer, String>();
		private final SortedMap<Integer, String> sortedMap=new TreeMap<Integer, String>();


		public List<Integer> getList() {
			return Collections.unmodifiableList(list);
		}

		public Containers setList(final List<Integer> list) {

			if ( list == null ) {
				throw new NullPointerException("null list");
			}

			this.list.clear();
			this.list.addAll(list);

			return this;
		}


		public Set<Integer> getSet() {
			return Collections.unmodifiableSet(set);
		}

		public Containers setSet(final Set<Integer> set) {

			if ( set == null ) {
				throw new NullPointerException("null set");
			}

			this.set.clear();
			this.set.addAll(set);

			return this;
		}


		public SortedSet<Integer> getSortedSet() {
			return Collections.unmodifiableSortedSet(sortedSet);
		}

		public Containers setSortedSet(final SortedSet<Integer> sortedSet) {

			if ( sortedSet == null ) {
				throw new NullPointerException("null sortedSet");
			}

			this.sortedSet.clear();
			this.sortedSet.addAll(sortedSet);

			return this;
		}


		public Collection<Integer> getCollection() {
			return Collections.unmodifiableCollection(collection);
		}

		public Containers setCollection(final Collection<Integer> collection) {

			if ( collection == null ) {
				throw new NullPointerException("null collection1");
			}

			this.collection.clear();
			this.collection.addAll(collection);

			return this;
		}


		public Map<Integer, String> getMap() {
			return Collections.unmodifiableMap(map);
		}

		public Containers setMap(final Map<Integer, String> map) {

			if ( map == null ) {
				throw new NullPointerException("null map");
			}

			this.map.clear();
			this.map.putAll(map);

			return this;
		}


		public SortedMap<Integer, String> getSortedMap() {
			return Collections.unmodifiableSortedMap(sortedMap);
		}

		public Containers setSortedMap(final SortedMap<Integer, String> sortedMap) {

			if ( sortedMap == null ) {
				throw new NullPointerException("null sortedMap");
			}

			this.sortedMap.clear();
			this.sortedMap.putAll(sortedMap);

			return this;
		}
	}

	@Bean public static final class Enums {

		@Bean public enum Simple {One, Two, Three}

		@Bean public enum Complex {

			One {
				@Override public void something() {}
			},

			Two {
				@Override public void something() {}
			},

			Three {
				@Override public void something() {}
			};

			public abstract void something();
		}


		private Simple simple;
		private Complex complex;


		public Simple getSimple() {
			return simple;
		}

		public Enums setSimple(final Simple simple) {

			this.simple=simple;

			return this;
		}


		public Complex getComplex() {
			return complex;
		}

		public Enums setComplex(final Complex complex) {

			this.complex=complex;

			return this;
		}
	}

	@Bean public static final class Beans {

		private Object object;


		public Object getObject() {
			return object;
		}

		public Beans setObject(final Object object) {

			this.object=object;

			return this;
		}
	}

	@Bean public static final class Specials {

		private Boolean flag;

		private final List<Integer> collection=new ArrayList<Integer>();
		private final List<Integer> iterable=new ArrayList<Integer>();

		private SortedMap<Integer, String> map=new TreeMap<Integer, String>();


		public Boolean getFlag() {
			return flag;
		}

		public Specials setFlag(final Boolean flag) {

			this.flag=flag;

			return this;
		}


		public List<Integer> getCollection() {
			return Collections.unmodifiableList(collection);
		}

		public Specials setCollection(final Collection<Integer> collection) {

			this.collection.clear();
			this.collection.addAll(collection);

			return this;
		}


		public List<Integer> getIterable() {
			return Collections.unmodifiableList(iterable);
		}

		public Specials setIterable(final Iterable<Integer> iterable) {

			this.iterable.clear();

			for (final Integer integer : iterable) {
				this.iterable.add(integer);
			}

			return this;
		}


		public SortedMap<Integer, String> getMap() {
			return Collections.unmodifiableSortedMap(map);
		}

		public Specials setMap(final Map<Integer, String> map) {
			this.map.clear();
			this.map.putAll(map);

			return this;
		}
	}


	@Bean public static final class Custom {

		private boolean encoding;
		private boolean decoding;


		public boolean isEncoding() {
			return encoding;
		}

		public void setEncoding(final boolean encoding) {
			this.encoding=encoding;
		}


		public boolean isDecoding() {
			return decoding;
		}

		public void setDecoding(final boolean decoding) {
			this.decoding=decoding;
		}


		public static void encode(final Bean.Info info) {
			if ( info.version() == 0 ) {
				info.version(123).field("encoding", true);
			}
		}

		public static void decode(final Bean.Info info) {
			if ( info.version() == 123 ) {
				info.field("decoding", true);
			}
		}
	}

	@Bean("id") public static final class Value {

		private final int id;


		public Value(final int id) {
			this.id=id;
		}


		public int getId() {
			return id;
		}


		@Override public boolean equals(final Object object) {
			return this == object || object instanceof Value && equals((Value)object);
		}

		@Override public int hashCode() {
			return id;
		}

		@Override public String toString() {
			return String.valueOf(id);
		}


		private boolean equals(final Value value) {
			return id == value.id;
		}

	}
}
