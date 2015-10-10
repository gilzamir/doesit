package gilzamir.doesit.sim.ai.test;

import gilzamir.doesit.sim.ai.NeuralNetGenetic;
import gilzamir.doesit.sim.ai.NeuralNetGenome;
import java.util.Arrays;
import multinet.net.NeuralNet;
import multinet.net.genetic.Genome;

public class TestSkinnerLikeEvaluator {
    public static void main(String args[]) {
        NeuralNetGenetic genetic = new NeuralNetGenetic(100);
        NeuralNetGenome.INPUTS = 4;
        NeuralNetGenome.OUTPUTS = 3;
        NeuralNetGenome.PROCESSING = 20;
        
        SkinnerLikeEvaluator ev = new SkinnerLikeEvaluator();
        
        genetic.setupPopulation();
        for (int i = 0; i < 20; i++) {
            System.out.println(genetic.evaluate(ev));
            genetic.next();
        }
        
        
        Genome ge = genetic.getOrganism()[0];
        NeuralNet net =  (NeuralNet) ge.decode();
        
        SkinnerApp.startApp(net);       
    }
}
