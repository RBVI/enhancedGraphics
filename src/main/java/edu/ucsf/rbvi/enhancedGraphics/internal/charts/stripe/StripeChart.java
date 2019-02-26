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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.stripe;

// System imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Cytoscape imports
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.AbstractChartCustomGraphics;

/**
 * The StripeChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class StripeChart extends AbstractChartCustomGraphics<StripeLayer> {
	private static final String COLORS = "colorlist";

	private List<Color> colorList = null;
	private int labelSize = 4;
	private String colorString = null;

	// Parse the input string, which is always of the form:
	// linechart:	[colorlist=value]
	// 						[position=value]
	// 						[scale=0.90]
	public StripeChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		// populateValues(args);

		if (args.containsKey(COLORS)) {
			if (attributes == null) 
				colorList = convertInputToColor(args.get(COLORS), new ArrayList<Double>());
			else
				colorString = args.get(COLORS);
		}
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	@Override 
	public List<StripeLayer> getLayers(CyNetworkView networkView, View nodeView) { 
		CyNetwork network = networkView.getModel();
		int nStripes = colorList.size();
		layers = new ArrayList<>();
		for (int stripe = 0; stripe < nStripes; stripe++) {
			Color color = colorList.get(stripe);

			// Create the stripe
			StripeLayer bl = new StripeLayer(stripe, nStripes, color);
			if (bl == null) continue;
			layers.add(bl);

		}

		shapeLayers = layers;
		return layers; 
	}

}
