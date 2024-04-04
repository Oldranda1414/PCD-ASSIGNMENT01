package pcd.ass01.masterworker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pcd.ass01.simengineconcurrent.AbstractAgent;
import pcd.ass01.simengineconcurrent.AbstractEnvironment;
import pcd.ass01.simengineconcurrent.SimulationListener;
import pcd.ass01.utils.Buffer;
import pcd.ass01.utils.latch.*;

public class Master extends Thread {

    private List<Task> senseDecideWorks;
    private List<Task> actWorks;
    private List<SimulationListener> listeners;
    private int nWorkers;
    private List<Worker> workers;
    private Buffer<Task> bagOfTasks;
    private ResettableLatch workersReady;
    private AtomicBoolean simulationOver;
    private AbstractEnvironment<? extends AbstractAgent> env;
    private int dt;
    //private int t0;
    private int nSteps;

    public Master(
        final int nWorkers,
        final List<Task> senseDecideWorks,
        final List<Task> actWorks,
        final AbstractEnvironment<? extends AbstractAgent> env,
        final int t0,
        final int dt,
        final int nSteps,
        final List<SimulationListener> listeners
    ) {
        this.nWorkers = nWorkers;
        this.senseDecideWorks = senseDecideWorks;
        this.actWorks = actWorks;
        this.env = env;
        this.dt = dt;
        //this.t0 = t0;
        this.workersReady = new ResettableLatchImpl(nWorkers);
        this.bagOfTasks = new BagOfTasks(this.workersReady);
        this.simulationOver = new AtomicBoolean(false);
        this.nSteps = nSteps;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        /* initialize the env and the agents inside */
		//int t = t0;
        try {
            //this.notifyReset(t, env);
            log("Starting Simulation");
            
            this.initWorkers();
            log("awaiting for workers to be ready");
            this.workersReady.await();   //wait for all workers to be ready
            this.workersReady.reset();

            for(int step = 1; step <= nSteps; step++) {
                
                log("executing step " + step + " of the simulation");
                
                this.env.step(dt);
                
                bagStep("sense-decide", senseDecideWorks);
                
                bagStep("act", actWorks);

                log("finished executing step " + step + " of the simulation");

                //t += dt;

                notifyNewStep(dt, env);
            }
            this.simulationOver.set(true);

            for(var worker : this.workers) {
                worker.interrupt();
            }
            for(var worker : this.workers) {
                worker.join();
            }

            log("master thread finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWorkers(){
        this.workers = new ArrayList<Worker>();
        for (int i = 0; i < this.nWorkers; i++) {
            var w = new Worker(this.bagOfTasks, this.simulationOver);
            this.workers.add(w);
            w.start();
        }
        log("workers initialized");
    }

    private void bagStep(String workName, List<Task> works) throws InterruptedException{
        fillBag(workName, works);

        if (!this.simulationOver.get()) {
            log("going to sleep until workers finish " + workName + " works");
            this.workersReady.await(); //wait for all workers to finish the tasks
            this.workersReady.reset();
        }
    }

    private void fillBag(String workName, List<Task> works) throws InterruptedException{
        for(var work : works){
            this.bagOfTasks.put(work);
        }
        log("filled bag with " + workName + " works");
    }
    
    private void log(String message){
        synchronized(System.out){
            System.out.println("[master]: " + message);
        }
    }
	
	private void notifyReset(int t0, AbstractEnvironment<? extends AbstractAgent> env) {
		for (var l: listeners) {
			// l.notifyInit(t0, env.getAgents(), env);
		}
	}

	private void notifyNewStep(int t, AbstractEnvironment<? extends AbstractAgent> env) {
		for (var l: listeners) {
			l.notifyStepDone(t, env);
		}
	}
}