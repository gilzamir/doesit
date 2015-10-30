package gilzamir.doesit.sim.ai;

import java.util.BitSet;
import java.util.LinkedList;
import multinet.net.NeuralNet;
import multinet.net.Neuron;
import multinet.net.NeuronType;
import multinet.net.ThresholdType;
import multinet.net.UpdateWeightGil;
import multinet.net.genetic.Encoding;
import multinet.net.genetic.EncodingGenerator;
import multinet.net.genetic.Evaluable;
import multinet.net.genetic.Genome;


public class NeuralNetGenome extends Genome {
    public static  int INPUTS = 3;
    public static  int OUTPUTS = 1;
    public static  int PROCESSING = 100;
    public static int CHROMOSSOMES = 6;
    
    public NeuralNetGenome(int size) {
        super(size);
    } 

    @Override
    public Evaluable decode() {
        NeuralNet net = new NeuralNet(new UpdateWeightGil());
        net.setPlasticityEnabled(true);
        Encoding global = getChromossome(0);
        
        net.restInput = global.getAsFloat(0, -100f, 100f);
        net.weightGain = global.getAsFloat(1, -100, 100);
        net.outputGain = global.getAsFloat(2, 0, 1);
        net.setLearningRate(global.getAsFloat(3, 1.0f, 2.0f));
        Encoding body = getChromossome(1);
        Encoding amp = null;
        Encoding shift  = null;
        Encoding plasticity = null;
        Encoding method = null;
        if (size() >= CHROMOSSOMES) {
            amp = getChromossome(2);
            shift = getChromossome(3);
            plasticity = getChromossome(4);
            method = getChromossome(5);
        }
        LinkedList<ProtoNeuron> neurons = new LinkedList<ProtoNeuron>();
        ProtoNeuron current = null;
        for (int i = 0; i < body.getNumberOfGenes(); i++) {
            BitSet gene = body.getGene(i);
            int ID = Encoding.toInt(gene, 0, 8);
            
            float value = 2.0f * (Encoding.toInt(gene, 8, 32)/(float)Encoding.MAX_INT24) - 1.0f;
            if (ID <= 51) {
                current = new ProtoNeuron();
                current.ID = ID;
                current.value = value;
                current.binary = gene;
               
                if (amp != null && shift != null && method != null) {
                    current.amp = amp.getAsFloat(i, 0.0f, 1.0f);
                    current.shift = shift.getAsFloat(i, -1.0f, 1.0f);
                    double m = method.getAsFloat(i, 0.0f, 1.0f);
                    if (m <= 0.25) {
                        current.learningMethod = 3;
                    } else if (m <= 0.5) {
                        current.learningMethod = 2;
                    } else if (m <= 0.75) {
                        current.learningMethod = 1;
                    } else {
                        current.learningMethod = 0;
                    }
                }
                current.t1 = current.t2 = null;
            } else {
                if (current != null) {
                    Terminal t = new Terminal();
                    t.binary = gene;
                    t.ID = ID;
                    t.value = value;
                    if (plasticity != null) {
                        t.plasticity = plasticity.getAsFloat(i, -1.0f, 1.0f);
                    }
                    if (current.t1 == null) {
                        current.t1 = t;
                    } else if (current.t2 == null) {
                        current.t2 = t;
                    } 
                    if (current.t1 != null & current.t2 != null) {
                        neurons.add(current);
                        current = null;
                    }
                }
            }
        }
       
        if (current != null  && current.t1 != null && current.t2 != null) {
            neurons.add(current);
        }
        
        
        for (ProtoNeuron proto : neurons) {
            Neuron ne;
            //System.out.println(proto.ID);
            if (proto.ID == 0) {
                int netid = net.addCell(NeuronType.INPUT);
                ne = net.getNeuron(netid);
                proto.ID = netid;
            } else if (proto.ID == 1) {
                int netid = net.addCell(NeuronType.OUTPUT);
                ne = net.getNeuron(netid);
                proto.ID = netid;
            } else /*if (proto.ID < 43)*/ {
                int netid = net.addCell(NeuronType.NORMAL);
                ne = net.getNeuron(netid);
                proto.ID = netid;
            } /*else  {
                int netid = net.addCell(NeuronType.MODULATORY);
                ne = net.getNeuron(netid);
                proto.ID = netid;
            }*/
            ne.setGain(proto.value);
            ne.setTimeConstant(proto.value);
            if (ne.getType() != NeuronType.OUTPUT) {
                ne.setGain(1.0f);
            }
            ne.setLearningRate(proto.learningRate);
            ne.setLearningMethod(proto.learningMethod);
            ne.setOutputThreshold(proto.amp);
            ne.setAmp(proto.amp);
            ne.setShift(proto.shift);
            
            if (proto.shift >= 0.0) {
                ne.setThresholdRule(ThresholdType.MAX);
            } else {
                ne.setThresholdRule(ThresholdType.MIN);
            }
            
            ne.setBias(0.0);
        }
        
        net.prepare(-10, 10);
        
        if (net.getInputSize() != INPUTS || net.getOutputSize() != OUTPUTS) {
            return null;
        }
        

        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {
                ProtoNeuron ni = neurons.get(i);
                ProtoNeuron nj = neurons.get(j);
                Terminal ti = ni.t2;
                Terminal tj = nj.t1;
                double in = ni.value;
                double out = nj.value;
                BitSet b1 = ti.binary;
                BitSet b2 = tj.binary;
                int eb = countEquals(b1, b2, 32);
                double w = 0.0f;
                if ((eb / 2) % 3 != 0) {
                    w = 100.0f * ((eb * (in + out)) / (24.0f)) - 50.0f;
                }

                net.setPlasticity(ni.ID, nj.ID, (ti.plasticity + tj.plasticity) * 0.5f);
                net.setWeight(ni.ID, nj.ID, w);                
            }
        }
        
        
        return net;
    }
    
    private int countEquals(BitSet s1, BitSet s2, int m) {
        int n = 0;
        for (int i = 0; i < m; i++) {
            if (s1.get(i) == s2.get(i)) {
                n++;
            }
        }
        return n;
    }    
}
class ProtoNeuron {
    public int ID;
    public int learningMethod;
    public float value, amp, learningRate, shift;
    public Terminal t1, t2;
    public BitSet binary;
}

