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

module.exports={

	isNil: isNil,
	isBoolean: isBoolean,
	isNumber: isNumber,
	isString: isString,
	isFunction: isFunction,
	isObject: isObject,
	isArray: isArray,
	isList: isList,

	isNode: isNode,
	isNodeList: isNodeList,
	isEvent: isEvent

};

//// Type Tests ////////////////////////////////////////////////////////////////////////////////////////////////////////

function isNil(object) {
	return object === null || typeof object === "undefined";
}

function isBoolean(object) {
	return typeof object === "boolean"
			|| object instanceof Boolean
			|| Object.prototype.toString.call(object) === "[object Boolean]";
}

function isNumber(object) {
	return typeof object === "number"
			|| object instanceof Number
			|| Object.prototype.toString.call(object) === "[object Number]";
}

function isString(object) {
	return typeof object === "string"
			|| object instanceof String
			|| Object.prototype.toString.call(object) === "[object String]";
}

function isFunction(object) {
	return object instanceof Function
			|| Object.prototype.toString.call(object) === "[object Function]";
}

function isObject(object) {
	return Object.prototype.toString.call(object) === "[object Object]";
}

function isArray(object) {
	return Array.isArray(object);
}

function isList(object) { // true if object is an array-like object
	return !isNil(object) && !isString(object) && !isFunction(object) && object.length >= 0;
}


//// DOM Type Tests ////////////////////////////////////////////////////////////////////////////////////////////////////

function isNode(object) {
	return object instanceof Node
			|| !!object && typeof object.nodeType === "number";
}

function isNodeList(object) {
	return object instanceof NodeList
			|| object instanceof HTMLCollection
			|| Object.prototype.toString.call(object) === "[object NodeList]"
			|| Object.prototype.toString.call(object) === "[object HTMLCollection]";
}

function isEvent(object) {
	return object instanceof Event
			|| /\[object \w+Event\]/.test(Object.prototype.toString.call(object));
}
