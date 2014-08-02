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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.bar;

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

/**
 * The BarChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class BarChart extends AbstractChartCustomGraphics<BarLayer> {
	private static final String COLORS = "colorlist";
	private static final String SEPARATION = "separation";

	private List<Color> colorList = null;
	private String colorString = null;
	private int separation = 0;
	private boolean showAxes = false;

	// Parse the input string, which is always of the form:
	// piechart:	[attributelist=value]
	//       			[colorlist=value]
	//            [labellist=value]
	//            [position=value]
	//            [scale=0.90]
	//            [separation=value]
	//            [showlabels=true]
	//            [valuelist=value]
	public BarChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		populateValues(args);

		if (args.containsKey(COLORS)) {
			if (attributes == null) 
				colorList = convertInputToColor(args.get(COLORS), values);
			else
				colorString = args.get(COLORS);
		}

		if (args.containsKey(SEPARATION)) {
			separation = Integer.parseInt(args.get(SEPARATION));
		}

		if (args.containsKey(SHOWYAXIS)) {
			showAxes = getBooleanValue(args.get(SHOWYAXIS));
		}
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	public Image getRenderedImage() { return null; }

	@Override 
	public List<BarLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
			return null;

		CyNode node = (CyNode)nodeView.getModel();

		// Create all of our pie slices. Each slice becomes a layer
		if (attributes != null && attributes.size() > 0) {
			// System.out.println("Getting data from attributes for node "+node);
			values = getDataFromAttributes (network, node, attributes, labels);
			// System.out.println("Data from attributes returns "+values.size()+" values");
			colorList = convertInputToColor(colorString, values);
		}

		if (labels != null && labels.size() > 0 &&
		    (labels.size() != values.size() ||
			   labels.size() != colorList.size())) {
			logger.error("number of labels (" + labels.size()
			             + "), values (" + values.size() + "), and colors ("
			             + colorList.size() + ") don't match");
		}

		List<BarLayer> labelList = new ArrayList<BarLayer>();


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
			if (values.get(bar) == 0.0) continue;

			// System.out.println("Creating bar #"+bar);
			// Create the slice
			BarLayer bl = new BarLayer(bar, nBars, separation, values.get(bar), minValue, maxValue, 
			                           normalized, ybase, colorList.get(bar), showAxes);
			if (bl == null) continue;
			layers.add(bl);

			if (label != null) {
				// System.out.println("Creating label for bar #"+bar);
				// Now, create the label
				BarLayer labelLayer = new BarLayer(bar, nBars, separation, minValue, maxValue, normalized,
			                                     labelMin, ybase, label, font, labelColor, showAxes);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}
		}

		// Now add all of our labels so they will be on top of our slices
		if (labelList != null && labelList.size() > 0)
			layers.addAll(labelList);
		return layers; 
	}

}
