package gilzamir.doesit.sim;

public class RoverEnginePowerProfile {
    public float accelerationPowerConsumption;
    public float steerPowerConsumption;
    public float brakePowerConsumption;
    public float armActionPowerConsumption;
    public float lightPowerConsumption;
    public float solarCaptureEfficiency;
    
    public RoverEnginePowerProfile() {
        this.accelerationPowerConsumption = 3.0f;
        this.steerPowerConsumption = 3.0f;
        this.brakePowerConsumption = 3.0f;
        this.brakePowerConsumption = 3.5f;
        this.armActionPowerConsumption = 4.0f;
        this.lightPowerConsumption = 10.0f;
        this.solarCaptureEfficiency = 10.0f;
    }
}
