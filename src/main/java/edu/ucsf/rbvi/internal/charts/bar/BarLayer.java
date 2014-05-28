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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class BarLayer implements PaintedShape {
	private boolean labelLayer = false;
	private String label;
	private Color color;
	private Color strokeColor = Color.BLACK;
	private Font font;
	protected Rectangle2D bounds;
	private double value;
	private double maxValue;
	private double minValue;
	private double ybase;
	private int bar;
	private int nBars;
	private int separation;
	float strokeWidth = 0.5f;

	public BarLayer(int bar, int nbars, int separation, double value, 
	                double minValue, double maxValue, double ybase, Color color) {
		labelLayer = false;
		this.color = color;
		this.bar = bar;
		this.nBars = nbars;
		this.separation = separation;
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.ybase = ybase;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
		// System.out.println("bar #"+bar+", value: "+value+", color: "+color+", minValue: "+minValue+", maxValue: "+maxValue);
	}

	public BarLayer(int bar, int nbars, int separation, double minValue, double maxValue,
	                double ybase, String label, Font font, Color labelColor) {
		labelLayer = true;
		this.bar = bar;
		this.nBars = nbars;
		this.separation = separation;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.ybase = ybase;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
		// System.out.println("bar #"+bar+", value: "+value+", color: "+color+", minValue: "+minValue+", maxValue: "+maxValue);
	}

	public Paint getPaint() {
		return color;
	}

	public Paint getPaint(Rectangle2D bounds) {
		return color;
	}

	public Shape getShape() {
		// create the slice or the label, as appropriate
		if (labelLayer)
			return labelShape();
		else
			return barShape();
	}

	public Stroke getStroke() {
		// We only stroke the slice
		if (!labelLayer)
			return new BasicStroke(strokeWidth);
		return null;
	}

	public Paint getStrokePaint() {
		return strokeColor;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public BarLayer transform(AffineTransform xform) {
		Shape newBounds = xform.createTransformedShape(bounds);
		// System.out.println("transformed bounds: "+newBounds.getBounds2D());
		BarLayer bl;
		if (labelLayer)
			bl = new BarLayer(bar, nBars, separation, minValue, maxValue, ybase, label, font, color);
		else 
			bl = new BarLayer(bar, nBars, separation, value, minValue, maxValue, ybase, color);
		bl.bounds = newBounds.getBounds2D();
		return bl;
	}

	private Shape barShape() {
		// System.out.println("sliceShape: bounds = "+bounds);
		Shape barShape = getBar(value);

		// If this is our first bar, draw our axes
		if (bar == 0) {
			Area axes = getAxes();
			axes.add(new Area(barShape));
			return axes;
		}
		return barShape;

	}

	private Shape labelShape() {
		// Get a bar that's in the right position and the maximum height
		Rectangle2D bar = getBar(minValue);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		Point2D labelPosition = new Point2D.Double(bar.getCenterX(), bar.getMaxY()+font.getSize()/2);

		Shape textShape = ViewUtils.getLabelShape(label, font);

		double maxHeight = bar.getWidth();

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, maxHeight, 0.0, 70.0);
		if (textShape == null) {
			return null;
		}

		return textShape;
	}

	private Rectangle2D getBar(double val) {
		double x = bounds.getX()-bounds.getWidth()/2;
		double y = bounds.getY()-bounds.getHeight()/2;
		double width = bounds.getWidth();
		double height = bounds.getHeight();

		double sliceSize = (width - (nBars * separation) + separation)/nBars; // only have n-1 separators
		if (sliceSize < 1.0 && separation > 0)
			sliceSize = width/nBars;

		// Account for the stroke
		sliceSize = sliceSize - sliceSize/10.0;
		strokeWidth = (float)sliceSize/20.0f;

		double min = minValue;
		double max = maxValue;

		if (Math.abs(max) > Math.abs(min))
			min = -1.0 * max;
		else
			max = -1.0 * min;

		double px1 = x + bar*sliceSize;
		double py1 = y + (ybase * height);
		// System.out.println("y = "+y+", py1 = "+py1);

		if (val > 0.0)
			py1 = py1 - ((ybase * height) * (val / max));
		else
			val = -val;

		double h = (ybase * height) * (val/max);
		// System.out.println("px1 = "+px1+", py1 = "+py1+", sliceSize = "+sliceSize+", h = "+h);
		return new Rectangle2D.Double(px1, py1, sliceSize, h);
	}

	private Area getAxes() {
		// At this point, make it simple -- a line at 0.0
		Rectangle2D firstBar = getBar(0.0);
		int saveBar = bar;
		bar = nBars-1;
		Rectangle2D lastBar = getBar(0.0);
		bar = saveBar;

		Path2D xAxes = new Path2D.Double();
		xAxes.moveTo(firstBar.getX(), firstBar.getY());
		xAxes.lineTo(lastBar.getX()+lastBar.getWidth(), lastBar.getY());
		BasicStroke stroke = new BasicStroke(0.5f/2.0f);
		return new Area(stroke.createStrokedShape(xAxes));
	}

}
