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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.stripe;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class StripeLayer implements PaintedShape {
	private boolean labelLayer = false;
	private String label;
	private Color color;
	private int fontSize;
	protected Rectangle2D bounds;
	private int stripe;
	private int nStripes;

	public StripeLayer(int stripe, int nStripes, Color color) {
		labelLayer = false;
		this.color = color;
		this.stripe = stripe;
		this.nStripes = nStripes;
		bounds = new Rectangle2D.Double(0, 0, 100, 50);
	}

	public StripeLayer(int stripe, int nStripes, String label, int fontSize) {
		labelLayer = true;
		this.stripe = stripe;
		this.nStripes = nStripes;
		this.label = label;
		this.fontSize = fontSize;
		this.color = Color.BLACK;
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
			return stripeShape();
	}

	public Stroke getStroke() {
		// We only stroke the slice
		if (!labelLayer)
			return new BasicStroke(0.1f);
		return null;
	}

	public Paint getStrokePaint() {
		return Color.BLACK;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public StripeLayer transform(AffineTransform xform) {
		Shape newBounds = xform.createTransformedShape(bounds);
		StripeLayer bl;
		if (labelLayer)
			bl = new StripeLayer(stripe, nStripes, label, fontSize);
		else 
			bl = new StripeLayer(stripe, nStripes, color);
		bl.bounds = newBounds.getBounds2D();
		return bl;
	}

	private Shape stripeShape() {
		// System.out.println("sliceShape: bounds = "+bounds);
		return getStripe(stripe, nStripes);
	}

	private Shape labelShape() {
		return null;
	}

	private Rectangle2D getStripe(int stripe, int nStripes) {
		double x = bounds.getX()-bounds.getWidth()/2;
		double y = bounds.getY()-bounds.getHeight()/2;
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		// Create the stripe
		return new Rectangle2D.Double((x + (stripe * width/nStripes)), y, width/nStripes, height);
	}

}
