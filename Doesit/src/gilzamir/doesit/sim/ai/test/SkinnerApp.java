package gilzamir.doesit.sim.ai.test;

import com.bulletphysics.collision.shapes.ConeShape;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.sun.awt.AWTUtilities;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import multinet.net.NeuralNet;



class GameCanvas extends JPanel {
    public SkinnerApp app;

    private int spriteWidth=32, spriteHeight=32, halfSpriteWidth=16, halfSpriteHeight=16;
    private int x = 0, y = 0, xl = 0, yl = 0;
    private float ang = 0;
    private Image actorSprite;
    private ImageObserver actorObserver;
        
    public GameCanvas(SkinnerApp app) {
        this.app = app;
        ImageIcon icon = new ImageIcon("/midias/skinner/actor.png");
        
        actorSprite = icon.getImage();
        actorObserver = icon.getImageObserver();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        if (app.skinnerBoxImpl != null) {
            x = app.toXScreen(app.skinnerBoxImpl.dx);
            y = app.toYScreen(app.skinnerBoxImpl.dy);
       
            GObject obj = app.skinnerBoxImpl.getLight();
            xl = app.toXScreen(obj.x);
            yl = app.toYScreen(obj.y);
            g.fillOval(xl, yl, 16, 16);

            Graphics2D g2 = (Graphics2D)g;
            if (app.skinnerBoxImpl.dx != 0.0) {
                ang = (float)Math.atan2(app.skinnerBoxImpl.dy, app.skinnerBoxImpl.dx);
            } else if (app.skinnerBoxImpl.dy > 0){
                ang = (float)Math.PI/2.0f;
            }  else {
                ang = (float)Math.PI/(-2.0f);
            }
            g2.rotate(ang, x+halfSpriteWidth, y+halfSpriteHeight);
            g2.drawImage(actorSprite, x, y, spriteWidth, spriteHeight, actorObserver); 
            System.out.println("POS " + x + ", " + y);
        }
    }
}

public class SkinnerApp extends JFrame {

    NeuralNet neuralNet;
    private OutputStreamWriter log1, log2;
    SkinnerLikeEvaluator skinnerBoxImpl;
    private GameCanvas areaDraw;

    private static boolean started = false;
    
    public SkinnerApp() {
        super("SkinnerApp");
        areaDraw = new GameCanvas(this);
        this.add(areaDraw);
        setSize(640,480);
    }

    
    public final int toXScreen(double x) {
        double tx = (x + 50.0)/100.0;
        return Math.round((float)(tx * (areaDraw.getWidth()-10)));
    }
    
    public final int toYScreen(double y) {
        double ty = 1.0 - (y + 50.0)/100.0;
        return Math.round((float)(ty * (areaDraw.getHeight()-10)));    
    }
    
    public void simpleInitApp() {       
       if (skinnerBoxImpl != null) {
            GObject li = skinnerBoxImpl.getLight();
        }
        
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
    
    public void update() {
        areaDraw.repaint();    
    }
    
    public void setSkinnerBoxImpl(SkinnerLikeEvaluator ev) {
        this.skinnerBoxImpl = ev;
    }

    
    public void setLightColor(Light light, ColorRGBA c, Node node, String meshName) {
        light.setColor(c);
        Geometry geo = (Geometry) node.getChild(meshName);
        geo.getMaterial().setColor("Diffuse", c);
    }
    
    public static void startApp(SkinnerLikeEvaluator ev, NeuralNet net) {
        final SkinnerApp app = new SkinnerApp();
        ev.showDisplay = true;
        ev.showEnergy = true;
        ev.showOutput = true;
        app.setSkinnerBoxImpl(ev);
        app.neuralNet = net;   
        app.setVisible(true);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        

        if (ev != null) {
             if (!started) {
                 ev.reset();
                 started = true;
             }
             System.out.println(ev.energy);

             while (ev.energy > 0) {
                 app.update();
                 app.skinnerBoxImpl.step(app.neuralNet);


                 try { 
                    Thread.sleep(100);
                 } catch(Exception e) {
                     e.printStackTrace();
                 }
             }
        }
    }

    public static void main(String args[]) {
        startApp(null, null);
    }
}
