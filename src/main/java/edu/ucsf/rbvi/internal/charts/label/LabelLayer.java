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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class LabelLayer implements PaintedShape {
	private String label;
	private Color color;
	private Color outlineColor;
	private Font font;
	private boolean shadow,outline,rescale;
	private double angle,outlineWidth;
	private float strokeSize;
	protected Rectangle2D bounds;
	protected Rectangle2D nodeBox;
	private Shape labelShape;
	private Object position;
	private Object anchor;

	public LabelLayer(String label, Rectangle2D bbox, Object position, Object anchor,
	                  Font font, Color labelColor, Color outlineColor, double outlineWidth, 
	                  boolean shadow, boolean outline, double angle, boolean rescale) {
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.nodeBox = bbox;
		this.position = position;
		this.anchor = anchor;
		this.outlineColor = outlineColor;
		this.outlineWidth = outlineWidth;
		this.shadow = shadow;
		this.rescale = rescale;

		this.outline = outline;
		this.angle = angle;
		bounds = new Rectangle2D.Double(0,0,50,50);
		if (outline && outlineColor == null)
			outlineColor = Color.BLACK;

		if (outline) {
			strokeSize = (float)outlineWidth*font.getSize2D()/20f;
		}

		labelShape = labelShape();
	}

	public LabelLayer copy() {
		LabelLayer copy = new LabelLayer(label, nodeBox, position, anchor, font, 
		                                 color, outlineColor, outlineWidth, shadow, outline, angle, rescale);
		return copy;
	}

	public Paint getPaint() {
		return color;
	}

	public Paint getPaint(Rectangle2D bounds) {
		return color;
	}

	public Shape getShape() {
		return labelShape;
	}

	public Stroke getStroke() {
		if (outline) {
			return new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}
		return null;
	}

	public Paint getStrokePaint() {
		return outlineColor;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public LabelLayer transform(AffineTransform xform) {
		bounds = xform.createTransformedShape(bounds).getBounds2D();
		labelShape = ViewUtils.createPossiblyTransformedShape(xform, labelShape, rescale);
		return this;
	}

	private Shape labelShape() {
		// System.out.println("labelShape: bounds = "+bounds);

		// System.out.println("Label = "+label);
		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_MIDDLE;

		Shape textShape = ViewUtils.getLabelShape(label, font);
		Rectangle2D textBounds = textShape.getBounds2D();
		double pad = textBounds.getHeight()*.3;
		Rectangle2D paddedBounds = new Rectangle2D.Double(textBounds.getX()-pad, textBounds.getY()-pad,
			                                                textBounds.getWidth()+pad*2,textBounds.getHeight()+pad*2);

		Point2D textBox = ViewUtils.positionAdjust(nodeBox, paddedBounds, position, anchor);
		if (textBox == null)
			textBox = new Point2D.Double(0.0,0.0);

		textShape = ViewUtils.positionLabel(textShape, textBox, tAlign, 0.0, 0.0, 0.0);
		if (textShape == null) {
			return null;
		}

		if (shadow) {
			textBounds = textShape.getBounds2D();
			pad = textBounds.getHeight()*.3;
			RoundRectangle2D shadow = new RoundRectangle2D.Double(textBounds.getX()-pad, 
			                                                      textBounds.getY()-pad,
			                                                      textBounds.getWidth()+pad*2,
			                                                      textBounds.getHeight()+pad*2,
			                                                      pad*2,pad*2);
			return shadow;
		}

		return textShape;
	}
}
