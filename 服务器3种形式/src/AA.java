import java.util.ArrayList;
import java.util.List;


public class AA {
	public static void main(String[] args) {
		AA a = new AA();
		for(int i=0;i<a.getSet().size();i++){
			List l = a.getSet();
			l.remove(i);
			System.out.println();
		}
	}
	public List<Integer> getSet(){
		ArrayList<Integer> set = new ArrayList<Integer>();
		set.add(1);
		set.add(2);
		return set;
	}
}
