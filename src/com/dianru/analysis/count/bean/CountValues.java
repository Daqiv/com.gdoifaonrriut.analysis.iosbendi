package com.dianru.analysis.count.bean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianru.analysis.bean.Define;

public class CountValues {
	
	public static Logger LOG = LogManager.getLogger(CountValues.class);
	public static String SQL_FIELDS[] = {
		"show_count","show_invalid","show_unique","show_saved",
		"jump_count","jump_invalid","jump_unique","jump_saved",
		"click_count","click_invalid","click_unique","click_saved","click_income","click_cost",
		"active_count","active_invalid","active_unique","active_saved","active_income","active_cost",
		"job_count","job_invalid","job_saved","job_unique","job_income","job_cost", "job_count1",
		"job_count2", "job_count3", "job_count4", "job_count5", 
		"job_count6", "job_count7", "job_count8", "job_count9"
	};
	
	public String[] getFileds() {
		return SQL_FIELDS;
	}
	
	protected  CountValues() {
		this.show_count = 0;
		this.show_invalid = 0;
		this.show_unique = 0;
		this.show_saved = 0;

		this.jump_count = 0;
		this.jump_invalid = 0;
		this.jump_unique = 0;
		this.jump_saved = 0;

		this.click_count = 0;
		this.click_invalid = 0;
		this.click_unique = 0;
		this.click_saved = 0;
		this.click_income = 0;
		this.click_cost = 0;

		this.active_count = 0;
		this.active_invalid = 0;
		this.active_unique = 0;
		this.active_saved = 0;
		this.active_income = 0;
		this.active_cost = 0;

		this.job_count = 0;
		this.job_invalid = 0;
		this.job_saved = 0;
		this.job_unique = 0;
		this.job_income = 0;
		this.job_cost = 0;
		
		this.job_count1 = 0;
		this.job_count2 = 0;
		this.job_count3 = 0;
		this.job_count4 = 0;
		this.job_count5 = 0;
		this.job_count6 = 0;
		this.job_count7 = 0;
		this.job_count8 = 0;
		this.job_count9 = 0;
	}
	
	public CountValues clone() {
		
		CountValues that = new CountValues();
		that.show_count = this.show_count;
		that.show_invalid = this.show_invalid;
		that.show_unique = this.show_unique;
		that.show_saved = this.show_saved;

		that.jump_count = this.jump_count;
		that.jump_invalid = this.jump_invalid;
		that.jump_unique = this.jump_unique;
		that.jump_saved = this.jump_saved;

		that.click_count = this.click_count;
		that.click_invalid = this.click_invalid;
		that.click_unique = this.click_unique;
		that.click_saved = this.click_saved;
		that.click_income = this.click_income;
		that.click_cost = this.click_cost;

		that.active_count = this.active_count;
		that.active_invalid = this.active_invalid;
		that.active_unique = this.active_unique;
		that.active_saved = this.active_saved;
		that.active_income = this.active_income;
		that.active_cost = this.active_cost;

		that.job_count = this.job_count;
		that.job_invalid = this.job_invalid;
		that.job_saved = this.job_saved;
		that.job_unique = this.job_unique;
		that.job_income = this.job_income;
		that.job_cost = this.job_cost;
		
		that.job_count1 = this.job_count1;
		that.job_count2 = this.job_count2;
		that.job_count3 = this.job_count3;
		that.job_count4 = this.job_count4;
		that.job_count5 = this.job_count5;
		that.job_count6 = this.job_count6;
		that.job_count7 = this.job_count7;
		that.job_count8 = this.job_count8;
		that.job_count9 = this.job_count9;
		
		return that;
	}
	
