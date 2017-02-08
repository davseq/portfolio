package finance.portfolio.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class Scraper {
	
	public String getUrlContents(String theUrl)
    {
      StringBuilder content = new StringBuilder();
   
      // many of these calls can throw exceptions, so i've just
      // wrapped them all in one try/catch statement.
      try
      {
        // create a url object
        URL url = new URL(theUrl);
        System.out.println(theUrl);
   
        // create a urlconnection object
        HttpURLConnection urlconnection = (HttpURLConnection)url.openConnection();
   
        // wrap the urlconnection in a bufferedreader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(),"utf-8"));
   
        String line;
   
        // read from the urlconnection via the bufferedreader
        while ((line = bufferedReader.readLine()) != null)
        {
          content.append(line + "\n");
        }
        bufferedReader.close();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      //System.out.println(content);
      return content.toString();
    }
	
	 public Document getProcessedXMLDocument(String xmlString) throws ParserConfigurationException, FileNotFoundException, 
		SAXException, IOException {
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			builder = builderFactory.newDocumentBuilder();
			//Document document = builder.parse(new FileInputStream(xmlString));
			//System.out.println("@@");
			//System.out.println(xmlString);
			//System.out.println("@@");
			Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes("utf-8")),"utf-8");
			return document;
		}
	 
	 public Document getAsXml(String url) {
			URL urlU = null;
			try {
				urlU = new URL(url);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Tidy tidy = new Tidy();
			tidy.setMakeClean(true);
			tidy.setXmlOut( true);
			tidy.setShowWarnings(false);
			Document document = null;
			   
			try {
				FileOutputStream fileOutputStream = new FileOutputStream("outXHTML.xml");
				document =  tidy.parseDOM( urlU.openStream(), fileOutputStream);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return document;
		}
	
	

}
