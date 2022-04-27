import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GPS {
	// these are the basic variables that we might run into
	public final int height = 2000, width = 800;
	int clicked_x, clicked_y;
	BufferedImage img = null;
	File myObj = new File("GPS_History");
	JLabel lb = null;
	String fileName = "minecraft_map.jpg";
	String currentInfo = null;
	String name = "";
	String djStart = null;
	String djEnd = null;
	int fontsize = 20;
	int point_r = 20;
	boolean djMode = false;
	LabeledGraph<String, Integer> graph = new LabeledGraph<String, Integer>();
	String write = "";

	public GPS(){
		// normal set up with the frames, panels, and canvas
		write = readFile();
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(width, height));
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(width, height));
		JPanel canvas = new JPanel() {
			public void paint(Graphics g) {
				try {
					img = ImageIO.read(new File(fileName)); // upload the photo
				} catch (IOException e) {
					e.printStackTrace();
				}
				g.drawImage(img, 0, 0, null);
				graph.draw(g, write);// call the draw function in the graph
				if(djStart != null && djEnd != null) {
					graph.drawDijkstraAlgorithm(djStart, djEnd, g); //this will draw the path for the algorithm
					djStart = null; //set this these as null after running it
					djEnd = null;
					return;
				}
			}
		};
		try {
			myObj.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//set up the size the canvas
		canvas.setPreferredSize(new Dimension(width, height));				
		canvas.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				clicked_x = e.getX();
				clicked_y = e.getY();
				String str = graph.isOn(clicked_x, clicked_y); // saves the name of vertex
				if(graph.isOn(clicked_x, clicked_y) != null) { 
					if(currentInfo == null) { //if no previous vertex was connected
						currentInfo = graph.isOn(clicked_x, clicked_y); // put this as a vertex to connect
						writeFile();
					}
					else if(str.equals(currentInfo)) { //if a vertex was clicked twice
						currentInfo = graph.isOn(clicked_x, clicked_y); 
						if(djStart == null) {// if it is the first vertex
							djStart = currentInfo;
							currentInfo = null;
						}
						else if(djEnd == null) { // if it is the second vertex 
							djEnd = currentInfo;
							frame.getContentPane().repaint(); //frame will be redrawn
							currentInfo = null;
						}
					}
					else { // connect two vertex
						graph.connect(currentInfo, str, graph.distance(graph.vertices.get(currentInfo).x,graph.vertices.get(currentInfo).y, graph.vertices.get(str).x, graph.vertices.get(str).y));
						editString(currentInfo, str);
						currentInfo = null;
						writeFile();
						frame.getContentPane().repaint();
					}
				}
				else { // if it is not clicked on a vertex
					// ask for the name of a vertex
					String name = JOptionPane.showInputDialog(canvas, "Enter Location Name:");
					if(name.equals(null)) { // if the string is empty, set the location as null
						name = "null";
						graph.addVertex(clicked_x, clicked_y, name);
						frame.getContentPane().repaint();
						write +=  name + " " + clicked_x + " " + clicked_y + " \n"; // put it into write
						writeFile();
					}
					else { 
						//if it is not empty, just add vertex and put it into write
						graph.addVertex(clicked_x, clicked_y, name);
						frame.getContentPane().repaint();
						write +=  name + " " + clicked_x + " " + clicked_y + " \n";
						writeFile();
					}
				}
			}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		canvas.setBackground(Color.BLACK);
		frame.add(panel);
		panel.add(canvas);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	// this will break up the write into multiple lines, and split the lines by the space 
	// find the corresponding line with the strings, add the second string behind it
	public void editString(String info1, String info2) {
		String curr = "" ;
		ArrayList<String[]> lines = new ArrayList<String[]>();
		for(int i = 0; i < write.length(); i++) {
			if(write.charAt(i) == '\n') {
				String[] strs = curr.split(" ");
				lines.add(strs);
				curr = ""; 
			}
			else {
				curr += write.charAt(i);
			}
		}
		String[] infoLine1 = {};
		String[] infoLine2 = {};
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i)[0].equals(info1)) {
				infoLine1 = lines.get(i);
			}
			else if(lines.get(i)[0].equals(info2)) {
				infoLine2 = lines.get(i);
			}
		}	
		// add to the end of the line
		infoLine1[infoLine1.length-1] = infoLine1[infoLine1.length-1] + " " + infoLine2[0];
		infoLine2[infoLine2.length-1] = infoLine2[infoLine2.length-1] + " " + infoLine1[0];
		write = "";
		// put lines back into write again
		for(int i = 0; i < lines.size(); i++) {
			for(int j = 0; j < lines.get(i).length; j++) {
				write+=lines.get(i)[j];
				write+=" ";
			}
			write+="\n";
		}
	}
	
	// at the start, read the file and add vertex and edges
	public String readFile() {
		BufferedReader br;
		String output = "";
		try {
			br = new BufferedReader(new FileReader(myObj.getName()));
			String line = "";
		    for (line = br.readLine(); line != null; line = br.readLine()) {
				output+=line + "\n";
				String[] words = line.split(" ");
				if(words.length >= 3) { // if it is a valid input line
					String name = words[0];
					int x = Integer.parseInt(words[1]);
					int y = Integer.parseInt(words[2]);
					graph.addVertex(x, y, name); // add the information into vertex
				}
		    }			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// do this again to find the connections
		try {
			br = new BufferedReader(new FileReader(myObj.getName()));
			String line = "";
		    for (line = br.readLine(); line != null; line = br.readLine()) {
				String[] words = line.split(" "); 
				if(words.length > 3) { // if there are connections to this vertex
					for(int i = 3; i < words.length; i++) {
						// connect the two vertex
						graph.connect(words[0], words[i], graph.distance(graph.vertices.get(words[i]).x, graph.vertices.get(words[i]).y, graph.vertices.get(words[0]).x, graph.vertices.get(words[0]).y));
					}
				}
		    }
		    br.close();
			return output; // write will be assigned to this text
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	// just simply write the file with the current write string
	public void writeFile() {
		try {
			File file = new File("GPS_History");
			FileWriter myWriter = new FileWriter("GPS_History");
			int line = 0;
			myWriter.write(write);
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		GPS run = new GPS();
	}
}
