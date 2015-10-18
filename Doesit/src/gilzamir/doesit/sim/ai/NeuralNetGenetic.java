package gilzamir.doesit.sim.ai;


import multinet.net.genetic.Encoding;
import multinet.net.genetic.GeneLayout32;
import multinet.net.genetic.Genetic;
import multinet.net.genetic.Genome;

public class NeuralNetGenetic  extends Genetic {
    
    public NeuralNetGenetic(int numberOfOrganisms) {
        super(numberOfOrganisms);
    }

    @Override
    public Genome newGenome() {
        final int numberOfChr  = NeuralNetGenome.CHROMOSSOMES;
        final int inputs = NeuralNetGenome.INPUTS;
        final int outputs = NeuralNetGenome.OUTPUTS;
        final int genes = (inputs+outputs) * 3 + NeuralNetGenome.PROCESSING;
        final int numberOfGenes[] = new int[numberOfChr];
        numberOfGenes[0] = 5;
        for (int i = 0; i < numberOfChr; i++) {
            numberOfGenes[i] = genes;
        }
        
        NeuralNetGenome genome = new NeuralNetGenome(numberOfChr);
        for (int i = 0; i < numberOfChr; i++) {
            Encoding enc = new Encoding(numberOfGenes[i], new GeneLayout32());
            genome.setChromossome(i, enc);
        }
        FixedInputOutputGenerator fiog =  new FixedInputOutputGenerator();
        fiog.inputs = inputs;
        fiog.outputs = outputs;
        genome.setGenerator(1, fiog);
        
        for (int i = 0; i < numberOfChr; i++) {
            genome.generate(i);
        }
        
        return genome;
    }
}
