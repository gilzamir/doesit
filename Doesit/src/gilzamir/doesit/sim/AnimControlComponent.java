package gilzamir.doesit.sim;

public class AnimControlComponent {
    private final float START = 0.0f;
    private float maxTime = 1.0f;
    private float step = 0.01f;
    private float time = START;
    private float defaultChannelSpeed;
    private float speed = 0;
    
    public AnimControlComponent(float dspeed){
        defaultChannelSpeed = dspeed;
        reset();
    }

    public void setDefaultChannelSpeed(float defaultChannelSpeed) {
        this.defaultChannelSpeed = defaultChannelSpeed;
        reset();
    }

    public float getTime() {
        return time;
    }
    
    public final void reset() {
        time = START;
        speed = defaultChannelSpeed;
    }
    
    public void step() {
        if (!finished()) {
            time += step;
            if (time >= maxTime) {
                speed = 0;
            }
        }
    }
    
    public boolean finished() {
        return time >= maxTime;
    }

    public float getSpeed() {
        return speed;
    }

    public void setStep(float step) {
        this.step = step;
    }
    
    public void setMaxTime(float maxTime) {
        this.maxTime = maxTime;
    }

    public float getMaxTime() {
        return maxTime;
    }
}