class Terminal {
    public int ID;
    public float value, plasticity;
    public BitSet binary;
}


class FixedInputOutputGenerator implements EncodingGenerator {
    int inputs;
    int outputs;

    public Encoding generate(Encoding enc) {
        int i = 0;
        int n = inputs * 3;
        for (i = 0; i < n; i += 3) {
            boolean neuronGene[] = new boolean[32];
            Encoding.randomBoolean(neuronGene);
            Encoding.setBoolean(neuronGene, 0, 8, false);
            enc.setGene(i, neuronGene);
            
            boolean terminalGene1[] = new boolean[32];
            Encoding.randomBoolean(terminalGene1);
            Encoding.setBoolean(terminalGene1, 0, 8, true);
            enc.setGene(i+1, terminalGene1);
            
            boolean terminalGene2[] = new boolean[32];
            Encoding.randomBoolean(terminalGene2);
            Encoding.setBoolean(terminalGene2, 0, 8, true);
            enc.setGene(i+2, terminalGene2);
        }

        n = (inputs+outputs) * 3;
        for (; i < n; i += 3) {
            boolean neuronGene[] = new boolean[32];
            Encoding.randomBoolean(neuronGene);
            Encoding.setBoolean(neuronGene, 0, 1, true);
            Encoding.setBoolean(neuronGene, 1, 8, false);
            enc.setGene(i, neuronGene);
            
            boolean terminalGene1[] = new boolean[32];
            Encoding.randomBoolean(terminalGene1);
            Encoding.setBoolean(terminalGene1, 0, 8, true);
            enc.setGene(i+1, terminalGene1);
            
            boolean terminalGene2[] = new boolean[32];
            Encoding.randomBoolean(terminalGene2);
            Encoding.setBoolean(terminalGene2, 0, 8, true);
            enc.setGene(i+2, terminalGene2);
        }
        
        for (; i < enc.getNumberOfGenes(); i++) {
            boolean gene[] = new boolean[32];
            Encoding.randomBoolean(gene);
            enc.setGene(i, gene);
        }
        return enc;
    }
}