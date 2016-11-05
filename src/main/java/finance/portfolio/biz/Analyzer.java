/**
 * 
 */
package finance.portfolio.biz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import finance.portfolio.common.Group;
import finance.portfolio.dto.Stock;
import finance.portfolio.helper.Scraper;

/**
 * @author David.Sequeira
 *
 */
public class Analyzer {
	
	private static final String BASE_DIR = "C:/Users/david.sequeira/Documents/Trade/";

	private static final int START_POINT_COL_INDEX = 9;
	
	private static final SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("dd MMM yyyy");//03 May 2016
	private static final SimpleDateFormat DATE_FORMAT_OUT = new SimpleDateFormat("MM/dd/yyyy");//10/19/2016
	

	//Create blank workbook
    XSSFWorkbook workbook;
    String filename;
    
    String[] period = {"daily","weekly","monthly"};
    String[] data = {"gainers","losers"};
    String[] group = {"a","b"};
    
    Map<String,Map<String,String>> scripsWithValues = new HashMap<String,Map<String,String>>();
    
    
    
    public Analyzer(String fileName){
    	filename = fileName;
    	try {
    		if(fileName!=null){
    			workbook = new XSSFWorkbook(fileName);
    		}
    		else{
    			workbook = new XSSFWorkbook();
    		}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
    }
	
	public static void main(String args[]){
		//Analyzer a = new Analyzer(BASE_DIR+"20161011_12.xlsx");
		Analyzer a = new Analyzer(null);
		
		a.gather();
		a.merge("MG_B","WG_B");
		a.merge("ML_B","WG_B");
		a.merge("MG_A","WG_A");
		a.merge("ML_A","WG_A");
		
		
		//List<Stock> dailyGainersA  = a.getDailyGainers(Group.A);
		//List<Stock> weeklyGainersA  = a.getWeeklyGainers(Group.A);
		//List<Stock> monthlyGainersA  = a.getMonthlyGainers(Group.A);
		//List<Stock> dailyLosersA  = a.getDailyLosers(Group.A);
		//List<Stock> weeklyLosersA  = a.getWeeklyLosers(Group.A);
		//List<Stock> monthlyLosersA  = a.getMonthlyLosers(Group.A);
		a.updatePortfolio();
		
	}
	
	/*
	 * Updates scrip values into the trade.xlsx excel sheet
	 */
	private void updatePortfolio() {
		String fileName = BASE_DIR+"Equity.xlsx";
		try {
			InputStream inp = new FileInputStream(fileName);
			Workbook trade =  WorkbookFactory.create(inp);
			Sheet portfolio = trade.getSheet("portfolio");
			List<String> scripCodes = getScrips(portfolio);
			updateSheetwithData(portfolio,scripCodes);
			FileOutputStream fileOut = new FileOutputStream(fileName);
			trade.write(fileOut);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void updateSheetwithData(Sheet portfolio, List<String> scripCodes) {
		CompanyData data = new CompanyData();
		Map<String,String> rediffCodes = data.getRediffCodes();
		System.out.println(rediffCodes);
		for (Iterator<String> iterator = scripCodes.iterator(); iterator.hasNext();) {
			String scripCode = iterator.next();
			String string = rediffCodes.get(scripCode);
			if(string!=null){
				String url = "http://money.rediff.com/money/jsp/chart_6month_new1.jsp?companyCode="+string+"&all=1";
				System.out.println(url);
				Scraper s = new Scraper();
				
				//Document doc = s.getAsXml(url);
				Document doc = null;
				try {
					doc = s.getProcessedXMLDocument(s.getUrlContents(url));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(doc.getChildNodes().getLength());
				Map<String,String> values = getScripMap(doc);
				scripsWithValues.put(string, values);
				
				int lastColNum =  portfolio.getRow(0).getLastCellNum();
				Row r = getRow(portfolio, scripCode);
				for(int i=START_POINT_COL_INDEX;i<lastColNum;i++){
					String key = DATE_FORMAT_OUT.format(portfolio.getRow(0).getCell(i).getDateCellValue());
					
					String price = values.get(key);
					if(price!=null){
						System.out.println("Key: "+key+ " Price: "+price);
						Cell c = r.createCell(i, CellType.NUMERIC);
						c.setCellValue(Double.parseDouble(price));
					}
					
				}
				
			}
			System.out.println(scripsWithValues);
		}
		
		updateSheet(portfolio);
	}

	private void updateSheet(Sheet portfolio) {
		// TODO Auto-generated method stub
		
	}

	private Map<String, String> getScripMap(Document document) {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Map<String,String> values = new HashMap<String,String>();
		try {
			
			System.out.println(document.getChildNodes().item(0).getNodeName());
			NodeList list =  (NodeList) xPath.compile("/graph/set").evaluate(document,XPathConstants.NODESET);
			System.out.println("No of Rows : "+list.getLength());
			
			if(list!=null && list.getLength()>0){
				for(int i=0;i<list.getLength();i++){
					Element node = (Element)list.item(i);//div
					//////System.out.println(node.getAttribute("data-bt"));
					
					try {
						Date d = DATE_FORMAT_IN.parse(node.getAttribute("name"));
						String dateKey = DATE_FORMAT_OUT.format(d);
						//System.out.println(dateKey);
						values.put(dateKey, node.getAttribute("value"));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}	
			}	
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return values;
	}

	private List<String> getScrips(Sheet portfolio) {
		List<String> scrips = null;
		int lastRow = portfolio.getLastRowNum();
		
		System.out.println(lastRow);
		if(lastRow>0){
			scrips = new ArrayList<String>(); 
			for (int i = 1; i <= lastRow; i++) {
				Row r = portfolio.getRow(i);
				int name = (int) r.getCell(0).getNumericCellValue();
				System.out.println(name);
				scrips.add(name+"");
			}
			System.out.println(scrips.size());
		}
		return scrips;
	}
	
	private Row getRow(Sheet portfolio,String scripCode) {
		List<String> scrips = null;
		int lastRow = portfolio.getLastRowNum();
		int i = 0;
		Row r = null;
		System.out.println(lastRow);
		if(lastRow>0){
			scrips = new ArrayList<String>(); 
			for (i = 1; i <= lastRow; i++) {
				r = portfolio.getRow(i);
				String name = ""+(int)r.getCell(0).getNumericCellValue();
				System.out.println("param:"+scripCode+" cellVal:"+name);
				if(name.equals(scripCode)){
					break;
				}
			}
			System.out.println("RowNum"+r.getCell(0).getNumericCellValue());
		}
		return r;
	}

	private Sheet merge(String string, String string2) {
		Sheet sheet1 = workbook.getSheet(string);
		Map<String,Stock> list1 = new TreeMap<String,Stock>();
		
		populateListFromSheet(sheet1, list1);
		
		Sheet sheet2 = workbook.getSheet(string2);
		Map<String,Stock> list2 = new TreeMap<String,Stock>();
		
		populateListFromSheet(sheet2, list2);
		
		 XSSFSheet spreadsheet = workbook.createSheet( 
	    		  string+"_"+string2);
	      //Create row object
	      XSSFRow row;
	      
	      int rowid = 0;
		for (Iterator<String> iterator = list1.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			if(list2.containsKey(name)){
				row = spreadsheet.createRow(rowid++);
		         if(rowid==0){
		        	 addMergeHeader(row);
		         }			
				
				 row.createCell(0).setCellValue(name);
		         row.createCell(1).setCellValue(list1.get(name).getChange()+"");
		         row.createCell(2).setCellValue(list2.get(name).getChange()+"");
		         row.createCell(3).setCellValue(list2.get(name).getCurrentPrice().toString());
		         
			}
			
		}
		
		writeWorkbookToFile();
		return sheet1;
		
	}

	private void addMergeHeader(XSSFRow row) {
		row.createCell(0).setCellValue("Name");
		row.createCell(1).setCellValue("Monnthly Change %");
		row.createCell(2).setCellValue("Weekly Change %");
		row.createCell(2).setCellValue("Current Price");
		
	}

	private void populateListFromSheet(Sheet sheet1, Map<String, Stock> list1) {
		int lastRow = sheet1.getLastRowNum();
		
		System.out.println(lastRow);
		if(lastRow>0){
			for (int i = 0; i < lastRow; i++) {
				Row r = sheet1.getRow(i);
				String name = r.getCell(0).getStringCellValue();
				Stock s = new Stock();
				s.setName(r.getCell(0).getStringCellValue());
				s.setPrevClose(new BigDecimal(r.getCell(2).getStringCellValue()));
				s.setCurrentPrice(new BigDecimal(r.getCell(3).getStringCellValue()));
				s.setChange(new BigDecimal(r.getCell(4).getStringCellValue()));
				list1.put(name, s);
			}
			System.out.println(list1.size());
		}
	}

	private void gather() {
		
		for(String g:group){
			for(String p:period){
				for(String d:data){
					List<Stock> stocks = new ArrayList<Stock>();
					String url = "http://money.rediff.com/"+d+"/bse/"+p+"/group"+g;
					System.out.println(url);
					getListing(stocks, url);
					createWorkSheet(stocks,getSheetName(g,p,d));
					//return stocks;
				}
			}
		}
		 //Write the workbook in file system
	      
	      String fileName = BASE_DIR+getFileName()+".xlsx";
	      this.filename = fileName;
	      writeWorkbookToFile();
		
	}

	private String getSheetName(String g, String p, String d) {
		StringBuffer sheetName = new StringBuffer();
		sheetName.append(p.substring(0,1).toUpperCase())
		.append(d.substring(0,1).toUpperCase())
		.append("_")
		.append(g.toUpperCase());
		return sheetName.toString();
				
	}

	private void createWorkSheet(List<Stock> dailyGainersA,String sheetName) {
		
	      //Create a blank sheet
	      XSSFSheet spreadsheet = workbook.createSheet( 
	    		  sheetName);
	      //Create row object
	      XSSFRow row;
	      
	      int rowid = 0;
	      
	      for (Stock stock : dailyGainersA)
	      {
	         row = spreadsheet.createRow(rowid++);
	         if(rowid==0){
	        	 addHeader(row);
	         }
	         row.createCell(0).setCellValue(stock.getName());
	         row.createCell(1).setCellValue(stock.getGroup());
	         row.createCell(2).setCellValue(stock.getPrevClose().toString());
	         row.createCell(3).setCellValue(stock.getCurrentPrice().toString());
	         row.createCell(4).setCellValue(stock.getChange().toString());
	      }
	}

	private void writeWorkbookToFile() {
		FileOutputStream out = null;
		try {			
			out = new FileOutputStream( 
			  new File(this.filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      try {
			workbook.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      System.out.println(this.filename+  
	      ".xlsx written successfully" );
	}

	private static String getFileName() {
		GregorianCalendar c = new GregorianCalendar();
		int m = c.get(GregorianCalendar.MONTH) + 1;
		 int d = c.get(GregorianCalendar.DATE);
		 String mm = Integer.toString(m);
		 String dd = Integer.toString(d);
		 return "" + c.get(GregorianCalendar.YEAR) + (m < 10 ? "0" + mm : mm) +
		     (d < 10 ? "0" + dd : dd)+"_"+c.get(GregorianCalendar.HOUR_OF_DAY);
	}

	private static void addHeader(XSSFRow row) {
		Cell cell = row.createCell(0);
        cell.setCellValue("Company");
        row.createCell(1).setCellValue("Group");
        row.createCell(2).setCellValue("Previous Close");
        row.createCell(3).setCellValue("Current Price");
        row.createCell(4).setCellValue("Change %age");
		
	}

	public List<Stock> getDailyGainers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/daily/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"DG_A");
		return stocks;
		
	}
	
	public List<Stock> getWeeklyGainers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/weekly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"WG_A");
		return stocks;
		
	}
	
	public List<Stock> getMonthlyGainers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/monthly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"MG_A");
		return stocks;
		
	}
	
	public List<Stock> getDailyLosers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/daily/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"DL_A");
		return stocks;
		
	}
	
	public List<Stock> getWeeklyLosers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/weekly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"WL_A");
		return stocks;
		
	}
	
