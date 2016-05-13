##getFileds
DetailHourKeys {  
	"created",
	"year",
	"mon",
	"day",
	"hour",
	"type",
	"data_from",
	"ad_from",
	"appid",
	"uid",
	"adid",
	"cid"
}  
DetailDayKeys {  

	"created",
	"year",
	"mon",
	"day",
	"type",
	"data_from",
	"ad_from",
	"appid",
	"uid",
	"adid",
	"cid"
}
少着hour字段是要对着个进行统计


SumHourKeys {
	"created",
	"year",
	"mon",
	"day",
	"hour",
	"type",
	"data_from",
	"ad_from"
}
对比detailHoursKeys少着adid（广告）和cid（广告主）和appid（应用）和uid（开发者）

SumDayKeys{
	"created",
	"year",
	"mon",
	"day",
	"type",
	"data_from",
	"ad_from"
}
少着hour字段是要对着个进行统计

MediaHourKeys {
	"created",
	"year",
	"mon",
	"day",
	"hour",
	"type",
	"data_from",
	"ad_from",
	"appid",
	"uid"
}
对比detailHoursKeys少着adid（广告）和cid（广告主）

MediaDayKeys{
	"created",
	"year",
	"mon",
	"day",
	"type",
	"data_from",
	"ad_from",
	"appid",
	"uid"
}
少着hour字段是要对着个进行统计

AdsHourKeys {
	"created",
	"year",
	"mon",
	"day",
	"hour",
	"type",
	"data_from",
	"ad_from",
	"adid",
	"cid"
}
对比detailHoursKeys少着uid（开发者）和appid（应用），是因为要忽略这几个参数进行统计

AdsDayKeys {
	"created",
	"year",
	"mon",
	"day",
	"type",
	"data_from",
	"ad_from",
	"adid",
	"cid"
}
少着hour字段是要对着个进行统计

##create
数据从**DetailHourKeys.create**的方法进行最基本的数据传入,然后通过变相的递归调用进行采用create的方法将最基本的数据进行传入
各个对应的基本bean当中，

##递归链
递归链--》调用countStore的save方法--》ReportToDatabase.save方法--》reportAdsHour进行类型转换并返回.save方法形成递归链

##getTable

* DetailHourKeys  
	* prefix_hour_created/100
* AdsHourKeys  
	* prefix_ad_hour_created/100
* MediaHourKeys  
	* prefix_media_hour_created/100
* SumHourKeys  
	* prefix_hour
	
填表对应修改hour转换为day



