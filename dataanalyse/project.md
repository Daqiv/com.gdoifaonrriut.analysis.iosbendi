##MediaFilterCache
1. 根据广告类型和媒体的评级和是否为网赚读取对应的**分成**和**扣量**比设置进入map
2. 将自己media自身中的数据和filters_level中的数据进行比较，只要不相同直接将数据设置进入**DATA-MEDIA**的redis中，  
**point：**这儿更新完之后会直接更新mediaApp中对象的值


##MeidaPriceCache
1. 根据**appid-adid**将分成价格price存入map中
2. 同时存入**DATA-MEDIA-PRICE**的redis中

## MediaCache ##
1. 通过media，media-app，media-channel联表查询 新建继承自media的mediaApp和mediaChannel对象并放入监视器map中

## AdsCache ##
1. 通过ads,ad_plan的联表查询 新建app对象并放入监视器中
2. 同时更新redis中存取的相关类型的值
	1. 设置控量处理


## ShowProcessor ##
1. 先求出媒体新增独立请求数，作为**invalid**假量字段
2. 如果广告id字段不为空，那么将**invalid**假量置为0
2. 计算**save**变量
4. 放入内存等待1分钟入库
4. **平滑控量**（针对mac）
5. **展示频次控制**（针对udid）

## ClickProcessor ##
1. 获取控量处理，如果剩余数为0，直接返回，不进行如下统计
2. 分别对从media和ad两个方面考虑isStop的取值
3. **控量**（没有区分平滑和快速）
4. **点击频次控制**
5. 获取今天是否被点击过（查询(按照时间，类型和标识查的)callBack表获取**历史纪录**的CallbackItem对象），如果是放入内存，等待统计，并返回
6. 按照的vals和media以及ad 重新初始化item
7. 进行防作弊计算invalid
8. 如果是以点击计费，计算出income和cost，并放入缓存**DATA-CLICK-INFO**
9. 计算**save**变量
10. 插入callback数据库和redis的value数据库
11. 添加进入内存的操作
12. 特殊回调模式，所以增加点击记录

## JumpProcessor ##
1. 获取控量处理，如果剩余数为0，直接返回，不进行如下统计
2. **控量**（没有区分平滑和快速）
3. 获取独立点击（针对今天的），如果为1，而且不存在30天的的历史纪录，计算invalid，save，更新callback数据库和redis的value数据库，添加进入内存的操作
4. 添加进入内存的操作
5. 特殊回调模式，所以增加点击记录

## ActiveProcessor ##
1. 获取30天的记录，如果记录为空（不存在点击，已经激活）直接返回
2. 如果callback中点击，跳转和激活都不存在，直接返回
3. 广告下线后，我们只接收广告主三天内的激活，给媒体下发只发1天内的激活
4. 多次激活对应的价格读取
5. 判断item.action广告是否已经激活
	1. 已经激活
		1. 进行下层判断并作激活处理
		2. 返回
	2. 没有激活
		1. 判断历史是否激活isActive
			1. 已经激活，直接返回
6. ip假量
	1. 针对同一个ip同一adid和同一action在当天不得超过指定值，超过指定为假量-->invalid
	2. redis中的值 **IP_20160513|action-adid-ip|num** 针对num自增1--》ipCount
	3. 如果应用媒体不是测试状态，那么判断ipCount>ads.ipNum-->invalid |= 1
7. 时间假量
	1. 计算line中的时间和callBack中的打开时间之差-->interval
	2. 和广告中的字段进行比较，如果超过设置范围-->invalid |=2
8. 下面开始计算invalid子字段
9. 乐抢评分浮动金额  
	1. 
10. 判断是进程激活
	1. 判断广告是否计入报表
		1. 是，无操作
		2. 否，将收入和消费清空
	2. unique = 1
11. 放入内存进行统计
12. 更新item中的数据将其更新到callback表中，如果是今天的数据直接update，如果不是，需要add
13. 将激活信息存入redis当中addActive，对应前面的isActive函数
14. 同步激活时间，用于控制深度任务的显示
15. 媒体所有广告前10不扣量，渠道针对单一广告前50不扣量
16. 下发请求对接sendScore将请求的url以及其他信息等放入**ACTION-HTTP-REQUEST**
17. 快速精准控量快速任务完成之后删除		
18. 快速任务实时上报IDFA给广告主，将对应的http请求和数据信息放入**REPORT-IDFA-TOCP**

