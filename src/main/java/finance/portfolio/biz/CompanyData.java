/**
 * 
 */
package finance.portfolio.biz;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import finance.portfolio.dto.Company;
import finance.portfolio.helper.Scraper;

/**
 * @author David.Sequeira
 *
 */
public class CompanyData {
	
	static final org.apache.log4j.Logger log = Logger.getLogger(CompanyData.class.getName());
	
	private Connection con;
	
	public CompanyData(){
		try{  
			Class.forName("com.mysql.cj.jdbc.Driver");  
			this.con=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3306/portfolio","root","Cre@t1ve@2024");
			//"jdbc:mysql://localhost:61936/portfolio","adminb5ZEeCP","rIWShGJ8Y_F1");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws TikaException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TikaException {
		
		String BASE_URL = "https://money.rediff.com/companies/";
		String url = BASE_URL+"All";
		Scraper s = new Scraper();
		
		Document pageDoc_old = s.getAsXml(url);
		CompanyData data = new CompanyData();
		
		int totalCompanies = getCompanyCount(pageDoc_old);
		System.out.println("number of companies:"+totalCompanies);
		
		//int totalCompanies = getCompanyCount(pageDoc);
		int pageSetSize=199;
		int toCompany = 1+pageSetSize;
		
		for(int startCompany = 1;toCompany<totalCompanies;startCompany=toCompany+1){
			toCompany=startCompany+pageSetSize;
			String pageUrl = url+"/"+startCompany+"-"+toCompany;
			System.out.println(pageUrl);
			log.debug(pageUrl);
			pageDoc_old = s.getAsXml(pageUrl);			
			int count = data.getCompanies(pageDoc_old);
			toCompany=startCompany+pageSetSize;
		}
		
		//114 to 122 - p - z
		/*for(int i=97;i<=122;i++){
			char letter = (char)i;
			String letterUrl = BASE_URL+letter;
			String htmlStr = s.parseToHTML(letterUrl);
			System.out.println(htmlStr);
			Document pageDoc = s.getProcessedXMLDocument(htmlStr);
			
			NodeList nl = pageDoc.getDocumentElement().getChildNodes();
			
			
			System.out.println(nl.getLength());
			System.out.println(nl.item(3).getNodeName());
			System.out.println();
			int totalCompanies = getCompanyCount(pageDoc);
			int pageSetSize=199;
			int toCompany = 1+pageSetSize;
			
			for(int startCompany = 1;toCompany<totalCompanies;startCompany=toCompany+1){
				toCompany=startCompany+pageSetSize;
				String pageUrl = letterUrl+"/"+startCompany+"-"+toCompany;
				log.info(pageUrl);
				pageDoc = s.getAsXml(pageUrl);			
				int count = data.getCompanies(pageDoc);
				toCompany=startCompany+pageSetSize;
			}
			//System.out.println(letterUrl);
			//int count = data.getCompanies(pageDoc);
		}*/
	}
		
		
		
		
		

	private int getCompanies(Document pageDoc) {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		int companyCount=0;
		try {
			NodeList result = (NodeList) xPath.compile("//table[@class='dataTable']").evaluate(pageDoc,XPathConstants.NODESET);			
			////System.out.println(result);
			if(result.getLength()>0){
				for(int i=0;i<result.getLength();i++){
					Element table = (Element)result.item(i);
					NodeList list =  (NodeList) xPath.compile("//tbody/tr").evaluate(table,XPathConstants.NODESET);
					//System.out.println("No of Rows : "+list.getLength());
					
					if(list!=null && list.getLength()>0){
						for(int j=0;j<list.getLength();j++){
							Element node = (Element)list.item(j);//div
							//System.out.println(node.getAttribute("data-bt"));
							Company company = getCompanyInfo(xPath,node);
							log.debug(company.getName());
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

		return companyCount;
	}

	private void addCompany(Company c) {
		
		try{  
			Statement stmt=con.createStatement();  
			ResultSet rs=stmt.executeQuery("select CODE from companies where CODE="+c.getCode());  
			if(!rs.next()){
				rs.close();
				String cName = c.getName().replaceAll("'", "''");
				String q = "insert into companies VALUES("+c.getCode()+",'"+cName+"','"+c.getGroup()+"','"+c.getRediffCode()+"');";
				//System.out.println(q);
				stmt.execute(q);
				//System.out.println(c);
			}
			else{
				log.info(c.getCode()+":"+c.getName()+" Exists");
			}
			stmt.close();
			////System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));  
			//con.close();  
			}catch(Exception e){ 
				System.out.println(e);
				}  
			
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
			log.info("No of Rows : "+pageInfoText);
			if(pageInfoText.lastIndexOf("<")!=-1){
				pageInfoText = pageInfoText.substring(pageInfoText.lastIndexOf("of ")+3,pageInfoText.lastIndexOf("<")).trim();
			}
			else{
				pageInfoText = pageInfoText.substring(pageInfoText.lastIndexOf("of ")+3,pageInfoText.length()).trim();
			}
			
			
			totalPages = Integer.parseInt(pageInfoText);
			//System.out.println("No of Pages : "+totalPages);
			
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

