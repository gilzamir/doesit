package gilzamir.doesit.sim.ai.test;

import java.util.Arrays;
import multinet.net.NeuralNet;
import multinet.net.genetic.Evaluator;
import multinet.net.genetic.Genome;

public class SkinnerLikeEvaluator implements Evaluator {

    private final int NUM_OF_LIGHT = 3;
    public float lightState[];
    public float lightReward[];
    public float energy = 2000;
    public float hour = 0;
    public boolean demageInput = false, showOutput = false, showEnergy = false, showDopamine = false;
    private float t;
    private final float maxEnergy = 25000;
    private float inputScale = 1.0f;
    private float backenergy = 0.0f;
    private float fitness = 0.0f;
    private float prate = 0.0f;
    private int step, n;
    private float dopamine = 1.0f;
    private int numberOfHours = 100;
    private int morningBegin = 0;
    private int nightBegin = 50;

    public void evaluate(Genome gen) {
        float sum = 0.0f;
        int counter = 3;
        for (int i = 0; i < counter; i++) {
            sum += oneevaluate(gen);
        }
        gen.setFitness(sum / counter);
    }

    public float oneevaluate(Genome gen) {
        reset();
        NeuralNet net = (NeuralNet) gen.decode();

        if (net == null) {
            return 0.0f;
        }

        net.randomizeMatrix(net.getWeight(), -50, 50);


        gen.setFitness(0.0f);

        int maxStep = 1500;

        while (energy > 0 && step < maxStep) {
            step(net);
        }

        return fitness / n;
    }

    public float getSunIntensity() {
        return t;
    }

    public void step(NeuralNet net) {

        if (showEnergy) {
            System.out.println("Energy: " + energy);
            System.out.println("Time: " + t);
            System.out.println("Hour: " + hour);
        }

        if (showDopamine) {
            System.out.println("Dopamine: " + dopamine);
        }
        
        int[] input = net.getInputs();
        if (hour >= nightBegin) {
            t = 0;
        } else {
            t = (hour + nightBegin) / (numberOfHours + nightBegin);
            if (t > 0.5) {
                t = t - (t-0.5f);
            }
        }

        float inputShift = (float) Math.random() * 0.4f - 0.2f;
        if (Math.abs(inputShift) > 0.2f) {
            inputShift = 0;
        }

        if (!demageInput) {
            net.setInput(input[0], lightState[0] * inputScale + inputShift);
            net.setInput(input[1], lightState[1] * inputScale + inputShift);
            net.setInput(input[2], lightState[2] * inputScale + inputShift);
            net.setInput(input[3], hour/(float)numberOfHours);
        } else {
            float h = hour + 0.5f;
            if (h > 1.0) {
                h = h - 1.0f;
            }
            
            net.setInput(input[0], lightState[0] * inputScale + inputShift);
            net.setInput(input[1], lightState[1] * inputScale + inputShift);
            net.setInput(input[2], lightState[2] * inputScale + inputShift);
            net.setInput(input[3], h);
        }

        net.dopamine = dopamine;
        net.process();
        prate = 1.0f - ((float) net.numberOfUpdates) / (float) (net.getSize() * net.getSize());
        double out[] = net.getOutput();

        if (showOutput) {
            System.out.println("Net Output: " + Arrays.toString(out));
        }

        for (int i = 0; i < out.length; i++) {
            lightState[i] = (float) out[i];

            fitness += lightState[i] * 10;

            dopamine += lightState[i] - 0.4;

            energy += -Math.abs(out[i]) * 40;
        }

        fitness += 10 * (1 - prate);

        float delta = energy - backenergy;
        backenergy = energy;
        fitness += delta * 2;

        energy += t * 30;

        if (energy > maxEnergy) {
            energy = maxEnergy;
        }

        n++;
        step++;
        hour++;
        if (hour > numberOfHours) {
            hour = 0;
        }
    }

    public void reset() {
        lightState = new float[NUM_OF_LIGHT]; //LIGHT STATE

        //BEGIN::INITIALIZE LIGHT STATE
        lightState[0] = 0.0f;
        lightState[1] = 0.0f;
        lightState[2] = 0.0f;
        //END: FINALIZE LIGHT STATE

        energy = 20000.0f; //max time to act 
        dopamine = 1.0f;
        backenergy = energy;
        hour = 0;
        n = 0;
        step = 0;
        fitness = 0;
    }
}
