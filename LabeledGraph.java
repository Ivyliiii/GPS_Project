
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

// LabeledGraph given by MR.FRIEDMAN
public class LabeledGraph<E, T> {
	int point_r = 20;
	int fontsize = 15;

	
	HashMap<E, Vertex> vertices; 
	
	// this class will store information and a set of edges
	public class Vertex { 
		E info;
		int x;
		int y;
		HashSet<Edge> edges;
		
		public Vertex(int x, int y, E info) {
			this.x = x;
			this.y = y;
			this.info = info;
			edges = new HashSet<Edge>();
			
		}
		public String toString() {
			return (String)info;
		}
	}

	// initiating the grpah
	public LabeledGraph() {
		vertices = new HashMap<E, Vertex>();
	}
	
	// function that puts adds new vertex
	public void addVertex(int x, int y, E info) {
		vertices.put(info, new Vertex(x, y, info));
	}
	
	// input information of the two actors and the movie title and this will create an edge
	public void connect(E info1, E info2, T label) {
		Vertex v1 = vertices.get(info1);
		Vertex v2 = vertices.get(info2);

		Edge e = new Edge(label, v1, v2);
		v1.edges.add(e);
		v2.edges.add(e);
		for(Vertex v : vertices.values()) {
			System.out.println("insides " + v.edges.size());
		}
	}
	
	// this is a part of the BFS, where it takes in a leadsTo HashMap, the start of the vertex, and the end
	public ArrayList<Vertex> backTrace(HashMap<Vertex, Vertex> leadsTo, Vertex end, Vertex start){
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		HashMap<Vertex, Vertex> map = leadsTo;
		Vertex curr = end;
		while(curr != null) { // since the start was maped to null, we know that we are at the end when we get to null
			path.add(curr); 
			curr = map.get(curr);
		}
		return path; // gives the arraylist of vertexes on the path
	}
	
	// edge will contain the movie that connects two movies
	private class Edge {
		T label;
		Vertex v1, v2;
		
		public Edge(T label, Vertex v1, Vertex v2) {
			this.label = label; this.v1 = v1; this.v2 = v2;
		}
		
		// this will give the other end of an edge
		private Vertex getNeighbor(Vertex v) {
			if (v.info.equals(v1.info)) {
				return v2;
			}
			return v1;
		}
		
	}
	
	
	// checks to see if the user is clicking in vicinity of a vertex
	public E isOn(int x, int y) {
		for(E info : vertices.keySet()) {
			int distance = distance(vertices.get(info).x, vertices.get(info).y, x, y);
			if(distance < point_r) {  //on if it is within 20
				return info;
			}
		}
		return null;
	}
	
	// pythagoream's theorem for finding the distance
	public int distance(int x1, int y1, int x2, int y2) {
		return (int)Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
	}
	
	// get the vertex based on the information
	public Vertex getVertex(E info){
		return vertices.get(info);
	}
	
	// this method will be used to draw the best path to travel form two vertices
	public void drawDijkstraAlgorithm(E start, E end, Graphics g) {
		System.out.println("drawing");
		ArrayList<Vertex> path = DijkstraAlgorithm(start, end); // find the shortest path
		if(path != null) { // if there is a valid path
			for(int i = 0; i < path.size()-1; i++) { 
				g.setColor(Color.RED); // trace out the path using red
				g.drawLine(vertices.get(path.get(i).info).x+point_r/2, vertices.get(path.get(i).info).y+point_r/2, vertices.get(path.get(i+1).info).x+point_r/2, vertices.get(path.get(i+1).info).y+point_r/2);
				g.fillOval(vertices.get(path.get(i).info).x, vertices.get(path.get(i).info).y, point_r, point_r);
			}
			g.setColor(Color.BLUE);//end points will be given a different color
			g.fillOval(vertices.get(path.get(path.size()-1).info).x, vertices.get(path.get(path.size()-1).info).y, point_r, point_r);
			g.fillOval(vertices.get(path.get(0).info).x, vertices.get(path.get(0).info).y, point_r, point_r);
		}
	}
	
