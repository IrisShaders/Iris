package net.coderbot.iris.parsing;

public class SmoothFloat{
	static final float SMOOTH_SCALE = (float)Math.log(0.01); // after `fadeTime`, we will have gone `1-0.01 == 99%` of the way
	
	private float current;
	private long previousTime;
	
	public float evaluate(float target, float fadeUpTime, float fadeDownTime){
		return evaluate(target, target > this.current? fadeUpTime : fadeDownTime);
	}
	
	public float evaluate(float target, float fadeTime){
		long now = System.currentTimeMillis();
		double timeDiff = (now - this.previousTime);
		double diff = target - this.current;
		this.previousTime = now;
		
		//Math.abs(diff) <= 1e-4 should be triggered at ~2 * fadeTime
		if(fadeTime <= 0 || Math.abs(diff) <= 1e-4 || timeDiff > fadeTime * 2){
			this.current = target;
			return this.current;
		}
		
		double timeStep = timeDiff / fadeTime;
		double step = Math.exp(timeStep * -4.61);
		
		this.current = (float) (this.current + diff * step);
		return this.current;
	}
}
