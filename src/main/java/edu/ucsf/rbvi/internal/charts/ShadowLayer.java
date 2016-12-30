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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class ShadowLayer implements PaintedShape {
	protected Rectangle2D bounds;
	protected BufferedImage image;
	protected Shape shape;

	public ShadowLayer(Shape shape, int size) {
		this.bounds = shape.getBounds2D();
		this.shape = ViewUtils.copyShape(shape);
		this.image = ViewUtils.getShadow(this.shape, (int)(bounds.getHeight()*0.05));
	}

	public Paint getPaint() {
		return new TexturePaint(image, bounds);
	}

	public Paint getPaint(Rectangle2D bounds) {
		return new TexturePaint(image, bounds);
	}

	public Shape getShape() {
		return bounds;
	}

	public Stroke getStroke() {
		return null;
	}

	public Paint getStrokePaint() {
		return null;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public ShadowLayer transform(AffineTransform xform) {
		Shape newShape = xform.createTransformedShape(shape);
		this.bounds = newShape.getBounds2D();
		this.image = ViewUtils.getShadow(newShape, (int)(bounds.getHeight()*0.05));
		this.shape = newShape;
		return this;
	}
}
