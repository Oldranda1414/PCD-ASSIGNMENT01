package pcd.ass01.simtrafficexamplesconcurrent;

import java.util.List;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import pcd.ass01.simengineconcurrent.AbstractAgent;
import pcd.ass01.simengineconcurrent.AbstractEnvironment;
import pcd.ass01.simengineconcurrent.AbstractSimulation;
import pcd.ass01.simengineconcurrent.SimulationListener;
import pcd.ass01.simtrafficbaseconcurrent.P2d;
import pcd.ass01.simtrafficbaseconcurrent.entity.TrafficLight;
import pcd.ass01.simtrafficbaseconcurrent.V2d;
import pcd.ass01.simtrafficbaseconcurrent.entity.CarAgent;
import pcd.ass01.simtrafficbaseconcurrent.environment.Road;
import pcd.ass01.simtrafficbaseconcurrent.environment.RoadsEnv;
import pcd.ass01.utils.Pair;
import java.awt.*;

import javax.swing.*;

public class RoadSimView extends JFrame implements SimulationListener {

	private RoadSimViewPanel panel;
	private static final int CAR_DRAW_SIZE = 10;
	
	public RoadSimView(AbstractSimulation<? extends AbstractEnvironment<? extends AbstractAgent>> sim) {
		super("RoadSim View");
		setSize(1500,600);
			
		panel = new RoadSimViewPanel(1500,600, sim, this); 
		panel.setSize(1500, 600);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);
		cp.add(BorderLayout.CENTER,panel);
		setContentPane(cp);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
			
	}
	
	public void display() {
		SwingUtilities.invokeLater(() -> {
			this.setVisible(true);
		});
	}
	
	
	class RoadSimViewPanel extends JPanel {
		
		List<CarAgent> cars;
		List<Road> roads;
		List<TrafficLight> sems;
		AbstractSimulation<? extends AbstractEnvironment<? extends AbstractAgent>> simulation;
		JButton startButton;
		
		public RoadSimViewPanel(int w, int h, AbstractSimulation<? extends AbstractEnvironment<? extends AbstractAgent>> sim, JFrame frame){
			this.simulation = sim;
			this.startButton = new JButton("Start");
			JTextField textField = new JTextField();
			JLabel label = new JLabel("Insert number of steps: ");
			textField.setText("1000");
			textField.setColumns(10);
			((AbstractDocument) textField.getDocument()).setDocumentFilter(new NumberOnlyFilter());
			startButton.addActionListener((click) -> {
				if (startButton.getText().equals("Stop")) {
					startButton.setText("Resume");
					try {
						this.simulation.stop();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					try {
						this.simulation.resume();
						if (startButton.getText().equals("Start")) {
							this.remove(label);
							this.remove(textField);
							new Thread(() -> {
								this.simulation.run(Integer.parseInt(textField.getText()));
								this.remove(this.startButton);
							}).start();
						}
						startButton.setText("Stop");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			this.add(label);
			this.add(textField);
			this.add(startButton);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (roads == null) return;
	        Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.clearRect(0,0,this.getWidth(),this.getHeight());
			
			Pair<Double, Double> maxBounds = new Pair<Double, Double>(
				roads.stream()
					.map(road -> {return road.getTo().x() > road.getFrom().x() ? road.getTo().x() : road.getFrom().x();})
					.reduce((r1, r2) -> {return r1 > r2 ? r1 : r2;}).get(),
				roads.stream()
					.map(road -> {return road.getTo().y() > road.getFrom().y() ? road.getTo().y() : road.getFrom().y();})
					.reduce((r1, r2) -> {return r1 > r2 ? r1 : r2;}).get() * 1.5d //it's times 1.5 because we dont want the longest road to be in a side of the screen
			);

			if (roads != null) {
				for (Road r: roads) {
					g2.drawLine(
						(int)mapValue(r.getFrom().x(), 0, maxBounds.getFirst(), 0, this.getWidth()), 
						(int)mapValue(r.getFrom().y(), 0, maxBounds.getSecond(), 0, this.getHeight()), 
						(int)mapValue(r.getTo().x(), 0, maxBounds.getFirst(), 0, this.getWidth()), 
						(int)mapValue(r.getTo().y(), 0, maxBounds.getSecond(), 0, this.getHeight())
					);
				}
			}
			
			if (sems != null) {
				for (TrafficLight s: sems) {
					if (s.isGreen()) {
						g.setColor(new Color(0, 255, 0, 255));
					} else if (s.isRed()) {
						g.setColor(new Color(255, 0, 0, 255));
					} else {
						g.setColor(new Color(255, 255, 0, 255));
					}
					P2d point = mapEntityOnRoad(s.getCurrentPosition(), s.getRoad());
					P2d mappedPoint = new P2d(
						mapValue(point.x(), 0, maxBounds.getFirst(), 0, this.getWidth()),
						mapValue(point.y(), 0, maxBounds.getSecond(), 0, this.getHeight())
					);
					g2.fillRect((int)mappedPoint.x() - 5, (int)mappedPoint.y() - 5, 10, 10);
				}
			}
			
			g.setColor(new Color(0, 0, 0, 255));

			if (cars != null) {
				for (CarAgent c: cars) {
					double pos = c.getCurrentPosition();
					Road r = c.getRoad();
					Pair<P2d, P2d> mappedRoad = new Pair<P2d, P2d>(
						new P2d(
							mapValue(r.getFrom().x(), 0, maxBounds.getFirst(), 0, this.getWidth()), 
							mapValue(r.getFrom().y(), 0, maxBounds.getSecond(), 0, this.getHeight())
						), new P2d(
							mapValue(r.getTo().x(), 0, maxBounds.getFirst(), 0, this.getWidth()), 
							mapValue(r.getTo().y(), 0, maxBounds.getSecond(), 0, this.getHeight())
						)
					);
					V2d dir = V2d.makeV2d(mappedRoad.getFirst(), mappedRoad.getSecond()).getNormalized().mul(pos);
					P2d point = mapEntityOnRoad(mappedRoad.getFirst().x() + dir.x(), r);
					g2.drawOval(
						(int)(mapValue(point.x(), 0, maxBounds.getFirst(), 0, this.getWidth()) - CAR_DRAW_SIZE/2), 
						(int)(mapValue(point.y(), 0, maxBounds.getSecond(), 0, this.getHeight()) - CAR_DRAW_SIZE/2), CAR_DRAW_SIZE , CAR_DRAW_SIZE
					);
				}
			}
		}

		private double mapValue(double value, double fromX, double fromY, double toX, double toY) {
			return toX + ((value - fromX) / (fromY - fromX)) * (toY - toX);
		}
		
		public void update(
			List<Road> roads, 
			List<CarAgent> cars,
			List<TrafficLight> sems
		) {
			this.roads = roads;
			this.cars = cars;
			this.sems = sems;
			repaint();
		}

		private P2d mapEntityOnRoad(double position, Road road) {
			double segmentLength = Math.sqrt(Math.pow(road.getTo().x() - road.getFrom().x(), 2) + Math.pow(road.getTo().y() - road.getFrom().y(), 2));
			double percentage = position / segmentLength;
			double newX = road.getFrom().x() + (road.getTo().x() - road.getFrom().x()) * percentage;
			double newY = road.getFrom().y() + (road.getTo().y() - road.getFrom().y()) * percentage;
			return new P2d(newX, newY);
		}
	}


	@Override
	public void notifyInit(int t, AbstractEnvironment<? extends AbstractAgent> env) { }

	@Override
	public void notifyStepDone(int t, int stepNumber, long deltaMillis, AbstractEnvironment<? extends AbstractAgent> env) {
		RoadsEnv e = ((RoadsEnv) env);
		panel.update(e.getRoads(), e.getAgentInfo(), e.getTrafficLights());
	}

	private class NumberOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            if (test(sb.toString())) {
                super.insertString(fb, offset, string, attr);
            } else {
                // Do nothing, if the input is not a number
            }
        }

        private boolean test(String text) {
            return text.matches("\\d*"); // Match zero or more digits
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            if (test(sb.toString())) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                // Do nothing, if the input is not a number
            }
        }

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset + length);

            if (test(sb.toString())) {
                super.remove(fb, offset, length);
            } else {
                // Do nothing, if the input is not a number
            }
        }
    }
}
