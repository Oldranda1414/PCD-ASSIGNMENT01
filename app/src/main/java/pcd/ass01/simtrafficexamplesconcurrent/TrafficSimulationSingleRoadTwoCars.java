package pcd.ass01.simtrafficexamplesconcurrent;

import pcd.ass01.simengineconcurrent.AbstractEnvironment;
import pcd.ass01.simengineconcurrent.AbstractSimulation;
import pcd.ass01.simengineconcurrent.AbstractStates;
import pcd.ass01.simtrafficbaseconcurrent.agent.CarAgent;
import pcd.ass01.simtrafficbaseconcurrent.environment.RoadsEnv;
import pcd.ass01.simtrafficbaseconcurrent.states.CarStates;

/**
 * 
 * Traffic Simulation about 2 cars moving on a single road, no traffic lights
 * 
 */
public class TrafficSimulationSingleRoadTwoCars extends AbstractSimulation {

	public TrafficSimulationSingleRoadTwoCars() {
		super();
	}
	
	public void setup() {

		int numberOfAgents = 2;

		int t0 = 0;
		int dt = 1;
		
		this.setupTimings(t0, dt);
		
		AbstractEnvironment<CarAgent> env = new RoadsEnv();
		this.setupEnvironment(env);
		AbstractStates<CarAgent> states = new CarStates();	
		this.setupAgentStates(states);
		for(int i = 1; i <= numberOfAgents; i++){
			var id = Integer.toString(i);
			this.addSenseDecide(this.getSenseDecide(id));
			this.addAct(this.getAct(id));
		}

		//Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));
		//CarAgent car1 = new CarAgentBasic("car-1", env, r, 0, 0.1, 0.2, 8);
		//this.addAgent(car1);		
		//CarAgent car2 = new CarAgentBasic("car-2", env, r, 100, 0.1, 0.1, 7);
		//this.addAgent(car2);
		
		/* sync with wall-time: 25 steps per sec */
		//this.syncWithTime(25);
	}	

	public Runnable getSenseDecide(String id){
		return () -> {
			//TODO put the sense decide here
		};
	}
	
	public Runnable getAct(String id){
		return () -> {
			this.getAgentStates().get(id).act(id, this.getEnvironment());
		};
	}
}