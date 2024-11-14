/**
 * 
 */
package finance.portfolio.biz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFFontFormatting;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.exception.TikaException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import finance.portfolio.common.Group;
import finance.portfolio.dto.Company;
import finance.portfolio.dto.Stock;
import finance.portfolio.helper.PortfolioSpreadSheet;
import finance.portfolio.helper.Scraper;

/**
 * @author David.Sequeira
 *
 */
public class Analyzer {
	
	private static final String BASE_DIR = "C:Users/David/Documents/Trade/";
	
	private static final int START_POINT_COL_INDEX = 9;
	
	private SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("dd MMM yyyy");//03 May 2016
	private SimpleDateFormat DATE_FORMAT_OUT = new SimpleDateFormat("MM/dd/yyyy");//10/19/2016

	private static final int MARKET_PRICE_COLUMN = 5;

	private static final boolean GAINERS_LOSERS = true;
	
	static final org.apache.log4j.Logger log = Logger.getLogger(Analyzer.class.getName());
	
	private Connection con;
	
	//Create blank workbook
    XSSFWorkbook workbook;
    String filename;
    
    String[] period = {"daily","weekly","monthly"};
    String[] data = {"gainers","losers"};
    String[] group = {"a","b"};
    
    Map<String,Map<String,String>> scripsWithValues = new HashMap<String,Map<String,String>>();
    
    
    
    public Analyzer(String fileName){
    	DATE_FORMAT_IN.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
    	DATE_FORMAT_OUT.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
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
    	try{  
			Class.forName("com.mysql.cj.jdbc.Driver");  
			this.con=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3306/portfolio","root","root");
			//"jdbc:mysql://localhost:61936/portfolio","adminb5ZEeCP","rIWShGJ8Y_F1");
		}
		catch(Exception e){
			System.out.println("Could not get connection to db");
			e.printStackTrace();
		}
    }
	
	public static void main(String args[]) throws Exception{
		//Analyzer a = new Analyzer(BASE_DIR+"20161011_12.xlsx");
		Analyzer a = new Analyzer(null);
		
		if(GAINERS_LOSERS){
			a.gather();			
			Map<String,List<Stock>> dgwg_a = a.merge("DG_A","WG_A");
			a.merge(dgwg_a, "ML_A");
			a.merge("DG_B","WG_B");
			a.merge("MG_B","WG_B");
			a.merge("ML_B","WG_B");
			a.merge("ML_B","WL_B");
			a.merge("WG_A","DL_A");
			a.merge("MG_A","WG_A");
			a.merge("MG_A","WL_A");
			a.merge("ML_A","WG_A");
			a.merge("ML_A","WL_A");
		}
		
		
		
		
		//List<Stock> dailyGainersA  = a.getDailyGainers(Group.A);
		//List<Stock> weeklyGainersA  = a.getWeeklyGainers(Group.A);
		//List<Stock> monthlyGainersA  = a.getMonthlyGainers(Group.A);
		//List<Stock> dailyLosersA  = a.getDailyLosers(Group.A);
		//List<Stock> weeklyLosersA  = a.getWeeklyLosers(Group.A);
		//List<Stock> monthlyLosersA  = a.getMonthlyLosers(Group.A);
		//a.updatePortfolio();
		
	}
	
	

