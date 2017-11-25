/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainarchlab;

/**
 *
 * @author Shrikant Pawar
 * Usage: This program can create truth tables for two input variables.
 * This program can evaluate boolean expressions.
 * This program also can generate electrical circuit design application
 * for NAND gates
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.*;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;


public class mainArchlab {
    
        
    // NAND gate drag'n'drop demo with connections etc
 JFrame frame = new JFrame("Gate Generator");
 ArrayList<Component> components = new ArrayList<Component>();
 ArrayList<Connection> lines = new ArrayList<Connection>();
 JLabel prompt = new JLabel("Welcome GSU Computer Architecture Class Fall 2017 to gate generator");
 DrawPanel drawPanel = new DrawPanel();
 boolean showInputs = true, showOutputs = true;
 boolean addingComponent = false, selectingOutput = false, selectingInput = false;
 Output selectedOutput = null;
 mainArchlab() {
    frame.setMinimumSize(new Dimension(500, 400));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(prompt, BorderLayout.PAGE_START);
    drawPanel.setLayout(null);
    frame.add(drawPanel);
    JPanel buttonPanel = new JPanel();
    frame.add(buttonPanel, BorderLayout.PAGE_END);
    JButton newComponentButton = new JButton("Add a Component");
    buttonPanel.add(newComponentButton);
    newComponentButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent arg0) {
          addNewComponent();
       }
    });
    JButton newConnectionButton = new JButton("Add a Connection");
    buttonPanel.add(newConnectionButton);
    newConnectionButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent arg0) {
          addNewConnection();
       }
    });
    frame.setVisible(true);
 }
 void addNewComponent() {
    prompt.setText("Click to add component");
    addingComponent = true;
 }
 void addNewConnection() {
    prompt.setText("Click an output to connect...");
    selectingOutput = true;
    showInputs = false;
    drawPanel.repaint();
 }
 abstract class Component extends JLabel implements MouseListener, MouseMotionListener {
    // A component is a movable draggable object with inputs and outputs
    ArrayList<Input> inputs = new ArrayList<Input>();
    ArrayList<Output> outputs = new ArrayList<Output>();
    public Component(String text, int x, int y) {
       super(text);
       addMouseListener(this);
       addMouseMotionListener(this);
    }
    public void addOutput(Output o) {
       outputs.add(o);
    }
    public void addInput(Input i) {
       inputs.add(i);
    }
    @Override
    public void paintComponent(Graphics g) {
       // can do custom drawing here, but just show JLabel text for now...
       super.paintComponent(g);
       Graphics2D g2d = (Graphics2D) g;
       if (showInputs) {
          for (Input in : inputs) {
             in.paintConnector(g2d);
          }
       }
       if (showOutputs) {
          for (Output out : outputs) {
             if (out.isAvailable()) out.paintConnector(g2d);
          }
       }
    }
    int startDragX, startDragY;
    boolean inDrag = false;
    @Override
    public void mouseEntered(MouseEvent e) {
       // not interested
    }
    @Override
    public void mouseExited(MouseEvent e) {
       // not interested
    }
    @Override
    public void mousePressed(MouseEvent e) {
       startDragX = e.getX();
       startDragY = e.getY();
    }
    @Override
    public void mouseReleased(MouseEvent e) {
       if (inDrag) {
          System.out.println("\"" + getText().trim() + "\" dragged to " + getX() + ", "
                + getY());
          inDrag = false;
       }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
       if (selectingInput) {
          for (Input in : inputs) {
             if (in.isAvailable() && in.contains(e.getPoint())) {
                System.out.println("Click on input connector of \"" + getText().trim()
                      + "\"");
                lines.add(new Connection(selectedOutput, in));
                drawPanel.repaint();
                selectingInput = false;
                showOutputs = true;
                prompt.setText(" ");
             }
          }
       }
       if (selectingOutput) {
          for (Output out : outputs) {
             if (out.isAvailable() && out.contains(e.getPoint())) {
                System.out.println("Click on output connector of \"" + getText().trim()
                      + "\"");
                selectedOutput = out;
                selectingOutput = false;
                showOutputs = false;
                selectingInput = true;
                showInputs = true;
                prompt.setText("Click an input to connect...");
                drawPanel.repaint();
                return;
             }
          }
       }
    }
    @Override
    public void mouseDragged(MouseEvent e) {
       int newX = getX() + (e.getX() - startDragX);
       int newY = getY() + (e.getY() - startDragY);
       setLocation(newX, newY);
       inDrag = true;
       frame.repaint();
    }
    @Override
    public void mouseMoved(MouseEvent arg0) {
       // not interested
    }
 }
 class NANDgate extends Component {
    NANDgate(int x, int y) {
       super("    NAND " + (components.size() + 1), x, y);
       setVerticalAlignment(SwingConstants.CENTER);
       setBounds(x, y, 70, 30);
       setBorder(new LineBorder(Color.black, 1));
       new Input(this, 0, 6);
       new Input(this, 0, getHeight() - 6);
       new Output(this, getWidth(), getHeight() / 2);
    }
 }
 abstract class Connector { // subclasses: Input, Output
    Component component;
    ArrayList<Connection> connections = new ArrayList<Connection>();
    int maxConnections;
    int x, y; // x,y is the point where lines connect
    int w = 10, h = 10; // overall width & height of Connector
    Shape shape;
    Color color;
    Connector(Component c, int x, int y) {
       this.component = c;
       this.x = x;
       this.y = y;
    }
    boolean isAvailable() {
       // check that max fanout/fanin will not be exceeded
       return connections.size() < maxConnections;
    }
    void addConnection(Connection con) {
       if (isAvailable()) connections.add(con);
       // may want to show error somehow if not available?
    }
    public int getX() {
       return component.getX() + x;
    }
    public int getY() {
       return component.getY() + y;
    }
    public boolean contains(Point p) {
       // used to check for mouse clicks on this Connector
       return shape.contains(p);
    }
    public void paintConnector(Graphics2D g2d) {
       if (isAvailable()) g2d.setColor(color);
       else g2d.setColor(Color.lightGray);
       g2d.fill(shape);
    }
 }
 class Output extends Connector {
    // Triangle, left facing, at RHS of component (points away from component)
    Output(Component owner, int x, int y) {
       super(owner, x, y);
       owner.addOutput(this);
       maxConnections = 4; // fanout for output
       x = owner.getWidth(); // RHS of Component
       y = owner.getHeight() / 2;
       Polygon p = new Polygon();
       p.addPoint(x - w, y - h / 2);
       p.addPoint(x - w, y + h / 2);
       p.addPoint(x, y);
       shape = p;
       color = Color.red;
    }
 }
 class Input extends Connector {
    // Triangle, left facing, at LHS of component (points into the component)
    Input(Component owner, int x, int y) {
       super(owner, x, y);
       owner.addInput(this);
       maxConnections = 1; // only one connection to an input
       Polygon p = new Polygon();
       p.addPoint(x, y - h / 2);
       p.addPoint(x, y + h / 2);
       p.addPoint(x + w, y);
       shape = p;
       color = Color.blue;
    }
 }
 class Connection {
    // a Connection connects an Output to an Input.
    Output output;
    Input input;
    public Connection(Output output, Input input) {
       this.output = output;
       this.input = input;
       output.addConnection(this);
       input.addConnection(this);
    }
    public void paintConnection(Graphics2D g2d) {
       g2d.drawLine(output.getX(), output.getY(), input.getX(), input.getY());
    }
 }
 class DrawPanel extends JPanel {
    // contains Components, draws lines (connecting pairs of components)
    DrawPanel() {
       addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
             if (addingComponent) {
                Component c = new NANDgate(e.getX(), e.getY());
                components.add(c);
                drawPanel.add(c);
                addingComponent = false;
                prompt.setText("Use mouse to drag components");
                drawPanel.repaint();
             }
          }
       });
    }
    @Override
    public void paintComponent(Graphics g) {
       super.paintComponent(g);
       Graphics2D g2d = (Graphics2D) g;
       g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
       for (Connection line : lines) {
          line.paintConnection(g2d);
       }
    }
 }
    
    
 
    
    
    public static void main(String[] args){
/*
        boolean p,q;

        System.out.println("P\tQ\tAND\tOR\tXOR\tNOT");

        p = false;
        q = false;
        System.out.print(p + "\t" + q + "\t" + (p&&q) + "\t");
        System.out.println((p||q)+"\t"+(p^q)+"\t"+(!p));

        p = false;
        q = true;
        System.out.print(p + "\t" + q + "\t" + (p&&q) + "\t");
        System.out.println((p||q)+"\t"+(p^q)+"\t"+(!p));

        p = true;
        q = false;
        System.out.print(p + "\t" + q + "\t" + (p&&q) + "\t");
        System.out.println((p||q)+"\t"+(p^q)+"\t"+(!p));

        p = true;
        q = true;
        System.out.print(p + "\t" + q + "\t" + (p&&q) + "\t");
        System.out.println((p||q)+"\t"+(p^q)+"\t"+(!p));

        System.out.println();
        
        */

        withBinary();
        new mainArchlab();
        
        Scanner keyboard = new Scanner(System.in);
        
       
        System.out.println("String consisting of only 0, 1, A, B, C where A = AND, B = OR, C = XOR");
        System.out.println("Enter your string to evaluate boolean expression:");
        String s = keyboard.nextLine();
        
        //	String s = "1C1B1B0A0";
		StringBuffer sb = new StringBuffer(s);
                System.out.println("The value of your expression is:" );
		System.out.println(evaluateBoolExpr(sb));
                
       
                           
    }

    public static void withBinary(){


        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter values of variable 'a' to get your truth table:");
       int a = keyboard.nextInt(); 
       System.out.println("Enter values of variable 'b' to get your truth table:");
       int b = keyboard.nextInt();
        int and = a&b;
        int or = a|b;
        int xor = a^b;
        int not  = a;

        System.out.println("A\tB\tAND\tOR\tXOR\tNOT");
        if(a==0 && b == 0 )
            not = 1;
            System.out.println(a + "\t" + b + "\t" + and + "\t" + or + "\t" + xor + "\t" + (not));

        b=1;
        and = a&b;
        or = a|b;
        xor = a^b;
        not = a;
        if(a==0 && b == 1)
            System.out.println(a + "\t" + b + "\t" + and + "\t" + or + "\t" + xor + "\t" + (not));

        a=1;    
        b=0;
        not = b;
        and = a&b;
        or = a|b;
        xor = a^b;
        not = a;
        if(a==1 && b == 0)
            System.out.println(a + "\t" + b + "\t" + and + "\t" + or + "\t" + xor + "\t" + (not));  

        a=1;    
        b=1;
        not = 0;
        and = a&b;
        or = a|b;
        xor = a^b;
        if(a==1 && b == 1)
            System.out.println(a + "\t" + b + "\t" + and + "\t" + or + "\t" + xor + "\t" + (not));  
        }
    
    
 	// Evaluates boolean expression
	// and returns the result
	static int evaluateBoolExpr(StringBuffer s)
	{
		int n = s.length();
	
		// Traverse all operands by jumping
		// a character after every iteration.
		for (int i = 0; i < n; i += 2) {
	
			// If operator next to current operand
			// is AND.
			if( i + 1 < n && i + 2 < n)
			{
				if (s.charAt(i + 1) == 'A') {
					if (s.charAt(i + 2) == '0' || 
							s.charAt(i) == 0)
						s.setCharAt(i + 2, '0');
					else
						s.setCharAt(i + 2, '1');
				}
		
				// If operator next to current operand
				// is OR.
				else if ((i + 1) < n && 
						s.charAt(i + 1 ) == 'B') {
					if (s.charAt(i + 2) == '1' ||
						s.charAt(i) == '1')
						s.setCharAt(i + 2, '1');
					else
						s.setCharAt(i + 2, '0');
				}
				
				// If operator next to current operand
				// is XOR (Assuming a valid input)
				else {
					if (s.charAt(i + 2) == s.charAt(i))
						s.setCharAt(i + 2, '0');
					else
						s.setCharAt(i + 2 ,'1');
				}
			}
		}
		return s.charAt(n - 1) - '0';
	}   
         
}
