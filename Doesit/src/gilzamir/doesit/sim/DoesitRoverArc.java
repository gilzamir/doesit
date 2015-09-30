package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

public class DoesitRoverArc {


    private Node armModelNode;
    private RigidBodyControl armModelControl;
    private BulletAppState physics;
    
    public DoesitRoverArc(float chassiMass) {
    }

    public void initGeometry(SimpleApplication app, Node rootNode) {
        
        armModelNode = new Node("armModel");
        
        Cylinder armBaseMesh = new Cylinder(16, 16, 0.5f, 1.5f);
        Geometry armGeo = new Geometry("ArmBase", armBaseMesh);
        Material mat = new Material(app.getAssetManager(), "/Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.Blue);
        armGeo.setMaterial(mat);
        armGeo.setLocalRotation( new Quaternion().fromAngles(-FastMath.DEG_TO_RAD * 90.0f,0, 0) ) ;
        
        armModelNode.attachChild(armGeo);
        
        Box solidJointMesh = new Box(0.2f, 0.2f, 0.2f);
        Geometry solidJointGeom = new Geometry("SolidJoint", solidJointMesh);
        solidJointGeom.setMaterial(mat);
        
        armModelNode.attachChild(solidJointGeom);
        solidJointGeom.move(0f,0.9f, 0f);
        
        rootNode.attachChild(armModelNode);
    }

    public void initPhysics(BulletAppState physicState) {
        
    }

    public void rotateArmBaseJoint(float value) {
    
    }
    
    public Node getNode() {
        return this.armModelNode;
    }

    public void brake(float force) {
    }

    public void accelerate(float value) {
    }
    
    public void steer(float value) {
    }
    
    public RigidBodyControl getControl() {
        return armModelControl;
    }
}
