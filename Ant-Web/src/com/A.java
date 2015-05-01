package com;

class A{
	
	private int count = 100;
	
	public synchronized void get() throws InterruptedException{
		if( count < 200){
			System.out.println(Thread.currentThread()+"进入了if");
			wait();
			System.out.println(Thread.currentThread()+"aaaaaa");
		}else{
			System.out.println(Thread.currentThread()+"进入else"+count);
			notifyAll();
		}
		System.out.println(Thread.currentThread()+"跳出了if ... else");
	}

	public void setCount(int count) {
		this.count = count;
	}
}