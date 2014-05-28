/* vim: set ts=2: */
/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.enhancedGraphics.internal.charts;

// System imports
import java.awt.Color;

import java.util.HashMap;
import java.util.Map;

public enum ColorGradients {
	REDGREEN("redgreen", Color.GREEN, null, Color.RED),
	REDBLUE("redblue", Color.RED, null, Color.BLUE),
	YELLOWWHITECYAN("yellowwhitecyan", Color.CYAN, Color.WHITE, Color.YELLOW),
	YELLOWCYAN("yellowcyan", Color.CYAN, null, Color.YELLOW),
	YELLOWBLACKCYAN("yellowblackcyan", Color.CYAN, Color.BLACK, Color.YELLOW),
	YELLOWBLUE("yellowblue", Color.BLUE, null, Color.YELLOW),
	ORANGEPURPLE("orangepurple", Color.ORANGE, null, Color.MAGENTA),
	BLUEGREENYELLOW("bluegreenyellow", Color.BLUE, Color.GREEN, Color.YELLOW),
	PURPLEYELLOW("purpleyellow", Color.MAGENTA, null, Color.YELLOW),
	GREENPURPLE("greenpurple", Color.GREEN, null, Color.MAGENTA),
	REDYELLOW("redyellow", Color.RED, null, Color.YELLOW);

	private Color up, zero, down;
	private String name;
	private static Map<String, ColorGradients>cMap;

	ColorGradients(String name, Color down, Color zero, Color up) {
		this.name = name;
		this.up = up;
		this.down = down;
		this.zero = zero;
		addGradient(this);
	}

	public String getLabel() { return name; }

	private void addGradient(ColorGradients col) {
		if (cMap == null) cMap = new HashMap<String,ColorGradients>();
		cMap.put(col.getLabel(), col);
	}

	public Color[] getColors() {
		if (zero == null) {
			Color[] retColors = new Color[2];
			retColors[0] = down;
			retColors[1] = up;
			return retColors;
		} else {
			Color[] retColors = new Color[3];
			retColors[0] = down;
			retColors[1] = zero;
			retColors[2] = up;
			return retColors;
		}
	}

	public static Color[] getGradient(String name) {
		if (cMap.containsKey(name))
			return cMap.get(name).getColors();
		return null;
	}
}
