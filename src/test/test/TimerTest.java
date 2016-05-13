package test.test;

import java.util.Timer;
import java.util.TimerTask;

import org.testng.annotations.Test;
/**
 * 定时器写法
 * 1.start 先执行一次任务，然后定时任务
 * 2.定时任务继承自timerTask，实现方法即可
 * @author goforit
 *
 */
public class TimerTest extends TimerTask{
	
	private static Timer timer = null;
	private static TimerTest timerTest = new TimerTest();
	 public static void start() {
	    	if(timer != null) return;
	  
	    	update();
	    	
	    	timer = new Timer();
	        timer.schedule(timerTest, 1000, 1000);
	    }
	 
	 public static void update() {
	    	System.out.println("timer called！");
	    }

	public void run() {
		update();
	}
	
	@Test
	public void runtest(){
		start();
		long begin = System.currentTimeMillis();
		boolean stop  = false;
		while(!stop){
			if(System.currentTimeMillis() - begin > 1000*60){
				stop = true;
			}
		}
	}
}
