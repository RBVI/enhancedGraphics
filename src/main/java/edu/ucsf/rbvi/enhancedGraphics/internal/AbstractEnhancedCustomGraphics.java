package edu.ucsf.rbvi.enhancedGraphics.internal;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class AbstractEnhancedCustomGraphics <T extends CustomGraphicLayer>
                implements CyCustomGraphics <T> {

	protected Long id = null;
	protected float fitRatio = 1.0f;
	protected List<T> layers;
	protected String displayName;
	protected int width = 50;
	protected int height = 50;
	protected Logger logger;

	protected AbstractEnhancedCustomGraphics() {
		layers = new ArrayList<T>();
		logger = Logger.getLogger(CyUserLog.NAME);
	}

	public Long getIdentifier() { return id; }

	public void setIdentifier(Long id) { this.id = id; }

	public void setWidth(final int width) { this.width = width; }
	public void setHeight(final int height) { this.height = height; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public List<T> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> nodeView) { return layers; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}
	public float getFitRatio() { return fitRatio; }
	public void setFitRatio(float fitRatio) { this.fitRatio = fitRatio; }
	public String toString() {
		return displayName;
	}

	abstract public Image getRenderedImage();
	abstract public String toSerializableString();


	protected Map<String, String> parseInput(String input) {
		Map<String,String> settings = new HashMap<String,String>();

		// Tokenize
		StringReader reader = new StringReader(input);
		StreamTokenizer st = new StreamTokenizer(reader);

		// We don't really want to parse numbers as numbers...
		st.ordinaryChar('/');
		st.ordinaryChar('_');
		st.ordinaryChar('-');
		st.ordinaryChar('.');
		st.ordinaryChars('0', '9');

		st.wordChars('/', '/');
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('.', '.');
		st.wordChars('0', '9');

		List<String> tokenList = new ArrayList<>();
		int tokenIndex = 0;
		int i;
		try {
			while ((i = st.nextToken()) != StreamTokenizer.TT_EOF) {
				switch(i) {
					case '=':
						// Get the next token
						i = st.nextToken();
						if (i == StreamTokenizer.TT_WORD || i == '"') {
							tokenIndex--;
							String key = tokenList.get(tokenIndex);
							settings.put(key, st.sval);
							tokenList.remove(tokenIndex);
						}
						break;
					case '"':
					case StreamTokenizer.TT_WORD:
						tokenList.add(st.sval);
						tokenIndex++;
						break;
					default:
						break;
				}
			}
		} catch (Exception e) { return new HashMap<String,String>(); }

		return settings;
	}

	protected Point2D parsePoint(String point) {
		if (point == null || point.length() == 0) return null;

		String tokens[] = point.split(",");
		if (tokens.length != 2)
			return null;
		try {
			float x = Float.parseFloat(tokens[0].trim());
			float y = Float.parseFloat(tokens[1].trim());
			return new Point2D.Float(x,y);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	// Parse out a stop list.  The stoplist is of the form:
	// 	r,g,b,a,stop|r,g,b,a,stop...
	protected int parseStopList(String stoplist, List<Color> colors, List<Float> stops) {
		if (stoplist == null || stoplist.length() == 0) return 0;
		int nStops = 0;

		String[] tokens = stoplist.split("\\|");
		for (String token: tokens) {
			String[] components = token.split(",");
			if (components.length != 4 && components.length != 5) {
				logger.warn("Unable to get stop from '"+token+"'.  Skipping this stop");
				continue;
			}

			int r = Integer.parseInt(components[0]);
			int g = Integer.parseInt(components[1]);
			int b = Integer.parseInt(components[2]);
			int a = 255;
			float stop;
			if (components.length == 5) {
				a = Integer.parseInt(components[3]);
				stop = Float.parseFloat(components[4]);
			} else {
				stop = Float.parseFloat(components[3]);
			}
			colors.add(new Color(r,g,b,a));
			stops.add(stop);
			nStops++;
		}
		return nStops;
	}

}
