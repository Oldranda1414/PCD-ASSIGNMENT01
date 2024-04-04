package pcd.ass01.simtrafficexamplesconcurrent;

import pcd.ass01.simtrafficexamplesconcurrent.simulations.*;

/**
 * 
 * Main class to create and run a simulation
 * 
 */
public class RunTrafficSimulation {

	public static void main(String[] args) {		

		final int nSteps = 100;

		var simulation = new TrafficSimulationSingleRoadTwoCars();
		// var simulation = new TrafficSimulationSingleRoadSeveralCars();
		//var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars();
		// var simulation = new TrafficSimulationWithCrossRoads();
		simulation.setup();
		
		//RoadSimStatistics stat = new RoadSimStatistics();
		RoadSimView view = new RoadSimView();
		view.display();
		
		//simulation.addSimulationListener(stat);
		simulation.addSimulationListener(view);
		simulation.run(nSteps);
	}
}
