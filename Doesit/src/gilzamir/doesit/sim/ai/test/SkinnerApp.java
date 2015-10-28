package gilzamir.doesit.sim.ai.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import multinet.net.NeuralNet;

public class SkinnerApp extends SimpleApplication implements ActionListener {

    private Node redNode, greenNode, blueNode;
    private Node actor;
    private Node model;
    private NeuralNet neuralNet;
    private SpotLight lightRed, lightGreen, lightBlue;
    private boolean stimulate = false;
    private int noise = 1;
    private boolean automatic = false;
    private OutputStreamWriter log1, log2;
    
    @Override
    public void simpleInitApp() {
        model = (Node)assetManager.loadModel("Models/skinner/skinner.j3o");
        redNode = (Node)model.getChild("LightRed");
        greenNode = (Node)model.getChild("LightGreen");
        blueNode = (Node)model.getChild("LightBlue");
        actor = (Node)model.getChild("Actor");
        rootNode.attachChild(model);
        
        lightRed = new SpotLight();
        lightRed.setName("RED");
        lightRed.setColor(ColorRGBA.Red);
        lightRed.setPosition(redNode.getWorldTranslation());
        lightRed.setDirection((actor.getWorldTranslation().subtract(redNode.getWorldTranslation())).normalizeLocal());
        lightRed.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightRed.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        lightGreen = new SpotLight();
        lightGreen.setName("GREEN");
        lightGreen.setColor(ColorRGBA.Green);
        lightGreen.setPosition(greenNode.getWorldTranslation());
        lightGreen.setDirection((actor.getWorldTranslation().subtract(greenNode.getWorldTranslation())).normalizeLocal());
        lightGreen.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightGreen.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        lightBlue = new SpotLight();
        lightBlue.setName("BLUE");
        lightBlue.setColor(ColorRGBA.Blue);
        lightBlue.setPosition(blueNode.getWorldTranslation());
        lightBlue.setDirection((actor.getWorldTranslation().subtract(blueNode.getWorldTranslation())).normalizeLocal());     
        lightBlue.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightBlue.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        
        DirectionalLight light1 = new DirectionalLight();
        light1.setDirection( (new Vector3f(1.0f, 1.0f, 1.0f)).normalize());
        light1.setColor(ColorRGBA.White);
        
        DirectionalLight light2 = new DirectionalLight();
        light2.setDirection( (new Vector3f(-1.0f, -1.0f, -1.0f)).normalize());
        light2.setColor(ColorRGBA.White);
        
        
        
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.LightGray);
        
