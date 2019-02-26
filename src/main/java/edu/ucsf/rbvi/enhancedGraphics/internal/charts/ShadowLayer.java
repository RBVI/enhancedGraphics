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
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import java.io.File;

import javax.imageio.ImageIO;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;

public class ShadowLayer implements Cy2DGraphicLayer {
	protected Rectangle2D bounds;
	protected Shape pShape;
	protected boolean rescale;
	protected double radius = 10.0;
	protected double upscale = 10.0;
	protected static int fileNumber = 0;

	public ShadowLayer(Shape shape, double offset, boolean rescale) {
		pShape = shape;
		bounds = new Rectangle2D.Double(0,0,50,50);
		this.rescale = rescale;
	}

	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		// Scale up the shape so we have room to create the gaussian
		AffineTransform scale = AffineTransform.getScaleInstance(upscale,upscale);
		Shape lShape = scale.createTransformedShape(pShape);
		Rectangle2D lShapeBounds = lShape.getBounds2D();

		// Create an image to hold the blur
		BufferedImage image = new BufferedImage((int)(lShapeBounds.getWidth()+radius*4),
		                                        (int)(lShapeBounds.getHeight()+radius*4),
																						BufferedImage.TYPE_INT_ARGB);

		AffineTransform trans = AffineTransform.getTranslateInstance(-lShapeBounds.getX()+radius*2.4,
		                                                             -lShapeBounds.getY()+radius*2.4);
		Shape gShape = trans.createTransformedShape(lShape);

		// Draw our shape
		Graphics2D g2 = image.createGraphics();
		g2.setPaint(getPaint());
		g2.draw(gShape);
		g2.fill(gShape);
		g2.dispose();

		/*
		try {
			ImageIO.write(image, "png", new File("/Users/scooter/beforeImage"+fileNumber+".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		// Blur it
		image = getGaussianBlurFilter((int)radius, true).filter(image, null);
		image = getGaussianBlurFilter((int)radius, false).filter(image, null);

		/*
		try {
			ImageIO.write(image, "png", new File("/Users/scooter/image"+fileNumber+".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileNumber++;
		*/

		// Now draw the shape on the canvas.
		g.drawImage(image, (int)(pShape.getBounds2D().getX()-radius*2/upscale), 
		                   (int)(pShape.getBounds2D().getY()-radius*2/upscale), 
		                   (int)(pShape.getBounds2D().getWidth()+(radius*4/upscale)+.5),
										   (int)(pShape.getBounds2D().getHeight()+(radius*4/upscale)+.5), null);
	}

	public Paint getPaint() {
		return new Color(0,0,0,150);
	}

	public Paint getPaint(Rectangle2D bounds) {
		return new Color(0,0,0,150);
	}

	public Shape getShape() {
		return pShape;
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
		bounds = xform.createTransformedShape(bounds).getBounds2D();
		pShape = ViewUtils.createPossiblyTransformedShape(xform, pShape, rescale);
		return this;
	}

	private BufferedImage changeImageWidth(BufferedImage image, int width) {
		float ratio = (float) image.getWidth() / (float) image.getHeight();
		int height = (int) (width / ratio);

		BufferedImage temp = new BufferedImage(width, height, image.getType());
		Graphics2D g2 = temp.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
		g2.dispose();

		return temp;
	}

	private ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
		if (radius < 1) {
			throw new IllegalArgumentException("Radius must be >= 1");
		}

		int size = radius * 2 + 1;
		float[] data = new float[size];

		float sigma = radius / 3.0f;
		float twoSigmaSquare = 2.0f * sigma * sigma;
		float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
		float total = 0.0f;

		for (int i = -radius; i <= radius; i++) {
			float distance = i * i;
			int index = i + radius;
			data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
			total += data[index];
		}

		for (int i = 0; i < data.length; i++) {
			data[i] /= total;
		}

		Kernel kernel = null;
		if (horizontal) {
			kernel = new Kernel(size, 1, data);
		} else {
			kernel = new Kernel(1, size, data);
		}
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}

}
