/**
 * 
 */
package finance.portfolio.biz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import finance.portfolio.dto.Company;
import finance.portfolio.helper.Scraper;

/**
 * @author David.Sequeira
 *
 */
public class CompanyData {
	
	private Connection con;
	
	public CompanyData(){
		try{  
			Class.forName("com.mysql.cj.jdbc.Driver");  
			this.con=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3306/portfolio","david_dev","R@ve1234");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String BASE_URL = "http://money.rediff.com/companies/";
		String url = BASE_URL+"All";
		Scraper s = new Scraper();
		
		Document pageDoc = s.getAsXml(url);
		CompanyData data = new CompanyData();
		
		int totalCompanies = getCompanyCount(pageDoc);
		int pageSetSize=199;
		int toCompany = 1+pageSetSize;
		
		/*for(int startCompany = 1;toCompany<totalCompanies;startCompany=toCompany+1){
			toCompany=startCompany+pageSetSize;
			String pageUrl = url+"/"+startCompany+"-"+toCompany;
			pageDoc = s.getAsXml(pageUrl);
			System.out.println(pageUrl);
			int count = data.getCompanies(pageDoc);
			toCompany=startCompany+pageSetSize;
		}*/
		
		//114 to 122
		for(int i=114;i<=122;i++){
			char letter = (char)i;
			String letterUrl = BASE_URL+letter;
			pageDoc = s.getAsXml(letterUrl);
			System.out.println(letterUrl);
			int count = data.getCompanies(pageDoc);
		}
	}
		
		
		
		
		

	private int getCompanies(Document pageDoc) {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		int companyCount=0;
		try {
			NodeList result = (NodeList) xPath.compile("//table[@class='dataTable']").evaluate(pageDoc,XPathConstants.NODESET);			
			System.out.println(result);
			if(result.getLength()>0){
				for(int i=0;i<result.getLength();i++){
					Element table = (Element)result.item(i);
					NodeList list =  (NodeList) xPath.compile("//tbody/tr").evaluate(table,XPathConstants.NODESET);
					System.out.println("No of Rows : "+list.getLength());
					
					if(list!=null && list.getLength()>0){
						for(int j=0;j<list.getLength();j++){
							Element node = (Element)list.item(j);//div
							//////System.out.println(node.getAttribute("data-bt"));
							Company company = getCompanyInfo(xPath,node);
							if(company!=null){
								addCompany(company);
								companyCount++;
							}
						}	
					}	
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	private void addCompany(Company c) {
		
		try{  
			Statement stmt=con.createStatement();  
			ResultSet rs=stmt.executeQuery("select CODE from companies where CODE="+c.getCode());  
			if(!rs.next()){
				rs.close();
				String cName = c.getName().replaceAll("'", "''");
				String q = "insert into companies VALUES("+c.getCode()+",'"+cName+"','"+c.getGroup()+"','"+c.getRediffCode()+"');";
				System.out.println(q);
				stmt.execute(q);
				System.out.println(c);
			}
			stmt.close();
			//System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));  
			//con.close();  
			}catch(Exception e){ System.out.println(e);}  
			}
		
	

	private Company getCompanyInfo(XPath xPath, Element node) {
		Company c = new Company();
		try {
			String temp = (String) xPath.compile("self::node()//td[position()=1]/a").evaluate(node,XPathConstants.STRING);
			c.setName(temp);
			temp = (String) xPath.compile("self::node()//td[position()=1]/a/@href").evaluate(node,XPathConstants.STRING);
			temp = temp.substring(temp.lastIndexOf('/')+1);
			c.setRediffCode(temp);
			temp = (String) xPath.compile("self::node()//td[position()=2]").evaluate(node,XPathConstants.STRING);
			c.setCode(Integer.parseInt(temp));
			temp = (String) xPath.compile("self::node()//td[position()=3]").evaluate(node,XPathConstants.STRING);
			c.setGroup(temp);
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}

	private static int getCompanyCount(Document firstPageDoc) {
		int totalPages = 0;
		XPath xPath =  XPathFactory.newInstance().newXPath();
		try {
			Element result = (Element) xPath.compile("//table[@class='pagination-container-company']").evaluate(firstPageDoc,XPathConstants.NODE);			
			System.out.println(result);
			
			String pageInfoText =  (String) xPath.compile("self::node()//tr/td").evaluate(result,XPathConstants.STRING);
			System.out.println("No of Rows : "+pageInfoText);
			
			pageInfoText = pageInfoText.substring(pageInfoText.lastIndexOf("of ")+3,pageInfoText.lastIndexOf("<")).trim();
			
			totalPages = Integer.parseInt(pageInfoText);
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totalPages;
	}

	public Map<String, String> getRediffCodes() {
		String selectSQL = "SELECT distinct CODE, REDIFF_CODE FROM companies";
		Map<String, String> result = new HashMap<String,String>();
		try {
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery(selectSQL );
			while (rs.next()) {
				String codeStr = rs.getString("CODE");
				String rediffCode = rs.getString("REDIFF_CODE");
				result.put(codeStr, rediffCode);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}  

	}

