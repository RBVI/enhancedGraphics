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
package edu.ucsf.rbvi.enhancedGraphics.internal.charts.circos;


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
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils.Position;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.ViewUtils.TextAlignment;

public class CircosLayer implements PaintedShape {
	private boolean labelLayer = false;
	private double arcStart;
	private double arc;
	private double radiusStart; // % of bounds for inner arc
	private double circleWidth; // % of bounds for distance between inner and outer arcs
	private double strokeWidth; 
	private String label;
	private Color color;
	private Color strokeColor = Color.BLACK;
	private Font font;
	private boolean labelSlice = true;
	private Position labelOffset = null;
	private double labelWidth = ViewUtils.DEFAULT_LABEL_WIDTH;
	private double labelSpacing = ViewUtils.DEFAULT_LABEL_LINE_SPACING;
	private double maxRadius = 0;
	private int circle = 0;
	private int nCircles = 0;
	protected Rectangle2D bounds;
	private boolean isClockwise;

	public CircosLayer(double radiusStart, double circleWidth, double arcStart, 
	                   double arc, boolean isClockwise, Color color, double strokeWidth, Color strokeColor) {
		labelLayer = false;
		this.arcStart = arcStart;
		this.arc = arc;
		this.isClockwise = isClockwise;
		this.color = color;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.strokeWidth = strokeWidth;
		this.strokeColor = strokeColor;
		bounds = new Rectangle2D.Double(0,0,100,100);
	}

	public CircosLayer(double radiusStart, double circleWidth, double arcStart, double arc, boolean isClockwise, 
	                   String label, Font font, Color labelColor, double labelWidth, double labelSpacing) {
		labelLayer = true;
		labelSlice = true;
		this.arcStart = arcStart;
		this.arc = arc;
		this.isClockwise = isClockwise;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.labelWidth = labelWidth;
		this.labelSpacing = labelSpacing;
		this.strokeColor = labelColor;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		bounds = new Rectangle2D.Double(0,0,100,100);
	}

	// Special version to label circles (not slices)
	public CircosLayer(double radiusStart, double circleWidth, double arcStart, boolean isClockwise, String label, Font font, Color labelColor, double labelWidth, double labelSpacing) {
		labelLayer = true;
		labelSlice = false;
		this.arcStart = arcStart;
		this.isClockwise = isClockwise;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.labelWidth = labelWidth;
		this.labelSpacing = labelSpacing;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.labelOffset = null;
		this.maxRadius = 0.0;
	}

	// Special version to label circles (not slices) but offset the labels to the left or right
	public CircosLayer(double radiusStart, double circleWidth, double arcStart, boolean isClockwise, String label, Font font, 
	                   Color labelColor, double labelWidth, double labelSpacing, Position labelOffset, double maxRadius, int circle, int nCircles) {
		labelLayer = true;
		labelSlice = false;
		this.arcStart = arcStart;
		this.isClockwise = isClockwise;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.labelWidth = labelWidth;
		this.labelSpacing = labelSpacing;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.labelOffset = labelOffset;
		this.maxRadius = maxRadius;
		this.circle = circle;
		this.nCircles = nCircles;
		bounds = new Rectangle2D.Double(0,0,100,100);
	}

	public Paint getPaint() {
		return color;
	}

	public Paint getPaint(Rectangle2D bounds) {
		return color;
	}

	public Shape getShape() {
		// create the slice or the label, as appropriate
		if (labelLayer && labelSlice)
			return labelShape();
		else if (labelLayer && labelOffset != null)
			return labelCircleWithOffset();
		else if (labelLayer && !labelSlice)
			return labelCircle();
		else 
			return sliceShape();
	}

	public Stroke getStroke() {
		// We only stroke the slice
		if (!labelLayer && strokeWidth > 0.0)
			return new BasicStroke((float)strokeWidth);
		return null;
	}

	public Paint getStrokePaint() {
		return strokeColor;
	}

