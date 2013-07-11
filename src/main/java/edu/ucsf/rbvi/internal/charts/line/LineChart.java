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
package edu.ucsf.rbvi.enhancedcg.internal.charts.line;

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
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import edu.ucsf.rbvi.enhancedcg.internal.charts.AbstractChartCustomGraphics;

/**
 * The LineChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class LineChart extends AbstractChartCustomGraphics<LineLayer> {
	private static final String COLORS = "colorlist";
	private static final String LINEWIDTH = "linewidth";

	private List<Color> colorList = null;
	private int labelSize = 4;
	private String colorString = null;
	private float lineWidth = 1.5f;

	// Parse the input string, which is always of the form:
	// linechart:	[attributelist=value]
	// 						[color=value]
	//						[labellist=value]
	// 						[position=value]
	// 						[linewidth=value]
	// 						[scale=0.90]
	//						[showlabels=true]
	// 						[valuelist=value]
	public LineChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		populateValues(args);

		if (args.containsKey(COLORS)) {
			if (attributes == null) 
				colorList = convertInputToColor(args.get(COLORS), values);
			else
				colorString = args.get(COLORS);
		}

		if (args.containsKey(LINEWIDTH)) {
			lineWidth = Float.parseFloat(args.get(LINEWIDTH));
		}
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	public Image getRenderedImage() { return null; }

	@Override 
	public List<LineLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		CyNode node = (CyNode)nodeView.getModel();
		// Create all of our pie slices. Each slice becomes a layer
		if (attributes != null && attributes.size() > 0) {
			values = getDataFromAttributes (network, (CyNode)node, attributes, labels);
			colorList = convertInputToColor(colorString, values);
		}

		List<LineLayer> labelList = new ArrayList<LineLayer>();

		double minValue = 0.000001;
		double maxValue = -minValue;
		for (double val: values) {
			minValue = Math.min(minValue, val);
			maxValue = Math.max(maxValue, val);
		}
			
		int nPoints = values.size();
		for (int point = 0; point < nPoints-1; point++) {
			// String label = null;
			// if (labels != null && labels.size() > 0)
			// 	label = labels.get(point);
			// if (values.get(point) == 0.0) continue;
			Color color = colorList.get(0);
			if (colorList.size() == values.size())
				color = colorList.get(point);

			// Create the line
			LineLayer bl = new LineLayer(point, nPoints, 
			                             values.get(point), values.get(point+1), 
			                             minValue, maxValue, color, lineWidth);
			if (bl == null) continue;
			layers.add(bl);

/*
			if (label != null) {
				// Now, create the label
				LineLayer labelLayer = new LineLayer(point, nPoints, minValue, maxValue, 
			                                     label, labelSize);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}
*/
		}

		// Now add all of our labels so they will be on top of our slices
		// if (labelList != null && labelList.size() > 0)
		// 	layers.addAll(labelList);
		return layers; 
	}

}
