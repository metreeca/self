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

package com.metreeca._tile.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;


abstract class Agent {

	private static final Agent Agent=GWT.create(Agent.class);


	public static Agent Agent() {
		return Agent;
	}


	protected Agent() {
		GWT.log("  ! [Agent] loading "+getClass().getName());
	}


	/**
	 * Retrieves the horizontal coordinate of a mouse event wrt a frame of reference.
	 *
	 * @param event the event whose horizontal coordinate is to be retrieved
	 * @param tile  a tile defining the frame of reference
	 *
	 * @return if <code>tile</code> is <code>null</code>, the horizontal offset coordinate of <code>event</code> wrt its
	 * current target; if <code>tile</code> is empty, the horizontal client coordinate of <code>event</code>; otherwise,
	 * the horizontal offset coordinate of <code>event</code> wrt the first element of the <code>tile</code>
	 */
	public abstract int x(final Event event, final Tile tile);

	/**
	 * Retrieves the vertical coordinate of a mouse event wrt a frame of reference.
	 *
	 * @param event the event whose vertical coordinate is to be retrieved
	 * @param tile  a tile defining the frame of reference
	 *
	 * @return if <code>tile</code> is <code>null</code>, the vertical offset coordinate of <code>event</code> wrt its
	 * current target; if <code>tile</code> is empty, the vertical client coordinate of <code>event</code>; otherwise,
	 * the vertical offset coordinate of <code>event</code> wrt the first element of the <code>tile</code>
	 */
	public abstract int y(final Event event, final Tile tile);


	/**
	 * Retrieves the front-most element at a point wrt a frame of reference.
	 *
	 * @param tile a tile whose first element defines the frame of reference for the coordinates; if empty or
	 *             <code>null</code>, the document body is assumed
	 * @param x    the horizontal target coordinate
	 * @param y    the vertical target coordinate
	 *
	 * @return a tile containing the front-most element at the given coordinates or an empty tile if no such element
	 * exists
	 */
	public final native Tile at(final Tile tile, final int x, final int y) /*-{

		var frame=tile && tile[0] || $doc.body;

		var cx=x+frame.getBoundingClientRect().left;
		var cy=y+frame.getBoundingClientRect().top;

		var element=$doc.elementFromPoint(cx, cy);

		return element && [element] || [];
	}-*/;


