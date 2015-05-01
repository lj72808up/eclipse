package com;

public class HelloWorld {

	public static void main(String[] args) {
		for(String arg:args){
			System.out.println("hello "+arg);
		}
	}
	
	public String sayHello(){
		return "hello";
	}
	
	public String sayWorld(){
		return "world";
	}
	
	public void except() throws Exception{
		throw new Exception("出粗啦");
	}
}
