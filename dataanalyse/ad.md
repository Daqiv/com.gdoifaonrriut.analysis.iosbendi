##ads.is-hand-stop
是否手动停止：0 默认，1手动

##ads.state
状态 1新广告,2审核通过,，3拒绝，4启动 5 停止 6软删除，7调试，8暂停, 9 留存

##ads.jobs
* {
    * "income":"11.00",
    * "runtime":1,
    * "jobs":[
        * {
            * "const":"8.00",
            * "interval":"1",
            * "note":"搜索“奥迪”首次下载注册绑卡获奖"
        * },
        * {
            * "const":"0.15",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * },
        * {
            * "const":"0.1",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * },
        * {
            * "const":"0.1",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * },
        * {
            * "const":"0.1",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * },
        * {
            * "const":"0.1",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * },
        * {
            * "const":"0.1",
            * "interval":"1",
            * "note":"再次打开试玩3分钟获奖"
        * }
    * ]
* }

##ads.billing
计费方式
> 1click；2callback；3job

##ads.priceCallbackIncome
接入单价callback
>11

##ads.priceCallbackCost
投放单价callback，值为price_income_callback的0.7
>8

##ads.dataType
数据类型，积分墙1 免费墙2 插屏3 全屏4 广告条5  
>**1/2cpa 3/4/5cpc**

##ads.cid
广告主ID

##ads.uid
开发者ID

##ads.deliveryType
投放方式 ：0，快速     1，平滑 

##ads.isHsFlag
欢试投放标记1.独家 2.首发 3.100%返现

##ads.dataFrom
广告来源：1普通广告，2渠道广告

##ads.priceClickIncome
接入单价click

##ads.priceClickCost
投放单价click

## ads.process_name ##
进程名字
>HTFMobile  
>去哪儿旅行  
>携程旅行

## ads.isHsReport ##
运行列表是否加入报表（0：默认不计，1计入报表）

## ads.ipNum ##
ip数量

## ads.interval ##
时间间隔

## ads.option ##
广告物料JSON信息(插屏:image和click_url)；(积分墙:icon,title,text1,text2,download,store,callbackurl,callbacks,ids,psize)  

* {
    * "show_rates":0,
    * "title":"现金宝",
    * "icon":"upload/ads/20150531/1433039895980448551.jpg",
    * "psize":"10.3",
    * "text1":"最受欢迎的手机理财应用，全行业下载量NO.1！",
    * "text2":"搜索“奥迪”首次下载注册绑卡获奖",
    * "callbacks":"",
    * "store":"https://itunes.apple.com/cn/app/xian-jin-bao/id479739476?mt=8",
    * "download":"https://itunes.apple.com/cn/app/xian-jin-bao/id479739476?mt=8",
    * "ids":"",
    * "short_url":"",
    * "is_paid_ads":0,
    * "is_session":1,
    * "is_openudid":0,
    * "is_ssid":0,
    * "redownload":1,
    * "show_monitor_url":"",
    * "proportion":0,
    * "is_ig":0,
    * "is_disk":0,
    * "is_process_exception":0,
    * "process_num":"",
    * "float_money":0,
    * "repeat_url":"",
    * "report_idfa_url":"",
    * "report_idfa_key":""
* }
