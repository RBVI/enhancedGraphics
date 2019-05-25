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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.pie;

// System imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.AbstractChartCustomGraphics;

/**
 * The PieChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class PieChart extends AbstractChartCustomGraphics<PieLayer> {
	private static final String COLORS = "colorlist";
	// TODO
	private static final String LABELOFFSET = "labeloffset";
	private static final String SORTSLICES = "sortslices";
	private static final String MINIMUMSLICE = "minimumslice";
	private static final String ARCSTART = "arcstart";
	

	private List<Color> colorList = null;
	private double arcStart = 0.0;
	private boolean sortSlices = true;
	private double minimumSlice = 2.0;
	private String colorString = null;

	// Parse the input string, which is always of the form:
	// piechart:	[arcstart=0.0]
	//       			[attributelist=value]
	//            [colorlist=value]
	//            [labelfont=value]
	//            [labellist=value]
	//            [labelsize=8]
	//            [labelstyle=plain]
	//            [minimumslice=5.0]
	//            [network=current]
	//            [position=value]
	//            [scale=0.90]
	//            [showlabels=true]
	//            [sortslices=true]
	//            [valuelist=value]
	public PieChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		populateValues(args);

		if (args.containsKey(COLORS)) {
			if (attributes == null) 
				colorList = convertInputToColor(args.get(COLORS), values);
			else
				colorString = args.get(COLORS);
		}

		if (args.containsKey(SORTSLICES))
			sortSlices = getBooleanValue(args.get(SORTSLICES));

		// Get our angular offset
		if (args.containsKey(ARCSTART)) {
			arcStart = getDoubleValue(args.get(ARCSTART));
		}
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	// public Image getRenderedImage() { return null; }

	public List<PieLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		layers = new ArrayList<>();
		CyNode node = (CyNode)nodeView.getModel();

		// Create all of our pie slices. Each slice becomes a layer
		if (attributes != null && attributes.size() > 0) {
			if (values == null || values.size() == 0) {
				values = getDataFromAttributes (network, node, attributes, labels);
				colorList = convertInputToColor(colorString, values);
			} else {
				boolean foundColors=false;
				// System.out.println("Have both attributes and values");
				// If we already have values, we must want to use the attributes to map our colors
				List<Double>attrValues = getDataFromAttributes (network, node, attributes, labels);
				if (colorString.indexOf(';') > 0) {
					// System.out.println("Found semi-separated colors");
					colorList = new ArrayList<Color>();
					String[] colors = colorString.split(";");
					if (colors.length != attrValues.size()) {
							logger.error("piechart: number of colors must match the number of attributes");
							return null;
					}
					int colorIndex = 0;
					for (Double value: attrValues) {
							if (value == null)
								colorList.add(null);
							else {
								foundColors=true;
								colorList.addAll(convertInputToColor(colors[colorIndex++], Collections.singletonList(value)));
							}
					}
					if (!foundColors) {
						logger.error("piechart: no colors found");
						return null;
					}
				} else {
					colorList = convertInputToColor(colorString, attrValues);
				}
				if (colorList == null) {
					logger.error("piechart: no colors found");
					return null;
				}
			}
		}

		values = convertData(values);

		if (labels != null && labels.size() > 0 &&
		    (labels.size() != values.size() ||
			   labels.size() != colorList.size())) {
			logger.error("piechart: number of labels (" + labels.size()
			             + "), values (" + values.size() + "), and colors ("
			             + colorList.size() + ") don't match");
			return null;
		}

		List<PieLayer> labelList = new ArrayList<PieLayer>();

		Font font = getFont();
		int nSlices = values.size();
		double arc = arcStart;
		for (int slice = 0; slice < nSlices; slice++) {
			String label = null;
			if (labels != null && labels.size() > 0)
				label = labels.get(slice);
			if (values.get(slice) == 0.0) continue;

			// Create the slice
			PieLayer pl = new PieLayer(arc, values.get(slice), colorList.get(slice), borderWidth, borderColor);
			if (pl == null) continue;
			layers.add(pl);

			if (label != null && label.length() > 0) {
				// Now, create the label
				PieLayer labelLayer = new PieLayer(arc, values.get(slice), label, font, labelColor);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}
			arc += values.get(slice).doubleValue();
		}

		// Now add all of our labels so they will be on top of our slices
		if (labelList != null && labelList.size() > 0)
			layers.addAll(labelList);
		shapeLayers = layers;
		return layers; 
	}

	private List<Double> convertData(List<Double> values) {
		double totalSize = 0.0;
		int nValues = values.size();
		for (Double d: values) {
			totalSize += d.doubleValue();
		}

		// Now we have an array of doubles, but we need to convert them
		// to degree offsets
		for (int index = 0; index < nValues; index++) {
			double v = values.get(index).doubleValue();
			values.set(index, v*360.0/totalSize);
		}
		return values;
	}

}
