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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.heatstrip;

// System imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;
import java.awt.Font;
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
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.AbstractChartCustomGraphics;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ColorGradients;

/**
 * The HeatStripChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class HeatStripChart extends AbstractChartCustomGraphics<HeatStripLayer> {
	private static final String COLORS = "colorlist";
	private static final String SEPARATION = "separation";

	private List<Color> colorList = null;
	private String colorString = null;
	private int separation = 0;
	private boolean showAxes = false;
	Color[] colorScale = null;

	// Parse the input string, which is always of the form:
	// heatstripchart:	[attributelist=value]
	//       						[colorlist=value]
	//       						[labellist=value]
	//            			[position=value]
	//            			[scale=0.90]
	//            			[separation=value]
	//            			[valuelist=value]
	public HeatStripChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		populateValues(args);

		colorScale = ColorGradients.YELLOWBLACKCYAN.getColors();
		if (args.containsKey(COLORS)) {
			// Get our colors
			String colorSpec = args.get(COLORS).toString();
			if (ColorGradients.getGradient(colorSpec) != null) {
				colorScale = ColorGradients.getGradient(colorSpec);
			} else {
				try {
					String [] colorArray = colorSpec.split(",");
					List<Color> colors = parseUpDownColor(colorArray);
					colorScale[1] = colors.get(2);
					colorScale[0] = colors.get(1);
					colorScale[2] = colors.get(0);
				} catch (Exception e) {
					System.err.println("Unable to parse up/down color: "+colorSpec);
					colorScale = ColorGradients.YELLOWBLACKCYAN.getColors();
				}
			}
		}

		if (args.containsKey(SEPARATION)) {
			separation = Integer.parseInt(args.get(SEPARATION));
		}

		if (args.containsKey(SHOWYAXIS)) {
			showAxes = getBooleanValue(args.get(SHOWYAXIS));
		}
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	@Override 
	public List<HeatStripLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		CyNode node = (CyNode)nodeView.getModel();

		// Create all of our pie slices. Each slice becomes a layer
		if (attributes != null && attributes.size() > 0) {
			values = getDataFromAttributes (network, node, attributes, labels);
		}

		if (labels != null && labels.size() > 0 &&
		    labels.size() != values.size()) {
			logger.error("number of labels (" + labels.size()
			             + "), values (" + values.size() + ") don't match");
		}

		List<HeatStripLayer> labelList = new ArrayList<HeatStripLayer>();

		double minValue = 0.000001;
		double maxValue = -minValue;
		for (Double val: values) {
			if (val == null) continue;
			minValue = Math.min(minValue, val);
			maxValue = Math.max(maxValue, val);
		}
		double labelMin = minValue;

		if (normalized) {
			minValue = rangeMin;
			maxValue = rangeMax;
		}
			
		int nBars = values.size();
		Font font = getFont();
		for (int bar = 0; bar < nBars; bar++) {
			String label = null;
			if (labels != null && labels.size() > 0)
				label = labels.get(bar);
			if (values.get(bar) == null || values.get(bar) == 0.0) continue;

			// Create the slice
			HeatStripLayer bl = new HeatStripLayer(bar, nBars, separation, values.get(bar), minValue, 
			                                       maxValue, normalized, colorScale, showAxes, borderWidth, scale);
			if (bl == null) continue;
			layers.add(bl);

			if (label != null) {
				// Now, create the label
				HeatStripLayer labelLayer = new HeatStripLayer(bar, nBars, separation, minValue, maxValue, 
				                                               normalized, labelMin, label, font, showAxes, scale);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}
		}

		// Now add all of our labels so they will be on top of our slices
		if (labelList != null && labelList.size() > 0)
			layers.addAll(labelList);

		shapeLayers = layers;
		return layers; 
	}

}
