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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.label;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.AbstractChartCustomGraphics;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

/**
 * The Label creates a list of custom graphics where each custom graphic represents
 * a slice of the pie.  The data for this is of the format: label1:value1:color1, etc.,
 * where value is numeric and the color is optional, but if specified, it must be one of
 * the named Java colors, hex RGB values, or hex RGBA values.
 */
public class Label extends AbstractChartCustomGraphics<LabelLayer> {
	private static final String COLOR = "color";
	// TODO
	private static final String ANGLE = "angle";
	private static final String ATTRIBUTE = "attribute";
	private static final String LABEL = "label";
	private static final String LABELOFFSET = "labeloffset";
	private static final String OUTLINE = "outline";
	private static final String OUTLINECOLOR = "outlineColor";
	private static final String SHADOW = "shadow";
	private static final String SHADOWCOLOR = "shadowColor";

	private Color color = null;
	private double labelAngle = 0.0;
	private String labelAttribute = null;
	private String label = null;
	private boolean shadowLabel = false;
	private boolean outlineLabel = false;
	private Color outlineColor = null;
	private Color shadowColor = null;

	// Parse the input string, which is always of the form:
	// label:
	//      [attribute=value]
	//      [color=value]
	//      [labelfont=value]
	//      [labellist=value]
	//      [labelsize=8]
	//      [labelstyle=plain]
	//      [network=current]
	//      [position=value]
	//      [scale=0.90]
	//      [label=value]
	public Label(String input) {
		Map<String, String> args = parseInput(input);

		// This will populate the values, attributes, and labels lists
		populateValues(args);

		if (args.containsKey(COLOR)) {
			color = parseColor(args.get(COLOR));
		}

		if (args.containsKey(ATTRIBUTE)) {
			labelAttribute = args.get(ATTRIBUTE);
		}

		if (args.containsKey(LABEL)) {
			label = args.get(LABEL);
		}

		if (args.containsKey(SHADOW)) {
			shadowLabel = getBooleanValue(args.get(SHADOW));
		}

		if (args.containsKey(SHADOWCOLOR)) {
			shadowColor = parseColor(args.get(SHADOWCOLOR));
			// Is the color opaque?  If so, make it translucent
			if (shadowColor.getAlpha() == 255) {
				shadowColor = new Color(shadowColor.getRed(), 
				                        shadowColor.getGreen(), 
				                        shadowColor.getBlue(), 125);
			}
		} else {
			shadowColor = new Color(255,255,255,125);
		}

		if (args.containsKey(OUTLINE)) {
			outlineLabel = getBooleanValue(args.get(OUTLINE));
		}

		if (args.containsKey(OUTLINECOLOR)) {
			outlineColor = parseColor(args.get(OUTLINECOLOR));
		}

		if (args.containsKey(ANGLE)) {
			labelAngle = getDoubleValue(args.get(ANGLE));
		}

	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	// public Image getRenderedImage() { return null; }

	public List<LabelLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { 
		CyNetwork network = networkView.getModel();
		if (!(nodeView.getModel() instanceof CyNode))
				return null;
		layers = new ArrayList<>();
		CyNode node = (CyNode)nodeView.getModel();

		double nodeWidth = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		double nodeHeight = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);

		Rectangle2D initialBox = new Rectangle2D.Double(0.0, 0.0, nodeWidth, nodeHeight);

		if (color == null) color = Color.BLACK;

		if (labelAttribute != null) {
			label = getLabelFromAttribute (network, node, labelAttribute);
		}

		Font font = getFont();
		List<LabelLayer> labelLayers = new ArrayList<>();
		if (label != null && label.length() > 0) {
			LabelLayer shadowLayer = null;

			// Create the label
			LabelLayer labelLayer = new LabelLayer(label, initialBox, position, anchor, font, 
			                                       color, outlineColor,
			                                       false, outlineLabel, labelAngle);

			// Create the shadow
			if (shadowLabel) {
				shadowLayer = new LabelLayer(label, initialBox, position, anchor, font, 
				                             shadowColor, outlineColor,
			                               true, false, labelAngle);
			}

			if (shadowLayer != null)
				labelLayers.add(shadowLayer);

			if (labelLayer != null)
				labelLayers.add(labelLayer);
		}
		shapeLayers = labelLayers;

		return labelLayers; 
	}

}
