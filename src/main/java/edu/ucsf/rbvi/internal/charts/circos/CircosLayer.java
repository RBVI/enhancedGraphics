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
	private double maxRadius = 0;
	private double labelY = 0;
	protected Rectangle2D bounds;

	public CircosLayer(double radiusStart, double circleWidth, double arcStart, double arc, Color color, double strokeWidth) {
		labelLayer = false;
		this.arcStart = arcStart;
		this.arc = arc;
		this.color = color;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.strokeWidth = strokeWidth;
		bounds = new Rectangle2D.Double(0,0,100,100);
	}

	public CircosLayer(double radiusStart, double circleWidth, double arcStart, double arc, String label, Font font, Color labelColor) {
		labelLayer = true;
		labelSlice = true;
		this.arcStart = arcStart;
		this.arc = arc;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		bounds = new Rectangle2D.Double(0,0,100,100);
	}

	// Special version to label circles (not slices)
	public CircosLayer(double radiusStart, double circleWidth, double arcStart, String label, Font font, Color labelColor) {
		labelLayer = true;
		labelSlice = false;
		this.arcStart = arcStart;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.labelOffset = null;
		this.maxRadius = 0.0;
	}

	// Special version to label circles (not slices) but offset the labels to the left or right
	public CircosLayer(double radiusStart, double circleWidth, double arcStart, String label, Font font, 
	                   Color labelColor, Position labelOffset, double maxRadius, double labelY) {
		labelLayer = true;
		labelSlice = false;
		this.arcStart = arcStart;
		this.label = label;
		this.font = font;
		this.color = labelColor;
		this.strokeColor = labelColor;
		this.radiusStart = radiusStart;
		this.circleWidth = circleWidth;
		this.labelOffset = labelOffset;
		this.maxRadius = maxRadius;
		this.labelY = labelY;
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
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, arc, label, font, color);
		else if (labelLayer && !labelSlice)
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, label, font, color, labelOffset, maxRadius, labelY);
		else
			pl = new CircosLayer(radiusStart, circleWidth, arcStart, arc, color, strokeWidth);
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
		Arc2D innerSlice = new Arc2D.Double(x, y, width*radiusStart, height*radiusStart, arcStart+arc, -arc, Arc2D.OPEN);
		Point2D innerStart = innerSlice.getStartPoint();
		Point2D innerEnd = innerSlice.getEndPoint();

		// Create the outer arc
		x = bounds.getX()-bounds.getWidth()*radiusEnd/2;
		y = bounds.getY()-bounds.getHeight()*radiusEnd/2;
		Arc2D outerSlice = new Arc2D.Double(x, y, width*radiusEnd, height*radiusEnd, arcStart, arc, Arc2D.OPEN);
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
		double width = bounds.getWidth();
		double height = circleWidth*bounds.getHeight();
		double x = bounds.getX();
		double yCircle = bounds.getY() - bounds.getHeight()*radiusStart/2;
		double yLabel = labelY*bounds.getHeight()/2;

		// System.out.println("labelCircleWithOffset ("+label+"): x="+x+", y="+yLabel+" maxRadius="+maxRadius+", radiusStart = "+radiusStart);

		ViewUtils.TextAlignment tAlign = ViewUtils.TextAlignment.ALIGN_CENTER;

		if (labelOffset == Position.WEST) {
			x = x - (maxRadius - circleWidth)*bounds.getWidth()/2;
			tAlign = ViewUtils.TextAlignment.ALIGN_RIGHT;
		} else if (labelOffset == Position.EAST) {
			x = x + (maxRadius + circleWidth)*bounds.getWidth()/2;
			tAlign = ViewUtils.TextAlignment.ALIGN_LEFT;
		}

		Shape textShape = ViewUtils.getLabelShape(label, font);

		Rectangle2D labelBounds = new Rectangle2D.Double(x, yLabel, width, height);
		Point2D labelPosition = new Point2D.Double(x,yLabel);
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);

		x = bounds.getX()-bounds.getWidth()*(radiusStart-circleWidth)/2;
		double y = bounds.getY()-bounds.getHeight()*(radiusStart-circleWidth)/2;

		// Draw label lines
		// We want to draw from the end of the label to the circle we're labeling.  We do this by
		// calculating the unit vector from the label to the center of the circle and then get
		// the endpoint of a vector of the desire length
		Point2D labelLineStart = ViewUtils.getLabelLineStart(textShape.getBounds2D(), tAlign);

		// Calculate the length of the line to the origin
		double length = Math.sqrt(Math.pow(labelLineStart.getX()-bounds.getX(), 2)+Math.pow(labelLineStart.getY()-bounds.getY(), 2));

		// Calculate the unit vector
		Point2D unitVector = new Point2D.Double((labelLineStart.getX()-bounds.getX())/length, 
		                                        (labelLineStart.getY()-bounds.getY())/length);

		// Get the adjusted length
		length = (radiusStart-circleWidth/2)*bounds.getWidth()/2;

		// Get the position of the end or our line
		Point2D lineEnd = new Point2D.Double(unitVector.getX()*length, unitVector.getY()*length);

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

		Shape textShape = ViewUtils.getLabelShape(label, font);

		// Point2D labelPosition = getLabelPosition(labelBounds, midpointAngle, 1.0);
		Point2D labelPosition = new Point2D.Double(x,y);

		// textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		return textShape;
	}

	private Shape labelShape() {
		// System.out.println("labelShape: bounds = "+bounds);
		double midpointAngle = arcStart + arc/2;

		ViewUtils.TextAlignment tAlign = getLabelAlignment(midpointAngle);

		Shape textShape = ViewUtils.getLabelShape(label, font);

		Point2D labelPosition = getLabelPosition(bounds, midpointAngle, 1.7);

		textShape = ViewUtils.positionLabel(textShape, labelPosition, tAlign, 0.0, 0.0, 0.0);
		if (textShape == null) {
			return null;
		}

		labelPosition = getLabelPosition(bounds, midpointAngle, 1.0);
		Shape labelLine = ViewUtils.getLabelLine(textShape.getBounds2D(), labelPosition, tAlign);

		// Combine the shapes
		Area textArea = new Area(textShape);
		textArea.add(new Area(labelLine));
		return textArea;
	}

	// Return a point on the midpoint of the arc
	private Point2D getLabelPosition(Rectangle2D bbox, double angle, double scale) {
		double midpoint = Math.toRadians(360.0-angle);
		double w = bbox.getWidth()/2*scale;
		double h = bbox.getHeight()/2*scale;
		double x, y;
		// Special case 90 and 270
		if (angle == 270.0) {
			x = 0.0;
			y = h;
		} else if (angle == 90.0) {
			x = 0.0;
			y = -h;
		} else {
			x = Math.cos(midpoint)*w;
			y = Math.sin(midpoint)*h;
		}

		return new Point2D.Double(x, y);
	}

	private ViewUtils.TextAlignment getLabelAlignment(double midPointAngle) {
		if (midPointAngle >= 280.0 && midPointAngle < 80.0)
			return ViewUtils.TextAlignment.ALIGN_LEFT;

		if (midPointAngle >= 80.0 && midPointAngle < 100.0)
			return ViewUtils.TextAlignment.ALIGN_CENTER_TOP;

		if (midPointAngle >= 100.0 && midPointAngle < 260.0)
			return ViewUtils.TextAlignment.ALIGN_RIGHT;

		if (midPointAngle >= 260.0 && midPointAngle < 280.0)
			return ViewUtils.TextAlignment.ALIGN_CENTER_BOTTOM;

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
