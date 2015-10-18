package gilzamir.doesit.sim.ai.test;

import gilzamir.doesit.sim.ai.NeuralNetGenetic;
import gilzamir.doesit.sim.ai.NeuralNetGenome;
import multinet.net.NeuralNet;
import multinet.net.NeuralNetEvent;
import multinet.net.NeuralNetListener;
import multinet.net.genetic.Genome;

public class TestSkinnerLikeEvaluator {
    public static void main(String args[]) {
        NeuralNetGenetic genetic = new NeuralNetGenetic(100);
        NeuralNetGenome.INPUTS = 4;
        NeuralNetGenome.OUTPUTS = 3;
        NeuralNetGenome.PROCESSING = 20;
        
        SkinnerLikeEvaluator ev = new SkinnerLikeEvaluator();
        
        genetic.setupPopulation();
        for (int i = 0; i < 30; i++) {
            System.out.println(genetic.evaluate(ev));
            genetic.next();
        }
        
        Genome ge = genetic.getOrganism()[0];
        NeuralNet net =  (NeuralNet) ge.decode();
        System.out.println(net.toString());
        net.setListener(new NeuralNetListener() {
            public void handleUpdateWeight(NeuralNetEvent evt) {
                if (evt != null) {
                    System.out.println(evt.getMessage());
                }
            }
        });
        
        SkinnerApp.startApp(net);       
    }
}
