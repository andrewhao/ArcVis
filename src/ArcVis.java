import processing.core.*;
import processing.pdf.*;
import toxi.geom.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;
import java.awt.image.BufferedImage;
import java.io.*;
import com.sun.image.codec.jpeg.*;

public class ArcVis extends PApplet {
	float radius = 1200;
	int width = 2000, height = 2000;
	float padding = 5;
	Vec2D windowCenter = new Vec2D(width/2, height/2); 
	HashMap<String, HashMap> arcMap = new HashMap();
	PFont fontA;
	PGraphics pg;
	
	public String normalizeWord(String w) {
		return w.replaceAll("[^A-Za-z\'\"]", "").toLowerCase();
	}
	
	public void setup() {
		
		size(width, height); //, PDF, "output.pdf");
		pg = (PGraphics)createGraphics(width, height, JAVA2D);
		String[] fontList = PFont.list();
		println(fontList);
		//fontA = loadFont("Helvetica-14.vlw");
		fontA = createFont("HelveticaNeue-Bold", 22, true);
		pg.textFont(fontA, 22);
		pg.strokeCap(PROJECT);
		pg.strokeWeight(1);
		//pg.background(255);
		pg.hint(ENABLE_NATIVE_FONTS);
		
		
		String[] text = loadStrings("human_rights.txt");
		ArrayList<String> textList = new ArrayList(Arrays.asList(text));
		//println(textList);
		
		String[] blacks = {"of", "the", "in", "a", "and", "to", "it", "is"};
		ArrayList<String> blacklist = new ArrayList(Arrays.asList(blacks));
		
		// Compute the directed arcs.
		for (String line : textList) {
			String[] words = line.split("[ ]");
			for (int i = 0; i < words.length; i++) {
				// trim punctuation
				String w = this.normalizeWord(words[i]);
				
				if (blacklist.contains(w)) {
					println("kill on "+w);
					continue;
				}
				
				String nextWord = null;
				boolean hasNexts = true;
				try {
					nextWord = this.normalizeWord(words[i+1]);
				} catch(Exception e) {
					// end of list
					hasNexts = false;
				}
				
				// Initialize default entry if none exists.
				if (!arcMap.containsKey(w)) {
					// Prepopulate with default data entry
					HashMap defaultDataEntry = new HashMap();
					// Initialize DDE with a "nexts" arraylist.
					// TODO: Should be a custom object.
					defaultDataEntry.put("nexts", new ArrayList());
					arcMap.put(w, defaultDataEntry);
				}
				
				if (hasNexts && !blacklist.contains(nextWord)) {
					HashMap arcData = arcMap.get(w);
					// Add to new arraylist
					((ArrayList)arcData.get("nexts")).add(nextWord);
				}
			}
		}
		

		// draw() once.
		noLoop();
		
		// Record to PDF too.
		//beginRecord(PDF, "output.pdf");
		smooth();
	}
	
	public void draw() {
		
		pg.beginDraw();
		
		//Circle c = new Circle(windowCenter, radius);
		//ellipse(c.x, c.y, c.getRadius(), c.getRadius());
		
		// Tracks the angle of the word being rendered
		float theta = 0;
		
		// How far to iterate the theta on the next word.
		float thetaIncrement = radians((float)360 / arcMap.size());
		
		// initial rendering setup
		pg.textAlign(LEFT, CENTER);
		
		pg.fill(0);
		//pg.fill(255, 187, 110); // sandstone
		
		// Translate coord system to window center.
		pg.translate(windowCenter.x, windowCenter.y);
		
		// For each word, render around circle
		ArrayList<String> sortedWords = new ArrayList(arcMap.keySet());
		Collections.sort(sortedWords);
		
		for (String word : sortedWords) {
			
			//String word = (String)(e.getKey());
			HashMap wordData = arcMap.get(word);
			
			// Render the text
			pg.text(word, radius/2 + padding, 0);

			// Rotate the text for next word.
			pg.rotate(thetaIncrement);
			
			// Store
			wordData.put("theta", theta);
			wordData.put("coordinates", new Vec2D(radius/2, theta).toCartesian());
			
			// Do business before the next render
			theta += thetaIncrement;
		}
		
		int maxNumArcs = 0;
		int threshold = floor(maxNumArcs / 3);
		int totalArcs = 0;
		float avgNumArcs = 0;
		for (HashMap m : arcMap.values()) {
			int numArcs = ((ArrayList)m.get("nexts")).size();
			totalArcs += numArcs;
			if (numArcs > maxNumArcs) {
				maxNumArcs = numArcs;
			}
		}
		avgNumArcs = (float)(totalArcs) / arcMap.size();
		println("==== MAX ARCS: " + maxNumArcs + " ====");
		println("==== TOT ARCS: " + totalArcs + " ====");
		println("==== AVG ARCS: " + avgNumArcs + " ====");
		
		// Now we're going to draw the arcs
		for (Map.Entry e : arcMap.entrySet()) {
			String word = (String)(e.getKey());
			HashMap wordData = (HashMap)(e.getValue());
			Vec2D fromCoords = (Vec2D)wordData.get("coordinates");
			ArrayList<String> arcs = (ArrayList<String>)wordData.get("nexts");
			
			for (String nextArcWord : arcs) {
				// baseline 40 opacity
				int opacity = (100 / maxNumArcs) * arcs.size() + 80;
				
				// if you're over the average (mean) # arcs, you get special
				// rendering.
				// Gets redder and more opaque the stronger the word.
				if (arcs.size() > 2) {
					pg.stroke(0, opacity);
					//pg.stroke(242, 141, 0, 255);
					pg.strokeWeight(5);
				} else {
					pg.stroke(0, 90); // sandstone					
					pg.strokeWeight(3);
				}
				println("\""+ word +"\": next arc word is: " + nextArcWord);
				Vec2D nextCoords = (Vec2D)(arcMap.get(nextArcWord).get("coordinates"));
				pg.line(fromCoords.x, fromCoords.y, nextCoords.x, nextCoords.y);
			}
		}

		println(arcMap);
//		saveFrame("output.png");
		//endRecord();
		
		pg.endDraw();
		image(pg, 0, 0);
		pg.save("output.png");
		
		delay(3);
		exit();
	}
}