	public List<Stock> getMonthlyLosers(Group g){
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/monthly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"ML_A");
		return stocks;
		
	}

	private void getListing(List<Stock> stocks, String url) {
		Scraper s = new Scraper();
		Document document = s.getAsXml(url);		
		
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		try {
			Element result = (Element) xPath.compile("//table[@class='dataTable']").evaluate(document,XPathConstants.NODE);			
			//System.out.println(result);
			
			NodeList list =  (NodeList) xPath.compile("//tbody/tr").evaluate(document,XPathConstants.NODESET);
			System.out.println("No of Rows : "+list.getLength());
			
			if(list!=null && list.getLength()>0){
				for(int i=0;i<list.getLength();i++){
					Element node = (Element)list.item(i);//div
					//////System.out.println(node.getAttribute("data-bt"));
					Stock stock = getStockInfo(xPath,node);
					if(stock!=null){
						stocks.add(stock);
					}
				}	
			}	
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	private Stock getStockInfo(XPath xPath, Element node) {
		Stock s = new Stock();
		
		try {
			String temp = (String) xPath.compile("self::node()//td[position()=1]/a").evaluate(node,XPathConstants.STRING);
			s.setName(temp);
			temp = (String) xPath.compile("self::node()//td[position()=1]/a/@href").evaluate(node,XPathConstants.STRING);
			//temp = temp.substring(temp.lastIndexOf('/')+1);
			s.setID(temp);
			temp = (String) xPath.compile("self::node()//td[position()=2]").evaluate(node,XPathConstants.STRING);
			s.setGroup(temp);
			temp = (String) xPath.compile("self::node()//td[position()=3]").evaluate(node,XPathConstants.STRING);
			temp = temp.replaceAll(",", "");
			s.setPrevClose(new BigDecimal(temp));
			temp = (String) xPath.compile("self::node()//td[position()=4]").evaluate(node,XPathConstants.STRING);
			temp = temp.replaceAll(",", "");
			s.setCurrentPrice(new BigDecimal(temp));
			BigDecimal change = s.getCurrentPrice().subtract(s.getPrevClose());
			
			BigDecimal percentChange = change.divide(s.getPrevClose(),5,RoundingMode.HALF_EVEN).multiply(new BigDecimal(100));
			s.setChange(percentChange);
			//System.out.println(s);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}
