package test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;









import beans.Person;

public class TestPerson {

	@Test
	public void muti2one(){
		String config = "mybatis/mybatis-config.xml";
		InputStream is = new TestPerson().getClass().getClassLoader().getResourceAsStream(config);
		SqlSessionFactory sfFactory = new SqlSessionFactoryBuilder().build(is);
		SqlSession session = sfFactory.openSession();
		
		List l = session.selectList("com.PersonMapper.selectPS","lisi");
		System.out.println(l);
	}
	
	
	@Test
	public void testMuti(){
		String config = "mybatis/mybatis-config.xml";
		InputStream is = new TestPerson().getClass().getClassLoader().getResourceAsStream(config);
		SqlSessionFactory sfFactory = new SqlSessionFactoryBuilder().build(is);
		SqlSession session = sfFactory.openSession();
		
		Map map = new HashMap();
		map.put("name", "zhangsan");
		List l = session.selectList("com.PersonMapper.selectPO",map );
		System.out.println(l);
	}
	
	@Test
	public void fun1() throws InterruptedException{
		String config = "mybatis/mybatis-config.xml";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(config);
		SqlSessionFactory sfFactory = new SqlSessionFactoryBuilder().build(is);
		SqlSession session = sfFactory.openSession();
		
		//List<Map<String,Integer>> result = session.selectList("com.PersonMapper.selectPerson", 1);// [{age=22, name=lisi, pid=1}]
		//Map<String,Integer> result = session.selectOne("com.PersonMapper.selectPerson", 1);
		//List result = session.selectList("com.PersonMapper.selectAllColumn", "1"); //[Person [name=lisi, age=22]]
		int[] arr = {1,2};
		List pl = session.selectList("com.PersonMapper.selectIds",arr);
		System.out.println(pl);  // {age=22, name=lisi, pid=1}
		
		
	}
	
	@Test
	public void fun2(){
		String config = "mybatis/mybatis-config.xml";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(config);
		SqlSessionFactory sfFactory = new SqlSessionFactoryBuilder().build(is);
		SqlSession session = sfFactory.openSession();
		
		Person p = new Person("zhaoliu",24);
		session.insert("com.PersonMapper.addPerson", p);
		session.commit();  //dml语句手动提交事务
	}

	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		//jdbc如果不开启显示控制事务,自动提交事务
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.198.63:1521:ai4ashow","miniaudita","miniaudita");
		conn.setAutoCommit(false);
		
		Statement stmt = conn.createStatement();
		Savepoint sp = conn.setSavepoint("aa");  //设置事务保存点
		stmt.execute("insert into aa values('999')");
		conn.rollback(sp);  //事务回滚
		stmt.execute("insert into aa values('111')");
		conn.commit();      //提交事务
	}
}