	/*
	 * Updates scrip values into the trade.xlsx excel sheet
	 */
	private void updatePortfolio() throws XPathExpressionException {
		System.out.println("UpdatePortfolio");
		String fileNamePart = BASE_DIR+"Equity";
		String fileExtn = "xlsx";
		String fileName=fileNamePart+"_"+getMonth()+"."+fileExtn;
		String BASE_COMPANIES_URL = "https://money.rediff.com/companies/";
		Scraper s = new Scraper();
		try {
			InputStream inp = new FileInputStream(fileName);
			//POIFSFileSystem fs = new POIFSFileSystem(inp); 
			
			XSSFWorkbook trade =  (XSSFWorkbook) WorkbookFactory.create(inp);
			XSSFSheet portfolio = trade.getSheet("portfolio");
			XSSFSheetConditionalFormatting my_cond_format_layer = portfolio.getSheetConditionalFormatting();
			XSSFConditionalFormattingRule my_rule = my_cond_format_layer.createConditionalFormattingRule(ComparisonOperator.GT, "F12");
			
			 XSSFFontFormatting my_rule_pattern = my_rule.createFontFormatting();
			 
			 
             my_rule_pattern.setFontColorIndex(IndexedColors.GREEN.getIndex());
             
             
             CellRangeAddress[] my_data_range = {CellRangeAddress.valueOf("J12:JY12")};
             my_cond_format_layer.addConditionalFormatting(my_data_range,my_rule);
             
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
		
		printAccelerometer(BASE_COMPANIES_URL, s);
		
		
	}

	private void printAccelerometer(String companyUrl, Scraper s) {
		List<Company> companies = getCompanies();
		if(CollectionUtils.isNotEmpty(companies)){
			double pcentageDrop = 0;
			double pcentageGain = 0;
			Map<Company,List<ChartData>> sixMonthInfo = new LinkedHashMap<Company,List<ChartData>>();
			Map<Company,List<ChartData>> filteredSixMonthInfopercentDrop = new LinkedHashMap<Company,List<ChartData>>();
			Map<Company,List<ChartData>> filteredSixMonthInfopercentGain = new LinkedHashMap<Company,List<ChartData>>();
			for(Company c:companies){
				List<ChartData> chartData = getSixMonthlyData(c.getRediffCode()+"", s);
				if(chartData.size()>0){
					sixMonthInfo.put(c, chartData);
				}
			}
			System.out.println("Relevant Companies : "+sixMonthInfo.size());
			Set<Entry<Company,List<ChartData>>> keySet = sixMonthInfo.entrySet();
			for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				Entry<Company, List<ChartData>> entry = (Entry<Company, List<ChartData>>) iterator.next();
				Company c = entry.getKey();
				List<ChartData> chartData = entry.getValue();				
				pcentageDrop = getpcentageDrop(chartData);				
				if((pcentageDrop>0.45 && pcentageDrop <=0.75)){
					System.out.println("PD:"+pcentageDrop);
					filteredSixMonthInfopercentDrop.put(entry.getKey(),entry.getValue());					
				}
				pcentageGain = getpcentageGain(chartData);
				if((pcentageGain>0.45 && pcentageGain <=0.75)){
					System.out.println("PG:"+pcentageGain);
					filteredSixMonthInfopercentGain.put(entry.getKey(),entry.getValue());					
				}
			}
			keySet = filteredSixMonthInfopercentDrop.entrySet();
			XPath xPath =  XPathFactory.newInstance().newXPath();
			for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				Entry<Company, List<ChartData>> entry = (Entry<Company, List<ChartData>>) iterator.next();
				Company c = entry.getKey();
				
				try{
					String pageUrl = companyUrl+(c.getName().replace(" ", "-"))+"/"+c.getRediffCode();
					
					String val = getStockMeterValue(s, pageUrl);
					System.out.println(c.getName()+": StockMeterValue:"+val);
					int meterVal = Integer.parseInt(val);
					if(meterVal>=60){
						System.out.println("$"+c.getName()+" ->"+pcentageDrop);
						System.out.println("*******************CHECK Meter Val :"+meterVal+"**********************");
					}
				}
				catch(Exception e){
					//Swallow
				}
				
				
				
				/*Elements es = doc.getElementsByAttributeValue("div", "class");
				if(es!=null &&es.size()>0){
					for (int i = 0; i < es.size(); i++) {
						org.jsoup.nodes.Element e = es.get(i);
						System.out.println(e);
					}
				}*/
				/*Document pageDoc = s.getAsXml(pageUrl);
				//System.out.println(pageDoc.getTextContent());
				//NodeList list =  (NodeList) xPath.compile("//div").evaluate(pageDoc,XPathConstants.NODESET);
				NodeList list = pageDoc.getElementsByTagName("div");
				System.out.println("size:"+list!=null ? list.getLength() :"cannot find");
				if(list!=null && list.getLength()>0){
					for (int i = 0; i < list.getLength(); i++) {
						Element e = (Element)list.item(i);
						System.out.println(e.getTextContent());
						if(e!=null && e.hasAttribute("class") ){
							//&& e.getAttribute("class").getBytes().equals("cmp_meterbox".getBytes())
							System.out.println("has ------"+e.getAttribute("class").getBytes());
						}
					}
					
				}*/
				
			}
			
			
			
			//System.out.println("$"+c.getName()+" ->"+chartData.size());
			/*Double pDrop = null;
			if(chartData.size()>0){
				pcentageDrop = getpcentageDrop(chartData);
				//System.out.println(pcentageDrop);
				if(pcentageDrop>0.75){
					System.out.println("$"+c.getName()+" ->"+chartData.size());
				}
			}*/
			
		}
	}

	private String getMonth() {
		GregorianCalendar c = new GregorianCalendar();
		//DATE_FORMAT_IN.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
		c.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
		int m = c.get(GregorianCalendar.MONTH) + 1;
		 int d = c.get(GregorianCalendar.DATE);
		 String mm = Integer.toString(m);
		 String dd = Integer.toString(d);
		 return "" + c.get(GregorianCalendar.YEAR) + (m < 10 ? "0" + mm : mm);
	}

	private double getpcentageGain(List<ChartData> chartData) {
		ChartData highest = null;
		ChartData lowest = null;
		for(ChartData data:chartData){
			if(highest==null){
				highest=data;				
			}
			if(lowest==null){
				highest=data;				
			}
			if(data.getValue().compareTo(highest.getValue())>0){
				highest = data;				
			}else{
				lowest = data;
			}			
		}
		
		double val = (highest.getValue().doubleValue() - lowest.getValue().doubleValue())/lowest.getValue().doubleValue();
		
		//System.out.println("Highest:"+highest.getValue()+", on "+highest.getWhen());
		//System.out.println("Lowest:"+lowest.getValue()+", on "+lowest.getWhen());
		return val;
	}

	private String getStockMeterValue(Scraper s, String pageUrl) {
		org.jsoup.nodes.Document doc = Jsoup.parse(s.getUrlContents(pageUrl));
		org.jsoup.nodes.Element masthead = doc.select("div.cmp_meterbox").first();
		masthead = masthead.getElementsByTag("img").first();
		String val = masthead.attr("src");
		//System.out.println(val);
		val = val.substring(val.lastIndexOf("/")+1,val.length()-4);
		return val;
	}

	private double getpcentageDrop(List<ChartData> chartData) {
		ChartData highest = null;
		ChartData lowest = null;
		for(ChartData data:chartData){
			if(highest==null){
				highest=data;				
			}
			if(lowest==null){
				highest=data;				
			}
			if(data.getValue().compareTo(highest.getValue())>0){
				highest = data;				
			}else{
				lowest = data;
			}			
		}
		
		double val = (highest.getValue().doubleValue() - lowest.getValue().doubleValue())/highest.getValue().doubleValue();
		
		//System.out.println("Highest:"+highest.getValue()+", on "+highest.getWhen());
		//System.out.println("Lowest:"+lowest.getValue()+", on "+lowest.getWhen());
		return val;
	}

	private void updateSheetwithData(Sheet portfolio, List<String> scripCodes) {
		CompanyData data = new CompanyData();
		Map<String,String> rediffCodes = data.getRediffCodes();
		System.out.println(rediffCodes);
		for (Iterator<String> iterator = scripCodes.iterator(); iterator.hasNext();) {
			String scripCode = iterator.next();
			String string = rediffCodes.get(scripCode);
			if(string!=null){
				String url_monthly = "https://money.rediff.com/money/jsp/chart_6month_new1.jsp?companyCode="+string+"&all=1";
				String url_day = "https://money.rediff.com/money1/chart_1day_new.php?companyCode="+string;
				
				System.out.println("Monthly: "+url_monthly);
				System.out.println("Daily: "+url_day);
				Scraper s = new Scraper();
				
				
				//Document doc = s.getAsXml(url);
				Document doc = null;
				Document docdaily = null;
				try {					
					doc = s.getProcessedXMLDocument(s.getUrlContents(url_monthly));
					docdaily = s.getProcessedXMLDocument(s.getUrlContents(url_day));
				}
				catch (Exception e){
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				//System.out.println(doc.getChildNodes().getLength());
				Map<String,String> values = getScripMap(doc);								
				String dayValue = getDayScrip(docdaily);
				System.out.println("Days Value:"+dayValue);
				scripsWithValues.put(string, values);
					
					if(dayValue!=null){	
					int lastColNum =  portfolio.getRow(0).getLastCellNum();
					Row r = getRow(portfolio, scripCode);
					log.info("Row:"+r.getRowNum());
					log.info(scripCode);
					for(int i=START_POINT_COL_INDEX;i<lastColNum;i++){
						String key = DATE_FORMAT_OUT.format(portfolio.getRow(0).getCell(i).getDateCellValue());
						
						String price = values.get(key);
						if(price!=null){
							
							
							Cell c = r.createCell(i, CellType.NUMERIC);
							c.setCellValue(Double.parseDouble(price));
							log.info("Col Index"+c.getColumnIndex());
							log.info("Key: "+key+ " Price: "+price);
						}
						
					}
					//Set todays latest Value				
					Cell c = r.createCell(MARKET_PRICE_COLUMN, CellType.NUMERIC);
					c.setCellValue(Double.parseDouble(dayValue));
				}
				
			}
			//System.out.println(scripsWithValues);
		}
		
		updateSheet(portfolio);
	}

	private List<ChartData> getSixMonthlyData(String companyCode, Scraper s) {
		String chartSixMonthUrl = "https://money.rediff.com/money1/chart_compPage.php?companyCode="+companyCode+"&mode=6month&exchange=bse&output=csv";
		List<ChartData> chartData = processInputFile(s.getUrlContents(chartSixMonthUrl));
		return chartData;
	}

	private void updateSheet(Sheet portfolio) {
		// TODO Auto-generated method stub
		
	}

	private Map<String, String> getScripMap(Document document) {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Map<String,String> values = new TreeMap<String,String>();
		try {
			
			//System.out.println(document.getChildNodes().item(0).getNodeName());
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
	
	private String getDayScrip(Document document) {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String value = null;
		try {
			
			System.out.println(document.getChildNodes().item(0).getNodeName());
			NodeList list =  (NodeList) xPath.compile("/graph/set").evaluate(document,XPathConstants.NODESET);
			System.out.println("No of Rows : "+list.getLength());
			
			if(list!=null && list.getLength()>0){
				Element node = (Element)list.item(list.getLength()-1);
				value = node.getAttribute("value");
			}	
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
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
				//System.out.println("param:"+scripCode+" cellVal:"+name);
				if(name.equals(scripCode)){
					break;
				}
			}
			//System.out.println("RowNum"+r.getCell(0).getNumericCellValue());
		}
		return r;
	}

	private Map<String,List<Stock>> merge(String string, String string2) throws Exception {
		Sheet sheet1 = workbook.getSheet(string);
		
		try {
			PortfolioSpreadSheet gSpreasdsheet = new PortfolioSpreadSheet();
			String gSheetID1 = gSpreasdsheet.getSpreadsheetIdByName(getFileName()+"_"+string);
		} catch (Exception e) {			
			System.out.println("Couldnt get sheet:"+getFileName()+"_"+string);
		}
		
		Map<String,List<Stock>> mergedData = new TreeMap<String,List<Stock>>();
		Map<String,Stock> list1 = new TreeMap<String,Stock>();
		
		
		populateListFromSheet(sheet1, list1);
		//MapUtil.sortByChangeDesc(list1);
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
		         List<Stock> mergedStocks = new ArrayList<Stock>();
		         mergedStocks.add(list1.get(name));
		         mergedStocks.add(list2.get(name));
				 row.createCell(0).setCellValue(name);
		         row.createCell(1).setCellValue(list1.get(name).getChange()+"");
		         row.createCell(2).setCellValue(list2.get(name).getChange()+"");
		         row.createCell(3).setCellValue(list2.get(name).getCurrentPrice().toString());
		         
			}
			
		}
		
		writeWorkbookToFile();
		return mergedData;
		
	}

	private void merge(Map<String, List<Stock>> dgwg_a, String sheetName) {
		Sheet sheet1 = workbook.getSheet(sheetName);
		
		Map<String,List<Stock>> mergedData = new TreeMap<String,List<Stock>>();
		Map<String,Stock> list1 = new TreeMap<String,Stock>();
		
		populateListFromSheet(sheet1, list1);
		for (Iterator<String> iterator = list1.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			if(dgwg_a.keySet().contains(name)){
				System.out.println("Merged: "+name);
			}
		}
		
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

/*
 * Creates basic Sheets
 * DG_A - Daily Gainers A group
 * DL_A - Daily Losers A group
 * WG -> Weekly Gainers
 * WL -> Weekly Losers
 * MG -> Monthly Gainers
 * ML -> Monthly Losers
 */
	private void gather() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		long start = System.currentTimeMillis();
		for(String g:group){
			for(String p:period){
				for(String d:data){
					List<Stock> stocks = new ArrayList<Stock>();
					String url = "https://money.rediff.com/"+d+"/bse/"+p+"/group"+g;
					System.out.println(url);
					getListing(stocks, url);
					createWorkSheet(stocks,getSheetName(g,p,d));
					try {
						createGoogleWorkSheet(stocks,getSheetName(g,p,d));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("continuing");
					}
					//return stocks;;
				}
			}
		}
		 //Write the workbook in file system
	      
	      String fileName = BASE_DIR+getFileName()+".xlsx";
	      this.filename = fileName;
	      writeWorkbookToFile();
	      System.out.println("Time taken :"+(System.currentTimeMillis()-start)+" msecs.");
		
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
	         row.createCell(0).setCellValue(stock.getName().trim());
	         row.createCell(1).setCellValue(stock.getGroup());
	         row.createCell(2).setCellValue(stock.getPrevClose().toString());
	         row.createCell(3).setCellValue(stock.getCurrentPrice().toString());
	         row.createCell(4).setCellValue(stock.getChange().toString());
	      }
	}
	
	private void createGoogleWorkSheet(List<Stock> dailyGainersA,String sheetName) throws Exception {
		PortfolioSpreadSheet spreadsheet = new PortfolioSpreadSheet();
		String newFileId = spreadsheet.createSpreadsheet(getFileName()+"_"+sheetName);
	    List<List<Object>> sheetData = new ArrayList<List<Object>>();
	    
	      //Create row object
	      List<Object> row;
	      
	      //int rowid = 0;
	      
	      for (Stock stock : dailyGainersA)
	      {
	    	  row = new ArrayList<Object>(); 
	        
	    	 row.add(stock.getName().trim());
	    	 row.add(stock.getGroup());
	    	 row.add(stock.getPrevClose().toString());
	    	 row.add(stock.getCurrentPrice().toString());
	    	 row.add(stock.getChange().toString());
	    	 sheetData.add(row);
	      }
	      spreadsheet.updateData(newFileId, null, sheetData);
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
		//DATE_FORMAT_IN.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
		c.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
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

	public List<Stock> getDailyGainers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/daily/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"DG_A");
		return stocks;
		
	}
	
	public List<Stock> getWeeklyGainers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/weekly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"WG_A");
		return stocks;
		
	}
	
	public List<Stock> getMonthlyGainers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/gainers/bse/monthly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"MG_A");
		return stocks;
		
	}
	
	public List<Stock> getDailyLosers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/daily/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"DL_A");
		return stocks;
		
	}
	
	public List<Stock> getWeeklyLosers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/weekly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"WL_A");
		return stocks;
		
	}
	
	public List<Stock> getMonthlyLosers(Group g) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		List<Stock> stocks = new ArrayList<Stock>();
		String url = "http://money.rediff.com/losers/bse/monthly/group"+g.toString().toLowerCase();
		System.out.println(url);
		getListing(stocks, url);
		createWorkSheet(stocks,"ML_A");
		return stocks;
		
	}
	
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "ASCII");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	private void getListing(List<Stock> stocks, String url) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		Scraper s = new Scraper();
		String str = null;
		Document document = null;
		/*try {
			System.out.println("Parsing into HTML");
			str =  s.parseToHTML(url);
			//System.out.println(str);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SAXException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (TikaException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		document = s.getProcessedXMLDocument(str);*/
		
		System.out.println("getAsXml() ..");
		document = s.getAsXml(url);
		

		
		
		
		NodeList nl = document.getDocumentElement().getChildNodes();
		
		//printDocument(document, System.out);
		System.out.println(nl.getLength());
		System.out.println(nl.item(1).getNodeName());
		System.out.println();
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		try {
			Element result = (Element) xPath.compile("//table[@class='dataTable']").evaluate(document,XPathConstants.NODE);			
			//System.out.println(result.getNodeValue());
			
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
			if(temp!=null){
				s.setName(temp.trim());
			}
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
	
	private List<ChartData> processInputFile(String string) {
	    List inputList = new ArrayList();
	    try{
	      BufferedReader br = new BufferedReader(new StringReader(string));
	      // skip the header of the csv
	      inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
	      br.close();
	    } catch (Exception e) {
	      //swallow
	    }
	    return inputList ;
	}
	
	private Function<String, ChartData> mapToItem = (line) -> {
		ChartData item = new ChartData();
		  String[] p = line.split(",");// a CSV has comma separated lines
		  if(p!=null && period.length==3){
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			  try{
				  Date tmp = sdf.parse(p[0]);
				  item.setWhen(tmp);
				  item.setValue(new BigDecimal(p[1]));
				  if(p.length>2)item.setPrevClose(new BigDecimal(p[2]));
			  }catch(Exception e){
				  e.printStackTrace();
			  }	
		  }
		 	  
		 /* item.setItemNumber(p[0]);//<-- this is the first column in the csv file
		  if (p[3] != null && p[3].trim().length() > 0) {
		    item.setSomeProeprty(p[3]);
		  }*/
		  //more initialization goes here
				  
		  return item;
	};
	
	private List<Company> getCompanies() {
		System.out.println("getCompanies");
		List<Company> companies = new ArrayList<Company>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from companies c where c.group in ('A','B') order by c.group,c.name ");
			Company c = null;
			while(rs.next()) {
				c = new Company();
				c.setCode(rs.getInt("CODE"));
				c.setName(rs.getString("NAME"));
				c.setGroup(rs.getString("GROUP"));
				c.setRediffCode(rs.getString("REDIFF_CODE"));
				companies.add(c);
			}
			rs.close();
			stmt.close();
			System.out.println("Number Of Companies"+companies.size());
			//// "+rs.getString(3));
			// con.close();
		} catch (Exception e) {
			// System.out.println(e);}
		}
		return companies;
	}
		

}
