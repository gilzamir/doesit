package gilzamir.doesit.sim.ai.test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import multinet.core.Synapse;
import multinet.net.NeuralNet;
import multinet.net.Neuron;

import multinet.net.genetic.Evaluator;
import multinet.net.genetic.Genome;

public class SkinnerLikeEvaluator implements Evaluator {

    public double energy;
    private final int w=100, h=100;
    public double px, py; //player position
    public double dx, dy, radio=8; //player direction;
    private final double[][] friction; //terrain friction map
    private ArrayList<GObject> objects = new ArrayList<GObject>();
    private int time;
    private int maxTime = 100;
    private double xmax = w/2.0;
    private double xmin = -w/2.0;
    private double ymax = h/2.0;
    private double ymin = -h/2.0;
   
    public boolean showEnergy, showOutput, showLightValue, showDisplay;
        
    public SkinnerLikeEvaluator() {
        friction = new double[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                friction[i][j] = 0;
            }
        }
    }
    
    public void evaluate(Genome gen) {
        float sum = 0.0f;
        int counter = 3;
        for (int i = 0; i < counter; i++) {
            sum += oneevaluate(gen);
        }
        gen.setFitness(sum / counter);
    }
    
    public double getPlayerX(){
        return px;
    }
    
    public double getPlayerY() {
        return py;
    }
    
    public double getPlayerDX() {
        return dx;
    }

    public double getPlayerDY() {
        return dy;
    }
    
    public float oneevaluate(Genome gen) {
        reset();
        
        float fitness = 0.0f;
        NeuralNet net = (NeuralNet) gen.decode();

        if (net == null) {
            return 0.0f;
        }

        for (Synapse syn : net.getSynapses().values()) {
            syn.setIntensity(Math.random() * 100 - 50);
        }

        gen.setFitness(0.0f);

        int maxStep = 5000;
        int step = 0;
        GObject light = objects.get(0);
        while (energy > 0 && step < maxStep) {
            step(net);
            
            time = ((time+1) % maxTime);
            light.value = time/maxTime;
            if (light.value < 0.5) {
                light.value = 0;
            }
            fitness++;
            double dl = GObject.distance(px, py, light.x, light.y);
            if (dl > 5) {
             energy +=  100.0/(0.005+dl);
            } else {
                energy = 0;
                fitness -= 10;
            }

            step++;
        }
        return fitness;
    }

    public GObject getLight() {
        return objects.get(0);
    }
    
    public void step(NeuralNet net) {
        List<Integer> input = (List<Integer>) net.getObject("input");
        List<Integer> output = (List<Integer>) net.getObject("output");
        double sensor[] = GObject.getDisplayInfo(px, py, dx, dy, objects);
        Neuron i1 = net.getNeuron(input.get(0));
        Neuron i2 = net.getNeuron(input.get(1));
        Neuron i3 = net.getNeuron(input.get(2));
        Neuron i4 = net.getNeuron(input.get(3));
        
        if (showEnergy) {
            System.out.println("Energy: " + energy);
        }

        if (showLightValue) {
            System.out.println("Light: " + getLight().value);
        }

        if (showDisplay) {
            System.out.println("Display: " + Arrays.toString(sensor));
        }
        
        i1.setInput(sensor[0]+0.001);
        i2.setInput(sensor[1]+0.001);
        i3.setInput(sensor[2]+0.001);
        i4.setInput(energy);
        
        net.proccess();
        net.update();
        
        Neuron o1 = net.getNeuron(output.get(0));
        Neuron o2 = net.getNeuron(output.get(1));
        
        double out[] = new double[2];
        out[0] = o1.getImplementation().getOutput(o1);
        out[1] = o2.getImplementation().getOutput(o2);
        
        if (showOutput) {
            System.out.println("Output: " + Arrays.toString(out));
        }
        
        double move = 10.0 * out[0] - 5.0;
        double rotate = 4.0 * out[1] - 2.0;
        double rotcon = rotate;
        
        
        energy -= Math.abs(move) * 10;
        energy -= Math.abs(rotcon) * 10;
        
        move(move);
        rotate(rotate);
    }
    
    public void move(double d){
        px += d * Math.cos(dx);
        py += d * Math.sin(dy);
        verifyWallCollision();
    }

    public void rotate(double d) {
        double t = Math.PI/2.0;
        if (d > t) {
            d = t;
        }
        if (d < -t) {
            d = -t;
        }
        dx  = dx * Math.cos(d) + dy * Math.sin(d);
        dy  = -dx * Math.sin(d) + dy * Math.sin(d);
        verifyWallCollision();
    }
    
    public void verifyWallCollision() {
        if ((px-radio) < xmin) {
            px = xmin + radio;
        } else if ((px+radio) > xmax) {
            px = xmax - radio; 
        }
        
        if ((py-radio) < ymin) {
            py = ymin + radio;
        } if ((py+radio) > ymax) {
            py = ymax-radio;
        }
    }
    
    public int xInt(double wx) {
        return Math.round((float)wx + 50);
    }
    
    public double yInt(double wy) {
        return Math.round((float)wy + 50);
    }
    
    public void reset() {
        energy = 5000;
        objects.clear();
        GObject light = new GObject();
        
        light.x = Math.random() * 100 - 50;
        light.y = Math.random() * 100 - 50;
        light.value = Math.random();
        light.r = 0.25;
        radio = 0.5;
        
        
        objects.add(light);
        
        
        px = Math.random() * 100 - 50;
        py = Math.random() * 100 - 50;
        
        dx = 1.0;
        dy = 0.0;
        
        time = 0;
    }
    
    
}

