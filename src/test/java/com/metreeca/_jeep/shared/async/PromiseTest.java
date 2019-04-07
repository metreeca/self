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

package com.metreeca._jeep.shared.async;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.Semaphore;

import static com.metreeca._jeep.shared.async.Promises.all;

import static org.junit.Assert.assertEquals;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;


@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class PromiseTest {

	private static final String string="string!";
	private static final Number number=123;

	private static final Exception error=new Exception("error!");
	private static final Exception fault=new Exception("fault!");


	//// Harness ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private final Timer timer=new Timer();
	private final Semaphore semaphore=new Semaphore(0);


	private <T> Promise<T> settled(final T value) {
		return Promises.<T>promise().value(value);
	}

	private <T> Promise<T> settled(final Exception error) {
		return Promises.<T>promise().error(error);
	}

	private <T> Promise<T> deferred(final T value) {

		final Promise<T> deferred=Promises.promise();

		timer.schedule(new TimerTask() {
			@Override public void run() {
				try { deferred.value(value); } finally { semaphore.release(); }
			}
		}, 100);

		return deferred;
	}

	private <T> Promise<T> deferred(final Exception error) {

		final Promise<T> deferred=Promises.promise();

		timer.schedule(new TimerTask() {
			@Override public void run() {
				try { deferred.error(error); } finally { semaphore.release(); }
			}
		}, 100);

		return deferred;
	}

	private void sync(final int count) {
		try {
			semaphore.acquire(count);
			timer.cancel();
		} catch ( final InterruptedException e ) {
			throw new Error(e);
		}
	}


	//// State /////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test(expected=IllegalStateException.class) public void testPromisesCantBeResolvedTwice() {
		Promises.promise()
				.value(string)
				.value(string);
	}

	@Test(expected=IllegalStateException.class) public void testPromisesCantBeRejectedTwice() {
		Promises.promise()
				.error(error)
				.error(error);
	}

	@Test(expected=IllegalStateException.class) public void testResolvedPromisesCantBeRejected() {
		Promises.promise()
				.value(string)
				.error(error);
	}

	@Test(expected=IllegalStateException.class) public void testRejectedPromisesCantBeResolved() {
		Promises.promise()
				.error(error)
				.value(string);
	}


	//// Handlers //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testHandlersSeeSettledValues() {

		final Collection<String> values=new HashSet<>();

		Promises.<String>promise()

				.value(string)

				.then(new Handler<String>() {
					@Override public void value(final String string) { values.add(string); }
				});

		assertEquals("values", singleton(string), values);
	}

	@Test public void testHandlersSeeDeferredValues() {

		final Collection<String> values=new HashSet<>();

		Promises.<String>promise()

				.then(new Handler<String>() {
					@Override public void value(final String string) { values.add(string); }
				})

				.value(string);

		assertEquals("deferred values", singleton(string), values);
	}

	@Test public void testHandlersSeeSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		Promises.<String>promise()

				.error(error)

				.then(new Handler<String>() {
					@Override public void error(final Exception error) { errors.add(error); }
				});

		assertEquals("settled errors", singleton(error), errors);
	}

	@Test public void testHandlersSeeDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		Promises.<String>promise()

				.then(new Handler<String>() {
					@Override public void error(final Exception error) { errors.add(error); }
				})

				.error(error);

		assertEquals("deferred errors", singleton(error), errors);
	}


	@Test public void testHandlersIgnoreFaultsFromSettledValues() {

		final Collection<String> values=new HashSet<>();

		settled(string)

				.then(new Handler<String>() {
					@Override public void value(final String s) throws Exception { throw fault; }
				})

				.then(new Handler<String>() {
					@Override public void value(final String string) { values.add(string); }
				});

		assertEquals("values", singleton(string), values);
	}

	@Test public void testHandlersIgnoreFaultsFromDeferredValues() {

		final Collection<String> values=new HashSet<>();

		deferred(string)

				.then(new Handler<String>() {
					@Override public void value(final String s) throws Exception { throw fault; }
				})

				.then(new Handler<String>() {
					@Override public void value(final String string) { values.add(string); }
				});

		sync(1);

		assertEquals("values", singleton(string), values);
	}

	@Test public void testHandlersIgnoreFaultsFromSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>settled(error)

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { throw fault; }
				})

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		assertEquals("errors", singleton(error), errors);
	}

	@Test public void testHandlersIgnoreFaultsFromDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>deferred(error)

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { throw fault; }
				})

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		sync(1);

		assertEquals("errors", singleton(error), errors);
	}


	//// Morphers //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testMorphersSeeSettledValues() {

		final Collection<String> values=new HashSet<>();

		Promises.<String>promise()

				.value(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) {

						values.add(string);

						return null;
					}
				});

		assertEquals("deferred values", singleton(string), values);
	}

	@Test public void testMorphersSeeDeferredValues() {

		final Collection<String> values=new HashSet<>();

		final Promise<String> promise=Promises.promise();

		promise.pipe(new Morpher<String, Number>() {
			@Override public Promise<Number> value(final String string) {

				values.add(string);

				return null;
			}
		});

		promise.value(string);

		assertEquals("deferred values", singleton(string), values);
	}

	@Test public void testMorphersSeeSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		Promises.<String>promise()

				.error(error)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> error(final Exception error) {

						errors.add(error);

						return null;
					}
				});

		assertEquals("deferred errors", singleton(error), errors);
	}

	@Test public void testMorphersSeeDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		final Promise<String> promise=Promises.promise();

		promise.pipe(new Morpher<String, Number>() {
			@Override public Promise<Number> error(final Exception error) {

				errors.add(error);

				return null;
			}
		});

		promise.error(error);

		assertEquals("deferred errors", singleton(error), errors);
	}


	@Test public void testMorphersTransformSettledValuesIntoSettledValues() {

		final Collection<Number> values=new HashSet<>();

		settled(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) { return settled(number); }
				})

				.then(new Handler<Number>() {
					@Override public void value(final Number number) { values.add(number); }
				});

		assertEquals("values", singleton(number), values);
	}

	@Test public void testMorphersTransformSettledValuesIntoDeferredValues() {

		final Collection<Number> values=new HashSet<>();

		settled(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) { return deferred(number); }
				})

				.then(new Handler<Number>() {
					@Override public void value(final Number number) { values.add(number); }
				});

		sync(1);

		assertEquals("values", singleton(number), values);

	}

	@Test public void testMorphersTransformDeferredValuesIntoSettledValues() {

		final Collection<Number> values=new HashSet<>();

		deferred(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) { return settled(number); }
				})

				.then(new Handler<Number>() {
					@Override public void value(final Number number) { values.add(number); }
				});

		sync(1);

		assertEquals("values", singleton(number), values);
	}

	@Test public void testMorphersTransformDeferredValuesIntoDeferredValues() {

		final Collection<Number> values=new HashSet<>();

		deferred(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) { return deferred(number); }
				})

				.then(new Handler<Number>() {
					@Override public void value(final Number number) { values.add(number); }
				});

		sync(2);

		assertEquals("values", singleton(number), values);
	}


	@Test public void testMorphersPipeFaultsFromSettledValues() {

		final Collection<Exception> errors=new HashSet<>();

		settled(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String s) throws Exception { throw fault; }
				})

				.then(new Handler<Number>() {
					@Override public void error(final Exception error) { errors.add(error); }
				});


		assertEquals("errors", singleton(fault), errors);
	}

	@Test public void testMorphersPipeFaultsFromDeferredValues() {

		final Collection<Exception> errors=new HashSet<>();

		deferred(string)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String s) throws Exception { throw fault; }
				})

				.then(new Handler<Number>() {
					@Override public void error(final Exception error) { errors.add(error); }
				});

		sync(1);

		assertEquals("errors", singleton(fault), errors);
	}

	@Test public void testMorphersPipeFaultsFromSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>settled(error)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> error(final Exception error) throws Exception { throw fault; }
				})

				.then(new Handler<Number>() {
					@Override public void error(final Exception error) { errors.add(error); }
				});


		assertEquals("errors", singleton(fault), errors);
	}

	@Test public void testMorphersPipeFaultsFromDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>deferred(error)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> error(final Exception error) throws Exception { throw fault; }
				})

				.then(new Handler<Number>() {
					@Override public void error(final Exception error) { errors.add(error); }
				});

		sync(1);

		assertEquals("errors", singleton(fault), errors);
	}


	@Test public void testMorphersForwardUnhandledSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>settled(error)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) throws Exception { return null; }
				})

				.pipe(new Morpher<Number, String>() {
					@Override public Promise<String> value(final Number number) throws Exception { return null; }
				})

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		assertEquals("errors", singleton(error), errors);
	}

	@Test public void testMorphersForwardUnhandledDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		this.<String>deferred(error)

				.pipe(new Morpher<String, Number>() {
					@Override public Promise<Number> value(final String string) throws Exception { return null; }
				})

				.pipe(new Morpher<Number, String>() {
					@Override public Promise<String> value(final Number number) throws Exception { return null; }
				})

				.then(new Handler<String>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		sync(1);

		assertEquals("errors", singleton(error), errors);
	}


	//// Composites ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testAllMergesSettledValues() {

		final List<String> values=new ArrayList<>();

		all(settled("one"), settled("two"), settled("three"))

				.then(new Handler<List<String>>() {
					@Override public void value(final List<String> strings) { values.addAll(strings); }
				});

		assertEquals("values", asList("one", "two", "three"), values);
	}

	@Test public void testAllMergesDeferredValues() {

		final List<String> values=new ArrayList<>();

		all(deferred("one"), deferred("two"), deferred("three"))

				.then(new Handler<List<String>>() {
					@Override public void value(final List<String> strings) { values.addAll(strings); }
				});

		sync(3);

		assertEquals("values", asList("one", "two", "three"), values);
	}

	@Test public void testAllMergesMixedValues() {

		final List<String> values=new ArrayList<>();

		all(deferred("one"), settled("two"), deferred("three"))

				.then(new Handler<List<String>>() {
					@Override public void value(final List<String> strings) { values.addAll(strings); }
				});

		sync(2);

		assertEquals("values", asList("one", "two", "three"), values);
	}

	@Test public void testAllMergesSettledErrors() {

		final Collection<Exception> errors=new HashSet<>();

		final Exception two=new Exception("two");
		final Exception three=new Exception("three");

		all(settled("one"), this.<String>settled(two), this.<String>settled(three))

				.then(new Handler<List<String>>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		assertEquals("errors", singleton(two), errors);
	}

	@Test public void testAllMergesDeferredErrors() {

		final Collection<Exception> errors=new HashSet<>();

		final Exception two=new Exception("two");
		final Exception three=new Exception("three");

		all(settled("one"), this.<String>deferred(two), this.<String>deferred(three))

				.then(new Handler<List<String>>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		sync(2);

		assertEquals("errors", singleton(two), errors);
	}

	@Test public void testAllMergesMixedErrors() {

		final Collection<Exception> errors=new HashSet<>();

		final Exception two=new Exception("two");
		final Exception three=new Exception("three");

		all(settled("one"), this.<String>deferred(two), this.<String>settled(three))

				.then(new Handler<List<String>>() {
					@Override public void error(final Exception error) throws Exception { errors.add(error); }
				});

		sync(1);

		assertEquals("errors", singleton(three), errors);
	}

}
