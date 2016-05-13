package com.dianru.analysis.bean;

public class AdsRunState {

	private int num;
	private float money;

	public AdsRunState(int num, float money) {
		this.num = num;
		this.money = money;
	}
	
	public AdsRunState(String stateString) {
		String ps[] = stateString.split(",");
		if(ps.length != 2) {
			this.num = 0;
			this.money = 0.0f;
			return;
		}
		this.num = Integer.parseInt(ps[0]);
		this.money = Float.parseFloat(ps[1]);
	}

	public void add(int num, float money) {
		this.num += num;
		this.money += money;
	}

	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public float getMoney() {
		return money;
	}
	public void setMoney(float money) {
		this.money = money;
	}
	public String toString() {
		return String.format("%d,%f", num, money);
	}
}
