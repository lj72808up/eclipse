package com;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestHello {

	private HelloWorld hw = new HelloWorld();
	@Before
	public void init(){
		System.out.println("测试开始");
	}
	
	@After
	public void tearDown(){
		System.out.println("方法测试结束");
	}
	
	@Test
	public void testHello(){
		Assert.assertEquals("hello方法测试失败", hw.sayHello(), "hello");
	}
	
	@Test
	public void testWorld(){
		Assert.assertEquals("world方法测试失败", hw.sayWorld(), "world2");
	}
	
	@Test(expected=Exception.class)
	public void testException() throws Exception{
		hw.except();
	}
}
