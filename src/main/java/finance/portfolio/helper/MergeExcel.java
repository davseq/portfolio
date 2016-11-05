package finance.portfolio.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;   
  
public class MergeExcel   
{   
  public static void main(String args[]) throws Exception   
  {   
      MergeExcel mergeExcel = new MergeExcel();   
      Workbook workBook1 = mergeExcel.checkFileFormat("first excel");   
     Workbook workBook2 = mergeExcel.checkFileFormat("second excel");   
     String path="result excel";   
      ArrayList<ArrayListRows> rowsArrayList = mergeExcel.getObjectsFromWorkBook(workBook1);     
      ArrayList<ArrayListRows> rowsArrayList1 = mergeExcel.getObjectsFromWorkBook(workBook2);   
       
      ArrayListRows firstRowList1 = rowsArrayList.get(0);   
      rowsArrayList.remove(0);   
      ArrayListRows firstRowList2 = rowsArrayList1.get(0);   
      rowsArrayList1.remove(0);   
      ArrayList<Integer> tot = mergeExcel.compareHeadings(firstRowList1,firstRowList2);   
      rowsArrayList1 = mergeExcel.sortColoumns(rowsArrayList1,tot);   
      ArrayList<ArrayListRows> totalList = mergeExcel.addRows(rowsArrayList,rowsArrayList1);   
      mergeExcel.writeToFile(path,totalList,firstRowList1);   
     /* Iterator<ArrayListRows> hfgks = totalList.iterator();  
      while(hfgks.hasNext())  
          System.out.println(((ArrayListRows)hfgks.next()).coloumns);*/  
         
  }   
  public void writeToFile(String path,ArrayList<ArrayListRows> totalList,ArrayListRows firstRowList1) throws Exception   
    {   
        HSSFWorkbook wb1 = new HSSFWorkbook();   
  
        HSSFSheet sheet1 = wb1.createSheet("new sheet");   
        for(int k=0;k<HSSFSheet.INITIAL_CAPACITY;k++)   
        {   
            sheet1.setColumnWidth( k, (short) 5000);   
        }   
              
            HSSFRow newTitleRow = sheet1.createRow((short)0);   
                     
             for(int index=0;index<firstRowList1.coloumns.size();index++)   
                  {   
                   newTitleRow.createCell(index).setCellValue(firstRowList1.coloumns.get(index));   
                  }                
               for(int index=0;index<totalList.size();index++)   
               {   
                      
                   HSSFRow newRow = sheet1.createRow((short)(index+1));   
                   ArrayListRows temp = totalList.get(index);   
                   for(int index1=0;index1<temp.coloumns.size();index1++)   
                      {   
  
                     HSSFCell cel =  newRow.createCell(index1);   
                          cel.setCellValue((temp.coloumns.get(index1)));   
  
                      }   
                     
               }   
                FileOutputStream fileOut = new FileOutputStream(path);   
                wb1.write(fileOut);   
                fileOut.close();      
                System.out.println("result.xls created Sucessfully");   
                  
                
                  
        }   
  
       
  public ArrayList<ArrayListRows> sortColoumns(ArrayList<ArrayListRows> rowsList,ArrayList<Integer> tot)   
  {   
      ArrayList<ArrayListRows> rowsArrayList = new ArrayList<ArrayListRows>();   
      for(int i = 0;i<rowsList.size();i++)   
      {   
          ArrayListRows temp = new ArrayListRows();   
          for(int j=0;j<(rowsList.get(i).coloumns.size());j++)   
          {   
              temp.coloumns.add(rowsList.get(i).coloumns.get(tot.get(j)));   
          }   
          rowsArrayList.add(temp);   
      }   
      return rowsArrayList;      
  }   
  public ArrayList<Integer> compareHeadings(ArrayListRows firstRowList1,ArrayListRows firstRowList2)   
  {   
      ArrayList<Integer> tot = new ArrayList<Integer>();   
      if(!(firstRowList1.coloumns.size() == firstRowList2.coloumns.size()))   
      {   
        System.out.println("cannot merge the files");   
        System.exit(0);   
        //return null;   
      }   
      System.out.println(firstRowList1.coloumns);   
      System.out.println(firstRowList2.coloumns);   
     for(int i=0;i<firstRowList1.coloumns.size();i++)   
      {   
         for(int j=0;j<firstRowList2.coloumns.size();j++)   
          {   
                  if((firstRowList1.coloumns.get(i).trim()).equals((firstRowList2.coloumns.get(j).trim())))   
                      {   
                        tot.add(j);   
                       break;   
                      }   
          }   
      }   
     return tot;   
  }   
  public ArrayList<ArrayListRows> addRows(ArrayList<ArrayListRows> rowsArrayList, ArrayList<ArrayListRows> rowsArrayList1 )   
  {   
      ArrayList<ArrayListRows> arrayRows = new ArrayList<ArrayListRows>();   
      Iterator<ArrayListRows> itera = rowsArrayList.iterator();   
      while(itera.hasNext())   
      {   
          arrayRows.add(itera.next());   
      }   
  
     for(int i=0;i<rowsArrayList1.size();i++)   
      {   
          int j;   
         for( j=0;j<rowsArrayList.size();j++)   
         {   
             if(compareColumns(rowsArrayList1.get(i), rowsArrayList.get(j)))   
                     {   
                        break;   
                     }   
                        
         }   
         if(j>=rowsArrayList1.size())   
                arrayRows.add(rowsArrayList1.get(i));   
      }     
      return arrayRows;   
  }   
  public boolean compareColumns(ArrayListRows row1,ArrayListRows row2)   
  {   
      for(int i=0;i<row1.coloumns.size();i++)   
      {   
      if(!row1.coloumns.get(i).equals(row2.coloumns.get(i)))   
          return false;   
      }   
  return true;   
            
  }   
  
