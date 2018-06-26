import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.*;
import java.awt.*;

public class GeneticDrift implements Runnable{
	
	//final static boolean wrapped = true;
	//final static int width = 256*3;
	//final static int height = 192*3;
	final static int width = (int) 100;
	final static int height = (int) 70;
	final static int psize = 6;
	final static int space = 0;
	
	static Thread t1;
	static WorldVisualizer visualizer;
	static Clock clock;
	static GameMaster gm = new GameMaster();
	
	public static void main(String[] args) {
		
		System.out.println("Initializing thread.");
		JFrame f = new JFrame("World");		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		f.setSize(psize*width + space*(width-1),psize*height+space*(height-1) );
		Neighborhood[][] edges = WorldGenerator.generateNeighborhoods(width,height);
		World world = WorldGenerator.generateEmptyWorld(edges,width,height);
		world = WorldGenerator.addRandomCell(world,Color.RED,(int)((width+(width/10))/2),(int)(height/2));
		world = WorldGenerator.addRandomCell(world,Color.GREEN,(int)((width-(width/10))/2),(int)(height/2));
		world = WorldGenerator.addRandomCell(world,Color.BLUE,(int)(width/2),(int)((height-height/5)/2));
		gm.setWorld(world);
		visualizer = new WorldVisualizer(world,psize,space);
		
		// Now draw it
		//f.add(clock);
		f.add(visualizer);
		f.setVisible(true);
		f.setResizable(false);
		
		t1 = new Thread(new GeneticDrift());
		t1.start();
		
	}
	
	public void run( ) {
		while(true) {
			
			try{
				Thread.sleep(1);
				//System.out.println("Waiting for a second...");
			} catch(InterruptedException e) {
				System.out.println("An exception has been thrown.");
			}
			
			visualizer.setWorld(gm.nextStep());
			//System.out.println("Now repaint.");
			visualizer.repaint();
			
			
		}
	}
	
	
}

class WorldVisualizer extends JPanel{
	
	Cell[][] vertices;
	int width;
	int height;
	int psize = 1;
	int space = 0;
	
	public WorldVisualizer(World w) {
		setBackground(Color.BLACK);
		this.vertices = w.getVertices();
		width = w.getWidth();
		height = w.getHeight();	
	
	}
	
	public WorldVisualizer(World w, int psize, int space) {
		this(w);
		this.psize = psize;
		this.space = space;
	}
	
	public void setWorld(World w) {
		this.vertices = w.getVertices();
		width = w.getWidth();
		height = w.getHeight();			
	}

	public void paintComponent(Graphics g) {
	
		super.paintComponent(g);
		
		for(int y=0; y < height; y++) {
			for(int x=0; x < width; x++) {
				g.setColor(vertices[x][y].getColor());
				g.fillRect((psize+space)*x,(psize+space)*y,psize,psize);
			}
			
		}
	}	
	
}

class Clock extends JPanel {

	int clock;
	
	public Clock () {
		// Nothing
	}
	
	public void setClock(int c) {
		clock = c;
	}

	public void paintComponent(Graphics g) {
	
		super.paintComponent(g);
	
		g.setColor(Color.black);
		g.fillRect(0,0,50,15);
		g.setColor(Color.white);
		g.drawString("t = " + clock, 0, 10);
	}
}


class GameMaster {

	LinkedList<Cell> alive;
	static World world;
	static World worldNew;
	static Cell[][] vertices; // Vertices
	static Neighborhood[][] edges; // Edges

	static int width;
	static int height;
	int clock = 0;
		
	public GameMaster() {
		// Nothing
	}
	
	
	public GameMaster(World world) {
		this.world = world;
		vertices = world.getVertices();
		edges = world.getEdges();
		width = world.getWidth();
		height = world.getHeight();
		worldNew = WorldGenerator.generateEmptyWorld(edges,width,height);
		//verticesNew = worldNew.getVertices();
	}
	
	public void setWorld(World world) {
		this.world = world;
		vertices = world.getVertices();
		edges = world.getEdges();
		width = world.getWidth();
		height = world.getHeight();
		worldNew = WorldGenerator.generateEmptyWorld(edges,width,height);
	}
	
