package org.isf.dicom.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.io.DicomCodingException;
import org.imgscalr.Scalr;
import org.isf.dicom.gui.DicomLoader;
import org.isf.dicom.gui.ThumbnailViewGui;
import org.isf.dicom.model.FileDicomDetail;
import org.isf.generaldata.MessageBundle;

/**
 * Magager for DICOM Files
 * 
 * @author Pietro Castellucci
 * @version 1.0.0
 */

public class SourceFiles extends Thread {

	private File file = null;
	private int patient = 0;
	private int filesCount = 0;
	private int filesLoaded = 0;
	private DicomLoader dicomLoader = null;
	private ThumbnailViewGui thumbnail = null;

	public SourceFiles(File sourceFile, int patient, int filesCount, ThumbnailViewGui thumbnail, JFrame frame) {
		this.patient = patient;
		this.file = sourceFile;
		this.filesCount = filesCount;
		this.thumbnail = thumbnail;
		this.dicomLoader = new DicomLoader(filesCount, frame);
		start();
	}

	public void run() {
		loadDicomDir(file, patient);
		dicomLoader.setVisible(false);
		thumbnail.initialize();
	}

	/**
	 * load a DICOM directory
	 */
	private void loadDicomDir(File sourceFile, int patient) {
		// installLibs();
		File[] files = sourceFile.listFiles();

		for (int i = 0; i < files.length; i++) {

			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}

			if (!files[i].isDirectory()) {
				loadDicom(files[i], patient);
				filesLoaded++;
				dicomLoader.setLoaded(filesLoaded);
			} else if (!".".equals(files[i].getName()) && !"..".equals(files[i].getName()))
				loadDicomDir(files[i], patient);
		}
	}

	public static int numeroFiles(File sourceFile, int patient) {
		int num = 0;

		File[] files = sourceFile.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory())
				num++;
			else if (!".".equals(files[i].getName()) && !"..".equals(files[i].getName()))
				num = num + numeroFiles(files[i], patient);
		}
		return num;
	}

	public boolean working() {
		// System.out.println("working "+loaded+" < "+numeroFiles);
		return (filesLoaded < filesCount);
	}

	public int getLoaded() {
		return filesLoaded;
	}

	/**
	 * load dicom file
	 */
	public synchronized static void loadDicom(File sourceFile, int paziente) {
		// installLibs();

		// System.out.println("File "+sourceFile.getName());

		if (".DS_Store".equals(sourceFile.getName()))
			return;

		try {
			Iterator<?> iter = ImageIO.getImageReadersByFormatName("DICOM");

			ImageReader reader = (ImageReader) iter.next();

			DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();

			ImageInputStream imageInputStream = ImageIO.createImageInputStream(sourceFile);

			reader.setInput(imageInputStream, false);

			BufferedImage original = null;

			try {
				original = reader.read(0, param);
			} catch (DicomCodingException dce) {
				JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.dicom.load.err") + " : " + sourceFile.getName(), MessageBundle.getMessage("angal.dicom.err"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			imageInputStream.close();

			BufferedImage scaled = Scalr.resize(original, 100);

			DicomStreamMetaData dicomStreamMetaData = (DicomStreamMetaData) reader.getStreamMetadata();

			DicomObject dicomObject = dicomStreamMetaData.getDicomObject();

			String patientID = dicomObject.getString(Tag.PatientID);
			// System.out.println("PatientID "+patientID);
			String patientName = dicomObject.getString(Tag.PatientName);
			String patientAddress = dicomObject.getString(Tag.PatientAddress);
			String patientAge = dicomObject.getString(Tag.PatientAge);
			String acquisitionsInSeries = dicomObject.getString(Tag.AcquisitionsInSeries);
			String acquisitionsInStudy = dicomObject.getString(Tag.AcquisitionsInStudy);
			String applicatorDescription = dicomObject.getString(Tag.ApplicatorDescription);
			String dicomMediaRetrievalSequence = dicomObject.getString(Tag.DICOMMediaRetrievalSequence);
			String patientComments = dicomObject.getString(Tag.PatientComments);
			String patientBirthDate = "";
			try {
				patientBirthDate = DateFormat.getDateInstance().format(dicomObject.getDate(Tag.PatientBirthDate));
			} catch (Exception ecc) {
			}
			String patientSex = dicomObject.getString(Tag.PatientSex);
			String modality = dicomObject.getString(Tag.Modality);
			String studyUID = dicomObject.getString(Tag.StudyInstanceUID);
			String studyDate = "";
			try {
				studyDate = DateFormat.getDateInstance().format(dicomObject.getDate(Tag.StudyDate, Tag.StudyTime));
			} catch (Exception ecc) {
			}
			String accessionNumber = dicomObject.getString(Tag.AccessionNumber);
			String studyDescription = dicomObject.getString(Tag.StudyDescription);
			String studyComments = dicomObject.getString(Tag.StudyComments);
			String seriesUID = dicomObject.getString(Tag.SeriesInstanceUID);
			String directoryRecordType = dicomObject.getString(Tag.DirectoryRecordType);
			String seriesInstanceUID = dicomObject.getString(Tag.SeriesInstanceUID);
			String seriesNumber = dicomObject.getString(Tag.SeriesNumber);
			String seriesDescriptionCodeSequence = dicomObject.getString(Tag.SeriesDescriptionCodeSequence);
			String sliceVector = dicomObject.getString(Tag.SliceVector);
			String sliceLocation = dicomObject.getString(Tag.SliceLocation);
			String sliceThickness = dicomObject.getString(Tag.SliceThickness);
			String sliceProgressionDirection = dicomObject.getString(Tag.SliceProgressionDirection);
			String seriesDate = "";
			try {
				seriesDate = DateFormat.getDateInstance().format(dicomObject.getDate(Tag.SeriesDate, Tag.SeriesTime));
			} catch (Exception ecc) {
			}
			String institutionName = dicomObject.getString(Tag.InstitutionName);
			String seriesDescription = dicomObject.getString(Tag.SeriesDescription);
			String instanceUID = dicomObject.getString(Tag.SOPInstanceUID);

			// Caricato... pronto per il salvataggio del file

			FileDicomDetail dicomFileDetail = new FileDicomDetail();
			dicomFileDetail.setDicomData(sourceFile);
			dicomFileDetail.setFileName(sourceFile.getName());
			dicomFileDetail.setDicomAccessionNumber(accessionNumber);
			dicomFileDetail.setDicomInstanceUID(instanceUID);
			dicomFileDetail.setDicomInstitutionName(institutionName);
			dicomFileDetail.setDicomPatientAddress(patientAddress);
			dicomFileDetail.setDicomPatientAge(patientAge);
			dicomFileDetail.setDicomPatientBirthDate(patientBirthDate);
			dicomFileDetail.setDicomPatientID(patientID);
			dicomFileDetail.setDicomPatientName(patientName);
			dicomFileDetail.setDicomPatientSex(patientSex);
			dicomFileDetail.setDicomSeriesDate(seriesDate);
			dicomFileDetail.setDicomSeriesDescription(seriesDescription);
			dicomFileDetail.setDicomSeriesDescriptionCodeSequence(seriesDescriptionCodeSequence);
			dicomFileDetail.setDicomSeriesInstanceUID(seriesInstanceUID);
			dicomFileDetail.setDicomSeriesNumber(seriesNumber);
			dicomFileDetail.setDicomSeriesUID(seriesUID);
			dicomFileDetail.setDicomStudyDate(studyDate);
			dicomFileDetail.setDicomStudyDescription(studyDescription);
			dicomFileDetail.setDicomStudyId(studyUID);
			dicomFileDetail.setIdFile(0);
			dicomFileDetail.setPatId(paziente);
			dicomFileDetail.setDicomThumbnail(scaled);
			dicomFileDetail.setModality(modality);
			DicomManagerFactory.getManager().saveFile(dicomFileDetail);

		} catch (Exception ecc) {
			ecc.printStackTrace();
		}
	}
}
