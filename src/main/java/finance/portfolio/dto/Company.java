/**
 * 
 */
package finance.portfolio.dto;

/**
 * @author David.Sequeira
 *
 */
public class Company {
	
	private int code;
	private String name;
	private String group;
	private String rediffCode;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getRediffCode() {
		return rediffCode;
	}
	public void setRediffCode(String rediffCode) {
		this.rediffCode = rediffCode;
	}
	@Override
	public String toString() {
		return "Company [code=" + code + ", name=" + name + ", group=" + group + ", rediffCode=" + rediffCode + "]";
	}

}
