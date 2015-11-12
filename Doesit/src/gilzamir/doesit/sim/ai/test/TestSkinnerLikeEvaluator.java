package gilzamir.doesit.sim.ai.test;

import gilzamir.doesit.sim.ai.NeuralNetGenetic;
import gilzamir.doesit.sim.ai.NeuralNetGenome;
import java.util.Scanner;
import multinet.net.NeuralNet;
import multinet.net.NeuralNetEvent;
import multinet.net.NeuralNetListener;
import multinet.net.genetic.Genome;

public class TestSkinnerLikeEvaluator {
    @SuppressWarnings({"null", "ConstantConditions"})
    public static void main(String args[]) {
        NeuralNetGenetic genetic = new NeuralNetGenetic(100);
        genetic.setCrossoverProbability(0.9f);
        genetic.setMutationProbability(0.008f);
       
        NeuralNetGenome.INPUTS = 4;
        NeuralNetGenome.OUTPUTS = 3;
        NeuralNetGenome.PROCESSING = 20;
        
        SkinnerLikeEvaluator ev = new SkinnerLikeEvaluator();
        
        genetic.setupPopulation();
        for (int i = 0; i < 20; i++) {
            System.out.println(genetic.evaluate(ev));
            genetic.next();
        }
        
        NeuralNet net = null;
        int n = 0;
        while (net == null &&  n < genetic.getOrganism().length) {
            Genome ge = genetic.getOrganism()[0];
            net =  (NeuralNet) ge.decode();
            n++;
        }
        if (net == null) {
            System.out.println("No viable organism!!!");
            System.exit(0);
        }
        net.randomizeMatrix(net.getWeight(), -50, 50);
     
        System.out.println(net.toString());
        net.setListener(new NeuralNetListener() {
            public void handleUpdateWeight(NeuralNetEvent evt) {
                if (evt != null) {
                    System.out.println(evt.getMessage());
                }
            }
        });
        System.out.println("Training finished!! Press any key and enter to continue...");
        (new Scanner(System.in)).nextLine();
        SkinnerApp.startApp(ev, net);       
    }
}
