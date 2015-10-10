package gilzamir.doesit.sim.ai.test;

import gilzamir.doesit.sim.ai.NeuralNetGenetic;
import gilzamir.doesit.sim.ai.NeuralNetGenome;
import java.util.BitSet;
import multinet.net.NeuralNet;
import multinet.net.genetic.Encoding;

public class TestNeuralNetGenetic {
    public static void main(String args[]) {
        NeuralNetGenetic genetic = new NeuralNetGenetic(10);
        
        NeuralNetGenome genome = (NeuralNetGenome)genetic.newGenome();
        NeuralNet net = (NeuralNet)genome.decode();
        for (int i = 0; i < genome.size(); i++) {
            Encoding enc = genome.getChromossome(i);
            for (int j = 0; j < enc.getNumberOfGenes();  j++) {
                BitSet b = enc.getGene(j);
                System.out.print(Encoding.toInt(b, 0, 8) + "::");
                System.out.print(Encoding.toInt(b, 8, 32) + "   ");
            }
            System.out.println();
        }
        if (net != null) {
            System.out.println(net.toString());
        }
    }
}