	public World nextStep() {
		
		alive = new LinkedList<Cell>();
		worldNew = WorldGenerator.generateEmptyWorld(edges,width,height);
		
		// Add living cells to LinkedList and clone old world into the new one
		Cell c;
		
		for(int y=0; y<height; y++) {			
			for(int x=0; x<width; x++) {
				c = vertices[x][y];
				if(c.isEmpty == false) {
					worldNew.getVertices()[x][y].cloneCell(c);
					alive.add(c);							
				} 
			}				
		}

		// Randomize LinkedList to prevent skewage
		Collections.shuffle(alive);		

		int aliveNum = alive.size();
		Random r = new Random();
		
		// Let living cells give birth 		
		for(int i=0; i<aliveNum; i++) {
			alive.getLast().giveBirth(worldNew);			
			alive.removeLast();		
		}
		
		
		setWorld(worldNew);		
		return world;
	}
	
	public World doNothing() {
		clock++;
		return world;
	}
	
	public int getClock() {
		return clock;
	}
		
}


class WorldGenerator {
	

	// Creates neighborhoods for every cells and checks if neighbors are over the edges		
	// The external administration by the GameMaster allows different topologies to be easily implemented
	// By default Von Neumann neighborhoods are chosen
	static public Neighborhood[][] generateNeighborhoods(int width, int height) {
		
		Neighborhood[][] edges = new Neighborhood[width][height];
		
		for(int y=0; y<height; y++) {			
			for(int x=0; x<width; x++) {
				edges[x][y]= new Neighborhood(width, height,new int[]{x,y});
				edges[x][y].createNeighborhood(); // create neighborhood
			}				
		}
		
		return edges;
	}
	

	static public World generateEmptyWorld(Neighborhood[][] edges, int width, int height) {
	
		Cell[][] vertices = new Cell[width][height];
		
		for(int y=0; y<height; y++) {			
			for(int x=0; x<width; x++) {
				vertices[x][y] = new Cell(x,y);
			}				
		}
		
		return (new World(vertices,edges,width,height));
	
	}
	
	static public World generateRandomWorld(Neighborhood[][] edges, int width, int height) {
	
		Cell[][] vertices = new Cell[width][height];
		Random r = new Random();
		
		for(int y=0; y<height; y++) {			
			for(int x=0; x<width; x++) {
				vertices[x][y]= new Cell(r,x,y);
			}				
		}
		
		return (new World(vertices,edges,width,height));
	
	}
	
	static public World addRandomCell(World world) {

		int width = world.getWidth();
		int height = world.getHeight();
		Cell[][] v = world.getVertices();
		Neighborhood[][] e = world.getEdges();
		Random r = new Random();
		
		int x = r.nextInt(width);
		int y = r.nextInt(height);
		
		v[x][y] = new Cell(r,x,y);
		
		return (new World(v, e, width, height));
		
	}

	static public World addRandomCell(World world, int x, int y) {

		int width = world.getWidth();
		int height = world.getHeight();
		Cell[][] v = world.getVertices();
		Neighborhood[][] e = world.getEdges();
		Random r = new Random();
		
		v[x][y] = new Cell(r,x,y);
		
		return (new World(v, e, width, height));
		
	}
	
	static public World addRandomCell(World world, Color c, int x, int y) {

		int width = world.getWidth();
		int height = world.getHeight();
		Cell[][] v = world.getVertices();
		Neighborhood[][] e = world.getEdges();
		Random r = new Random();
		
		v[x][y] = new Cell(r,x,y);
		v[x][y].setColor(c);
		
		return (new World(v, e, width, height));
		
	}
	

}

class World {
	
	Cell[][] vertices;
	Neighborhood[][] edges;
	int width;
	int height;
	
	public World(Cell[][] vertices, Neighborhood[][] edges, int width, int height) {
		this.vertices = vertices;
		this.edges = edges;
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Cell[][] getVertices() {
		return vertices;
	}
	
	public Neighborhood[][] getEdges() {
		return edges;
	}

	public void setVertices(Cell[][] vertices) {
		this.vertices = vertices;
	}
	
	public void setEdges(Neighborhood[][] edges) {
		this.edges = edges;
	}	
	
}


class Cell {
	
		//Neighborhood nbh;
	
		boolean isEmpty = true;

		int x,y;
		
		String race;
		Color c = Color.BLACK;		
		int maxAge;
		int birthRate; 
		int age; 