        rootNode.addLight(amb);
        rootNode.addLight(lightRed);
        rootNode.addLight(lightGreen);
        rootNode.addLight(lightBlue);
        rootNode.addLight(light1);
        rootNode.addLight(light2);
        
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(-15.320519f, 12.324081f, 14.880732f));
        cam.lookAtDirection(new Vector3f(0.67468536f, -0.37986183f, -0.6328541f), Vector3f.UNIT_Y);
    
    
        inputManager.addMapping("ToogleRed", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("ToogleGreen", new KeyTrigger(KeyInput.KEY_G));        
        inputManager.addMapping("ToogleBlue", new KeyTrigger(KeyInput.KEY_B));        
        inputManager.addMapping("Stimulate", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("ToogleNoise", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("ToogleAutomatic", new KeyTrigger(KeyInput.KEY_A));
        
        inputManager.addListener(this, "ToogleRed", "ToogleGreen", "ToogleBlue",
                "Stimulate", "ToogleNoise", "ToogleAutomatic");
        
        try {
            log1 = new OutputStreamWriter(new FileOutputStream("log1.txt"));
            log2 = new OutputStreamWriter(new FileOutputStream("log2.txt"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public void setNeuralNet(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }

    private long automaticTime = 0;
   // private  float lightReward[] = {-1.0f, 1.0f, 1.0f};
    private float energy = 20000;
    @Override
    public void update() {
        super.update(); //To change body of generated methods, choose Tools | Templates.
        
        if (automatic) {
            long curTime = System.currentTimeMillis();
            if (curTime - automaticTime > 10) {
                stimulate = true;
                automaticTime = curTime;
            }
        }
        
        if (energy < 0) {
            System.out.println("Game Over!!!!");
        }
        
        if (energy > 0 && stimulate && neuralNet != null) {
            
            float maxEnergy = 25000;
            int[] in = neuralNet.getInputs();
            neuralNet.lambda = energy;
            if (noise > 0) {
                neuralNet.setInput(in[0], getLightState(lightRed));
                neuralNet.setInput(in[1], getLightState(lightGreen));
                neuralNet.setInput(in[2], getLightState(lightBlue));
                neuralNet.setInput(in[3], energy/maxEnergy);
            } else {
                neuralNet.setInput(in[1], getLightState(lightRed));
                neuralNet.setInput(in[0], getLightState(lightGreen));
                neuralNet.setInput(in[2], getLightState(lightBlue));   
                neuralNet.setInput(in[3], energy/maxEnergy);
            }
            System.out.println("NOISE: " + noise);
            neuralNet.process();
            
            try {
                if (noise == 1) {
                    log1.write("" + (neuralNet.numberOfUpdates/(2.0f*neuralNet.getSize())) + "\n" );
                } else {
                    log2.write("" + (neuralNet.numberOfUpdates/(2.0f*neuralNet.getSize())) + "\n");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            
            
            double out[] = neuralNet.getOutput();

            float alfa = 0.2f;
            float beta = -0.2f;
            if (out[0] > alfa) {
                lightOn(lightRed, ColorRGBA.Red, redNode, "redMesh1");
            } else if (out[0] < beta) {
                lightOff(lightRed, redNode, "redMesh1");
            }
            energy -= 2 * Math.abs(out[0]);
            
            if (out[1] > alfa) {
                lightOn(lightGreen, ColorRGBA.Green, greenNode, "greenMesh1");
            } else if (out[1] < beta) {
                lightOff(lightGreen, greenNode, "greenMesh1");
            }
            energy -= 2 * Math.abs(out[1]);
            
            if (out[2] > alfa) {
                lightOn(lightBlue, ColorRGBA.Blue, blueNode, "blueMesh1");
            } else if (out[2] < beta) {
                lightOff(lightBlue, blueNode, "blueMesh1");
            }
            energy -= 2 * Math.abs(out[2]);
            
            
            double prate  = neuralNet.numberOfUpdates/(float)(neuralNet.getSize()*neuralNet.getSize());
            
            energy += -3.33 * (1-prate) - 20 * getLightState(lightRed) + 10 * getLightState(lightGreen) 
                    + 10 * getLightState(lightBlue);
            
            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
            
            System.out.println("Energy: " + energy);
            System.out.println(Arrays.toString(out));
            stimulate = false;
        }
    }

    public float getLightState(Light light) {
        if (light.getColor().equals(ColorRGBA.Black)) {
            return -0.5f;
        } else {
            return 0.5f;
        }
    }
    
    public void lightOn(Light light, ColorRGBA c, Node node, String meshName) {
        light.setColor(c);
        Geometry geo = (Geometry) node.getChild(meshName);
        geo.getMaterial().setColor("Diffuse", c);
    }
    
    public void lightOff(Light light, Node node, String meshName) {
        light.setColor(ColorRGBA.Black);
        Geometry geo = (Geometry) node.getChild(meshName);
        geo.getMaterial().setColor("Diffuse", ColorRGBA.Black);
    }
    
    public static void startApp(NeuralNet net) {
       SkinnerApp app = new SkinnerApp();
       AppSettings settings = new AppSettings(true);
       settings.setTitle("SkinnerBox");
       app.setSettings(settings);
       app.setShowSettings(false);
       app.setDisplayStatView(false);
       app.setDisplayFps(false);
       app.neuralNet = net;
       app.start();

    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("ToogleRed")) {
            if (isPressed) {
                if (getLightState(lightRed) == 0.5) {
                    lightOff(lightRed, redNode, "redMesh1");
                } else {
                    lightOn(lightRed, ColorRGBA.Red, redNode, "redMesh1");
                }
            }
        } else  if (name.equals("ToogleGreen")) {
            if (isPressed) {
                if (getLightState(lightGreen) == 0.5) {
                    lightOff(lightGreen, greenNode, "greenMesh1");
                } else {
                    lightOn(lightGreen, ColorRGBA.Green, greenNode, "greenMesh1");
                }                
            }
        } else  if (name.equals("ToogleBlue")) {
            if (isPressed) {
                if (getLightState(lightBlue) == 0.5) {
                    lightOff(lightBlue, blueNode, "blueMesh1");
                } else {
                    lightOn(lightBlue, ColorRGBA.Blue, blueNode, "blueMesh1");
                }                
            }
        } else  if (name.equals("Stimulate")) {
            if (isPressed) {
                stimulate = true;
            }
        } else if (name.equals("ToogleNoise")) {
            if (isPressed) {
                noise = -noise;
            }
        } else if (name.equals("ToogleAutomatic")) {
            if (isPressed) {
                automatic = true;
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        try {
            log1.close();
            log2.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static void main(String args[]) {
        startApp(null);
    }
}
