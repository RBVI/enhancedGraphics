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
	private double rangeMax;
	private double rangeMin;
	private double maxValue;
	private double minValue;
	private double labelMin;
	private double ybase;
	private double bar;
	private double nBars;
	private double separation;
	private double scale;
	private boolean showYAxis = false;
	private boolean normalized = false;
	double strokeWidth = 0.01f;
	private double labelWidth = ViewUtils.DEFAULT_LABEL_WIDTH;
	private double labelSpacing = ViewUtils.DEFAULT_LABEL_LINE_SPACING;

	public BarLayer(int bar, int nbars, int separation, double value, 
	                double minValue, double maxValue, double rangeMin, double rangeMax, boolean normalized, double ybase, Color color,
									boolean showAxes, double borderWidth, double scale, Color borderColor) {
		labelLayer = false;
		this.color = color;
		this.bar = (double)bar;
		this.nBars = (double)nbars;
		this.separation = (double)separation;
		this.value = value;
		this.rangeMax = rangeMax;
		this.rangeMin = rangeMin;
		if (normalized) {
			this.minValue = Math.max(-1.0, minValue);
			this.maxValue = Math.min(1.0, maxValue);
		} else {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		this.ybase = ybase;
		this.showYAxis = showAxes;
		this.normalized = normalized;
		this.strokeWidth = borderWidth;
		this.scale = scale;
		this.strokeColor = borderColor;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
		// System.out.println("bar #"+bar+", value: "+value+", color: "+color+", minValue: "+minValue+", maxValue: "+maxValue);
	}

	public BarLayer(int bar, int nbars, int separation, double minValue, double maxValue, double rangeMin, double rangeMax, boolean normalized, 
	                double labelMin, double ybase, String label, Font font, Color labelColor, double labelWidth, double labelSpacing, boolean showAxes,
									double scale) {
		labelLayer = true;
		this.bar = (double)bar;
		this.nBars = (double)nbars;
		this.separation = (double)separation;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.labelWidth = labelWidth;
		this.labelSpacing = labelSpacing;
		this.strokeColor = labelColor;
		this.rangeMax = rangeMax;
		this.rangeMin = rangeMin;
		this.labelMin = labelMin;
		if (normalized) {
			this.minValue = Math.max(-1.0, minValue);
			this.maxValue = Math.min(1.0, maxValue);
		} else {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		this.ybase = ybase;
		this.showYAxis = showAxes;
		this.normalized = normalized;
		this.scale = scale;
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
		if (!labelLayer) {
			return new BasicStroke((float)strokeWidth);
		}
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
		this.bounds = newBounds.getBounds2D();
		return this;
	}

	private Shape barShape() {
		// System.out.println("sliceShape: bounds = "+bounds);
		System.out.println(value);
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
		Rectangle2D barShape = getBar(labelMin);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		Point2D labelPosition = new Point2D.Double(barShape.getCenterX(), barShape.getMaxY()+font.getSize()/2);

		Shape textShape = ViewUtils.getLabelShape(label, font, labelWidth, labelSpacing);

		double maxHeight = barShape.getWidth();

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, maxHeight, 0.0, 70.0);

		// If we're showing the axis, add the labels
		if (bar == 0 && showYAxis) {
			double minVal = minValue;
			String minLabel = String.valueOf(minValue);
			double maxVal = maxValue;
			String maxLabel = String.valueOf(maxValue);
			
			if(normalized) { // it means that a range was given
				// normalized means that the values ranges between -1 and 1
				// if values are all positive (or negatives), we put the min (or max) at 0
				minVal = rangeMin < 0 ? -1 : 0;
				minLabel = String.valueOf(rangeMin);
				maxVal = rangeMax > 0 ? 1 : 0;
				maxLabel = String.valueOf(rangeMax);
			}
			
			Area a = new Area(axisLabelShape(minVal, minLabel));
			a.add(new Area(axisLabelShape(maxVal, maxLabel)));
			if (minVal < 0.0 && maxVal > 0.0) {
				a.add(new Area(axisLabelShape(0.0, "0.0")));
			}
			if (textShape != null)
				a.add(new Area(textShape));
			return a;
		}

		return textShape;
	}

	private Shape axisLabelShape(double value, String label) {
		Rectangle2D bar = getBar(value);
		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_RIGHT;
		double y = bar.getY();
		if (value < 0.0)
			y = bar.getMaxY();
		Point2D labelPosition = new Point2D.Double(bar.getMinX()-font.getSize(), y);
		Shape textShape = ViewUtils.getLabelShape(label, font, labelWidth, labelSpacing);
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		return textShape;
	}

	private Rectangle2D getBar(double val) {
		double x = (bounds.getX()-bounds.getWidth()/2)*scale;
		double y = (bounds.getY()-bounds.getHeight()/2)*scale;
		double width = bounds.getWidth()*scale;
		double height = bounds.getHeight()*scale;

		// System.out.println("bar: "+bar+" is at "+x+","+y+" "+width+"x"+height);

		double sliceSize = (width - (nBars * separation) + separation)/nBars; // only have n-1 separators
		// System.out.println("sliceSize = "+sliceSize);
		if (sliceSize < 1.0 && separation > 0)
			sliceSize = width/nBars;

		// FIXME: really don't want to hard-code the stroke!
		// strokeWidth = (float)sliceSize/100.0f;

		// Account for the stroke
		sliceSize = sliceSize - strokeWidth;

		double min = minValue;
		double max = maxValue;

		if (Math.abs(max) > Math.abs(min))
			min = -1.0 * max;
		else
			max = -1.0 * min;

		double px1 = x + bar*(sliceSize + strokeWidth + separation);
		double py1;
		double h;

		if (val > 0.0) {
			py1 = y + (ybase * height) - ((ybase * height) * (val / max)) - strokeWidth;
			h = (ybase * height) * (val/max) + strokeWidth/4;
		} else {
			py1 = y + (ybase * height) - strokeWidth/4;
			h = (ybase * height) * (-val/max) - strokeWidth;
		}

		// System.out.println("px1 = "+px1+", py1 = "+py1+", sliceSize = "+sliceSize+", h = "+h+", strokeWidth = "+strokeWidth);
		return new Rectangle2D.Double(px1, py1, sliceSize, h);
	}

	private Area getAxes() {
		// At this point, make it simple -- a line at 0.0
		Rectangle2D firstBar = getBar(0.0);
		double saveBar = bar;
		bar = nBars-1;
		Rectangle2D lastBar = getBar(0.0);
		bar = saveBar;

		/*
		double x = firstBar.getX();
		double y = 0.0;
		double w = lastBar.getX()+lastBar.getWidth()-firstBar.getX();
		double h = 0.0;

		Rectangle2D xAxes = new Rectangle2D.Double(x, y, w, h);
		return new Area(xAxes);
		*/

		Path2D xAxes = new Path2D.Double();
		xAxes.moveTo(firstBar.getX(), 0.0);
		xAxes.lineTo(lastBar.getX()+lastBar.getWidth(), 0.0);
		xAxes.lineTo(lastBar.getX()+lastBar.getWidth(), 0.0);
		xAxes.lineTo(firstBar.getX(), 0.0);
		xAxes.closePath();

		if (showYAxis) {
			Rectangle2D bottom = getBar(minValue);
			Rectangle2D top = getBar(maxValue);
			xAxes.moveTo(bottom.getX(), bottom.getMaxY());
			xAxes.lineTo(top.getX(), top.getMinY());
		}
		return new Area(xAxes);
	}

}