	public final native JavaScriptObject keys() /*-{

		// See https://developer.mozilla.org/en/DOM/KeyboardEvent#Virtual_key_codes

		var keys={
			CANCEL: 3, // Cancel key.
			HELP: 6, // Help key.
			BACK_SPACE: 8, // Backspace key.
			TAB: 9, // Tab key.
			CLEAR: 12, // "5" key on Numpad when NumLock is unlocked. Or on Mac, clear key which is positioned at NumLock key.
			RETURN: 13, // Return/enter key on the main keyboard.
			ENTER: 14, // Reserved, but not used.
			SHIFT: 16, // Shift key.
			CONTROL: 17, // Control key.
			ALT: 18, // Alt (Option on Mac) key.
			PAUSE: 19, // Pause key.
			CAPS_LOCK: 20, // Caps lock.
			ESCAPE: 27, // Escape key.
			SPACE: 32, // Space bar.
			PAGE_UP: 33, // Page Up key.
			PAGE_DOWN: 34, // Page Down key.
			END: 35, // End key.
			HOME: 36, // Home key.
			LEFT: 37, // Left arrow.
			UP: 38, // Up arrow.
			RIGHT: 39, // Right arrow.
			DOWN: 40, // Down arrow.
			SELECT: 41,
			PRINT: 42,
			EXECUTE: 43,
			PRINTSCREEN: 44, // Print Screen key.
			INSERT: 45, // Ins(ert) key.
			DELETE: 46, // Del(ete) key.
			0: 48,
			1: 49,
			2: 50,
			3: 51,
			4: 52,
			5: 53,
			6: 54,
			7: 55,
			8: 56,
			9: 57,
			COLON: 58, // Colon (":") key
			SEMICOLON: 59, // Semicolon (";") key.
			LESS_THAN: 60, // Less-than ("<") key
			EQUALS: 61, // Equals ("=") key.
			GREATER_THAN: 62, // Greater-than (">") key
			QUESTION_MARK: 63, // Question mark ("?") key
			AT: 64, // At mark ("@") key
			A: 65,
			B: 66,
			C: 67,
			D: 68,
			E: 69,
			F: 70,
			G: 71,
			H: 72,
			I: 73,
			J: 74,
			K: 75,
			L: 76,
			M: 77,
			N: 78,
			O: 79,
			P: 80,
			Q: 81,
			R: 82,
			S: 83,
			T: 84,
			U: 85,
			V: 86,
			W: 87,
			X: 88,
			Y: 89,
			Z: 90,
			CONTEXT_MENU: 93,
			NUMPAD0: 96, // 0 on the numeric keypad.
			NUMPAD1: 97, // 1 on the numeric keypad.
			NUMPAD2: 98, // 2 on the numeric keypad.
			NUMPAD3: 99, // 3 on the numeric keypad.
			NUMPAD4: 100, // 4 on the numeric keypad.
			NUMPAD5: 101, // 5 on the numeric keypad.
			NUMPAD6: 102, // 6 on the numeric keypad.
			NUMPAD7: 103, // 7 on the numeric keypad.
			NUMPAD8: 104, // 8 on the numeric keypad.
			NUMPAD9: 105, // 9 on the numeric keypad.
			MULTIPLY: 106, // * on the numeric keypad.
			ADD: 107, // + on the numeric keypad.
			SEPARATOR: 108,
			SUBTRACT: 109, // - on the numeric keypad.
			DECIMAL: 110, // Decimal point on the numeric keypad.
			DIVIDE: 111, // / on the numeric keypad.
			F1: 112, // F1 key.
			F2: 113, // F2 key.
			F3: 114, // F3 key.
			F4: 115, // F4 key.
			F5: 116, // F5 key.
			F6: 117, // F6 key.
			F7: 118, // F7 key.
			F8: 119, // F8 key.
			F9: 120, // F9 key.
			F10: 121, // F10 key.
			F11: 122, // F11 key.
			F12: 123, // F12 key.
			F13: 124, // F13 key.
			F14: 125, // F14 key.
			F15: 126, // F15 key.
			F16: 127, // F16 key.
			F17: 128, // F17 key.
			F18: 129, // F18 key.
			F19: 130, // F19 key.
			F20: 131, // F20 key.
			F21: 132, // F21 key.
			F22: 133, // F22 key.
			F23: 134, // F23 key.
			F24: 135, // F24 key.
			NUM_LOCK: 144, // Num Lock key.
			SCROLL_LOCK: 145, // Scroll Lock key.
			CIRCUMFLEX: 160, // Circumflex ("^") key
			EXCLAMATION: 161, // Exclamation ("!") key
			DOUBLE_QUOTE: 162, // Double quote (""") key
			HASH: 163, // Hash ("#") key
			DOLLAR: 164, // Dollar sign ("$") key
			PERCENT: 165, // Percent ("%") key
			AMPERSAND: 166, // Ampersand ("&") key
			UNDERSCORE: 167, // Underscore ("_") key
			OPEN_PAREN: 168, // Open parenthesis ("(") key
			CLOSE_PAREN: 169, // Close parenthesis (")") key
			ASTERISK: 170, // Asterisk ("*") key
			PLUS: 171, // Plus ("+") key
			PIPE: 172, // Pipe ("|") key
			HYPHEN_MINUS: 173, // Hyphen/Minus ("-") key
			OPEN_CURLY_BRACKET: 174, // Open curly bracket ("{") key
			CLOSE_CURLY_BRACKET: 175, // Close curly bracket ("}") key
			TILDE: 176, // Tilde ("~") key
			COMMA: 188, // Comma (",") key.
			PERIOD: 190, // Period (".") key.
			SLASH: 191, // Slash ("/") key.
			BACK_QUOTE: 192, // Back tick ("`") key.
			OPEN_BRACKET: 219, // Open square bracket ("[") key.
			BACK_SLASH: 220, // Back slash ("\") key.
			CLOSE_BRACKET: 221, // Close square bracket ("]") key.
			QUOTE: 222, // Quote (''') key.
			META: 224, // Command key on Mac.
			ALTGR: 225, // AltGr key on Linux
			WIN: 91, // Windows logo key on Windows. Or Super or Hyper key on Linux
			KANA: 21,
			HANGUL: 21,
			EISU: 22, // "英数" key on Japanese Mac keyboard
			JUNJA: 23,
			FINAL: 24,
			HANJA: 25,
			KANJI: 25,
			CONVERT: 28,
			NONCONVERT: 29,
			ACCEPT: 30,
			MODECHANGE: 31,
			SELECT: 41,
			PRINT: 42,
			EXECUTE: 43,
			SLEEP: 95
		};

		for (var k in keys) {
			if ( keys.hasOwnProperty(k) ) {
				keys[k.replace(/_/g, '')]=keys[k]; // alias removing underscores
			}
		}

		keys.CTRL=keys.CONTROL;
		keys.ESC=keys.ESCAPE;
		keys.INS=keys.INSERT;
		keys.DEL=keys.DELETE;
		keys.BACK=keys.BACK_SPACE;
		keys.CANC=keys.CANCEL;

		keys.LESS=keys.LESS_THAN;
		keys.GREATER=keys.GREATER_THAN;
		keys.QUESTION=keys.QUESTION_MARK;
		keys.MINUS=keys.HYPHEN_MINUS;

		return keys;

	}-*/;
}
