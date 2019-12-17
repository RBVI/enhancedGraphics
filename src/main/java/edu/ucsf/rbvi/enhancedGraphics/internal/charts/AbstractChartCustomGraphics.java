package edu.ucsf.rbvi.enhancedGraphics.internal.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;

import edu.ucsf.rbvi.enhancedGraphics.internal.AbstractEnhancedCustomGraphics;

abstract public class AbstractChartCustomGraphics<T extends CustomGraphicLayer> 
                extends AbstractEnhancedCustomGraphics<T> {
	// Standard command strings
	public static final String ANCHOR = "anchor";
	public static final String ALL = "all";
	public static final String ATTRIBUTELIST = "attributelist";
	public static final String BORDERWIDTH = "borderwidth";
	public static final String BORDERCOLOR = "bordercolor";
	public static final String CLEAR = "clear";
	public static final String CURRENT = "current";
	public static final String LABELCOLOR = "labelcolor";
	public static final String LABELFONT = "labelfont";
	public static final String LABELOFFSET = "labeloffset"; // TODO
	public static final String LABELSTYLE = "labelstyle";
	public static final String LABELSIZE = "labelsize";
	public static final String LABELWIDTH = "labelwidth";
	public static final String LABELSPACING = "labelspacing"; // line spacing
	public static final String LABELS = "labellist";
	public static final String LIST = "list";
	public static final String NETWORK = "network";
	public static final String POSITION = "position";
	public static final String RANGE = "range";
	public static final String SCALE = "scale";
	public static final String SHOWLABELS = "showlabels";
	public static final String SHOWYAXIS = "showyaxis";
	public static final String SHOWXAXIS = "showxaxis";
	public static final String SIZE = "size";
	public static final String VALUES = "valuelist";
	public static final String YBASE = "ybase";

	protected List<Double> values = null;
	protected List<String> labels = null;
	protected List<String> attributes = null;
	protected double rangeMax = 0.0;
	protected double rangeMin = 0.0;
	protected	double ybase = 0.5;
	protected	double scale = 1.0;
	protected	double borderWidth = 0.1;
	protected Color labelColor = Color.BLACK;
	protected Color borderColor = Color.BLACK;
	protected int labelSize = ViewUtils.DEFAULT_SIZE;
	protected String labelFont = ViewUtils.DEFAULT_FONT;
	protected int labelStyle = ViewUtils.DEFAULT_STYLE;
	protected double labelWidth = ViewUtils.DEFAULT_LABEL_WIDTH;
	protected double labelSpacing = ViewUtils.DEFAULT_LABEL_LINE_SPACING;
	protected boolean normalized = false;
	protected List<? extends CustomGraphicLayer> shapeLayers = null;
	protected Object position = null;
	protected Point2D labelOffset = null;
	protected Object anchor = null;

	@Override
	public Image getRenderedImage() {
		if (shapeLayers == null) return null;

		// First, get our bounding box
		Rectangle2D bounds = new Rectangle2D.Double();
		for (CustomGraphicLayer layer: shapeLayers) {
			// Shape shape = ps.getShape();
			bounds = bounds.createUnion(layer.getBounds2D());
		}

		// Now, create the image
		BufferedImage image = new BufferedImage((int)bounds.getWidth()*4, (int)bounds.getHeight()*4, 
		                                        BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.translate(-bounds.getX()*4, -bounds.getY()*4);
		g2.scale(4.0,4.0);
		for (CustomGraphicLayer layer: shapeLayers) {
			if (layer instanceof PaintedShape) {
				PaintedShape ps = (PaintedShape)layer;
				Shape shape = ps.getShape();
				if (ps.getStroke() != null) {
					Paint strokePaint = ps.getStrokePaint();
					if (strokePaint == null) strokePaint = Color.BLACK;
					g2.setPaint(strokePaint);
					g2.setStroke(ps.getStroke());
					g2.draw(shape);
				}
				g2.setPaint(ps.getPaint());
				g2.fill(shape);
			} else if (layer instanceof Cy2DGraphicLayer) {
			}
		}
		return image;
	}

	protected void populateValues(Map<String, String> args) {
		if (args.containsKey(RANGE)) {
			rangeMin = 0.0;
			rangeMax = 0.0;
			String split[] = args.get(RANGE).split(",");
			try {
				if (split.length == 2) {
					rangeMin = getDoubleValue(split[0]);
					rangeMax = getDoubleValue(split[1]);
				}
			} catch (NumberFormatException e) {
				rangeMin = 0.0;
				rangeMax = 0.0;
			}
			if (rangeMin == 0.0 && rangeMax == 0.0) {
				logger.warn("Unable to parse min/max values from '"+args.get(RANGE)+"'");	
			}
		}

		if (args.containsKey(BORDERWIDTH)) {
			try {
				borderWidth = getDoubleValue(args.get(BORDERWIDTH));
			} catch (NumberFormatException e) {
				logger.warn("Unable to parse border width from '"+args.get(BORDERWIDTH)+"'");	
			}
		}
		
		if(args.containsKey(BORDERCOLOR)) {
			borderColor = getColorValue(args.get(BORDERCOLOR));
		}

		values = null;
		if (args.containsKey(VALUES)) {
			// Get our values.  convertData returns an array of values in degrees of arc
			values = convertInputToDouble(args.get(VALUES));
			if (values == null) {
				logger.error("Cannot parse "+VALUES+" from input '"+args.get(VALUES)+"'");
				return;
			}
			if (rangeMax != 0.0 || rangeMin != 0.0) {
				values = normalize(values, rangeMin, rangeMax);
				normalized = true;
			}
		}

		labels = new ArrayList<String>();
		if (args.containsKey(LABELS)) {
			// Get our labels.  These may or may not be printed depending on options
			labels = getStringList(args.get(LABELS));
			if (labels == null) {
				logger.error("Cannot parse "+LABELS+" from input '"+args.get(LABELS)+"'");
				return;
			}
		}

		boolean showLabels = true;
		if (args.containsKey(SHOWLABELS)) {
			showLabels = getBooleanValue(args.get(SHOWLABELS));
		}

		// Font information
		if (args.containsKey(LABELSIZE))
			labelSize = getIntegerValue(args.get(LABELSIZE));

		if (args.containsKey(LABELFONT))
			labelFont = args.get(LABELFONT);

		if (args.containsKey(LABELSTYLE))
			labelStyle = getFontStyle(args.get(LABELSTYLE));

		if (args.containsKey(LABELWIDTH))
			labelWidth = getDoubleValue(args.get(LABELWIDTH));

		if (args.containsKey(LABELSPACING))
			labelSpacing = getDoubleValue(args.get(LABELSPACING));

		if (args.containsKey(LABELCOLOR))
			labelColor = getColorValue(args.get(LABELCOLOR));

		if (args.containsKey(SCALE)) {
			try {
				scale = getDoubleValue(args.get(SCALE));
			} catch (NumberFormatException e) {
				return;
			}
		}

		// Get our position
		if (args.containsKey(POSITION)) {
			String pos = args.get(POSITION);
			position = ViewUtils.getPosition(pos);
			if (position == null) {
				if (pos.indexOf(",") > 0) {
					String[] point = pos.split(",");
					if (point.length == 2) {
						double x = Double.parseDouble(point[0]);
						double y = Double.parseDouble(point[1]);
						position = new Point2D.Double(x, y);
					}
				}
				if (position == null) {
					logger.warn("Cannot parse "+POSITION+" from input '"+pos+"'");
					return;
				}
			}
		}
		
		if (args.containsKey(LABELOFFSET)) {
			String offset = args.get(LABELOFFSET);
			if (offset.indexOf(",") > 0) {
				String[] point = offset.split(",");
				if (point.length == 2) {
					double x = Double.parseDouble(point[0]);
					double y = Double.parseDouble(point[1]);
					labelOffset = new Point2D.Double(x, y);
				}
			}
			if (labelOffset == null) {
				logger.warn("Cannot parse "+LABELOFFSET+" from input '"+offset+"'");
				return;
			}
		}

		if (args.containsKey(ANCHOR)) {
			String a = args.get(ANCHOR);
			anchor = ViewUtils.getPosition(a);
			if (anchor == null) {
				logger.warn("Cannot parse "+ANCHOR+" from input '"+a+"'");
				return;
			}
		}

		// Get our size (if we have one)
		// *not used anywhere*
		Rectangle2D size = null;
		if (args.containsKey(SIZE)) {
			String sizeString = args.get(SIZE);
			size = getSize(sizeString);
		} 

		// Get the base of the chart
		if (args.containsKey(YBASE)) {
			String yb = args.get(YBASE);
			if (yb.equalsIgnoreCase("bottom"))
				ybase = 1.0;
			else if (yb.equalsIgnoreCase("top"))
				ybase = 0.0;
			else if (yb.equalsIgnoreCase("middle"))
				ybase = 0.5;
			else {
				try {
					ybase = getDoubleValue(yb);
				} catch (NumberFormatException e) {
					logger.warn("Cannot parse "+YBASE+" from input '"+args.get(YBASE)+"'");
					ybase = 0.5;
				}
			}
		}

		if (!showLabels)
			labels = null;

		if (args.containsKey(ATTRIBUTELIST)) {
			attributes = getStringList(args.get(ATTRIBUTELIST));
			if (attributes == null || attributes.size() == 0) {
				logger.error("Cannot parse "+ATTRIBUTELIST+" from input '"+args.get(ATTRIBUTELIST)+"'");
			}
		}
	}

	public List<Double> convertInputToDouble(String input) {
		return parseStringList(input);
	}

	public String getLabelFromAttribute (CyNetwork network, CyNode node, 
	                                     String attribute) {
		if (attribute == null) return null;
		CyRow row = network.getRow(node);
		if (row == null) {
			logger.warn("Cannot find row for node "+node);
			return null;
		}
		Object label = row.getRaw(attribute);
		if (label == null) {
			logger.warn("Cannot find attribute '"+attribute+"' for node "+node);
			return null;
		}
		return label.toString();
	}

	/**
 	 * Get values from a list of attributes.  The attributesList can either be a single list attribute with
 	 * numeric values or a list of integer or floating point attributes.  At some point, it might be interesting
 	 * to think about other combinations, but this is a good starting point.
 	 *
 	 * @param node the node we're getting the custom graphics from
 	 * @param attributelist the list of column names
 	 * @param labels the list of labels if the user wants to override the attribute names
 	 * @return the list of values
 	 * @ if the attributes aren't numeric
 	 */
	public List<Double> getDataFromAttributes (CyNetwork network, CyNode node, 
	                                           List<String>attributeList, List<String> labels) 
	{
		List<Double> values = new ArrayList<Double>();

		// Get the row
		CyRow row = network.getRow(node);
		if (row == null) {
			logger.warn("Cannot find row for node "+node);
			return values;
		}

		CyTable table = row.getTable();

		// Get the first attribute
		String column = attributeList.get(0);
		if (column == null || table.getColumn(column) == null) {
			logger.warn("Cannot find node attribute column "+attributeList.get(0));
			return values;
		}

		Class<?> columnType = table.getColumn(column).getType();

		// If this is a single attribute, we assume it's a list
		if (attributeList.size() == 1 && columnType.equals(List.class)) {
			Class<?> type = table.getColumn(column).getListElementType();
			if (type == Double.class) {
				//values.addAll(row.getList(column, Double.class));
				List<Double> dList = row.getList(column, Double.class);
				if(dList != null) {
					values.addAll(dList);
				}
			} else if (type == Integer.class) {
				List<Integer> iList = row.getList(column, Integer.class);
				for (Integer i: iList) 
					values.add(i.doubleValue());
			} else if (type == Long.class) {
				List<Long> lList = row.getList(column, Long.class);
				for (Long l: lList) 
					values.add(l.doubleValue());
			} else if (type == Float.class) {
				List<Float> fList = row.getList(column, Float.class);
				for (Float f: fList) 
					values.add(f.doubleValue());
			} else if (type == String.class) {
				List<String> sList = row.getList(column, String.class);
				for (String s: sList) 
					values.add(Double.valueOf(s));
			}
		} else {
			for (String col: attributeList) {
				if (table.getColumn(col) == null)
					continue;

				Class<?> type = table.getColumn(col).getType();
				if (type == Double.class) {
					values.add(row.get(col, Double.class));
				} else if (type == Integer.class) {
					Integer i = row.get(col, Integer.class);
					values.add(i.doubleValue());
				} else if (type == Float.class) {
					Float f = row.get(col, Float.class);
					values.add(f.doubleValue());
				} else if (type == String.class) {
					String s = row.get(col, String.class);
					values.add(Double.valueOf(s));
				}
			}
		}

		// Finally, if we have user-supplied ranges, normalize
		if (rangeMax != 0.0 || rangeMin != 0.0) {
			values = normalize(values, rangeMin, rangeMax);
			normalized = true;
		}
		if (labels != null && labels.size() == 0)
			labels.addAll(attributeList);
		return values;
	}

	public List<Double> convertStringList(List<String> input)  {
		List<Double> values = new ArrayList<Double>(input.size());
		for (String s: input) {
			try {
				Double d = Double.valueOf(s);
				values.add(d);
			} catch (NumberFormatException e) {
				return null;
			}
			// System.out.println("");
		}
		return values;
	}

	public List<Double> convertIntegerList(List<Integer> input) {
		List<Double> values = new ArrayList<Double>(input.size());
		for (Integer s: input) {
			double d = s.doubleValue();
			values.add(d);
		}
		return values;
	}

	public List<Double> parseStringList(String input)  {
		if (input == null)
			return null;
		String[] inputArray = input.split(",");
		return convertStringList(Arrays.asList(inputArray));
	}

	public List<String> getStringList(String input) {
		if (input == null || input.length() == 0)
			return new ArrayList<String>();

		String[] inputArray = input.split(",",-1);
		return Arrays.asList(inputArray);
	}

	/**
 	 * Return the boolean equivalent of the input
 	 *
 	 * @param input an input value that is supposed to be Boolean
 	 * @return the boolean value it represents
 	 */
	public boolean getBooleanValue(Object input) {
		if (input instanceof Boolean)
			return ((Boolean)input).booleanValue();
		return Boolean.parseBoolean(input.toString());
	}

	public int getFontStyle(String input) {
		if (input.equalsIgnoreCase("italics"))
			return Font.ITALIC;
		if (input.equalsIgnoreCase("bold"))
			return Font.BOLD;
		if (input.equalsIgnoreCase("bolditalic"))
			return Font.ITALIC|Font.BOLD;
		return Font.PLAIN;
	}

	public Color getColorValue(String input) {
		String [] colorArray = new String[1];
		colorArray[0] = input;
		List<Color> colors = parseColorList(colorArray);
		return colors.get(0);
	}

	/**
 	 * Return the double equivalent of the input
 	 *
 	 * @param input an input value that is supposed to be a double
 	 * @return the a double value it represents
 	 * @throws NumberFormatException is the value is illegal
 	 */
	public double getDoubleValue(Object input) throws NumberFormatException {
		if (input instanceof Double)
			return ((Double)input).doubleValue();
		else if (input instanceof Integer)
			return ((Integer)input).doubleValue();
		else if (input instanceof String)
			return Double.parseDouble((String)input);
		throw new NumberFormatException("input can not be converted to double");
	}

	/**
 	 * Return the integer equivalent of the input
 	 *
 	 * @param input an input value that is supposed to be a integer
 	 * @return the a integer value it represents
 	 * @throws NumberFormatException is the value is illegal
 	 */
	public int getIntegerValue(Object input) throws NumberFormatException {
		if (input instanceof Integer)
			return ((Integer)input).intValue();
		else if (input instanceof Integer)
			return ((Integer)input).intValue();
		else if (input instanceof String)
			return Integer.parseInt((String)input);
		throw new NumberFormatException("input can not be converted to integer");
	}


	/**
 	 * Return the size specified by the user in the width and height fields of the Rectangle
 	 * The size can be either "sss" where "sss" will be both the height and the width or
 	 * "hhhxwww" where hhh is the height and www is the width.
 	 *
 	 * @param input the input size
 	 * @return a rectangle to get the width and height from
 	 */
	public Rectangle2D getSize(Object input)  {
		if (input instanceof Rectangle2D) 
			return (Rectangle2D) input;
		else if (input instanceof Double) {
			double v = ((Double)input).doubleValue();
			return new Rectangle2D.Double(0.0,0.0,v,v);
		} else if (input instanceof Integer) {
			double v = ((Integer)input).doubleValue();
			return new Rectangle2D.Double(0.0,0.0,v,v);
		} else if (input instanceof String) {
			String inputString = (String)input;
			String[] sizes = inputString.split("[xX]");
			if (sizes.length == 1) {
				double v = Double.parseDouble(sizes[0]);
				return new Rectangle2D.Double(0.0,0.0,v,v);
			} else if (sizes.length == 2) {
				double h = Double.parseDouble(sizes[0]);
				double w = Double.parseDouble(sizes[1]);
				return new Rectangle2D.Double(0.0,0.0,w,h);
			} 
		}
		return null;
	}

	/*
	 * Return the font information
	 */
	public Font getFont() {
		return new Font(labelFont, labelStyle, labelSize);
	}

	public List<Double> arrayMax(List<Double> maxValues, List<Double> values) {
		// Initialize, if necessary
		if (maxValues == null) {
			maxValues = new ArrayList<Double>(values.size());
			for (Double d: values)
				maxValues.add(Math.abs(d));
			return maxValues;
		}

		// OK, now we need to actually do the work...
		for (int index = 0; index < values.size(); index++) {
			maxValues.set(index, Math.max(maxValues.get(index), Math.abs(values.get(index))));
		}
		return maxValues;
	}

	public void normalize(List<Double> values, List<Double> maxValues) {
		for (int index = 0; index < values.size(); index++) {
			values.set(index, values.get(index)/maxValues.get(index));
		}
	}

	/**
 	 * Takes a map of objects indexed by a string keyword and returns
 	 * a map of strings indexed by that keyword.  This involves figuring
 	 * out if the object is a list, and if so converting it to a comma
 	 * separated string
 	 *
 	 * @param argMap the map of objects indexed by strings
 	 * @return the serialized map
 	 */
	public Map<String,String> serializeArgMap(Map<String, Object> argMap) {
		Map<String,String> sMap = new HashMap<String,String>();
		for (String key: argMap.keySet()) {
			sMap.put(key, serializeObject(argMap.get(key)));
		}
		return sMap;
	}

	/**
 	 * Serialize an object that might be a list to a string
 	 */
	private String serializeObject(Object obj) {
		String result;
		if (obj instanceof List) {
			result = "";
			for (Object o: (List)obj) {
				result += o.toString()+",";
			}
			result = result.substring(0, result.length()-1);
		} else
			result = obj.toString();

		return result;
	}

	private static final String	CONTRASTING = "contrasting";
	private static final String	DOWN = "down:";
	// ML: added ':' at the end of missing
	private static final String	MISSING = "missing:";
	private static final String	MODULATED = "modulated";
	private static final String	RAINBOW = "rainbow";
	private static final String RANDOM = "random";
	private static final String	UP = "up:";
	private static final String	ZERO = "zero:";
	private static final double EPSILON = 1E-8f;

	public List<Color> convertInputToColor(String input, List<Double>values)  {
		int nColors = values.size();

		if (input == null) {
			// give the default: contrasting colors
			return generateContrastingColors(nColors);
		}

		// OK, we have three posibilities.  The input could be a keyword, a comma-separated list of colors, or
		// a list of Color objects.  We need to figure this out first...
		// See if we have a csv
		String [] colorArray = input.split(",");
		// Look for up/down special case
		if (colorArray.length == 2 &&
		    (colorArray[0].toLowerCase().startsWith(UP) ||
		     colorArray[0].toLowerCase().startsWith(DOWN))) {
			return parseUpDownColor(colorArray, values);
		} else if (colorArray.length == 3 &&
		    (colorArray[0].toLowerCase().startsWith(UP) ||
		     colorArray[0].toLowerCase().startsWith(DOWN) ||
	     colorArray[0].toLowerCase().startsWith(ZERO))) {
			return parseUpDownColor(colorArray, values);
		} else if (colorArray.length == 4 &&
		    (colorArray[0].toLowerCase().startsWith(UP) ||
		     colorArray[0].toLowerCase().startsWith(DOWN) ||
	       colorArray[0].toLowerCase().startsWith(ZERO) ||
			   colorArray[0].toLowerCase().startsWith(MISSING))) {
			return parseUpDownColor(colorArray, values);
		} else if (colorArray.length > 1)
			return parseColorList(colorArray);
		else
			return parseColorKeyword(input.trim(), nColors);
	}

	public List<Color> parseUpDownColor(String[] colorArray)  {
		if (colorArray.length < 2) {
			return null;
		}

		String [] colors = new String[4];
		colors[2] = "black";
		colors[3] = "grey";
		for (int index = 0; index < colorArray.length; index++) {
			if (colorArray[index].toLowerCase().startsWith(UP)) {
				colors[0] = colorArray[index].substring(UP.length());
			} else if (colorArray[index].toLowerCase().startsWith(DOWN)) {
				colors[1] = colorArray[index].substring(DOWN.length());
			} else if (colorArray[index].toLowerCase().startsWith(ZERO)) {
				colors[2] = colorArray[index].substring(ZERO.length());
			} else if (colorArray[index].toLowerCase().startsWith(MISSING)) {
				colors[3] = colorArray[index].substring(MISSING.length());
			}
		} 
		return parseColorList(colors);
	}

	private List<Color> parseUpDownColor(String[] colorArray, List<Double>values)  {
		List<Color> upDownColors = parseUpDownColor(colorArray);
		Color up = upDownColors.get(0);
		Color down = upDownColors.get(1);
		Color zero = upDownColors.get(2);
		Color missing = upDownColors.get(3);
		// System.out.println("up color = "+up);
		// System.out.println("down color = "+down);
		// System.out.println("zero color = "+zero);
		// System.out.println("missing color = "+missing);

		// System.out.println("values.size() = "+values.size());
		List<Color> results = new ArrayList<Color>(values.size());
		for (Double v: values) {
			// System.out.println("Looking at value "+v);
			// ML: A NaN should be treated as missing
			if (v == null || v.isNaN()) {
				results.add(missing);
				continue;
			}
			double vn = v;
			if (!normalized)
				vn = normalize(v, rangeMin, rangeMax);
			// System.out.println("Value = "+v+", Normalized value = "+vn);
			if (vn < (-EPSILON)) 
				results.add(scaleColor(-vn, zero, down));
			else if (vn > EPSILON)
				results.add(scaleColor(vn, zero, up));
			else
				results.add(zero);
		}
		return results;
	}

	private Color scaleColor(double v, Color zero, Color c) {
		if (rangeMin == 0.0 && rangeMax == 0.0) return c;

		// We want to scale our color to be between "zero" and "c"
		// v = 1-v;
		int b = (int)(c.getBlue()*v + zero.getBlue()*(1-v));
		int r = (int)(c.getRed()*v + zero.getRed()*(1-v));
		int g = (int)(c.getGreen()*v + zero.getGreen()*(1-v));
		//ML: Added alpha
		int a = (int)(c.getAlpha()*v + zero.getAlpha()*(1-v));
		//int b = (int)(Math.abs(c.getBlue()-zero.getBlue())*v)+c.getBlue();
		//int r = (int)(Math.abs(c.getRed()-zero.getRed())*v)+c.getRed();
		//int g = (int)(Math.abs(c.getGreen()-zero.getGreen())*v)+c.getGreen();
		// System.out.println("scaleColor: v = "+v+" r="+r+" g="+g+" b="+b);
		return new Color(r, g, b, a);
	}

	// Zero-centered normalization.  Zero values must remain zero,
	// negative values must be negative, and positive values must be
	// positive.  Note that if the user gives us unbalanced ranges, this
	// approach to normalization will inflate the smaller of the ranges
	private double normalize(double v, double rangeMin, double rangeMax) {
		// ML : a NaN should not be normalized
		if (Double.isNaN(v)) return v;
		
		if (rangeMin == 0.0 && rangeMax == 0.0) return v;
		double range = rangeMax-rangeMin;
		double val = 0.0;

		// Clamp v
		if (v < rangeMin) v = rangeMin;
		if (v > rangeMax) v = rangeMax;

		if ((rangeMin > 0.0 && rangeMax > 0.0) ||
		    (rangeMin < 0.0 && rangeMax < 0.0))
			val = (v - rangeMin) / range;
		else if (v < 0.0 && rangeMin < 0.0)
			val = -(v / rangeMin);
		else if (v > 0.0 && rangeMax > 0.0)
			val = (v / rangeMax);

		return val;
	}

	//ML: Change from private to protected
	protected List<Double> normalize(List<Double> vList, double rangeMin, double rangeMax) {
		// System.out.println("Normalize list");
		for (int i = 0; i < vList.size(); i++) {
			Double v = vList.get(i);
			if (v != null) {
				Double vn = normalize(v, rangeMin, rangeMax);
				// System.out.println("Value = "+v+", Normalized value = "+vn);
				vList.set(i, vn);
			}
		}
		// System.out.println("Normalize list..done");
		return vList;
	}

	private List<Color> parseColorKeyword(String input, int nColors)  {
		if (input.equals(RANDOM))
			return generateRandomColors(nColors);
		else if (input.equals(RAINBOW))
			return generateRainbowColors(nColors);
		else if (input.equals(MODULATED))
			return generateModulatedRainbowColors(nColors);
		else if (input.equals(CONTRASTING))
			return generateContrastingColors(nColors);
		else {
			String [] colorArray = new String[1];
			colorArray[0] = input;
			List<Color> colors = parseColorList(colorArray);
			return colors;
		}
	}

	public Color parseColor(String colorString)  {
		if (colorString == null) return null;
		colorString = colorString.trim();
		if (colorString.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
			// We have a hex value with either 6 (rgb) or 8 (rgba) digits
			int r = Integer.parseInt(colorString.substring(1,3), 16);
			int g = Integer.parseInt(colorString.substring(3,5), 16);
			int b = Integer.parseInt(colorString.substring(5,7), 16);
			if (colorString.length() > 7) {
				int a = Integer.parseInt(colorString.substring(7,9), 16);
				return new Color(r,g,b,a);
			} else {
				return new Color(r,g,b);
			}
		} else {
			// Check for color string
			Color c = ColorKeyword.getColor(colorString);
			if (c == null) {
				logger.warn("Can't find color '"+colorString+"'");
				return null;
			}
			return c;
		}
	}

	public List<Color> parseColorList(String[] inputArray)  {
		List<Color> colors = new ArrayList<Color>();
		// A color in the array can either be a hex value or a text color
		for (String colorString: inputArray) {
			if (colorString == null) continue;
			colorString = colorString.trim();
			if (colorString.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
				// We have a hex value with either 6 (rgb) or 8 (rgba) digits
				int r = Integer.parseInt(colorString.substring(1,3), 16);
				int g = Integer.parseInt(colorString.substring(3,5), 16);
				int b = Integer.parseInt(colorString.substring(5,7), 16);
				if (colorString.length() > 7) {
					int a = Integer.parseInt(colorString.substring(7,9), 16);
					colors.add(new Color(r,g,b,a));
				} else {
					colors.add(new Color(r,g,b));
				}
			} else {
				// Check for color string
				Color c = ColorKeyword.getColor(colorString);
				if (c == null) {
					logger.warn("Can't find color '"+colorString+"'");
					return null;
				}
				colors.add(c);
			}
		}
		return colors;
	}

	private List<Color> generateRandomColors(int nColors) {
		// System.out.println("Generating random colors");
		Calendar cal = Calendar.getInstance();
		int seed = cal.get(Calendar.SECOND);
		Random rand = new Random(seed);

		List<Color> result = new ArrayList<Color>(nColors);
		for (int index = 0; index < nColors; index++) {
			int r = rand.nextInt(255);
			int g = rand.nextInt(255);
			int b = rand.nextInt(255);
			result.add(index, new Color(r,g,b,200));
		}
		return result;
	}

	// Rainbow colors just divide the Hue wheel into n pieces and return them
	private List<Color> generateRainbowColors(int nColors) {
		// System.out.println("Generating rainbow colors");
		List<Color> values = new ArrayList<Color>();
		for (float i = 0.0f; i < (float)nColors; i += 1.0f) {
			values.add(new Color(Color.HSBtoRGB(i/(float)nColors, 1.0f, 1.0f)));
		}
		return values;
	}

	// Rainbow colors just divide the Hue wheel into n pieces and return them, but
	// in this case, we're going to change the saturation and intensity
	private List<Color> generateModulatedRainbowColors(int nColors) {
		// System.out.println("Generating modulated colors");
		List<Color> values = new ArrayList<Color>();
		for (float i = 0.0f; i < (float)nColors; i += 1.0f) {
			float sat = (Math.abs(((Number) Math.cos((8 * i) / (2 * Math.PI))).floatValue()) * 0.7f) 
			             + 0.3f;
			float br = (Math.abs(((Number) Math.sin(((i) / (2 * Math.PI)) + (Math.PI / 2)))
			                      .floatValue()) * 0.7f) + 0.3f;

			// System.out.println("Color("+(i/(float)nColors)+","+sat+","+br+")");
			values.add(new Color(Color.HSBtoRGB(i/(float)nColors, sat, br)));
		}
		return values;
	}

	// This is like rainbow, but we alternate sides of the color wheel
	private List<Color> generateContrastingColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		for (int i = 0; i < nColors; i++) {
			float hue;
			if (i%2 == 1)
				hue = (float)i/(float)nColors + 0.5f;
			else
				hue = (float)i/(float)nColors;
			Color rgbColor = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
			// System.out.println("RGBColor = "+rgbColor);
			values.add(rgbColor);
		}
		return values;
	}

	// TODO: add brewer colors
}
