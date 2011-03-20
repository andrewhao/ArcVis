import processing.core.*;	
import toxi.geom.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.*;
import java.io.*;

public class ArcVis extends PApplet {
	float radius = 500;
	int width = 800, height = 800;
	Vec2D windowCenter = new Vec2D(width/2, height/2); 
	HashMap<String, ArrayList<String>> arcMap = new HashMap();
	PFont fontA;
	
	public String normalizeWord(String w) {
		return w.replaceAll("[^A-Za-z\'\"]", "").toLowerCase();
	}
	
	public void setup() {
		size(width, height, JAVA2D);
		String[] fontList = PFont.list();
		println(fontList);
		fontA = loadFont("Helvetica-Bold-14.vlw");
		textFont(fontA, 14);
		smooth();
		String[] text = loadStrings("human_rights.txt");
		ArrayList<String> textList = new ArrayList(Arrays.asList(text));
		println(textList);
		
		Pattern p = Pattern.compile("[ ]");
		
		for (String line : textList) {
			String[] words = p.split(line);
			for (int i = 0; i < words.length; i++) {
				// trim punctuation
				String w = words[i];
				String nextWord;
				try {
					nextWord = words[i+1];
				} catch(Exception e) {
					// end of list
					break;
				}
				w = this.normalizeWord(w);
				if (!arcMap.containsKey(w)) {
					arcMap.put(w, new ArrayList());
				}
				ArrayList<String> arcList = arcMap.get(w);
				arcList.add(nextWord);
			}
		}
		
		println(arcMap);
		
		// draw() once.
		noLoop();
	}
	
	public void draw() {
		Circle c = new Circle(windowCenter, radius);
		//ellipse(c.x, c.y, c.getRadius(), c.getRadius());

		float thetaIncrement = radians((float)360 / arcMap.size());
		
		textAlign(LEFT, CENTER);
		fill(0);
		
		// Translate coord system to window center.
		translate(windowCenter.x, windowCenter.y);
		
		// For each word, render around circle
		for (String word : arcMap.keySet()) {
			
			// Render the text
			text(word, radius/2, 0);

			// Rotate the text for next word.
			rotate(thetaIncrement);
		}

	}
}