	public static CountValues create(int action, int count, int invalid, int unique, int saved,
			float income, float cost) {
		CountValues item = new CountValues();
				
		if(action == Define.ACTION_SHOW) {
			item.show_count = count;
			item.show_invalid = invalid;
			item.show_unique = unique;
			item.show_saved = saved;
		} else if(action == Define.ACTION_JUMP) {
			item.jump_count = count;
			item.jump_invalid = invalid;
			item.jump_unique = unique;
			item.jump_saved = saved;
		} else if(action == Define.ACTION_CLICK) {
			item.click_count = count;
			item.click_invalid = invalid;
			item.click_unique = unique;
			item.click_saved = saved;
			item.click_income = income;
			item.click_cost = cost;
		} else if(action == Define.ACTION_ACTIVE) {
			item.active_count = count;
			item.active_invalid = invalid;
			item.active_unique = unique;
			item.active_saved = saved;
			item.active_income = income;
			item.active_cost = cost;
		} else if(action == Define.ACTION_JOB) {
			item.job_count = count;
			item.job_invalid = invalid;
			item.job_unique = unique;
			item.job_saved = saved;
			item.job_income = income;
			item.job_cost = cost;
		}else if (action == Define.ACTION_JOB_NEXT) {
			switch (invalid) {
			case 1:
				item.job_count1 = count;
				break;
			case 2:
				item.job_count2 = count;
				break;
			case 3:
				item.job_count3 = count;
				break;
			case 4:
				item.job_count4 = count;
				break;
			case 5:
				item.job_count5 = count;
				break;
			case 6:
				item.job_count6 = count;
				break;
			case 7:
				item.job_count7 = count;
				break;
			case 8:
				item.job_count8 = count;
				break;
			case 9:
				item.job_count9 = count;
				break;
			}
			item.job_income = income;
			item.job_cost = cost;
		}
		LOG.trace(String.format("job countvalues：action:%d,income:%f,cost:%f,invalid:%d,count:%d",action,income,cost,invalid,count));
		return item;
	}
	
	public Object[] getValues() {
		return new Object[] {
			show_count,show_invalid,show_unique,show_saved,
			jump_count, jump_invalid,jump_unique,jump_saved,
			click_count,click_invalid,click_unique,click_saved,click_income,click_cost,
			active_count,active_invalid,active_unique,active_saved,active_income,active_cost,
			job_count,job_invalid,job_saved,job_unique,job_income,job_cost,
			job_count1, job_count2, job_count3, job_count4,
			job_count5, job_count6, job_count7, job_count8, job_count9
		};
	}
	
	public int show_count;
	public int show_invalid;
	public int show_unique;
	public int show_saved;

	public int jump_count;
	public int jump_invalid;
	public int jump_unique;
	public int jump_saved;

	public int click_count;
	public int click_invalid;
	public int click_unique;
	public int click_saved;
	public float click_income;
	public float click_cost;

	public int active_count;
	public int active_invalid;
	public int active_unique;
	public int active_saved;
	public float active_income;
	public float active_cost;

	public int job_count;
	public int job_invalid;
	public int job_saved;
	public int job_unique;
	public float job_income;
	public float job_cost;
	
	public int job_count1;
	public int job_count2;
	public int job_count3;
	public int job_count4;
	public int job_count5;
	public int job_count6;
	public int job_count7;
	public int job_count8;
	public int job_count9;

	public void add(CountValues that) {
		this.show_count += that.show_count;
		this.show_invalid += that.show_invalid;
		this.show_unique += that.show_unique;
		this.show_saved += that.show_saved;

		this.jump_count += that.jump_count;
		this.jump_invalid += that.jump_invalid;
		this.jump_unique += that.jump_unique;
		this.jump_saved += that.jump_saved;

		this.click_count += that.click_count;
		this.click_invalid += that.click_invalid;
		this.click_unique += that.click_unique;
		this.click_saved += that.click_saved;
		this.click_income += that.click_income;
		this.click_cost += that.click_cost;

		this.active_count += that.active_count;
		this.active_invalid += that.active_invalid;
		this.active_unique += that.active_unique;
		this.active_saved += that.active_saved;
		this.active_income += that.active_income;
		this.active_cost += that.active_cost;

		this.job_count += that.job_count;
		this.job_invalid += that.job_invalid;
		this.job_saved += that.job_saved;
		this.job_unique += that.job_unique;
		this.job_income += that.job_income;
		this.job_cost += that.job_cost;
		
		this.job_count1 += that.job_count1;
		this.job_count2 += that.job_count2;
		this.job_count3 += that.job_count3;
		this.job_count4 += that.job_count4;
		this.job_count5 += that.job_count5;
		this.job_count6 += that.job_count6;
		this.job_count7 += that.job_count7;
		this.job_count8 += that.job_count8;
		this.job_count9 += that.job_count9;
	}
}