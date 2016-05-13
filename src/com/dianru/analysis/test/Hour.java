package com.dianru.analysis.test;

public class Hour {
	
	 private  int  created;
	 private  int year;
	 private  int mon; // 月
	 private int day;   //日
	 private int hour; // 时',
	 private int appid;
	 private int uid;
	 private int adid;
	 private int cid;
	 private int type; //广告类型：积分墙1 免费墙2 插屏3 全屏4 广告条5 热门推荐6,
	 private int data_from; //数据来源：1sdk,2api,3channel（自身渠道，别人渠道）',
	 private int ad_from; //广告来源：0普通广告，1渠道广告',
	 
	 private int show_count; // '展示有效次数',
	 private int  show_unique; //展示独立',
	 private int show_saved; //展示扣量',
	 private int  click_count;// 点击有效次数',
	 private int  click_unique; //点击独立展次',
	 private int  click_saved; //点击扣量',
	 private int  jump_count; //跳转有效次数（二次点击）',
	 private int  jump_unique; //跳转独立（二次点击）',
	 private int  jump_saved; //跳转扣量（二次点击）',
	 private int  active_count; //激活有效次数',
	 private int  active_unique; //激活独立次数',
	 private int  active_saved; //激活扣量',
	 private float  click_income; //点击接入价',
	 private float   click_cost; //点击成本',
	 private float   active_income; //回调接入价',
	 private float   active_cost; //回调成本',
	 
