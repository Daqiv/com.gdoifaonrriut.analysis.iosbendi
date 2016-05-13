package test;

import java.util.Date;

import com.dianru.analysis.tools.DBCreateTable;

public class Test {
	/**
	 * 测试获取时间的写法
	 */
	public static void test1(){
		System.out.println(String.valueOf(new Date().getTime() / 1000));
	}
	
	/**
	 * 按2进制算出位移
	 */
	public static void test2(){
		int sshift = 0;
        int ssize = 1;
        int i = 0;
        while (ssize < 16) {
        	i++;
            ++sshift;
            System.out.println("sshift "+i+" time:"+ sshift);
            ssize <<= 1;
            System.out.println("ssize "+i+" time:"+ ssize);
        }
        System.out.println("sshift final:" + sshift);
	} 
	
	
	/**
	 * 测试获取hashcode的算法
	 */
	public static void test3(){
		System.out.println(new DBCreateTable().hashCode());
	}
	
	
	/**
	 * 测试非运算 正数非为负数绝对值加1 负数非为正数绝对值减一
	 */
	public static void test4(){
		int a=-2;
		System.out.println("a 非的结果是："+(~a));
	}
	
	/**
	 * 测试异或运算 做差运算
	 */
	public static void test5(){
		int a=15;
		int b=3;
		System.out.println("a 与 b 异或的结果是："+(a^b));
	}
	
	/**
	 * 测试与运算 测试结果或为0或为1
	 */
	public static void test6(){
		int a=129;
		int b=124;
		System.out.println("a 和b 与的结果是："+(a&b));
	}
	
	/**
	 * 测试或运算 无规律
	 */
	public static void test7(){
		int a=129;
		int b=122;
		System.out.println("a 和b 或的结果是："+(a|b));
	}
	public static void main(String[] args) {
//		test1();
//		test2();
//		test3();
//		test4();
//		test5();
//		test6();
//		test7();
	}
}
