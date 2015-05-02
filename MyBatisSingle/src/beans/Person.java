package beans;

import java.util.List;

public class Person {

	private String name;
	private int age;
	private List<Order> olist;
	private School s;
	
	public Person() {   // 必须有空残构造,否则result无法映射
		super();
	}

	
	public School getS() {
		return s;
	}

	public void setS(School s) {
		this.s = s;
	}

	public List<Order> getOlist() {
		return olist;
	}

	public void setOlist(List<Order> olist) {
		this.olist = olist;
	}


	public Person(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", age=" + age + ", olist=" + olist
				+ ", s=" + s + "]";
	}
}
