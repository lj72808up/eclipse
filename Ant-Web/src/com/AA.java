package com;

public class AA {

	public static void main(String[] args) throws InterruptedException {
		final A aa = new A();
		
		new Thread(){
			@Override
			public void run() {
				aa.setCount(200);
				try {
					aa.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		aa.get();
	}
}


