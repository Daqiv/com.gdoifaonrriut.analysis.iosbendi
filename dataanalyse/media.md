##media.options
* {  
   	* "is_openudid" : 1,  
	* "redownload" : 1,  
	* "is_ssid" : 1,  
	* "cheat_buckle_proportion" : 23,  
	* "is_ig" : 1,  
	* "is_disk" : 1,  
	* "is_session" : 1,  
	* "is_process_exception" : 1  
* }

##media.type
媒体分类 1 应用 2 渠道
>1/2

##media.state  
状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停, 9 留存

##media.rates

* {
	* "1" : {
		* "save" : 25,
		* "rate" : 100
	* },
	* "2" : {
		* "save" : 0,
		* "rate" : 0
	* },
	* "3" : {
		* "save" : 0,
		* "rate" : 0
	* },
	* "4" : {
		* "save" : 0,
		* "rate" : 0
	* },
	* "5" : {
		* "save" : 0,
		* "rate" : 0
	* }
* }

##media.mlevel
评级
>1

##media.is_wangzhuan
是否网赚 1是，2不是


##media-app.ratio	
虚拟汇率
>100

## media-app.offer-wall
积分墙配置json（皮肤style，状态开启state1开启，2关闭，是否回调callback，类型type（视频积分墙，全屏积分墙，列表积分墙等））  

* {  
	* "callback" : 2,    
	* "state" : 1,    
	* "style" : "orange",  
	* "type" : 1  
* }

##media-app.callback_url	
积分回调地址  
> http://www.dianru.com/test/test.aspx?app=dianru
