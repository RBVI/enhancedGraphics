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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils.TextAlignment;

public class LabelLayer implements PaintedShape {
	private String label;
	private Color color;
	private double maxWidth;
	private double lineSpacing = ViewUtils.DEFAULT_LABEL_LINE_SPACING;
	private ViewUtils.TextAlignment labelAlignment;
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
	private Point2D offset;
	private double paddingScale;

	public LabelLayer(String label, Rectangle2D bbox, Object position, Object anchor, Point2D offset,
	                  Font font, ViewUtils.TextAlignment labelAlignment, Color labelColor, Color outlineColor, double outlineWidth,
	                  boolean shadow, boolean outline, double angle, boolean rescale, double maxWidth, double lineSpacing, double padding) {
		this.label = label;
		this.font = font;
		this.labelAlignment = labelAlignment;
		this.color = labelColor;
		this.nodeBox = bbox;
		this.position = position;
		this.anchor = anchor;
		this.offset = offset;
		this.outlineColor = outlineColor;
		this.outlineWidth = outlineWidth;
		this.shadow = shadow;
		this.rescale = rescale;
		this.maxWidth = maxWidth;
		this.lineSpacing = lineSpacing;
		this.paddingScale = padding;

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
		LabelLayer copy = new LabelLayer(label, nodeBox, position, anchor, offset, font, labelAlignment, 
		                                 color, outlineColor, outlineWidth, shadow, outline, angle, rescale, maxWidth, lineSpacing, paddingScale);
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
		// ML:
//		return bounds;
		return this.getShape().getBounds2D();
	}

	public LabelLayer transform(AffineTransform xform) {
		bounds = xform.createTransformedShape(bounds).getBounds2D();
		labelShape = ViewUtils.createPossiblyTransformedShape(xform, labelShape, rescale);
		return this;
	}

	// Original copy from ViewUtils
	// Modified by Marc Legeay
	// - Deleted the maxWidth, maxHeight and rotation unused parameters
	//
	// The starting setup is this one:
	// 	- - - - - -
	// |           |
	// |  X-----   |
	// |  |text |  |
	// |   -----   |
	// |           |
	//  - - - - - - 
	// Where 'X' is the position point
	//
	private Shape positionLabel(Shape lShape, Point2D position, TextAlignment tAlign, double padding) {

		// Figure out how to move the text to center it on the bbox
		double textWidth = lShape.getBounds2D().getWidth(); 
		double textHeight = lShape.getBounds2D().getHeight();

		// System.out.println("  Text size = ("+textWidth+","+textHeight+")");

		double pointX = position.getX();
		double pointY = position.getY();

		double textStartX = pointX;
		double textStartY = pointY;

		switch (tAlign) {
		case ALIGN_CENTER:
			// This is the same as CENTER_TOP
			// System.out.println("  Align = CENTER");
		case ALIGN_CENTER_TOP:
			// 	- - - - - -
			// |            |
			// |   --X--    |
			// |  |text |   |
			// |   -----    |
			// |            |
			//  - - - - - - 
			
			// System.out.println("  Align = CENTER_TOP");
			textStartX = pointX - textWidth/2; // we center horizontally
			textStartY = pointY + padding; // the text box is already under the point
			break;
		case ALIGN_CENTER_BOTTOM:
			// 	- - - - - -
			// |            |
			// |   -----    |
			// |  |text |   |
			// |   --X--    |
			// |            |
			//  - - - - - - 
			
			// System.out.println("  Align = CENTER_BOTTOM");
			textStartX = pointX - textWidth/2; // we center horizontally
			textStartY = pointY - textHeight - padding; // initially the position point is on top of the shape
			break;
		case ALIGN_RIGHT:
			// 	- - - - - -
			// |            |
			// |   -----X   |
			// |  |text |   |
			// |   -----    |
			// |            |
			//  - - - - - - 
			
			// System.out.println("  Align = RIGHT");
			textStartX = pointX - textWidth - padding; // initially the position point is on the left of the shape
			textStartY = pointY + padding;
			break;
		case ALIGN_LEFT:
			// 	- - - - - -
			// |            |
			// |  X-----    |
			// |  |text |   |
			// |   -----    |
			// |            |
			//  - - - - - - 
			
			// System.out.println("  Align = LEFT");
			textStartX = pointX + padding; // initially the position point is already on the left of the shape
			textStartY = pointY + padding;
			break;
		case ALIGN_MIDDLE:
			// 	- - - - - -
			// |            |
			// |   -----    |
			// |  |teXt |   |
			// |   -----    |
			// |            |
			//  - - - - - - 
			
			// System.out.println("  Align = MIDDLE");
			textStartX = pointX - textWidth/2; // we center horizontally
			textStartY = pointY - textHeight/2; // we center vertically
			break;
		default:
			// System.out.println("  Align = "+tAlign);
		}

		// System.out.println("  Text bounds = "+lShape.getBounds2D());
		// System.out.println("  Position = "+position);

		if(offset != null) {
			textStartX += offset.getX();
			textStartY += offset.getY();
		}
		
		// System.out.println("  Offset = ("+textStartX+","+textStartY+")");

		// Use the bounding box to create an Affine transform.  We may need to scale the font
		// shape if things are too cramped, but not beneath some arbitrary minimum
		AffineTransform trans = new AffineTransform();
		trans.translate(textStartX, textStartY);

		// System.out.println("  Transform: "+trans);
		return trans.createTransformedShape(lShape);
	}

	private Shape labelShape() {
		// System.out.println("labelShape: bounds = "+bounds);

		// System.out.println("Label = "+label);
		// ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_MIDDLE;

		double pad = nodeBox.getWidth()*paddingScale;
		//double pad=0;
		
		// ML: debug
		Shape textShape = ViewUtils.getLabelShape(label, font, maxWidth-2*pad, lineSpacing);
		// ML We move the shape to be at 0,0
		AffineTransform replaceTrans = new AffineTransform();
		replaceTrans.translate(-textShape.getBounds2D().getX(), -textShape.getBounds2D().getY());
		textShape=replaceTrans.createTransformedShape(textShape);
		
		Rectangle2D textBounds = textShape.getBounds2D();
//		Rectangle2D paddedBounds = new Rectangle2D.Double(textBounds.getX()-pad, textBounds.getY()-pad,
//			                                                textBounds.getWidth()+pad*2,textBounds.getHeight()+pad*2);

		Point2D textBox = ViewUtils.positionAdjust(nodeBox, textBounds, position, anchor);
		if (textBox == null)
			textBox = new Point2D.Double(0.0,0.0);

		// ML: Do not use the Utils method anymore
//		textShape = ViewUtils.positionLabel(textShape, textBox, labelAlignment, 0.0, 0.0, 0.0);
		textShape = positionLabel(textShape, textBox, labelAlignment, pad);
		if (textShape == null) {
			return null;
		}

		if (shadow) {
			textBounds = textShape.getBounds2D();
			//pad = textBounds.getHeight()*.3;
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
