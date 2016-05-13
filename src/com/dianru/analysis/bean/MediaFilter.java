package com.dianru.analysis.bean;

import java.io.IOException;
import java.io.Serializable;

/**
 * @project : com.dianru.analysis
 * @author JunWu.zhu
 * @date: 2014年10月13日
 * @email : icerivercomeon@gmail.com
 * @qq : 369990256
 * @description : 防作弊等级比例设置
 */
public class MediaFilter implements Serializable {
	private static final long serialVersionUID = 1L;
	private int rate;// 分成比率(100以内)
	private int save;// 扣量比率(100以内)

	public MediaFilter(int rate, int save) {
		this.rate = rate;
		this.save = save;
	}


	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(rate);
		out.writeInt(save);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		this.rate = in.readInt();
		this.save = in.readInt();
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getSave() {
		return save;
	}

	public void setSave(int save) {
		this.save = save;
	}

}