class GObject {
    double x, y;
    double r;
    double value;
    
    public double[] hit(double rx, double ry, double dx, double dy) {
        //(x-xc)*(x-xc) + (y-yc)*(y-yc) = r * r
        //x = rx + t * dx;
        //y = ry + t * dy;
        //(rx+t*dx-xc)^2 = a^2 - 2 * a * xc + xc^2
        //a^2 = rx^2 + 2 * rx * t * dx + t^2 * dx^2
        //rx^2 + 2 * rx * t * dx + t^2 * dx^2 -  2 * xc * rx -  2*xc*t*dx + xc^2
        //A = dx^2 + dy^2
        //B = 2 * rx * dx + 2 * ry * dy - 2 * xc * dx - 2 * yc * dy
        //C = xc^2 + yc^2- r^2
        double xc = x, yc = y;
        double A = dx * dx + dy * dy;
        double B = 2 * rx * dx + 2 * ry * dy - 2 * xc * dx - 2 * yc * dy;
        double C = xc*xc + yc*yc - r*r;
        
        double delta = B*B - 4 * A * C;
        
        if (delta < 0) {
            return null;
        } else if (delta == 0) {
            return new double[]{-B/2.0*A};
        } else {
            double t[] = new double[2];
            double d = Math.sqrt(delta);
            double a2 = 2 * A;
            t[0] = (-B + d)/a2;
            t[1] = (-B-d)/a2;
            return t;
        }
    }
    
    public static double distance(double x1, double y1, double x2, double y2){ 
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static double[] getDisplayInfo(double px, double py, 
            double dx, double dy, ArrayList<GObject> objects) {
        
        double retina[] = {0,0,0};
   
        final double pi8 = Math.PI/20;
        ArrayList<double[]> ray = new ArrayList<double[]>();
   
        double xdir = Math.cos(pi8) * dx + Math.sin(pi8) * dy;
        double ydir = -Math.sin(pi8) * dx + Math.cos(pi8) * dy;
        ray.add(new double[]{xdir, ydir});
        
        ray.add(new double[]{dx, dy});
        
        xdir = Math.cos(-pi8) * dx + Math.sin(-pi8) * dy;
        ydir = -Math.sin(-pi8) * dx + Math.cos(-pi8) * dy;
        ray.add(new double[]{xdir, ydir});

        int k = 0;
        for (double[] r : ray){
            double tmin = Double.MAX_VALUE;
            for (GObject obj : objects) {
                double t[] = obj.hit(px, py, r[0], r[1]);
                if (t != null) {
                    Arrays.sort(t);
                    if (t[0] > 0 && t[0] < tmin) {
                        retina[k] = obj.value * 1.0/t[0];
                        tmin = t[0];
                    } else if (t.length > 1 && t[1] > 0 && t[1] < tmin){
                        retina[k] = obj.value * 1.0/t[1];
                        tmin = t[1];
                    }
                }
            }
            k++;
        }
        
        return retina;
    }
    
    public static double size(double dx, double dy) {
        return Math.sqrt(dx*dx + dy * dy);
    } 
    
    public static double rotateX(double dx, double dy, double ang) {
        return dx * Math.cos(ang) + dy * Math.sin(ang);
    }
    
    public static double rotateY(double dx, double dy, double ang) {
        return -dx * Math.sin(ang) + dy * Math.cos(ang);
    }
    
    public static final double DEG2RAD = Math.PI/180.00;
    public static final double RAD2DEG = 180/Math.PI;
    
    public static void main(String args[]) {
        GObject obj = new GObject();
        obj.x = -1.0;
        obj.y = -1.0;
        obj.r = 0.8;
        obj.value = 1.0;
        
        ArrayList<GObject> objects = new ArrayList<GObject>();
        objects.add(obj);
        double xeye = 1.0;
        double yeye = 1.0;
        double rot = 10;
        rot = rot * DEG2RAD;
        double x = rotateX(xeye, yeye,  rot);
        double y = rotateY(xeye, yeye, rot);
        
        double size = GObject.size(x, y);
        double[] retina = GObject.getDisplayInfo(0, 0, x/size, y/size, objects);
        
        System.out.println(Arrays.toString(retina));
    }
}
