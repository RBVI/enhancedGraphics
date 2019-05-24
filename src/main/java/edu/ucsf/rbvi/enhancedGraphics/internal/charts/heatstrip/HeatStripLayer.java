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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class HeatStripLayer implements PaintedShape {
	private boolean labelLayer = false;
	private String label;
	private Color[] colorScale;
	private Font font;
	protected Rectangle2D bounds;
	private double value;
	private double rangeMax;
	private double rangeMin;
	private double maxValue;
	private double minValue;
	private double labelMin;
	private double scale;
	private double bar;
	private double nBars;
	private double separation;
	private boolean showYAxis = false;
	private boolean normalized = false;
	double strokeWidth = 0.5f;
	Color strokeColor = Color.BLACK;
	float[] dist3 = {0.0f, 0.5f, 1.0f};
	float[] dist2 = {0.0f, 1.0f};
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public HeatStripLayer(int bar, int nbars, int separation, double value, 
	                      double minValue, double maxValue, boolean normalized, 
	                      Color[] colorScale, boolean showAxes, double borderWidth, double scale, Color borderColor) {
		labelLayer = false;
		this.colorScale = colorScale;
		this.bar = (double)bar;
		this.nBars = (double)nbars;
		this.separation = (double)separation;
		this.value = value;
		this.rangeMax = maxValue;
		this.rangeMin = minValue;
		this.normalized = normalized;
		this.strokeWidth = borderWidth;
		this.strokeColor = borderColor;
		this.scale = scale;
		if (normalized) {
			this.minValue = -1.0;
			this.maxValue = 1.0;
		} else {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		this.showYAxis = showAxes;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
	}

	public HeatStripLayer(int bar, int nbars, int separation, double minValue, double maxValue,
	                      boolean normalized, double labelMin, String label, Font font, boolean showAxes, 
												double scale) {
		labelLayer = true;
		this.bar = bar;
		this.nBars = nbars;
		this.separation = separation;
		this.label = label;
		this.font = font;
		this.rangeMax = maxValue;
		this.rangeMin = minValue;
		this.normalized = normalized;
		this.scale = scale;
		if (normalized) {
			this.minValue = -1.0;
			this.maxValue = 1.0;
		} else {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		this.showYAxis = showAxes;
		this.labelMin = labelMin;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
	}

	public Paint getPaint() {
		if (labelLayer)
			return Color.BLACK;
		return createGradPaint();
	}

	public Paint getPaint(Rectangle2D bounds) {
		if (labelLayer)
			return Color.BLACK;
		return createGradPaint();
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
			return new BasicStroke((float)strokeWidth);
		return null;
	}

	public Paint getStrokePaint() {
		return this.strokeColor;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public HeatStripLayer transform(AffineTransform xform) {
		Shape newBounds = xform.createTransformedShape(bounds);
		this.bounds = newBounds.getBounds2D();
		return this;
	}

	private Shape barShape() {
		Shape strip = getHeatStrip(value);

		// If this is our first bar, draw our axes
		if (bar == 0) {
			Area axes = getAxes();
			axes.add(new Area(strip));
			return axes;
		}

		return strip;
	}

	private Shape labelShape() {
		// Get a bar that's in the right position and the maximum height
		Rectangle2D barShape = getHeatStrip(labelMin);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		Point2D labelPosition = new Point2D.Double(barShape.getCenterX(), barShape.getMaxY()+font.getSize()/2);

		Shape textShape = ViewUtils.getLabelShape(label, font);

		double maxHeight = barShape.getWidth();

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, maxHeight, 0.0, 70.0);

		// If we're showing the axis, add the labels
		if (bar == 0 && showYAxis) {
			Area a = new Area(axisLabelShape(minValue));
			a.add(new Area(axisLabelShape(maxValue)));
			if (minValue < 0.0 && maxValue > 0.0)
				a.add(new Area(axisLabelShape(0.0)));
			if (textShape != null)
				a.add(new Area(textShape));
			return a;
		}

		return textShape;
	}

	private Rectangle2D getHeatStrip(double val) {
		double x = (bounds.getX()-bounds.getWidth()/2)*scale;
		double y = (bounds.getY()-bounds.getHeight()/2)*scale;
		double width = bounds.getWidth()*scale;
		double height = bounds.getHeight()*scale;

		double yMid = y + (0.5 * height);
		double sliceSize = (width - (nBars * separation) + separation)/nBars; // only have n-1 separators
		if (sliceSize < 1.0 && separation > 0) {
			sliceSize = width/nBars;
			separation = 0;
		} 

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
			py1 = y + (0.5 * height) - ((0.5 * height) * (val / max)) - strokeWidth;
			h = (0.5 * height) * (val/max) + strokeWidth/4;
		} else {
			py1 = y + (0.5 * height) - strokeWidth/4;
			h = (0.5 * height) * (-val/max) - strokeWidth;
		}

		return new Rectangle2D.Double(px1, py1, sliceSize, h);
	}

	private Paint createGradPaint() {
		double x = bounds.getX()-bounds.getWidth()/2;
		double y = bounds.getY()-bounds.getHeight()/2;
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		if (colorScale.length == 2)
			return new LinearGradientPaint((float)x, (float)(y+height),
			                               (float)x, (float)y, dist2, colorScale);
		else
			return new LinearGradientPaint((float)x, (float)(y+height),
			                               (float)x, (float)y, dist3, colorScale);
	}

	private Area getAxes() {
		// At this point, make it simple -- a line at 0.0
		Rectangle2D firstBar = getHeatStrip(0.0);
		double saveBar = bar;
		bar = nBars-1;
		Rectangle2D lastBar = getHeatStrip(0.0);
		bar = saveBar;

		Path2D xAxes = new Path2D.Double();
		xAxes.moveTo(firstBar.getX(), 0.0);
		xAxes.lineTo(lastBar.getX()+lastBar.getWidth(), 0.0);
		xAxes.lineTo(lastBar.getX()+lastBar.getWidth(), 0.0);
		xAxes.lineTo(firstBar.getX(), 0.0);
		xAxes.closePath();

		if (showYAxis) {
			Rectangle2D bottom = getHeatStrip(minValue);
			Rectangle2D top = getHeatStrip(maxValue);
			xAxes.moveTo(bottom.getX(), bottom.getMaxY());
			xAxes.lineTo(top.getX(), top.getMinY());
		}
		return new Area(xAxes);

	}

	private Shape axisLabelShape(double value) {
		Rectangle2D bar = getHeatStrip(value);
		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_RIGHT;
		double y = bar.getY();
		if (value < 0.0)
			y = bar.getMaxY();
		Point2D labelPosition = new Point2D.Double(bar.getMinX()-font.getSize(), y);
		Shape textShape = null;
		if (normalized) {
			if (value == minValue) {
				textShape = ViewUtils.getLabelShape(Double.toString(rangeMin), font);
			} else if (value == maxValue) {
				textShape = ViewUtils.getLabelShape(Double.toString(rangeMax), font);
			} else {
				textShape = ViewUtils.getLabelShape(Double.toString(value), font);
			}
		} else {
			textShape = ViewUtils.getLabelShape(Double.toString(value), font);
		}
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		return textShape;
	}

}
