import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class BouncingBall extends Frame implements ActionListener {
	
	private Canvas canvas;
	private LinkedList<Ball> ball_all_list = new LinkedList<Ball>();
	private Object Lock1 = new Object();
	private boolean exit = false;
	
	public BouncingBall(String title) {
		
		super(title);
		canvas = new Canvas();
		add("Center", canvas);
		Panel p = new Panel();
		Button s = new Button("Start");
		Button c = new Button("Close");
		p.add(s);
		p.add(c);
		s.addActionListener(this);
		c.addActionListener(this);
		add("South", p);
	}

	public void actionPerformed(ActionEvent evt) {
		
		if (evt.getActionCommand() == "Start") {
			
			/*
			 * Start with 5 Balls
			 */
			Ball b1 = new Ball(canvas, 20, Color.BLACK, 130, 100);
			Ball b2 = new Ball(canvas, 20, Color.BLACK, 170, 100);
			Ball b3 = new Ball(canvas, 20, Color.BLACK, 200, 100);
			Ball b4 = new Ball(canvas, 20, Color.BLACK, 230, 100);
			Ball b5 = new Ball(canvas, 20, Color.BLACK, 200, 140);

			b1.setDir(-2, -1);
			b2.setDir(-1, -2);
			b3.setDir(1, -2);
			b4.setDir(2, 1);
			b5.setDir(-1, 2);

			b1.start();
			b2.start();
			b3.start();
			b4.start();
			b5.start();
			
		} else if (evt.getActionCommand() == "Close")
			System.exit(0);
	}

	public static void main(String[] args) {
		
		Frame f = new BouncingBall("Bounce Thread");
		f.setSize(400, 300);
		WindowDestroyer listener = new WindowDestroyer();
		f.addWindowListener(listener);
		f.setVisible(true);
	}

	class Ball extends Thread {
		
		private Canvas box;
		private int size = 20;
		private int rad = 10;
		private int x = 0, y =0;
		private int dx = 5, dy = 5;
		private Color color;
		private boolean alive = true;

		public Ball(Canvas c, int size, Color color) {
			
			this(c, size, color, -1, -1);
		}
		public Ball(Canvas c, int size, Color color, int x, int y) {
			
			ball_all_list.add(this);
			box = c;
			this.size = size;
			this.rad = size/2;
			this.color = color;
			if(x != -1) this.x = x;
			if(y != -1) this.y = y;			
		}
		public void setDir(int dx, int dy) {
			
			this.dx = dx;
			this.dy = dy;
		}
		public void setSize(int size) {
			
			this.size = size;
			this.rad = size/2;
		}
		public void draw() {
			
			Graphics g = box.getGraphics();
			g.setXORMode(box.getBackground());
			g.setColor(this.color);
			g.fillOval(x, y, size, size);
			g.dispose();
		}

		public void move() {
			
			if(!this.alive) return;
			x += dx;
			y += dy;
			Dimension d = box.getSize();
			if (x < 0) {
				x = 0;
				dx = -dx;
			}
			if (x + size >= d.width) {
				x = d.width - size;
				dx = -dx;
			}
			if (y < 0) {
				y = 0;
				dy = -dy;
			}
			if (y + size >= d.height) {
				y = d.height - size;
				dy = -dy;
			}
		}
		public boolean collisionCheck() {
			
			if(!this.alive) return false;
			Ball b1 = this;
			Ball b2;
			boolean boooom = false;

			for(int i=0; i<ball_all_list.size(); i++)
			{
				b2 = ball_all_list.get(i);
				if(b1 == b2) continue;
				synchronized(Lock1) {
				if (isCollide(b1, b2) && b1.alive && b2.alive) {
					/*
					 * If b1-b2 collide, set Ball.alive false
					 * And make four small Balls.  
					 */
					//System.out.println("Collide! : " + b1.size + ", " + b2.size + "\nb1, b2 : " + b1.alive + b2.alive);
					b1.alive = false; b2.alive = false;
					boooom = true;
					split(b1, b2);
					split(b2, b1);
					break;
				}
				}
			}
			return boooom;
		}
		public Boolean isCollide(Ball b1, Ball b2) {
			/*
			 * Check whether b1 b2 collide
			 */
			int center1_x = b1.x + b1.rad;
			int center1_y = b1.y + b1.rad;
			int center2_x = b2.x + b2.rad;
			int center2_y = b2.y + b2.rad;
			double distPower = ((center1_x - center2_x)*(center1_x - center2_x) + (center1_y - center2_y)*(center1_y - center2_y));
			double radiusPower = (b1.rad + b2.rad) * (b1.rad + b2.rad);
			if(distPower < radiusPower) return true;
			else return false;
		}
		public void split(Ball b1, Ball b2) {
			
			if(b1.size <= 3) {
				//System.exit(0);
				//exit = true;
				return ;
			}
			int center_x = b1.x + b1.rad;	// b1's center coordinate
			int center_y = b1.y + b1.rad;
			int center_x_b2 = b2.x + b2.rad;	// b2's center coordinate
			int center_y_b2 = b2.y + b2.rad;
			int nsize = size/2;	
			int ndx1, ndy1, ndx2, ndy2;
			int ncenter1_x, ncenter1_y;
			int ncenter2_x, ncenter2_y;
			double tmpdx = b2.x - b1.x;
			double tmpdy = b1.y - b2.y;
			double angle = Math.atan2(tmpdy, tmpdx);
			double nangle1 = angle + Math.toRadians(90);
			double nangle2 = angle - Math.toRadians(90);

			ncenter1_x = (int)(Math.cos(nangle1)*nsize) + center_x;
			ncenter1_y = -(int)(Math.sin(nangle1)*nsize) + center_y;
			ncenter2_x = (int)(Math.cos(nangle2)*nsize) + center_x;
			ncenter2_y = -(int)(Math.sin(nangle2)*nsize) + center_y;
			
			double angle_for_nb1 = 0, angle_for_nb2 = 0;	
			angle_for_nb1 = Math.atan2((center_y_b2 - ncenter1_y), (ncenter1_x - center_x_b2));
			angle_for_nb2 = Math.atan2((center_y_b2 - ncenter2_y), (ncenter2_x - center_x_b2));

			ndx1 = (int)(Math.cos(angle_for_nb1)*6);
			ndy1 = -(int)(Math.sin(angle_for_nb1)*6);
			ndx2 = (int)(Math.cos(angle_for_nb2)*6);
			ndy2 = -(int)(Math.sin(angle_for_nb2)*6);

			Ball nb1 = new Ball(box, nsize, Color.black, ncenter1_x-nsize/2, ncenter1_y-nsize/2);
			Ball nb2 = new Ball(box, nsize, Color.black, ncenter2_x-nsize/2, ncenter2_y-nsize/2);
			nb1.setDir(ndx1, ndy1);
			nb2.setDir(ndx2, ndy2);
			nb1.start();
			nb2.start();
			
		}
		public void run() {
			
			draw();
			while(!exit) {
				draw();
				move();	
				boolean dead = collisionCheck();
				if(dead) {					
					//System.out.println("Dead!");
					break;
				}
				if(!this.alive) break;	
				draw();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}