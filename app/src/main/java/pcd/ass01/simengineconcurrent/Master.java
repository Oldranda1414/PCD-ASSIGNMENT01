package pcd.ass01.simengineconcurrent;

import java.util.List;

import pcd.ass01.simengineconcurrent.latch.*;

public class Master extends Thread {

    private List<Runnable> senseDecideWorks;
    private List<Runnable> actWorks;
    private int nWorkers;
    private int nAgents;
    private BoundedBuffer<Runnable> bagOfTasks;
    private ResettableLatch workersDone;
    private ResettableLatch workReady;
    private AbstractEnvironment env;
    private int dt;
    private int nSteps;

    public Master(
        final int nWorkers,
        final List<Runnable> senseDecideWorks,
        final List<Runnable> actWorks,
        final AbstractEnvironment env,
        int dt,
        int nSteps
    ) {
        this.nWorkers = nWorkers;
        this.nAgents = senseDecideWorks.size();
        this.senseDecideWorks = senseDecideWorks;
        this.actWorks = actWorks;
        this.env = env;
        this.dt = dt;
        this.workersDone = new ResettableLatchImpl(nWorkers);
        this.workReady = new ResettableLatchImpl(1);
        this.bagOfTasks = new BagOfTasks(this.nAgents);
        this.nSteps = nSteps;
    }

    @Override
    public void run() {
        try {
            log("Starting Simulation");
            Worker[] workers = new Worker[this.nWorkers];
            for(int i = 0; i < this.nWorkers; i++) {
                workers[i] = new Worker(this.bagOfTasks, this.workersDone, this.workReady/*, this.env, this.agentWills*/);
                workers[i].start();
            }
            for(int step = 1; step <= nSteps; step++) {
                log("executing step " + step + " of the simulation");
                this.env.step(dt);

                log("filling the bag with tasks sense-decide");
                for(var work : senseDecideWorks){
                        this.bagOfTasks.put(work);
                }
                this.workReady.countDown(); //notifing workers that bag is full of tasks
                log("going to sleep until workers finish current tasks");
                this.workersDone.await(); //wait for all workers to finish the tasks

                log("filling the bag with tasks act");
                for(var work : actWorks){
                        this.bagOfTasks.put(work);
                }
                this.workReady.countDown(); //notifing workers that bag is full of tasks
                log("going to sleep until workers finish current tasks");
                this.workersDone.await(); //wait for all workers to finish the tasks
                log("finished executing step " + step + " of the simulation");
                this.workReady.reset(); //reset the latch for the next step
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void log(String message){
        synchronized(System.out){
            System.out.println("[master]: " + message);
        }
    }

}