package gilzamir.doesit.sim.ai.test;


import multinet.net.NeuralNet;
import multinet.net.genetic.Evaluator;
import multinet.net.genetic.Genome;

public class SkinnerLikeEvaluator implements Evaluator {
    private final int NUM_OF_LIGHT = 3;
    public float lightState[];
    public float lightReward[];
    public float energy = 1000;
    
    public void evaluate(Genome gen) {
        reset();
        NeuralNet net = (NeuralNet) gen.decode();
        if (net == null) {
            gen.setFitness(0.0f);
            return;
        }
        int[] input = net.getInputs();
        float fitness = 0;
        gen.setFitness(0.0f);
        int n = 0;
        float reward = 0;

        while (energy > 0) {
            n++;
            for (int l = 0; l < lightState.length; l++) {
                if (Math.random() < 0.5) {
                    lightState[l] = 1.0f - lightState[l];
                }
            }

            reward = 0;
            for (int i = 0; i < lightReward.length; i++) {
                if (lightState[i] == 1.0) {
                    reward += lightReward[i];
                } else {
                    reward -= lightReward[i];
                }
            }
            
            
            net.setInput(input[0], lightState[0]);
            net.setInput(input[1], lightState[1]);
            net.setInput(input[2], lightState[2]);
            net.setInput(input[3], reward);
            
            net.process();
            double out[] = net.getOutput();
            for (int i = 0; i < out.length; i++) {
                if (out[i] > 0.2f) {
                    lightState[i] = 1.0f;
                } else if (out[i] < -0.2){
                    lightState[i] = 0.0f;  
                } 

                if (lightState[i] == 1.0 && lightReward[i] > 0) {
                    fitness++;
                } else if (lightState[i] == 0.0 && lightReward[i] < 0) {
                    fitness++;
                }

            }
        
            energy--;
        }
        float prate = (net.numberOfUpdates)/(float)net.getSize();
        gen.setFitness((2.0f * fitness/n + 1.0f * prate)/3.0f);
    }
    
    public void reset() {
        lightState = new float[NUM_OF_LIGHT]; //LIGHT STATE
        lightReward = new float[NUM_OF_LIGHT]; //LIGHT REWARD
        
        //BEGIN::INITIALIZE LIGHT STATE
        lightState[0] = (Math.random() < 0.5 ? 1.0f : 0.0f);
        lightState[1] = (Math.random() < 0.5 ? 1.0f : 0.0f);
        lightState[2] = (Math.random() < 0.5 ? 1.0f : 0.0f); 
        //END: FINALIZE LIGHT STATE
        
        lightReward[0] = -33.33f; //it's light on reward
        lightReward[1] = 33.33f; //it's light on reward
        lightReward[2] = 33.33f; //it's light on reward
        
        energy = 200.0f; //max time to act 
    }
}