	 public String getSql(int type, String date){
			
			String  year = date.substring(0, 4);
			String mon = date.substring(4, 6);
			String day = date.substring(6, 8);
			String tableName = "";
			String sql = "";
			
			switch (type) {
			case 1:
				tableName = "cpa_media_hour_" + year +mon;
				
				sql = "INSERT INTO "+tableName+"(`created`,`year`,`mon`,`day`,`hour`,`type`,`data_from`,`ad_from`,`appid`,`uid`,`show_count`,`show_invalid`,`show_unique`,`show_saved`,`jump_count`,`jump_invalid`,`jump_unique`,`jump_saved`,`click_count`,`click_invalid`,`click_unique`,`click_saved`,`click_income`,`click_cost`,`active_count`,`active_invalid`,`active_unique`,`active_saved`,`active_income`,`active_cost`,`job_count`,`job_invalid`,`job_saved`,`job_unique`,`job_income`,`job_cost`) VALUES "
						+ "('"+date+"','"+year+"','"+mon+"','"+day+"','"+this.getHour()+"','6','1','0','"+this.getAppid()+"','"+this.getUid()+"','"+this.getShow_count()+"','0','"+this.getShow_unique()+"','"+this.getShow_saved()+"','"+this.getJump_count()+"','0','"+this.getJump_unique()+"','"+this.getJump_saved()+"','"+this.getClick_count()+"','0','"+this.getClick_unique()+"','"+this.click_saved+"','0.0','0.0','"+this.getActive_count()+"','0','"+this.active_unique+"','"+this.active_saved+"','0.0','0.0','0','0','0','0','0.0','0.0');";

				break;
			case 2:
				tableName = "cpa_ad_hour_" + year +mon;

				sql = "INSERT INTO "+tableName+"(`created`,`year`,`mon`,`day`,`hour`,`type`,`data_from`,`ad_from`,`adid`,`cid`,`show_count`,`show_invalid`,`show_unique`,`show_saved`,`jump_count`,`jump_invalid`,`jump_unique`,`jump_saved`,`click_count`,`click_invalid`,`click_unique`,`click_saved`,`click_income`,`click_cost`,`active_count`,`active_invalid`,`active_unique`,`active_saved`,`active_income`,`active_cost`,`job_count`,`job_invalid`,`job_saved`,`job_unique`,`job_income`,`job_cost`) VALUES "
						+ "('"+date+"','"+year+"','"+mon+"','"+day+"','"+this.getHour()+"','6','1','0','"+this.getAdid()+"','"+this.getCid()+"','"+this.getShow_count()+"','0','"+this.getShow_unique()+"','"+this.getShow_saved()+"','"+this.getJump_count()+"','0','"+this.getJump_unique()+"','"+this.getJump_saved()+"','"+this.getClick_count()+"','0','"+this.getClick_unique()+"','"+this.click_saved+"','0.0','0.0','"+this.getActive_count()+"','0','"+this.active_unique+"','"+this.active_saved+"','0.0','0.0','0','0','0','0','0.0','0.0');";
				
				break;
			case 3:	//这个是最底层数据
				tableName = "cpa_hour_"  + year +mon;
				sql = "INSERT INTO "+tableName+"(`created`,`year`,`mon`,`day`,`hour`,`type`,`data_from`,`ad_from`, `appid`,`uid`,`adid`,`cid`,`show_count`,`show_invalid`,`show_unique`,`show_saved`,`jump_count`,`jump_invalid`,`jump_unique`,`jump_saved`,`click_count`,`click_invalid`,`click_unique`,`click_saved`,`click_income`,`click_cost`,`active_count`,`active_invalid`,`active_unique`,`active_saved`,`active_income`,`active_cost`,`job_count`,`job_invalid`,`job_saved`,`job_unique`,`job_income`,`job_cost`) VALUES "
						+ "('"+date+"','"+year+"','"+mon+"','"+day+"','"+this.getHour()+"','6','1','0','"+this.getAppid()+"','"+this.getUid()+"','"+this.getAdid()+"','"+this.getCid()+"','"+this.getShow_count()+"','0','"+this.getShow_unique()+"','"+this.getShow_saved()+"','"+this.getJump_count()+"','0','"+this.getJump_unique()+"','"+this.getJump_saved()+"','"+this.getClick_count()+"','0','"+this.getClick_unique()+"','"+this.getClick_saved()+"','0.0','0.0','"+this.getActive_count()+"','0','"+this.getActive_unique()+"','"+this.getActive_saved()+"','0.0','0.0','0','0','0','0','0.0','0.0');";

				break;
			case 4:
				tableName = "cpa_hour";
				
				sql = "INSERT INTO "+tableName+"(`created`,`year`,`mon`,`day`,`hour`,`type`,`data_from`,`ad_from`,`appid`,`uid`,`show_count`,`show_invalid`,`show_unique`,`show_saved`,`jump_count`,`jump_invalid`,`jump_unique`,`jump_saved`,`click_count`,`click_invalid`,`click_unique`,`click_saved`,`click_income`,`click_cost`,`active_count`,`active_invalid`,`active_unique`,`active_saved`,`active_income`,`active_cost`,`job_count`,`job_invalid`,`job_saved`,`job_unique`,`job_income`,`job_cost`) VALUES "
						+ "('"+date+"','"+year+"','"+mon+"','"+day+"','"+this.getHour()+"','6','1','0','"+this.getAppid()+"','"+this.getUid()+"','"+this.getShow_count()+"','0','"+this.getShow_unique()+"','"+this.getShow_saved()+"','"+this.getJump_count()+"','0','"+this.getJump_unique()+"','"+this.getJump_saved()+"','"+this.getClick_count()+"','0','"+this.getClick_unique()+"','"+this.click_saved+"','0.0','0.0','"+this.getActive_count()+"','0','"+this.active_unique+"','"+this.active_saved+"','0.0','0.0','0','0','0','0','0.0','0.0');";

				
				break;
			default:
				break;
			}
			//ON DUPLICATE KEY UPDATE `show_count`=`show_count`+'"+this.active_count+"',`show_invalid`=`show_invalid`+'0',`show_unique`=`show_unique`+'"+this.getActive_unique()+"',`show_saved`=`show_saved`+'"+this.active_saved+"',`jump_count`=`jump_count`+'"+this.jump_count+"',`jump_invalid`=`jump_invalid`+'0',`jump_unique`=`jump_unique`+'"+this.getJump_unique()+"',`jump_saved`=`jump_saved`+'"+this.getJump_saved()+"',`click_count`=`click_count`+'"+this.getClick_count()+"',`click_invalid`=`click_invalid`+'0',`click_unique`=`click_unique`+'"+this.getClick_unique()+"',`click_saved`=`click_saved`+'"+this.getClick_saved()+"',`click_income`=`click_income`+'0.0',`click_cost`=`click_cost`+'0.0',`active_count`=`active_count`+'"+this.active_count+"',`active_invalid`=`active_invalid`+'0',`active_unique`=`active_unique`+'"+this.active_unique+"',`active_saved`=`active_saved`+'"+this.active_saved+"',`active_income`=`active_income`+'0.0',`active_cost`=`active_cost`+'0.0',`job_count`=`job_count`+'0',`job_invalid`=`job_invalid`+'0',`job_saved`=`job_saved`+'0',`job_unique`=`job_unique`+'0',`job_income`=`job_income`+'0.0',`job_cost`=`job_cost`+'0.0'"
			
			//cpa_media_hour_201411    cpa_media_hour_201411
			
			//cpa_hour_201411    cpa_hour_201412
			
			//cpa_hour
			
			//cpa_ad_hour_201411    cpa_ad_hour_201412
			
			return sql;
		}	
		
