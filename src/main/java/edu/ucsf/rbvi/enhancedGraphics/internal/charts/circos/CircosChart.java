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
	private static final String ARCDIRECTION = "arcdirection";
	private static final String FIRSTARC = "firstarc";
	private static final String FIRSTARCWIDTH = "firstarcwidth";
	private static final String ARCWIDTH = "arcwidth";
	private static final String STROKEWIDTH = "outlineWidth";
	private static final String STROKECOLOR = "outlineColor";

	//ML:
	private List<List<Double>> valueList = null;
	private List<Color> colors = null;
	private List<String> circleLabels = null;
	private boolean labelCircles = false;
	private Position labelOffset = null;
	private double arcStart = 0.0;
	private boolean isClockwise = false;
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
		//ML: We want to populate the VALUES ourself
		String strValues=null;
		if (args.containsKey(VALUES)) {
			strValues = args.get(VALUES); // We save it
			args.remove(VALUES); // And remove it from the Map
		}
		// Then we populate: (end of ML)
		
		// This will populate the values, attributes, and labels lists
		populateValues(args);
		
		if(!args.containsKey(LABELS)) {
			// By default, if no labels are given, we use the attribute
			// Here with circos, labels are for slices not circles
			// So no labels means we don't label
			labels = null;
		}
		
		//ML: Now we can infer values
		//If values is surrounded by [ ] then it is a list
		if(strValues != null && strValues.startsWith("[") && strValues.endsWith("]")) {
			valueList = new ArrayList<>();
			
			strValues = strValues.substring(1, strValues.length()-1); // We get rid of the first [ and last ]
			for(String s : strValues.split("\\],\\[")) {
				values = convertInputToDouble(s);
				if(values == null) {
					logger.error("Cannot parse "+VALUES+" from input '"+s+"' of the input list '" + strValues +"'");
					return;
				}
				if (rangeMax != 0.0 || rangeMin != 0.0) {
					values = normalize(values, rangeMin, rangeMax);
					normalized = true;
				}
				valueList.add(convertData(values));
			}
		} else { // if not it is as before:
			values = null;
			if (strValues != null) {
				// Get our values.  convertData returns an array of values in degrees of arc
				values = convertInputToDouble(strValues);
				if (values == null) {
					logger.error("Cannot parse "+VALUES+" from input '"+strValues+"'");
					return;
				}
				if (rangeMax != 0.0 || rangeMin != 0.0) {
					values = normalize(values, rangeMin, rangeMax);
					normalized = true;
				}
			}
		}
		//(end of ML)

		//ML: We take care of the COLORS later:
		if (args.containsKey(COLORS)) {
//			if (attributes == null) 
//				colors = convertInputToColor(args.get(COLORS), values);
//			else
				colorString = args.get(COLORS);
		}
		//(end of ML)

		// System.out.println("colorString = "+colorString);

		if (args.containsKey(SORTSLICES))
			sortSlices = getBooleanValue(args.get(SORTSLICES));

		// Get our angular offset
		if (args.containsKey(ARCSTART)) {
			arcStart = getDoubleValue(args.get(ARCSTART));
		}
		
		if(args.containsKey(ARCDIRECTION)) {
			String direction = args.get(ARCDIRECTION).trim().toLowerCase();
			
			// By default it is counterclockwise, so we just look for some "clockwise" keywords
			// All other values will be considered counterclockwise
			isClockwise = direction.equals("clockwise") || direction.equals("cw") || direction.equals("clock");
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
		
//		// We can only have one type of labels (slices or circles)
//		if(labelCircles) {
//			// So if we label the circles, we don't label the slices
//			labels = null;
//		}

		if (args.containsKey(STROKEWIDTH))
			outlineWidth = getDoubleValue(args.get(STROKEWIDTH));
		else if (args.containsKey(BORDERWIDTH))
			outlineWidth = getDoubleValue(args.get(BORDERWIDTH));
		
		if (args.containsKey(STROKECOLOR))
			borderColor = getColorValue(args.get(STROKECOLOR));
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }
	
	//ML:
	private List<Color> convertInputToColor(String colorString, List<Double> values, int index) {
		if(colorString == null || !colorString.startsWith("[") || !colorString.endsWith("]")) {
			return convertInputToColor(colorString, values);
		}
		
		colorString=colorString.substring(1, colorString.length()-1);
		
		String colorsplit[] = colorString.split("\\],\\[");
		if(index >= colorsplit.length) {
			return convertInputToColor(colorString, values);
		}
		
		String strcolor = colorsplit[index];
		return convertInputToColor(strcolor, values);
		
	}

	public List<CircosLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		layers = new ArrayList<>();
		CyNode node = (CyNode)nodeView.getModel();

		//ML:
//		List<List<Double>> valueList = null;
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
				//ML:
				colorList = new ArrayList<List<Color>>();
				int index=0;
				for (String attr: attributes) {
					values = getDataFromAttributes (network, node, Collections.singletonList(attr), labels);
					values = convertData(values);
					valueList.add(values);
					//ML:
					colorList.add(convertInputToColor(colorString, values, index++));
				}
				//ML
//				colors = convertInputToColor(colorString, values);  // We only do this once
				nCircles = valueList.size();
			} else {
				// System.out.println("Got values");
				// If we already have values, we must want to use the attributes to map our colors
				colorList = new ArrayList<List<Color>>();
				//ML:
				int index=0;
				for (String attr: attributes) {
					List<Double>attrValues = 
						getDataFromAttributes (network, node, Collections.singletonList(attr), labels);
					// System.out.println("Found "+attrValues.size()+" values in '"+attr+"'");
					// System.out.println("colorString = "+colorString);
					//ML:
//					colors = convertInputToColor(colorString, attrValues);
					colors = convertInputToColor(colorString, attrValues, index++);
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
		} else {
			//ML: If there is no attribute, we look at values
			if (valueList != null) {
				colorList = new ArrayList<>();
				for(int i=0; i<valueList.size(); ++i) {
					values = convertData(valueList.get(i));
					valueList.set(i, values);
					colors = convertInputToColor(colorString, values, i);
					if (colors == null) {
						return null;
					}
					colorList.add(colors);
				}
				nCircles = valueList.size();
			} else if(values != null) {
				// There is only 1 circle
				nCircles = 1;
				values = convertData(values);
				colors = convertInputToColor(colorString, values, 0);
			}
			//(end of ML)
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
		    (values != null && labels.size() != values.size() ||
			   colors != null && labels.size() != colors.size())) {
			logger.error("circoschart: number of labels (" + labels.size()
			             + "), values (" + (values != null ? values.size() : "null") + "), and colors ("
			             + (colors != null ? colors.size() : "null") + ") don't match");
			return null;
		}

		// System.out.println("Got circle labels: nCircles="+nCircles);

		List<CircosLayer> labelList = new ArrayList<CircosLayer>();

		double rad = firstArc;
		double maxRadius = firstArc + firstArcWidth + arcWidth*(nCircles-1);
		for (int circle = 0; circle < nCircles; circle++) {
//			String circleLabel = attributes.get(circle);
//
//			if (cLabels != null)
//				circleLabel = cLabels.get(circle);

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
				if (values.get(slice) <= 0.0) {
					logger.warn("The slice "+slice+" of circle "+circle+" has a negative value: "+values.get(slice)+". This slice is ignored.");
					continue;
				}

				// System.out.println("Slice: "+slice+", value: "+values.get(slice));
				// System.out.println("Slice: "+slice+", color: "+colors.get(slice));
				// System.out.println("Value: "+values.get(slice)+" color: "+colors.get(slice));
	
				// Create the slice
				CircosLayer pl = new CircosLayer(rad, circleWidth, arc, values.get(slice), isClockwise, colors.get(slice), outlineWidth, borderColor);
				if (pl == null) continue;
				layers.add(pl);
	
				// Only create the labels for the last circle
				if (label != null && circle == (nCircles-1)) {
					// Now, create the label
					CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arc, values.get(slice), isClockwise, label, font, labelColor, labelWidth, labelSpacing);
					if (labelLayer != null)
						labelList.add(labelLayer);
				}
				if(isClockwise) {
					arc -= values.get(slice).doubleValue();
				} else {
					arc += values.get(slice).doubleValue();
				}
			}

//			if (labelCircles && labelOffset == null) {
//				CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arcStart, isClockwise, circleLabel, font, labelColor, labelWidth, labelSpacing);
//				if (labelLayer != null)
//					labelList.add(labelLayer);
//			}

			rad += circleWidth;
		}

		// For the offset labels, we want to add them here so we can control the order a little more
		// rationally

		// reset our starting radius
		// System.out.println("Drawing circles");
		rad = maxRadius;
		for (int circle = nCircles-1; circle >= 0; circle--) {
			String circleLabel = "";
			
			if(attributes != null && circle < attributes.size()) {
				circleLabel = attributes.get(circle);
			}

			if (cLabels != null)
				circleLabel = cLabels.get(circle);

			double circleWidth = arcWidth;
			if (circle == 0) 
				circleWidth = firstArcWidth;

			if (labelCircles && labelOffset != null) {
				CircosLayer labelLayer = new CircosLayer(rad, circleWidth, arcStart, isClockwise, circleLabel, font, labelColor, labelWidth, labelSpacing, 
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
			if(d >= 0) { // We do not draw negative slices
				totalSize += d.doubleValue();
			}
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
