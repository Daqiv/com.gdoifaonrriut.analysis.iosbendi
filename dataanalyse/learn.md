##  定时器写法
* 1.start 先执行一次任务，然后定时任务
* 2.定时任务继承自timerTask，实现方法即可
 
## log4j查找错误  
再打印错误日志的地方把必要的日志进行传入
	
## 信号量使用
* 只限于linux平台
*只限于USR2信号量
* 使用kill -s SIGUSR2 pid
 
 >
* public static class ShutdownSignal implements SignalHandler {  
	@Override  
	* public void handle(Signal sign) {  
		* SERVER_LOOP = false;  
		* LOG.info("server recv signal : " + sign.getName());  
	* }   
* }  
* Signal sign = new Signal("USR2");  
* Signal.handle(sign, new ShutdownSignal()); 
 

## 线程池的停止  
 
* if(executorService != null) {  
	* executorService.shutdown();  
	* try {  
		* executorService.awaitTermination(10, TimeUnit.SECONDS);  
	* } catch (InterruptedException e) {   
		* LOG.error("wait executor service exception : " + e.toString());  
	* }  
* }  
awaitTermination 等待线程池中的线程执行完毕，如果在指定时间内没有执行完毕，那么返回false
否则返回ture

shutdown 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。		

## 线程join的方法
 join() 方法主要是让调用改方法的thread完成run方法里面的东西后， 在执行join()方法后面的代码

## 文件的创建
1. 先判断目录文件是否存在，如果不存在创建目录文件
2.	创建所需要的文件
3. 针对这个结构可以创建一个稳定的目录标量，创建什么文件直接传入即可

## redis删除的应用
1. 获取redis成功删除后的用量，假如不为0的话，那么说明删除成功，如果删除成功的话可以结合下一步的操作
2. 如果不为0为1或者2的话，那么是不是要考虑一下这种情况呢，如果这种情况发生那么可是和平台丢量有大大的关系了

##assert java关键字
主要在开发和测试时开启  
1. assert exp1 此时的exp1为一个boolean类型的表达式
2. assert exp1 : exp2 此时的exp1同上，而exp2可以为基本类型或一个Object
**point：**在测试方法中assert报错后，其后的代码不执行

##遍历map
1. keySet
2. values
3. entrySet  

针对取出的值可采用for循环进行便利  

* obj：Collection
* iterator

##修改map中的对象
在map中object的值修改之后，在map中存放的值已经改变，其实可以这么理解，map只是容器，存放对象的地址，我们把对改掉后，从map中查出来的值也就改变了





