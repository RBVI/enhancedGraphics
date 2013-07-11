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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import edu.ucsf.rbvi.enhancedcg.internal.charts.ViewUtils;

public class LineLayer implements PaintedShape {
	private boolean labelLayer = false;
	private String label;
	private Color color;
	private int fontSize;
	protected Rectangle2D bounds;
	private double value1;
	private double value2;
	private double maxValue;
	private double minValue;
	private float lineWidth;
	private int point;
	private int nPoints;

	public LineLayer(int point, int nPoints, double value1, double value2,
	                double minValue, double maxValue, Color color, 
	                float lineWidth) {
		labelLayer = false;
		this.color = color;
		this.point = point;
		this.nPoints = nPoints;
		this.value1 = value1;
		this.value2 = value2;
		this.lineWidth = lineWidth;
		this.minValue = minValue;
		this.maxValue = maxValue;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
	}

	public LineLayer(int point, int nPoints, double minValue, double maxValue,
	                String label, int fontSize) {
		labelLayer = true;
		this.point = point;
		this.nPoints = nPoints;
		this.label = label;
		this.fontSize = fontSize;
		this.color = Color.BLACK;
		this.minValue = minValue;
		this.maxValue = maxValue;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
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
			return lineShape();
	}

	public Stroke getStroke() {
		// We only stroke the slice
		if (!labelLayer)
			return new BasicStroke(lineWidth);
		return null;
	}

	public Paint getStrokePaint() {
		return color;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public LineLayer transform(AffineTransform xform) {
		Shape newBounds = xform.createTransformedShape(bounds);
		LineLayer bl;
		if (labelLayer)
			bl = new LineLayer(point, nPoints, minValue, maxValue, label, fontSize);
		else 
			bl = new LineLayer(point, nPoints, value1, value2, 
			                   minValue, maxValue, color, lineWidth);
		bl.bounds = newBounds.getBounds2D();
		return bl;
	}

	private Shape lineShape() {
		// System.out.println("sliceShape: bounds = "+bounds);
		return getLine(value1, value2);
	}

	private Shape labelShape() {
/*
		// Get a line that's in the right position and the maximum height
		Rectangle2D line = getLine(minValue);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		Point2D labelPosition = new Point2D.Double(line.getCenterX(), line.getMaxY()+fontSize/2);

		Shape textShape = ViewUtils.getLabelShape(label, null, 0, fontSize);

		double maxHeight = line.getWidth();

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, maxHeight, 0.0, 70.0);
		if (textShape == null) {
			return null;
		}

		return textShape;
*/
		return null;
	}

	private Line2D getLine(double val1, double val2) {
		double x = bounds.getX()-bounds.getWidth()/2;
		double y = bounds.getY()-bounds.getHeight()/2;
		double width = bounds.getWidth();
		double height = bounds.getHeight();

		double yMid = y + (0.5 * height);
		double pointSize = width / (nPoints-1);

		double divisor = 1.0d;
		if (maxValue > minValue)
			divisor = (maxValue-minValue) / (height-1.0);

		double px1 = x + point*pointSize;
		double px2 = x + (point+1)*pointSize;
		double py1 = y + (height-((val1-minValue)/divisor));
		double py2 = y + (height-((val2-minValue)/divisor));

		return new Line2D.Double(px1, py1, px2, py2);
	}

}
