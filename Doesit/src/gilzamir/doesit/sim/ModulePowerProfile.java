package gilzamir.doesit.sim;

public class ModulePowerProfile extends  AbstractComponent {
    public Float getActionRequiredPower(String action) {
        return getFloat(action);
    }
    
    public void setActionRequiredPower(String action, Float power) {
        this.setFloat(action, power);
    }
}