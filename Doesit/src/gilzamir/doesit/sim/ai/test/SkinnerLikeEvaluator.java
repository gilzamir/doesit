package gilzamir.doesit.sim.ai.test;


import multinet.net.NeuralNet;
import multinet.net.Neuron;
import multinet.net.genetic.Evaluator;
import multinet.net.genetic.Genome;

public class SkinnerLikeEvaluator implements Evaluator {
    private final int NUM_OF_LIGHT = 3;
    public float lightState[];
    public float lightReward[];
    public float energy = 100;

    public void evaluate(Genome gen) {
        float sum = 0.0f;
        int counter = 5;
        for (int i = 0; i < counter; i++) {
            sum += oneevaluate(gen);
        }
        gen.setFitness(sum/counter);
    }
    
    public float oneevaluate(Genome gen) {
        reset();
        NeuralNet net = (NeuralNet) gen.decode();
        if (net == null) {
            return 0.0f;
        }
        int[] input = net.getInputs();
        float fitness = 0;
        gen.setFitness(0.0f);
        int n = 0;
        float reward = 0;
        float noise = 1.0f;

        int step = 0;
        int maxStep = 1000;
        float prate = 1.0f - (net.numberOfUpdates)/(float)net.getSize();
        while (energy > 0 && step < maxStep) {
            n++;
            step++;
            for (int l = 0; l < lightState.length; l++) {
                if (Math.random() < 0.05) {
                    lightState[l] =  (-1) * lightState[l];
                }
            }
            
            reward = 0;
            for (int i = 0; i < lightReward.length; i++) {
                if (lightState[i] == 0.5) {
                    reward += lightReward[i];
                    energy += lightReward[i];
                } else {
                    reward -= lightReward[i];
                    energy -= lightReward[i];
                }
            }
            
            
            net.setInput(input[0], lightState[0] * noise);
            net.setInput(input[1], lightState[1] * noise);
            net.setInput(input[2], lightState[2] * noise);
            net.setInput(input[3], reward);
            

            
            net.process();
            double out[] = net.getOutput();
            double alfa = 0.05;
            double beta = -0.05;
            for (int i = 0; i < out.length; i++) {
                boolean isInRange = false;
                if (out[i] > alfa) {
                    lightState[i] = 0.5f;
                    
                    isInRange = true;
                } else if (out[i] <  beta){
                    lightState[i] = -0.5f;  
                    isInRange = true;
                } else {
                    fitness -= 20;
                }

                if (lightState[i] == 0.5 && lightReward[i] > 0) {
                    fitness += 10;
                    if (isInRange) {
                        energy += 1.0f;
                    } else {
                        energy -= 1.0f;
                    }
                } else if (lightState[i] == -0.5 && lightReward[i] < 0) {
                    fitness += 10;
                    if (isInRange) {
                        energy += 1.0f;
                    } else {
                        energy -= 8.0f;
                    }
                } else {
                    energy -= 10.0f;
                }
            }
        
            energy -= 8 * prate;
        }

        //gen.setFitness((1.0f * fitness/n + 1.0f * prate)/2.0f);
      //  gen.setFitness(fitness/n);
    //    return (fitness/n + 2 * prate)/3.0f;
        return (fitness/n + prate) * 0.5f;
    }
    
    public void reset() {
        lightState = new float[NUM_OF_LIGHT]; //LIGHT STATE
        lightReward = new float[NUM_OF_LIGHT]; //LIGHT REWARD
        
        //BEGIN::INITIALIZE LIGHT STATE
        lightState[0] = (Math.random() < 0.5 ? 0.5f : -0.5f);
        lightState[1] = (Math.random() < 0.5 ? 0.5f : -0.5f);
        lightState[2] = (Math.random() < 0.5 ? 0.5f : -0.5f); 
        //END: FINALIZE LIGHT STATE
        
        lightReward[0] = -0.33f; //it's light on reward
        lightReward[1] = 0.33f; //it's light on reward
        lightReward[2] = 0.33f; //it's light on reward
        
        energy = 100.0f; //max time to act 
    }
}
