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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.circos;

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
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.AbstractChartCustomGraphics;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils.Position;

/**
 * The CircosChart creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class CircosChart extends AbstractChartCustomGraphics<CircosLayer> {
	private static final String COLORS = "colorlist";
	// TODO
	private static final String LABELCIRCLES = "labelcircles"; // Indicate whether to label the circles and where
	private static final String CIRCLELABELS = "circlelabels"; // String list to provide actual labels
	private static final String SORTSLICES = "sortslices";
	private static final String MINIMUMSLICE = "minimumslice";
	private static final String ARCSTART = "arcstart";
	private static final String FIRSTARC = "firstarc";
	private static final String FIRSTARCWIDTH = "firstarcwidth";
	private static final String ARCWIDTH = "arcwidth";
	private static final String STROKEWIDTH = "outlineWidth";

	private List<Color> colors = null;
	private List<String> circleLabels = null;
	private boolean labelCircles = false;
	private Position labelOffset = null;
	private double arcStart = 0.0;
	private boolean sortSlices = true;
	private double minimumSlice = 2.0;
	private double firstArc = 0.2; // 20% out for first inner arc
	private double arcWidth = 0.1; // 10% of node width for arcs
	private double firstArcWidth = 0.1; // 10% of node width for arcs
	private double outlineWidth = 0.1; // 

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
	public CircosChart(String input) {
		Map<String, String> args = parseInput(input);
		// This will populate the values, attributes, and labels lists
		populateValues(args);

		if (args.containsKey(COLORS)) {
			if (attributes == null) 
				colors = convertInputToColor(args.get(COLORS), values);
			else
				colorString = args.get(COLORS);
		}

		// System.out.println("colorString = "+colorString);

		if (args.containsKey(SORTSLICES))
			sortSlices = getBooleanValue(args.get(SORTSLICES));

		// Get our angular offset
		if (args.containsKey(ARCSTART)) {
			arcStart = getDoubleValue(args.get(ARCSTART));
		}

		if (args.containsKey(FIRSTARC)) {
			firstArc = getDoubleValue(args.get(FIRSTARC));
		}

		if (args.containsKey(ARCWIDTH)) {
			arcWidth = getDoubleValue(args.get(ARCWIDTH));
		}

		if (args.containsKey(FIRSTARCWIDTH)) {
			firstArcWidth = getDoubleValue(args.get(FIRSTARCWIDTH));
		} else {
			firstArcWidth = arcWidth;
		}

		if (args.containsKey(LABELCIRCLES)) {
			labelOffset = Position.getPosition(args.get(LABELCIRCLES));
			if (labelOffset != null) {
				labelCircles = true;
			} else {
				labelCircles = getBooleanValue(args.get(LABELCIRCLES));
			}
		}

		if (args.containsKey(CIRCLELABELS))
			circleLabels = getStringList(args.get(CIRCLELABELS));

		if (args.containsKey(STROKEWIDTH))
			outlineWidth = getDoubleValue(args.get(STROKEWIDTH));
		else if (args.containsKey(BORDERWIDTH))
			outlineWidth = getDoubleValue(args.get(BORDERWIDTH));
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	public List<CircosLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		layers = new ArrayList<>();
		CyNode node = (CyNode)nodeView.getModel();

		List<List<Double>> valueList = null;
		List<List<Color>> colorList = null;
		List<String> cLabels = circleLabels;
		int nCircles = 0;

		Font font = getFont();

		// Create all of our slices. Each slice becomes a layer
		if (attributes != null && attributes.size() > 0) {
			if (values == null || values.size() == 0) {
				// System.out.println("No values");
				// OK, the colors are constant, the slice width changes
				valueList = new ArrayList<List<Double>>();
				for (String attr: attributes) {
					values = getDataFromAttributes (network, node, Collections.singletonList(attr), labels);
					values = convertData(values);
					valueList.add(values);
				}
				colors = convertInputToColor(colorString, values);  // We only do this once
				nCircles = valueList.size();
			} else {
				// System.out.println("Got values");
				// If we already have values, we must want to use the attributes to map our colors
				colorList = new ArrayList<List<Color>>();
				for (String attr: attributes) {
					List<Double>attrValues = 
						getDataFromAttributes (network, node, Collections.singletonList(attr), labels);
					// System.out.println("Found "+attrValues.size()+" values in '"+attr+"'");
					// System.out.println("colorString = "+colorString);
					colors = convertInputToColor(colorString, attrValues);
					// System.out.println("convertInputToColor returns: "+colors);
					if (colors == null) {
						return null;
					}
					// System.out.println("Colors for "+attr+"="+colors);
					colorList.add(colors);
				}
				values = convertData(values);
				nCircles = colorList.size();
			}
		}

		// System.out.println("nCircles = "+nCircles);

		if (cLabels != null && (cLabels.size() != nCircles || ((cLabels.size() == 1) && (nCircles == 1)))) {
			// First, see if circleLabels actually points to an attribute
			if (cLabels.size() == 1) {
				CyColumn column = network.getDefaultNodeTable().getColumn(cLabels.get(0));
				if (column != null) {
					// OK, we found the attribute
					if (column.getType().equals(List.class) && column.getListElementType().equals(String.class)) {
						cLabels = new ArrayList<String>(network.getRow(node).getList(cLabels.get(0), String.class));
					} else if (column.getType().equals(String.class)) {
						cLabels = getStringList(network.getRow(node).get(cLabels.get(0), String.class));
					}
				}
			}
			if (cLabels.size() != nCircles) {
				// System.out.println("Wrong circle label size");
				logger.error("circoschart: number of circle labels (" + circleLabels.size()
				             + "), doesn't match the number of circles ("+nCircles+")");
				return null;
			} else {
				// System.out.println("Got circle labels: nCircles="+nCircles);
			}
		}


		if (labels != null && labels.size() > 0 &&
		    (labels.size() != values.size() ||
			   labels.size() != colors.size())) {
			logger.error("circoschart: number of labels (" + labels.size()
			             + "), values (" + values.size() + "), and colors ("
			             + colors.size() + ") don't match");
			return null;
		}

		// System.out.println("Got circle labels: nCircles="+nCircles);

		List<CircosLayer> labelList = new ArrayList<CircosLayer>();

		double rad = firstArc;
		double maxRadius = firstArc + firstArcWidth + arcWidth*(nCircles-1);
		for (int circle = 0; circle < nCircles; circle++) {
			String circleLabel = attributes.get(circle);

			if (cLabels != null)
				circleLabel = cLabels.get(circle);

			if (valueList != null)
				values = valueList.get(circle);

			if (colorList != null)
				colors = colorList.get(circle);

			int nSlices = values.size();
			double arc = arcStart;
			double circleWidth = arcWidth;
			if (circle == 0) 
				circleWidth = firstArcWidth;

			for (int slice = 0; slice < nSlices; slice++) {
				String label = null;
				if (labels != null && labels.size() > 0)
					label = labels.get(slice);
				if (values.get(slice) == 0.0) continue;

				// System.out.println("Slice: "+slice+", value: "+values.get(slice));
				// System.out.println("Slice: "+slice+", color: "+colors.get(slice));
				// System.out.println("Value: "+values.get(slice)+" color: "+colors.get(slice));
	
				// Create the slice
				CircosLayer pl = new CircosLayer(rad, circleWidth, arc, values.get(slice), colors.get(slice), outlineWidth);
				if (pl == null) continue;
				layers.add(pl);
	
				// Only create the labels for the last circle
				if (label != null && circle == (nCircles-1)) {
					// Now, create the label
					CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arc, values.get(slice), label, font, labelColor);
					if (labelLayer != null)
						labelList.add(labelLayer);
				}
				arc += values.get(slice).doubleValue();
			}

			if (labelCircles && labelOffset == null) {
				CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arcStart, circleLabel, font, labelColor);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}

			rad += circleWidth;
		}

		// For the offset labels, we want to add them here so we can control the order a little more
		// rationally

		// reset our starting radius
		// System.out.println("Drawing circles");
		rad = maxRadius;
		for (int circle = nCircles-1; circle >= 0; circle--) {
			String circleLabel = attributes.get(circle);

			if (cLabels != null)
				circleLabel = cLabels.get(circle);

			double circleWidth = arcWidth;
			if (circle == 0) 
				circleWidth = firstArcWidth;

			if (labelCircles && labelOffset != null) {
				CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arcStart, circleLabel, font, labelColor, 
				                                         labelOffset, maxRadius, circle, nCircles);
				if (labelLayer != null)
					labelList.add(labelLayer);
			}

			rad -= circleWidth;
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
