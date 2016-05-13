##数据类型
1. ads.state-->状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停
2. ads.num-->active_num-->投放数量
3. ads.click_num-->click_num-->点击投放数量
4. ads.type-->adtype-->广告类型：积分墙1 免费墙2 插屏3 全屏4 广告条5
5. ads.datatype-->数据类型，1积分墙，2推荐墙
6. ads.is_hs_flag-->欢试投放标记 1.独家 2.首发 3.100%返现
7. ads.deliveryType-->投放方式 ：0，快速 1，平滑
8. ads.is_hs_report-->运行列表是否加入报表（0：默认不计，1计入报表）
##变量及常量
1.  QUERY_STRING-->从指定table中查出关于（点击，独立点击（经过过滤），激活点击）的总数针对于广告id


## 流程和步骤
1. 从_ad_day_year中查出指定的数据总和针对广告id
2. 如果不是启动状态的广告，把其将**DATA_ACTIVE_REMAIN**中的剩余值重置为0，已经激活的余量保持不变，continue
3. 在如下情况中，将**DATA_ACTIVE_REMAIN**余量设置为0，激活设置为满，将广告设置为停止状态，continue
	* 激活数目超过投放数目
	* 过滤点击超过点击投放数目 
4. 计算存入缓冲**DATA_ACTIVE_REMAIN**中的值
	* 计算剩余量（如果active_num为0--》设置为1000000）
	* 计算激活数（如果独立点击超过点击投放--》将remain设置为0）
5. 结合具体类型将对应的值存入缓存**DATA_ACTIVE_REMAIN**中
	