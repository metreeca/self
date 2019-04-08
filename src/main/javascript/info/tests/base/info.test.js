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

function suite(chart, tests) {

	var side=$("<aside/>").appendTo("body");
	var port=info.d3.select($("<div/>").appendTo("body")[0]);

	info.d3.tsv("../../base/info.test.tsv", function (row, index) {

		return {

			letter: "Letter "+String(row.letter),
			kind: "AEIOU".indexOf(String(row.letter)) >= 0 ? "vowel" : "consonant",
			index: index+1,
			english: row.english ? Number(row.english) : undefined,
			italian: row.italian ? Number(row.italian) : undefined,
			average: ((Number(row.english))+(Number(row.italian)))/2,

			city: String(row.city),
			lat: row.lat ? Number(row.lat) : undefined,
			lng: row.lng ? Number(row.lng) : undefined

		}

	}, function (error, table) {
		chart(port.datum($.extend(table, {

			insert: function () {

				var k=this.length/4;

				for (var i=0; i < k; ++i) {

					var source=Math.floor(Math.random()*this.length);
					var target=Math.floor(Math.random()*this.length);

					this.splice(target, 0, {
						letter: this[source].letter+"'",
						english: this[source].english,
						italian: this[source].italian,
						average: this[source].average
					})
				}
			},

			remove: function () {

				var k=this.length/4;

				for (var i=0; i < k; ++i) {

					var target=Math.floor(Math.random()*this.length);

					this.splice(target, 1);
				}
			},

			shake: function () {
				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) {
							row[p]+=row[p]*0.1*(Math.random()*2-1);
						}
					}
				}
			},

			center: function () {

				var sum={};

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) { sum[p]=(sum[p] || 0)+row[p]; }
					}
				}

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) { row[p]-=sum[p]/table.length; }
					}
				}
			},

			up: function () {

				var scale={};

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) {
							scale[p]=Math.max(scale[p] || 0, Math.abs(row[p]));
						}
					}
				}

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) {
							row[p]+=scale[p]/10;
						}
					}
				}
			},

			down: function () {

				var scale={};

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) {
							scale[p]=Math.max(scale[p] || 0, Math.abs(row[p]));
						}
					}
				}

				for (var i=0; i < this.length; ++i) {

					var row=this[i];

					for (var p in row) {
						if ( row.hasOwnProperty(p) && +row[p] === row[p] ) {
							row[p]-=scale[p]/10;
						}
					}
				}
			},

			shuffle: function () { //@ http://jsfromhell.com/array/shuffle
				for (var j, x, i=this.length; i;
						j=Math.floor(Math.random()*i), x=this[--i], this[i]=this[j], this[j]=x) {}
			}

		})));
	});

	$(window).resize(function () { chart(port); });


	function suite(section, tests) {

		for (var label in tests) {
			if ( tests.hasOwnProperty(label) ) {
				if ( typeof tests[label] === "function" ) {

					section.append(test(label, tests[label]));

				} else {

					suite($("<section/>").appendTo(side), tests[label]);

				}
			}
		}

	}

	function test(label, test) {
		return $("<button/>").text(label).click(function () {

			var opts=test.call(chart, port.datum());

			if ( opts ) {
				chart(opts);
			}

			chart(port);

		});
	}

	suite(side, tests);
}
