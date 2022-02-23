
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.awt.event.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jadex.adapter.jade.tools.logger.gui.ToggleDisplayingAction;

import java.util.ArrayList;
import java.util.List;


public class CarWashGUI extends BaseAgent {

	short GUI_WIDTH = 1800;
	short GUI_HEIGHT = 600;
	short GUI_XSTART = 0;
	short GUI_YSTART = 0;

	int manuelCount = 0;
	int automaticCount = 0;

	int creationTimeManuel = 30;
	int creationTimeAutomatic = 35;
	final int SIMDURATION = 10 * 60 ; // 10 minutes
	int simClock = 0;

	ArrayList<CarEntity> entityList = new ArrayList<CarEntity>(); 

	GUIFrame guiframe;

	private AgentController t1 = null;

	ContainerController container1 = null;
	String currentEntityType ;

	public String createNextEntity(String entityType, int creationTime, int count) {

		System.out.println("** createNextEntity - creationTime of " + entityType + " is " + creationTime);
		
		
		addBehaviour(new WakerBehaviour(this, creationTime) {
			protected void handleElapsedTimeout() {

				System.out.println("********* handle time elapsed  " + creationTime + " entity: " + entityType);

				createEntity(entityType, creationTime, count);
				
				
				
				

			}
		});
		currentEntityType=entityType;
		
		return currentEntityType;
	}

	OneShotBehaviour carArrivalForManuelWashing = new OneShotBehaviour() {
		public void action() {
			// TODO Auto-generated method stub
			
			BetaDistribution betaDistribution =  new BetaDistribution(0.60, 0.76);
			
			double randomValue = 6.5+49*betaDistribution.sample();

			creationTimeManuel += Math.round(randomValue);

			++manuelCount;
			createNextEntity("Manuel", (int) creationTimeManuel * 1000, manuelCount);

			System.out.println(" interarrival time for the next Manuel Entity : " + randomValue + " creation Time M  "
					+ creationTimeManuel + " manuelCount " + manuelCount);
		}
	};