		// Constructor for an empty cell
		public Cell (int x, int y) {
			this.x = x;
			this.y = y;
			// Nothing
		}
		
		public Cell(Random r, int x, int y) {
			this.x = x;
			this.y = y;
			randomLivingCell(r);
		}
		
		public Cell cloneCell(Cell cell) {
			race = cell.race;
			isEmpty = cell.isEmpty;
			c = cell.c;			
			return this;
		}
		
		public Cell makeBaby(Cell cell) {
			Random r = new Random();
			//if(isEmpty || isColorUnequal(cell)) {
			if(isEmpty || (race!=cell.race)) {
				race = cell.race;
				isEmpty = cell.isEmpty;
				float f = r.nextFloat();
				c = cell.getColor();
				/*
				if(f < 0.05) {
					c = CellColor.randomColor(cell.getColor());
					//c = new Color(0, 25 + r.nextInt(231),0);
				} else {
					c = cell.getColor();
				}
				*/
			} 
			return this;
		}
		
		public void giveBirth(World worldNew) {
			
			int[] pos = new int[2];
			worldNew.vertices[x][y].cloneCell(this);
			pos = worldNew.getEdges()[x][y].getRandomNeighbor().getPos();
			worldNew.vertices[pos[0]][pos[1]].makeBaby(this);
		}
		
		public void randomLivingCell(Random r) {
			race = ""+(char) (97 + r.nextInt(26));
			isEmpty = false;
			c = (new Color[]{Color.RED,Color.GREEN,Color.BLUE})[r.nextInt(3)];
			int maxAge = 1+r.nextInt(100);
			int birthRate = 1 + r.nextInt(100);
			int age = 0;
			
		}

		public void setColor(Color c) {
			this.c = c;
		}
		
		public int getPosX() {
			return x;
		}
		
		public int getPosY() {
			return y;
		}
		
		public Color getColor() {
			return c; 
		}

}

// Checks for neighbours
class Neighborhood {
	
	int x,y;
	int width, height;
	Random r = new Random();
	Neighbor[] nbh;
	
	public Neighborhood(int width, int height, int[] pos) {
		x = pos[0]; 
		y = pos[1];
		this.width = width;
		this.height = height;
	}
	
	// Fills LinkedList with neighbors
	public void createNeighborhood() {

		// Creates a Von-Neumann neighborhood
		//
		//				[ ][X][ ]
		//				[X][O][X]
		//				[ ][X][ ]
		//
		
		nbh = new Neighbor[4];
		
		if(y+1  >= height) {
			nbh[0] = new Neighbor(new int[]{x,0},true);
		} else {
			nbh[0] = new Neighbor(new int[]{x,y+1},false);
		}
		
		if(x+1 >= width) {
			nbh[1] = new Neighbor(new int[]{0,y},true);
		} else {
			nbh[1] = new Neighbor(new int[]{x+1,y},false);
		}
	
		if(x-1 < 0) {
			nbh[2] = new Neighbor(new int[]{width-1,y}, true);
		} else {
			nbh[2] = new Neighbor(new int[]{x-1,y}, false);
		}

		if(y-1 < 0) {
			nbh[3] = new Neighbor(new int[]{x,height-1}, true);
		} else {
			nbh[3] = new Neighbor(new int[]{x,y-1},false);
		}

	}
	
	public Neighbor getRandomNeighbor() {
		return nbh[r.nextInt(4)];
	}

}


class Neighbor {
	
	int[] pos;
	boolean isOverEdge = false;
	
	public Neighbor(int[] pos, boolean isOverEdge) {
		this.isOverEdge = isOverEdge;
		this.pos = pos;
	}
	
	public int[] getPos() {
		return pos;		
	}
	
}

class CellColor {
	
	public static Random rnd = new Random();
	
	/*
	int r,g,b;
	
	public CellColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	*/
	
	public static Color randomColor(Color c) {
		
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		int j = 25 + rnd.nextInt(231);
		int length = (int) Math.sqrt(r*r + g*g + b*b);
		if(length!=0) {
			r = (int)((r/length)*j);
			g = (int)((g/length)*j);
			b = (int)((b/length)*j);
		} else {
			r = j;
			g = j;
			b = j;
		}
		
		return (new Color(r,g,b));
		
	}

}