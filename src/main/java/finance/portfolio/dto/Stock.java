/**
 * 
 */
package finance.portfolio.dto;

import java.math.BigDecimal;

/**
 * @author David.Sequeira
 *
 */
public class Stock {
	
	private String name,group;
	
	private String ID;
	private String exchange;
	private BigDecimal prevClose,currentPrice,change;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public BigDecimal getPrevClose() {
		return prevClose;
	}
	public void setPrevClose(BigDecimal prevClose) {
		this.prevClose = prevClose;
	}
	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}
	public BigDecimal getChange() {
		return change;
	}
	public void setChange(BigDecimal change) {
		this.change = change;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	@Override
	public String toString() {
		return "Stock [name=" + name + ", group=" + group + ", ID=" + ID + ", exchange=" + exchange + ", prevClose="
				+ prevClose + ", currentPrice=" + currentPrice + ", change=" + change + "]";
	}

}