	// Creates a Automatic Batch
	OneShotBehaviour carArrivalForAutomatic = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			UniformIntegerDistribution uniformDistribution = new UniformIntegerDistribution(24, 60);
			double randomValue = uniformDistribution.sample();
			System.out.println(" interarrival time for the next Automatic Entity : " + randomValue + " creation Time A  "
					+ creationTimeAutomatic + " AutomaticCount " + automaticCount );
			creationTimeAutomatic += Math.round(randomValue);

			
				++automaticCount;
				createNextEntity("Automatic", creationTimeAutomatic * 1000, automaticCount);
			

		}
	};

	OneShotBehaviour runSimulation = new OneShotBehaviour() {

		public void action() {

			System.out.println("In the runSimulation one sho behaviour");	

			{
				System.out.println("creationTimeManuel" + creationTimeManuel + " creationTimeAutomatic " + creationTimeAutomatic);
			}
		}
	};

	OneShotBehaviour rinseAndDry = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			TriangularDistribution triangularDistribution = new TriangularDistribution(8, 10, 15);
			double randomValue = triangularDistribution.sample();
			System.out.println(" Rinse and dry process will take " + randomValue + " minutes.   ");
			creationTimeManuel += Math.round(randomValue);

		}
	};
	
	OneShotBehaviour foaming = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			TriangularDistribution triangularDistribution = new TriangularDistribution(5, 7, 10);
			double randomValue = triangularDistribution.sample();
			System.out.println(" Foaming process will take  " + randomValue + " minutes.   ");
			creationTimeManuel += Math.round(randomValue);

		}
	};
	
	OneShotBehaviour automaticCarWashing = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			TriangularDistribution triangularDistribution = new TriangularDistribution(3, 5, 10);
			double randomValue = triangularDistribution.sample();
			int value = 10;
			System.out.println("Automatic washing process will take " + value + " minutes.   ");
			creationTimeAutomatic += value;

		}
	};
	
	OneShotBehaviour interiorCleaning = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			TriangularDistribution triangularDistribution = new TriangularDistribution(18, 20, 30);
			double randomValue = triangularDistribution.sample();
			
			System.out.println(" Interior Cleaning Process will take " + randomValue + " minutes.   ");
			
			if(currentEntityType == "M")
				creationTimeManuel += Math.round(randomValue);
			else
				creationTimeAutomatic += Math.round(randomValue);
				

		}
	};
	
	OneShotBehaviour paying = new OneShotBehaviour() {

		public void action() {
			// TODO Auto-generated method stub
			TriangularDistribution triangularDistribution = new TriangularDistribution(3, 4, 5);
			double randomValue = triangularDistribution.sample();
			
			System.out.println(" Paying process will take " + randomValue + " minutes.   ");
			
			if(currentEntityType == "M")
				creationTimeManuel += Math.round(randomValue);
			else
				creationTimeAutomatic += Math.round(randomValue);

		}
	};
	public void createEntity(String agentType, int creationTime, int partCount) {

		Object[] dtss = new Object[3];
		dtss[0] = agentType;
		dtss[1] = partCount;
		dtss[2] = creationTime;

		String entityName = agentType + "_" + (partCount) + "_" + creationTime;

		startAgent("EntityContainer", "localhost", entityName, "EntityAgent", dtss);

		entityList.add(new CarEntity(entityName, agentType, partCount, creationTime));

		System.out.println("A new " + agentType + " is created at " + LocalTime.now());
	}

	public void startAgent(String containerName, String host, String agentName, String agentClass, Object[] dtss) {
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);

		ContainerController container = null;
		if (container1 == null) {
			container = runtime.createAgentContainer(profile);
			container1 = container;
		} else {
			container = container1;
		}

	}

	public void setup() {


		initiateGUIFrame();

		// Ticker for simulation timing
		addBehaviour(simTimer);
		addBehaviour(runSimulation);

		addBehaviour(new EvaluateMessages(this, 100));
	}

	public void initiateGUIFrame() {
		guiframe = new GUIFrame();
		guiframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiframe.setLocationRelativeTo(null);
		guiframe.setBounds(GUI_XSTART, GUI_YSTART, GUI_WIDTH, GUI_HEIGHT);

		guiframe.setVisible(true);

		guiframe.setLayout(new BorderLayout());
		guiframe.setName("SimpleSim");
		guiframe.setTitle("JADE GUI Mainframe");
		guiframe.setVisible(true);

		guiframe.createBufferStrategy(2);
		guiframe.addMouseMotionListener(guiframe);
		guiframe.addMouseListener(guiframe);

	}

	public class EvaluateMessages extends TickerBehaviour {

		private static final long serialVersionUID = 657002871747329933L;

		public EvaluateMessages(Agent a, long interval) {
			super(a, interval);
		}

		protected void onTick() {

			ACLMessage msg = receive();

			if (msg != null) {
				
				String content = msg.getContent();
				
					Object[] dtss = new Object[0];

					

					int crTime = creationTimeManuel > creationTimeAutomatic ? creationTimeManuel + 3000 : creationTimeAutomatic + 3000;
					
			}

		}

	}

	public class CheckAgents extends TickerBehaviour {

		private static final long serialVersionUID = 657002871747329933L;

		public CheckAgents(Agent a, long interval) {
			super(a, interval);
		}

		public void infoBox(String infoMessage, String titleBar) {
			JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
		}

		protected void onTick() {


		}
	}

	TickerBehaviour simTimer = new TickerBehaviour(this, 1000) {
		protected void onTick() {

			if (simClock % 10 == 0)
				System.out.println("sim Time:" + simClock + " simDuration ");

			if (simClock < SIMDURATION) {

				if (simClock == creationTimeManuel)
					addBehaviour(carArrivalForManuelWashing);

				if (simClock == creationTimeAutomatic)
					addBehaviour(carArrivalForAutomatic);

				simClock++;

				guiframe.updateScenery();

			}
		}
	};

	WakerBehaviour info = new WakerBehaviour(this, 10) {
		protected void handleElapsedTimeout() {

		}
	};

	class RunSimulation implements ActionListener {

		public void actionPerformed(ActionEvent e) {

		}
	}

	public class GUIFrame extends JFrame implements MouseMotionListener, MouseListener {

		Graphics g;
		private BufferStrategy strategy;
		int mX, mY;
		int HOR_OFFSET = 0;
		int VER_OFFSET = 0;
		TextField tf1 = new TextField();
		TextField tf2 = new TextField();
		int drawMode = 0;

		JButton startButton = new JButton("RUN>");

		ButtonGroup buttonGroup;

		public void GUIFrame() {

		}

		public void mouseMoved(MouseEvent me) {

		}

		public void mousePressed(MouseEvent e) {

		}

		public void mouseReleased(MouseEvent e) {

		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {

		}

		public void mouseClicked(MouseEvent e) {

			mX = (int) e.getPoint().getX() - HOR_OFFSET; // +HOR_OFFSET;
			mY = (int) e.getPoint().getY() - VER_OFFSET; // +VER_OFFSET;

		}

		public void mouseDragged(MouseEvent me) {
			mouseMoved(me);
		}

		public Graphics2D getGraphicsContext() {
			return (Graphics2D) (g = (Graphics2D) strategy.getDrawGraphics());
		}

		public void render() {

			BufferStrategy bs = this.getBufferStrategy();
			if (bs == null) {
				createBufferStrategy(3);
				return;
			}

			Graphics g = bs.getDrawGraphics();

			updateScenery();
			g.dispose();
			bs.show();
		}

		public void updateScenery()

		{

			BufferStrategy bs = this.getBufferStrategy();
			if (bs == null) {
				this.createBufferStrategy(3);
				return;
			}

			Graphics g = bs.getDrawGraphics();

			g.clearRect(0, 0, guiframe.getWidth(), guiframe.getHeight());
			g.setColor(Color.RED);

			g.drawString("Simulation Time: " + simClock, 50, 60);

			int HOROFF = 200;
			int VEROFF = 150;
			int stepWidth = 10;
			int lineLength = 20;

			for (int i = 0; i < simClock; i++)
				g.drawLine(HOROFF + (i * stepWidth), VEROFF, HOROFF + (i * stepWidth), VEROFF + lineLength);

			int i = 1;
			for (CarEntity entity : entityList) {
				g.drawString(entity.entityName, 50, 80 + i++ * 15);

				if (entity.entityType.compareTo("Manuel") == 0)
					g.drawString("M", HOROFF + entity.creationTime * stepWidth / 1000, VEROFF - 30);

				if (entity.entityType.compareTo("Automatic") == 0)
					g.drawString("A", HOROFF + entity.creationTime * stepWidth / 1000,
							VEROFF + 30 + lineLength + (entity.entityOrder % 4) * 15);

				

			}

			g.dispose();
			bs.show();

		}

	}
}
