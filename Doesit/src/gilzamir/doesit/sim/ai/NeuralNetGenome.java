package gilzamir.doesit.sim.ai;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import multinet.core.Synapse;
import multinet.net.NeuralNet;
import multinet.net.Neuron;
import multinet.net.NeuronType;
import multinet.net.UpdateWeightGil;
import multinet.net.genetic.Encoding;
import multinet.net.genetic.EncodingGenerator;
import multinet.net.genetic.Evaluable;
import multinet.net.genetic.Genome;


public class NeuralNetGenome extends Genome {
    public static  int INPUTS = 3;
    public static  int OUTPUTS = 1;
    public static  int PROCESSING = 100;
    public static int CHROMOSSOMES = 4;
    
    public NeuralNetGenome(int size) {
        super(size);
    } 

    @Override
    public Evaluable decode() {
        List<Integer> inputs = new ArrayList<Integer>();
        List<Integer> outputs = new ArrayList<Integer>();
        NeuralNet net = new NeuralNet(new UpdateWeightGil());
        Encoding global = getChromossome(0);
        
        net.setDouble("inputrest", (double)global.getAsFloat(0, -100f, 100f));
        net.setDouble("weightgain", (double)global.getAsFloat(1, -100, 100));
        net.setDouble("outputgain", (double)global.getAsFloat(2, 0.0f, 1.0f));
        net.setDouble("learningrate", (double)global.getAsFloat(2, 0.002f, 1.0f));
        Encoding body = getChromossome(1);
        Encoding amp = null;
        Encoding shift  = null;
        if (size() >= CHROMOSSOMES) {
            amp = getChromossome(2);
            shift = getChromossome(3);
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
               
                if (amp != null && shift != null) {
                    current.amp = amp.getAsFloat(i, 0.0f, 1.0f);
                    current.shift = shift.getAsFloat(i, -1.0f, 1.0f); 
                }
                current.t1 = current.t2 = null;
            } else {
                if (current != null) {
                    Terminal t = new Terminal();
                    t.binary = gene;
                    t.ID = ID;
                    t.value = value;

                    if (amp != null) { 
                        t.amp = amp.getAsFloat(i, 0.0f, 1.0f);
                    }
                    if (shift != null) {
                        t.shift = shift.getAsFloat(i, -1.0f, 1.0f);
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
                ne = net.createNeuron();
                ne.setType(NeuronType.INPUT);
                proto.ID = ne.getID();
                inputs.add(ne.getID());
            } else if (proto.ID == 1) {
                ne = net.createNeuron();
                ne.setType(NeuronType.NORMAL);
                proto.ID = ne.getID();
                outputs.add(ne.getID());
            } else /*if (proto.ID < 43)*/ {
                ne = net.createNeuron();
                ne.setType(NeuronType.OUTPUT);
                proto.ID = ne.getID();
            } /*else  {
                int netid = net.addCell(NeuronType.MODULATORY);
                ne = net.getNeuron(netid);
                proto.ID = netid;
            }*/
            ne.setDouble("gain", (double)proto.value);
            ne.setTimeConstant(proto.value);
            if (ne.getType() != NeuronType.OUTPUT) {
                ne.setDouble("gain", 1.0);
            }
            ne.setDouble("leanringrate", (double)proto.learningRate);
            ne.setInteger("method", proto.learningMethod);
            ne.setDouble("amp", (double)proto.amp);
            ne.setDouble("shift", (double)proto.shift);
            
            ne.setDouble("bias", 0.0);
        }
        
        net.setObject("input", inputs);
        net.setObject("output", outputs);
        if (inputs.size() != INPUTS || outputs.size() != OUTPUTS) {
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
                double w;
                //if ((eb / 2) % 3 != 0) {
                   // w = 100.0f * ((eb * (in + out)) / (24.0f)) - 50.0f;
                w = ((eb * (in + out)) / (24.0f));
                //}
                Synapse synapse = net.createSynapse(ni.ID, nj.ID, Math.random() * 100 - 50);
                synapse.setDouble("plasticity", w);
//                net.setAmp(ni.ID, nj.ID, (ti.amp+tj.amp) * 0.5f);
//                net.setShift(ni.ID, nj.ID, (ti.shift + tj.shift) * 0.5f);
//                net.setWeight(ni.ID, nj.ID, w);                
            }
        }
        
        net.setDouble("updaterate", 0.0);
        
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
    public float value, plasticity, amp, shift;
    public BitSet binary;
}


class FixedInputOutputGenerator implements EncodingGenerator {
    int inputs;
    int outputs;

    public Encoding generate(Encoding enc) {
        int i;
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