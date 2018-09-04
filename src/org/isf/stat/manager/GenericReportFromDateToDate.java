package org.isf.stat.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import org.isf.generaldata.GeneralData;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.utils.db.DbQueryLogger;
import org.isf.utils.db.DbSingleConn;
import org.isf.utils.excel.ExcelExporter;

/*--------------------------------------------------------
 * GenericReportLauncer2Dates
 *  - lancia tutti i report che come parametri hanno "da data" "a data"
 * 	- la classe prevede l'inizializzazione attraverso 
 *    dadata, adata, nome del report (senza .jasper)
 *---------------------------------------------------------
 * modification history
 * 09/06/2007 - prima versione
 *
 *-----------------------------------------------------------------*/
	public class GenericReportFromDateToDate {
		public  GenericReportFromDateToDate(String fromDate, String toDate, String jasperFileName, String defaultName, boolean toExcel) {
			try{
		        HashMap<String, String> parameters = new HashMap<String, String>();
				HospitalBrowsingManager hospManager = new HospitalBrowsingManager();
				Hospital hosp = hospManager.getHospital();
				
				parameters.put("Hospital", hosp.getDescription());
				parameters.put("Address", hosp.getAddress());
				parameters.put("City", hosp.getCity());
				parameters.put("Email", hosp.getEmail());
				parameters.put("Telephone", hosp.getTelephone());
				parameters.put("fromdate", fromDate + ""); // real param
				parameters.put("todate", toDate + ""); // real param
			
				StringBuilder sbFilename = new StringBuilder();
				sbFilename.append("rpt");
				sbFilename.append(File.separator);
				sbFilename.append(jasperFileName);
				sbFilename.append(".jasper");
				File jasperFile = new File(sbFilename.toString());
				
				sbFilename = new StringBuilder();
				sbFilename.append("PDF");
				sbFilename.append(File.separator);
				sbFilename.append(defaultName);
				File defaultFilename = new File(sbFilename.toString());
		
				Connection conn = DbSingleConn.getConnection();
				
				JasperReport jasperReport = (JasperReport)JRLoader.loadObject(jasperFile);
				if (toExcel) {
					JRQuery query = jasperReport.getMainDataset().getQuery();

					String queryString = query.getText();

					queryString = queryString.replace("$P{fromdate}", "'" + fromDate + "'");
					queryString = queryString.replace("$P{todate}", "'" + toDate + "'");

					DbQueryLogger dbQuery = new DbQueryLogger();
					ResultSet resultSet = dbQuery.getData(queryString, true);
					JFileChooser fcExcel = new JFileChooser();
					FileNameExtensionFilter excelFilter = new FileNameExtensionFilter("Excel (*.xlsx)","xlsx");
					FileNameExtensionFilter excelFilter2003 = new FileNameExtensionFilter("Excel 97-2003 (*.xls)","xls");
					fcExcel.addChoosableFileFilter(excelFilter);
					fcExcel.addChoosableFileFilter(excelFilter2003);
					fcExcel.setFileFilter(excelFilter);
					fcExcel.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fcExcel.setSelectedFile(defaultFilename);
					
					int iRetVal = fcExcel.showSaveDialog(null);
					if(iRetVal == JFileChooser.APPROVE_OPTION)
					{
						File exportFile = fcExcel.getSelectedFile();
						FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
						String extension = selectedFilter.getExtensions()[0];
						if (!exportFile.getName().endsWith(extension)) exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
						
						ExcelExporter xlsExport = new ExcelExporter();
						if (exportFile.getName().endsWith(".xls"))
							xlsExport.exportResultsetToExcelOLD(resultSet, exportFile);
						else
							xlsExport.exportResultsetToExcel(resultSet, exportFile);
					}
					dbQuery.releaseConnection();
					
				} else {
					JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
					String PDFfile = "rpt/PDF/"+jasperFileName+".pdf";
					JasperExportManager.exportReportToPdfFile(jasperPrint, PDFfile);
					if (GeneralData.INTERNALVIEWER)
						JasperViewer.viewReport(jasperPrint,false);
					else { 
						try{
							Runtime rt = Runtime.getRuntime();
							rt.exec(GeneralData.VIEWER +" "+ PDFfile);
							
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
