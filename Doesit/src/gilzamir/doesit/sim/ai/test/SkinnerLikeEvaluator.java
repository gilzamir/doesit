package gilzamir.doesit.sim.ai.test;


import multinet.net.NeuralNet;
import multinet.net.genetic.Evaluator;
import multinet.net.genetic.Genome;

public class SkinnerLikeEvaluator implements Evaluator {
    private final int NUM_OF_LIGHT = 3;
    public float lightState[];
    public float lightReward[];
    public float energy = 2000;

    public void evaluate(Genome gen) {
        float sum = 0.0f;
        int counter = 3;
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
        float inputScale = 1.0f;

        int step = 0;
        int maxStep = 1000;
        float prate = 0.0f;
        final float maxEnergy = 25000;
        float backenergy = energy;
        while (energy > 0 && step < maxStep) {
            n++;
            step++;
            for (int l = 0; l < lightState.length; l++) {
                if (Math.random() < 0.5) {
                    lightState[l] =  (-1) * lightState[l];
                }
            }
            float inputShift = (float)Math.random() * 0.4f - 0.2f;
            if (Math.abs(inputShift) > 0.1f) {
                inputShift = 0;
            }
            
            net.setInput(input[0], lightState[0] * inputScale + inputShift);
            net.setInput(input[1], lightState[1] * inputScale + inputShift);
            net.setInput(input[2], lightState[2] * inputScale + inputShift);
            net.setInput(input[3], energy/maxEnergy);
            net.lambda  = energy;
            net.process();
            prate = 1.0f - ((float)net.numberOfUpdates)/(float)(net.getSize()*net.getSize());
            double out[] = net.getOutput();
            double alfa = 0.2;
            double beta = -0.2;
            for (int i = 0; i < out.length; i++) {
                
                if (out[i] > alfa) {
                    lightState[i] = 0.5f;    
                } else if (out[i] <  beta){
                    lightState[i] = -0.5f;  
                }

                if (lightState[i] == 0.5 && lightReward[i] > 0) {
                    fitness += 10;
                } else if (lightState[i] == -0.5 && lightReward[i] < 0) {
                    fitness += 10;
                }
                
                energy += -Math.abs(out[i])*10;
            }
        
            energy += -1.0f * (1.0-prate) - 10 * lightState[0] + 10 * lightState[1]
                    + 10 * lightState[2];
            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
            
            float delta = energy - backenergy;
            backenergy = energy;
            fitness += delta * 2;
        }

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
        
        energy = 20000.0f; //max time to act 
    }
}