  public ArrayList<ArrayListRows> getObjectsFromWorkBook(Workbook wb)   
  {   
      ArrayList<ArrayListRows> arrayRows = new ArrayList<ArrayListRows>();   
        Sheet sheet=null;              
       Row row=null;   
        Cell cell=null;   
           
        if(wb!=null){   
            sheet=wb.getSheetAt(0);   
            row=null;   
            cell=null;   
            Iterator<Row> rows=sheet.rowIterator();   
            while(rows.hasNext())   
            {   
                ArrayListRows allo = new ArrayListRows();   
                row=(Row)rows.next();                  
                Iterator<Cell> cells=row.cellIterator();   
                   
                while(cells.hasNext())   
                {   
                cell=( Cell)cells.next();   
                if(cell.getCellType()==Cell.CELL_TYPE_STRING){   
                        allo.coloumns.add(cell.getStringCellValue());   
                           
                }   
                else  
                    allo.coloumns.add(""+cell.getNumericCellValue());      
                }   
                arrayRows.add(allo);   
                   
            }   
        }   
      return arrayRows;   
  }   
  public  Workbook checkFileFormat(String fileName){   
        Workbook hwb=null;   
        FileInputStream checkFis=null;   
        try{   
             checkFis=new FileInputStream(fileName);   
             //Instantiate the Workbook using HSSFWorkbook   
             hwb=new HSSFWorkbook(checkFis);   
             Sheet sheet=hwb.getSheetAt(0);   
             Iterator<Row> rows=sheet.rowIterator();   
               
             Iterator<Cell> cells=null;   
             Row row=null;   
             Cell cell=null;   
             int check=0;   
             //Read the file as HSSFWorkbook   
             while(rows.hasNext()){   
                check++;   
                row=(HSSFRow)rows.next();   
                cells=row.cellIterator();   
                while(cells.hasNext()){   
                    cell=(HSSFCell)cells.next();   
                }   
                if(check==2)   
                    break;   
            }   
            //Return HSSFWorkbook type object if there is no exception in reading the file using HSSFWorkbook   
            return hwb;   
        }catch(ClassCastException ce){  //Instantiate the Workbook using XSSFWorkbook in case of class cast exception   
            Workbook xwb=null;   
            //System.out.println("class cast");   
            try{   
            xwb=new XSSFWorkbook(checkFis);        
            checkFis.close();   
            }catch(IOException e){   
            e.printStackTrace();   
            //System.out.println("class cast io");   
        }   
        return xwb; }   
        catch(Exception e){ //Instantiate the Workbook using XSSFWorkbook in case of Exception while reading file through HSSFWorkbook   
        Workbook xwb=null;   
        try{   
        checkFis.close();   
        checkFis=null;   
        checkFis=new FileInputStream(fileName);   
        xwb=new XSSFWorkbook(checkFis);    
        checkFis.close();   
        }catch(IOException ie){   
        ie.printStackTrace();   
  
    }   
           
    return xwb;   
    }   
    }   
}   
  
  
  
  
  
  