	public Rectangle2D getBounds2D() {
		return bounds;
	}

	public CircosLayer transform(AffineTransform xform) {
		Shape newShape = xform.createTransformedShape(bounds);
		Rectangle2D newBounds = newShape.getBounds2D();

		CircosLayer pl;
		if (labelLayer && labelSlice)
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, arc, isClockwise, label, font, color, labelWidth, labelSpacing);
		else if (labelLayer && !labelSlice)
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, isClockwise, label, font, color, labelWidth, labelSpacing, 
			                     labelOffset, maxRadius, circle, nCircles);
		else
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, arc, isClockwise, color, strokeWidth, strokeColor);
		pl.bounds = newBounds;
		return pl;
	}

	private Shape sliceShape() {
		double x = bounds.getX()-bounds.getWidth()*radiusStart/2;
		double y = bounds.getY()-bounds.getHeight()*radiusStart/2;
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		double radiusEnd = radiusStart+circleWidth;

		Path2D path = new Path2D.Double();

		// Create the inner arc
		Arc2D innerSlice;
		if(isClockwise) {
			innerSlice = new Arc2D.Double(x, y, width*radiusStart, height*radiusStart, arcStart, -arc, Arc2D.OPEN);
		} else {
			innerSlice = new Arc2D.Double(x, y, width*radiusStart, height*radiusStart, arcStart+arc, -arc, Arc2D.OPEN);
		}
		Point2D innerStart = innerSlice.getStartPoint();
		Point2D innerEnd = innerSlice.getEndPoint();

		// Create the outer arc
		x = bounds.getX()-bounds.getWidth()*radiusEnd/2;
		y = bounds.getY()-bounds.getHeight()*radiusEnd/2;
		Arc2D outerSlice;
		if(isClockwise) {
			outerSlice = new Arc2D.Double(x, y, width*radiusEnd, height*radiusEnd, arcStart-arc, arc, Arc2D.OPEN);
		} else {
			outerSlice = new Arc2D.Double(x, y, width*radiusEnd, height*radiusEnd, arcStart, arc, Arc2D.OPEN);
		}
		Point2D outerStart = outerSlice.getStartPoint();
		Point2D outerEnd = outerSlice.getEndPoint();

		path.moveTo(innerStart.getX(), innerStart.getY());
		appendArc(path, innerSlice);
		path.lineTo(outerStart.getX(), outerStart.getY());
		appendArc(path, outerSlice);
		path.lineTo(innerStart.getX(), innerStart.getY());
		path.closePath();
		
		return path;
	}

	private Shape labelCircleWithOffset() {
		// bounds has the width and height of the node
		// bounds (x,y) is the center of the node
		
		// We try to align the labels so that the middle one is on the horizontal axis of the node
		double offset = nCircles/2-circle;
		
		// We assume that each label is on only one line
		// we compute the size of one line with a dummy label
		double lineHeight = ViewUtils.getLabelShape("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz-_,:|\"", font, Double.MAX_VALUE, 1).getBounds().getHeight();
		
		// the line height of each label is lineHeight
		// the label spacing is lineHeight*labelSpacing
		// we multiply it by the offset to know where to position this specific label
		double yLabel = offset*lineHeight*(1+labelSpacing);
		double x = bounds.getX();
		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_CENTER;

		// x (center of the node)
		// maxRadius*bounds.getWidth()/2 = distance between the node center and last circle
		// we add the circleWidth as a margin between the circle and the labels
		// (circleWidth is a ratio, the real width is circleWidth*bounds.getWidth()/2)
		if (labelOffset == Position.WEST) {
			x = x - (maxRadius + circleWidth)*bounds.getWidth()/2;
			tAlign = ViewUtils.TextAlignment.ALIGN_RIGHT;
		} else if (labelOffset == Position.EAST) {
			x = x + (maxRadius + circleWidth)*bounds.getWidth()/2;
			tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		}

		Shape textShape = ViewUtils.getLabelShape(label, font, labelWidth, labelSpacing);

		Point2D labelPosition = new Point2D.Double(x,yLabel);
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		
		// Draw label lines
		// We want to draw from the end of the label to the circle we're labeling.  We do this by
		// calculating the unit vector from the label to the center of the circle and then get
		// the endpoint of a vector of the desire length
		Point2D labelLineStart = ViewUtils.getLabelLineStart(textShape.getBounds2D(), tAlign);

		// In case of a non-circular node, we have an ellipse
		// The equation of the ellipse is : x^2/a^2 + y^2/b^2 = 1
		// where a and b are the width (resp. height) of the ellipse
		// Here we aim at the middle of the donut
		// We compute the intersection between this ellipse and the line
		// passing through (0,0) and (x0, y0)
		// where (x0, y0) is labelLineStart
		double a = (radiusStart-circleWidth/2) * bounds.getWidth()/2;
		double b = (radiusStart-circleWidth/2) * bounds.getHeight()/2;
		double x0 = labelLineStart.getX();
		double y0 = labelLineStart.getY();
		
		double discriminant = a*b/Math.sqrt(Math.pow(a, 2)*Math.pow(y0, 2) + Math.pow(b, 2)*Math.pow(x0, 2));

		// Get the position of the end or our line
		Point2D lineEnd = new Point2D.Double(x0*discriminant, y0*discriminant);

		// Create the line
		Shape labelLine = ViewUtils.getLabelLine(textShape.getBounds2D(), lineEnd, tAlign);

		// Combine the shapes
		Area textArea = new Area(textShape);
		textArea.add(new Area(labelLine));

		return textArea;
	}

	private Shape labelCircle() {
		double midpointAngle = 90.0;
		// double x = bounds.getX()-bounds.getWidth()*radiusStart/2;
		// double y = bounds.getY()-bounds.getHeight()*radiusStart/2;
		double width = bounds.getWidth();
		double height = circleWidth*bounds.getHeight();
		double x = bounds.getX();
		double y = bounds.getY() - bounds.getHeight()*radiusStart/2;
		Rectangle2D labelBounds = new Rectangle2D.Double(x, y, width, height);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_CENTER;

		Shape textShape = ViewUtils.getLabelShape(label, font, labelWidth, labelSpacing);

		// Point2D labelPosition = getLabelPosition(labelBounds, midpointAngle, 1.0);
		Point2D labelPosition = new Point2D.Double(x,y);

		// textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		return textShape;
	}

	private Shape labelShape() {
		// System.out.println("labelShape: bounds = "+bounds);
		double midpointAngle = arcStart + arc/2;
		if(isClockwise) {
			midpointAngle = arcStart - arc/2;
		}
		// double x = bounds.getX()-bounds.getWidth()*radiusStart/2;
		// double y = bounds.getY()-bounds.getHeight()*radiusStart/2;
		// Rectangle2D startPosition = new Rectangle2D.Double(x,y);
		//
		double width = bounds.getWidth()*(radiusStart+circleWidth/2);
		double height = bounds.getHeight()*(radiusStart+circleWidth/2);
		double x = bounds.getX();
		double y = bounds.getY();
		Rectangle2D labelBounds = new Rectangle2D.Double(x, y, width, height);

		ViewUtils.TextAlignment tAlign = getLabelAlignment(midpointAngle);

		Shape textShape = ViewUtils.getLabelShape(label, font, labelWidth, labelSpacing);

		Point2D labelPosition = getLabelPosition(labelBounds, midpointAngle, (1+circleWidth));

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		if (textShape == null) {
			return null;
		}

		labelPosition = getLabelPosition(labelBounds, midpointAngle, 1.0);
		Shape labelLine = ViewUtils.getLabelLine(textShape.getBounds2D(), labelPosition, tAlign);

		// Combine the shapes
		Area textArea = new Area(textShape);
		textArea.add(new Area(labelLine));
		return textArea;
	}

	// Return a point on the midpoint of the arc
	private Point2D getLabelPosition(Rectangle2D bbox, double angle, double scale) {
		double midpoint = Math.toRadians(angle);
		double w = bbox.getWidth()/2*scale;
		double h = bbox.getHeight()/2*scale;
		double x, y;

		x = Math.cos(midpoint)*w+bbox.getX();
		y = -Math.sin(midpoint)*h+bbox.getY(); // Java Y axis is from up to down, so we take the opposite of sinus
		
		return new Point2D.Double(x, y);
	}

	private ViewUtils.TextAlignment getLabelAlignment(double midPointAngle) {
		// We make sure angles are in [0;360[
		while(midPointAngle < 0) {
			midPointAngle += 360;
		}
		while(midPointAngle >= 360) {
			midPointAngle -= 360;
		}
		
		if (midPointAngle >= 280.0 || midPointAngle < 80.0)
			return ViewUtils.TextAlignment.ALIGN_LEFT;

		if (midPointAngle >= 80.0 && midPointAngle < 100.0)
			return ViewUtils.TextAlignment.ALIGN_CENTER_BOTTOM;

		if (midPointAngle >= 100.0 && midPointAngle < 260.0)
			return ViewUtils.TextAlignment.ALIGN_RIGHT;

		if (midPointAngle >= 260.0 && midPointAngle < 280.0)
			return ViewUtils.TextAlignment.ALIGN_CENTER_TOP;

		return ViewUtils.TextAlignment.ALIGN_LEFT;
	}

	private void sortSlicesBySize(List<Double>values, List<Color>colors, List<String>labels, double minimumSlice) {
		Double[] valueArray = values.toArray(new Double[1]);
		values.clear();
		Color[] colorArray = colors.toArray(new Color[1]);
		colors.clear();
		String[] labelArray = labels.toArray(new String[1]);
		labels.clear();
		
		Integer[] sortedIndex = new Integer[valueArray.length];
		for (int i = 0; i < valueArray.length; i++) sortedIndex[i] = new Integer(i);
		IndexComparator iCompare = new IndexComparator(valueArray);
		Arrays.sort(sortedIndex, iCompare);

		double otherValues = 0.0;
		
		// index now has the values in sorted order
		for (int index = valueArray.length-1; index >= 0; index--) {
			if (valueArray[sortedIndex[index]] >= minimumSlice) {
				values.add(valueArray[sortedIndex[index]]);
				colors.add(colorArray[sortedIndex[index]]);
				labels.add(labelArray[sortedIndex[index]]);
			} else {
				otherValues = otherValues + valueArray[sortedIndex[index]];
			}
		}

		if (otherValues > 0.0) {
			values.add(otherValues);
			colors.add(Color.LIGHT_GRAY);
			labels.add("Other");
		}
	}

	private void appendArc(Path2D path, Arc2D arc) {
		PathIterator pi = arc.getPathIterator(null);
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int pathType = pi.currentSegment(coords);

			switch(pathType) {
			case PathIterator.SEG_CLOSE:
			case PathIterator.SEG_MOVETO:
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				break;
			}

			pi.next();
		}
	}

  private class IndexComparator implements Comparator<Integer> {
    Double[] data = null;
    Integer[] intData = null;

    public IndexComparator(Double[] data) { this.data = data; }

    public IndexComparator(Integer[] data) { this.intData = data; }

    public int compare(Integer o1, Integer o2) {
      if (data != null) {
        if (data[o1.intValue()] < data[o2.intValue()]) return -1;
        if (data[o1.intValue()] > data[o2.intValue()]) return 1;
        return 0;
      } else if (intData != null) {
        if (intData[o1.intValue()] < intData[o2.intValue()]) return -1;
        if (intData[o1.intValue()] > intData[o2.intValue()]) return 1;
        return 0;
      }
      return 0;
    }
	}

}