	// this is the algorithm itself
	public ArrayList<Vertex> DijkstraAlgorithm(E start, E end) {
		PQ<Vertex> toVisit = new PQ<Vertex>(); // PQ to rank the places to do it
		ArrayList<Vertex> visited = new ArrayList<Vertex>(); //keep the visited places to not visit an vertex again
		HashMap<Vertex, Edge> leadsTo = new HashMap<Vertex, Edge>(); // keeps the least number of steps and the edge that conencts it
		HashMap<Vertex, Integer> distances = new HashMap<Vertex, Integer>(); // tracks distance of each vertex to the start value
		// sets all distance values as very big
		for(Vertex v: vertices.values()) {
			distances.put(v, Integer.MAX_VALUE);
		}
		// will not run if they are null
		if(vertices.get(end) == null) {
			return null;
		}
		else if(vertices.get(start) == null) {
			return null;
		}
		Vertex curr = null;
		toVisit.add(getVertex(start), 0);
		leadsTo.put(getVertex(start), null);
		distances.put(getVertex(start), 0); // distance from itself is 0
		while(toVisit.size() != 0) { // as long as visited is not empty
			curr = toVisit.pop();
			if(curr.info.equals(end)) { // if we got to the end
				return backTraceWithEdges(leadsTo, vertices.get(start), vertices.get(end));
			}
			for(Edge e :curr.edges) { // loops through all of the edges of a vertex
				if(visited.contains(e.getNeighbor(curr))){ // if there is a shorter path, ignore it 
					continue;
				}
				if(distances.get(curr) + (int)e.label < distances.get(e.getNeighbor(curr))) { // if the distance of another path is shorter, then put new distance in
					leadsTo.put(e.getNeighbor(curr), e);
					toVisit.put(e.getNeighbor(curr), (int)distances.get(curr) + (int)e.label);
					distances.put(e.getNeighbor(curr), (int)distances.get(curr) + (int)e.label);
				}
			}
			visited.add(curr);//put it in visited so we will not go to it
		}
		return null;
	}
	
	// finds the path that goes from the start to end
	public ArrayList<Vertex> backTraceWithEdges(HashMap<Vertex, Edge> lt, Vertex start, Vertex end){
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		HashMap<Vertex, Edge> leadsTo = lt;
		Vertex curr = end; 
		// finds the lead tos previously until we get to the end
		while (leadsTo.get(curr) != null) { // why is this part giving me an error
			path.add(0,curr);
			curr = leadsTo.get(curr).getNeighbor(curr); 
		}
		path.add(0,start);
		return path;
		
	}
	
	// this function would be called when we need to draw the vertex and lines on the screen
	public void draw(Graphics g, String write) {
		g.setColor(Color.BLACK); // always change the colors back to black
		g.setFont(new Font("Verdana", Font.PLAIN, fontsize));
		Graphics2D g2 = (Graphics2D) g;
		for(Vertex v : vertices.values()) { // loop through vertex
			g.fillOval(v.x, v.y, point_r, point_r);
			System.out.println(v.info);
			g.drawString((String) v.info, v.x, v.y-fontsize/4);
			for(Edge e : v.edges) { // loop through every edge of the vertex
		        g2.setStroke(new BasicStroke(fontsize/3));
				g.drawLine(e.getNeighbor(v).x+point_r/2, e.getNeighbor(v).y+point_r/2, v.x+point_r/2, v.y+point_r/2);
			}
		}
	}

	// this is for testing the Dijkstra Algorithm, and it will go the D, C, B, and A way instead of the way with less vertex
	/*public static void main(String[] args) {
		LabeledGraph<String, Integer> graph = new LabeledGraph<String, Integer>();
		graph.addVertex(2, 3, "A");
		graph.addVertex(4, 5, "B");
		graph.addVertex(6, 7, "C");
		graph.addVertex(6, 9, "D");
		graph.addVertex(14, 13, "E");
		graph.connect("A", "B", 2);
		graph.connect("B", "C", 3);
		graph.connect("C", "D", 5);
		graph.connect("D", "E", 10);
		graph.connect("E", "A", 2);
		for(LabeledGraph<String, Integer>.Vertex v : graph.DijkstraAlgorithm("D", "A")) {
			System.out.println(v.info);
		}
		
	}*/
		
}