	public int getCreated() {
		return created;
	}

	public void setCreated(int created) {
		this.created = created;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMon() {
		return mon;
	}

	public void setMon(int mon) {
		this.mon = mon;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getAppid() {
		return appid;
	}

	public void setAppid(int appid) {
		this.appid = appid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getAdid() {
		return adid;
	}

	public void setAdid(int adid) {
		this.adid = adid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getData_from() {
		return data_from;
	}

	public void setData_from(int data_from) {
		this.data_from = data_from;
	}

	public int getAd_from() {
		return ad_from;
	}

	public void setAd_from(int ad_from) {
		this.ad_from = ad_from;
	}

	public int getShow_count() {
		return show_count;
	}

	public void setShow_count(int show_count) {
		this.show_count = show_count;
	}

	public int getShow_unique() {
		return show_unique;
	}

	public void setShow_unique(int show_unique) {
		this.show_unique = show_unique;
	}

	public int getShow_saved() {
		return show_saved;
	}

	public void setShow_saved(int show_saved) {
		this.show_saved = show_saved;
	}

	public int getClick_count() {
		return click_count;
	}

	public void setClick_count(int click_count) {
		this.click_count = click_count;
	}

	public int getClick_unique() {
		return click_unique;
	}

	public void setClick_unique(int click_unique) {
		this.click_unique = click_unique;
	}

	public int getClick_saved() {
		return click_saved;
	}

	public void setClick_saved(int click_saved) {
		this.click_saved = click_saved;
	}

	public int getJump_count() {
		return jump_count;
	}

	public void setJump_count(int jump_count) {
		this.jump_count = jump_count;
	}

	public int getJump_unique() {
		return jump_unique;
	}

	public void setJump_unique(int jump_unique) {
		this.jump_unique = jump_unique;
	}

	public int getJump_saved() {
		return jump_saved;
	}

	public void setJump_saved(int jump_saved) {
		this.jump_saved = jump_saved;
	}

	public int getActive_count() {
		return active_count;
	}

	public void setActive_count(int active_count) {
		this.active_count = active_count;
	}

	public int getActive_unique() {
		return active_unique;
	}

	public void setActive_unique(int active_unique) {
		this.active_unique = active_unique;
	}

	public int getActive_saved() {
		return active_saved;
	}

	public void setActive_saved(int active_saved) {
		this.active_saved = active_saved;
	}

	public float getClick_income() {
		return click_income;
	}

	public void setClick_income(float click_income) {
		this.click_income = click_income;
	}

	public float getClick_cost() {
		return click_cost;
	}

	public void setClick_cost(float click_cost) {
		this.click_cost = click_cost;
	}

	public float getActive_income() {
		return active_income;
	}

	public void setActive_income(float active_income) {
		this.active_income = active_income;
	}

	public float getActive_cost() {
		return active_cost;
	}

	public void setActive_cost(float active_cost) {
		this.active_cost = active_cost;
	}
	
	public static void main(String[] args) {
		
		Hour h1 = new Hour();
		String sql1 = h1.getSql(1, "20151111");
		System.out.println(sql1);
		
		Hour h2 = new Hour();
		String sql2 = h2.getSql(1, "20151231");
		System.out.println(sql2);
	}
}