## JobProcessor ##
1. **0开始active，1以后都是job** 刚开始是激活，激活完成之后就开始做任务了，所有默认job的active_num为1
2. 获取30天的记录，如果记录为空（不存在点击，已经激活）直接返回
3. 多次激活对应价格读取，判断
4. **cost**乘以针对media和ad的**rate**比率求出花费
5. app下发**积分**计算
6. 将积分下发采用下发请求放入**ACTION-HTTP-REQUEST**
7. 放入内存的等待被统计
8. 同步激活时间，用于控制深度任务的显示
9. 将明细数据设置进入**callback**表当中  

##CallbackProcessor##
1. 在所有处理器中只有ShowProcessor中没有继承此类
2. 此类封装了一个方法，根据日期，cpa/cpc, adid ,mac, udid 来获取30天的天表中是否存在此数据

## BaseProcessor ##
* checkUnique
	* 存储按天为期限，隔天为新的文件
	* key为adid，appid，mac，udid
	* 存在为0 不存在放入，并返回1
## isStop ##
各Processor**满足以下一种状态将isStop置为true**  

* ShowProcessor 
	* 如果**媒体**不是投放和测试状态，
	* 如果**广告**不是启动和调试状态，
* ClickProcessor 
	* 如果**媒体**不是渠道媒体不是投放和测试状态
	* **广告**
		* 如果广告是停止状态而且还是手动停止的
		* 如果广告是停止或者暂停或者留存状态的，并且此广告在此状态的持续时间大于1周的（当前时间和广告的更新时间）
		* 如果广告不是启动和调试状态，而且媒体不是渠道媒体
* JumpProcessor
	* 逻辑与ClickProcessor相同
* ActiceProcessor
	* 无
* JobProcessor
	* 无
## invalid ##
* ShowProcessor
	* checkUnique检查针对媒体而不是广告的独立请求数
	* 如果遍历广告的集合只要i>0则invalid为1
	* 存入内存等待统计
* ClickProcessor
	* 如果是isStop的状态，将invalid置为4
	* 针对应用媒体进行的判断
		1. 如果invalid此时为0，而且此媒体为应用媒体，
		2. 如果传入的media中ip所造的城市和时间同时符合，invalid为8
	* 和RedisStore集合起来的防作弊模式，这次主要起到的是设置作用
		1. 检查session是否作弊，收集did和udid的对应关系
		2. 收集udid，和ssid+localip的对应关系
		3. 收集udid和openudid的对应关系
		4. 增加磁盘点击记录
		5. 检查rload，并对invalid赋值
		6. 检查越狱扣量root 并对invalid赋值
		7. 媒体特殊处理，针对osver系统版本作弊屏蔽 
* JumpProcessor
	* 和ClickProcessor处理逻辑一致
* ActiveProcessor
	* ip假量
	* 时间假量
	* 同1个媒体应用id，同1个appuseid，同1个广告id，1天之内只能激活1次
	* 判断媒体是否启用防作弊模式
		1. 分别获取媒体和广告防作弊设置
		2. 针对上述获取的媒体和广告的设置进行之前结合RedisStore的作弊处理，赋值invalid字段（只要之前一条符合，其余各条便不执行，因为条件的修改导致invalid不为0）
		3. 超过24小时不返回数据（对于不是启动和停止状态的广告）invalid = 8
* JobProcessor
	* 如果action是Define.ACTION-JOB-NEXT，
	invalid = active_num - 1;
## RedisStore
1. 验证did是否去重
	* 验证规则（	第5位替换成5、第8位替换成8、第15位替换成7、第25位替换成4）
	* 验证是否重复 （广告+session）唯一性，按天提重 
	* 设计到的方法
	* redis中值得组成部分啦
## save ##
1. ShowProcessor
	1. 根据媒体和广告求出是否要进行**cpa随机扣量**
	2. 独立展次不为0，save为false，不是停止状态 将扣量设置为1
2. ClickProcessor
 	1. 媒体所有广告前10不扣量，渠道针对单一广告前50不扣量
 
	 