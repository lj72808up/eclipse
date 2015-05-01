package beans;

public class Order {

	private String oid;
	private String oname;
	
	public Order() {
		super();
	}

	public Order(String oid, String oname) {
		super();
		this.oid = oid;
		this.oname = oname;
	}
	
	
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getOname() {
		return oname;
	}
	public void setOname(String oname) {
		this.oname = oname;
	}

	@Override
	public String toString() {
		return "Order [oid=" + oid + ", oname=" + oname + "]";
	}
	
	
}
