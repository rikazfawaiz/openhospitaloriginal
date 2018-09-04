package org.isf.admission.gui;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.EventListenerList;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.admtype.model.AdmissionType;
import org.isf.disctype.model.DischargeType;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.dlvrrestype.manager.DeliveryResultTypeBrowserManager;
import org.isf.dlvrrestype.model.DeliveryResultType;
import org.isf.dlvrtype.manager.DeliveryTypeBrowserManager;
import org.isf.dlvrtype.model.DeliveryType;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.examination.service.ExaminationOperations;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.patient.gui.PatientSummary;
import org.isf.patient.model.Patient;
import org.isf.pregtreattype.manager.PregnantTreatmentTypeBrowserManager;
import org.isf.pregtreattype.model.PregnantTreatmentType;
import org.isf.utils.jobjects.BusyState;
import org.isf.utils.jobjects.ShadowBorder;
import org.isf.utils.jobjects.VoDateTextField;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.manager.Interaction;

import com.toedter.calendar.JDateChooser;

/**
 * This class shows essential patient data and allows to create an admission
 * record or modify an existing one
 * 
 * release 2.5 nov-10-06
 * 
 * @author flavio
 * 
 */

/*----------------------------------------------------------
 * modification history
 * ====================
 * 23/10/06 - flavio - borders set to not resizable
 *                     changed Disease IN (/OUT) into Dignosis IN (/OUT)
 *                     
 * 10/11/06 - ross - added RememberDate for admission Date
 * 				   - only diseses with flag In Patient (IPD) are displayed
 *                 - on Insert. in edit all are displayed
 *                 - the correct way should be to display the IPD + the one aready registered
 * 18/08/08 - Alex/Andrea - Calendar added
 * 13/02/09 - Alex - Cosmetic changes to UI
 * 10/01/11 - Claudia - insert ward beds availability 
 * 01/01/11 - Alex - GUI and code reengineering
 * 29/12/11 - Nicola - insert alert IN/OUT patient for communication module
 -----------------------------------------------------------*/
public class AdmissionBrowser extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private EventListenerList admissionListeners = new EventListenerList();

	public interface AdmissionListener extends EventListener {
		public void admissionUpdated(AWTEvent e);

		public void admissionInserted(AWTEvent e);
	}

	public void addAdmissionListener(AdmissionListener l) {
		admissionListeners.add(AdmissionListener.class, l);
	}

	public void removeAdmissionListener(AdmissionListener listener) {
		admissionListeners.remove(AdmissionListener.class, listener);
	}

	private void fireAdmissionInserted(Admission anAdmission) {
		AWTEvent event = new AWTEvent(anAdmission, AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = admissionListeners.getListeners(AdmissionListener.class);
		for (int i = 0; i < listeners.length; i++)
			((AdmissionListener) listeners[i]).admissionInserted(event);
	}

	private void fireAdmissionUpdated(Admission anAdmission) {
		AWTEvent event = new AWTEvent(anAdmission, AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = admissionListeners.getListeners(AdmissionListener.class);
		for (int i = 0; i < listeners.length; i++)
			((AdmissionListener) listeners[i]).admissionUpdated(event);
	}

	private Patient patient = null;

	private boolean editing = false;

	private Admission admission = null;

	private PatientSummary ps = null;
	
	private JTextArea textArea = null;

	private JTabbedPane jTabbedPaneAdmission;

	private JPanel jPanelAdmission;

	private JPanel jPanelOperation;

	private JPanel jPanelDelivery;

	private int pregnancyTabIndex;
	
	private JPanel jContentPane = null;
	
	// enable is if patient is female
	private boolean enablePregnancy = false;

	// viewing is if you set ward to pregnancy
	private boolean viewingPregnancy = false;

	private GregorianCalendar visitDate = null;

	private float weight = 0.0f;

	private VoDateTextField visitDateField = null;

	private VoLimitedTextField weightField = null;

	private JDateChooser visitDateFieldCal = null; // Calendar

	private JComboBox treatmTypeBox = null;

	private final int preferredWidthDates = 110;
	
	private final int preferredWidthDiagnosis = 550;
	
	private final int preferredWidthTypes = 220;
	
	private final int preferredWidthTransfusionSpinner = 55;

	private final int preferredHeightLine = 24;
	
	private GregorianCalendar deliveryDate = null;

	private VoDateTextField deliveryDateField = null;

	private JDateChooser deliveryDateFieldCal = null;

	private JComboBox deliveryTypeBox = null;

	private JComboBox deliveryResultTypeBox = null;
	
	private ArrayList<PregnantTreatmentType> treatmTypeList = null;

	private ArrayList<DeliveryType> deliveryTypeList = null;

	private ArrayList<DeliveryResultType> deliveryResultTypeList = null;

	private GregorianCalendar ctrl1Date = null;

	private GregorianCalendar ctrl2Date = null;

	private GregorianCalendar abortDate = null;

	private JDateChooser ctrl1DateFieldCal = null;

	private JDateChooser ctrl2DateFieldCal = null;

	private JDateChooser abortDateFieldCal = null;
	
	private JComboBox wardBox;

	private ArrayList<Ward> wardList = null;

	// save value during a swith
	private Ward saveWard = null;

	private String saveYProg = null;

	private JTextField yProgTextField = null;

	private JTextField FHUTextField = null;

	private JPanel wardPanel;

	private JPanel fhuPanel;

	private JPanel yearProgPanel;
	
	private JComboBox diseaseInBox;
	
	private DiseaseBrowserManager dbm = new DiseaseBrowserManager();

	private ArrayList<Disease> diseaseInList = dbm.getDiseaseIpdIn();
	
	private ArrayList<Disease> diseaseOutList = dbm.getDiseaseIpdOut();

	private JCheckBox malnuCheck;

	private JPanel diseaseInPanel;

	private JPanel malnuPanel;
	
	private GregorianCalendar dateIn = null;

	private JDateChooser dateInFieldCal = null;

	private JComboBox admTypeBox = null;

	private ArrayList<AdmissionType> admTypeList = null;

	private JPanel admissionDatePanel;

	private JPanel admissionTypePanel;
	
	private JComboBox diseaseOut1Box = null;
	
	private JComboBox diseaseOut2Box = null;
	
	private JComboBox diseaseOut3Box = null;

	private JPanel diseaseOutPanel;
	
	private JPanel diseaseOut1Panel;
	
	private JPanel diseaseOut2Panel;
	
	private JPanel diseaseOut3Panel;
	
	private JComboBox operationBox = null;

	private JRadioButton operationResultRadioP = null;

	private JRadioButton operationResultRadioN = null;

	private JRadioButton operationResultRadioU = null;

	private ArrayList<Operation> operationList = null;
	
	private GregorianCalendar operationDate = null;

	private JDateChooser operationDateFieldCal = null;

	private VoDateTextField operationDateField = null;

	private float trsfUnit = 0.0f;

	private JSpinner trsfUnitField = null;
	
	private GregorianCalendar dateOut = null;

	private JDateChooser dateOutFieldCal = null;

	private JComboBox disTypeBox = null;

	private ArrayList<DischargeType> disTypeList = null;

	private JPanel dischargeDatePanel;

	private JPanel dischargeTypePanel;
	
	private JPanel bedDaysPanel;

	private JPanel buttonPanel = null;

	private JLabel labelRequiredFields;

	private JButton closeButton = null;

	private JButton saveButton = null;
	
	private JButton jButtonExamination = null;

	private JPanel operationDatePanel;

	private JPanel transfusionPanel;

	private JPanel operationPanel;

	private JPanel resultPanel;

	private JPanel visitDatePanel;

	private JPanel weightPanel;

	private JPanel treatmentPanel;

	private JPanel deliveryDatePanel;

	private JPanel deliveryTypePanel;

	private JPanel deliveryResultTypePanel;

	private JPanel control1DatePanel;

	private JPanel control2DatePanel;

	private JPanel abortDatePanel;

	private VoLimitedTextField bedDaysTextField;
	
	private AdmissionBrowserManager admMan = new AdmissionBrowserManager();
	
	final String DATE_TIME_FORMAT = "dd/MM/yy HH:mm:ss";

	/*
	 * from AdmittedPatientBrowser
	 */
	public AdmissionBrowser(JFrame parentFrame, AdmittedPatient admPatient, boolean editing) {
		super(parentFrame, (editing ? MessageBundle.getMessage("angal.admission.editadmissionrecord") : MessageBundle.getMessage("angal.admission.newadmission")), true);
		addAdmissionListener((AdmissionListener) parentFrame);
		this.editing = editing;
		patient = admPatient.getPatient();
		if (("" + patient.getSex()).equalsIgnoreCase("F")) {
			enablePregnancy = true;
		}
		ps = new PatientSummary(patient);

		if (editing) {
			admission = admMan.getCurrentAdmission(patient);
			if (admission.getWardId().equalsIgnoreCase("M")) {
				viewingPregnancy = true;
			} else {
			}
		} else {
			admission = new Admission();
		}
		
		if (editing) {
			dateIn = admission.getAdmDate();
		} else {
			dateIn = new GregorianCalendar(); //RememberDates.getLastAdmInDateGregorian();
		}
		
		initialize(parentFrame);
		
		this.addWindowListener(new WindowAdapter(){
			
			public void windowClosing(WindowEvent e) {
				//to free memory
				if (diseaseInList != null) diseaseInList.clear();
				if (diseaseOutList != null) diseaseOutList.clear();
				dispose();
			}			
		});
	}

	/*
	 * from PatientDataBrowser
	 */
	public AdmissionBrowser(JFrame parentFrame, JFrame parentParentFrame, Patient aPatient, Admission anAdmission) {
		super(parentFrame, MessageBundle.getMessage("angal.admission.editadmissionrecord"), true);
		addAdmissionListener((AdmissionListener) parentParentFrame);
		addAdmissionListener((AdmissionListener) parentFrame);
		this.editing = true;
		patient = aPatient;
		if (("" + patient.getSex()).equalsIgnoreCase("F")) {
			enablePregnancy = true;
		}
		ps = new PatientSummary(patient);

		admission = admMan.getAdmission(anAdmission.getId());
		if (admission.getWardId().equalsIgnoreCase("M")) {
			viewingPregnancy = true;
		} 
		
		if (editing) {
			dateIn = admission.getAdmDate();
		} else {
			dateIn = new GregorianCalendar(); //RememberDates.getLastAdmInDateGregorian();
		}
		
		initialize(parentFrame);
		
		this.addWindowListener(new WindowAdapter(){
			
			public void windowClosing(WindowEvent e) {
				//to free memory
				if (diseaseInList != null) diseaseInList.clear();
				if (diseaseOutList != null) diseaseOutList.clear();
				dispose();
			}			
		});
		
	}

	private void initialize(JFrame parent) {

		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		JPanel data = new JPanel();
		data.setLayout(new BorderLayout());
		data.add(getPatientDataPanel(), java.awt.BorderLayout.WEST);
		data.add(getJTabbedPaneAdmission(), java.awt.BorderLayout.CENTER);
		return data;
	}

	private JPanel getPatientDataPanel() {
		JPanel data = new JPanel();
		data.add(ps.getPatientCompleteSummary());
		return data;
	}

	private JTabbedPane getJTabbedPaneAdmission() {
		if (jTabbedPaneAdmission == null) {
			jTabbedPaneAdmission = new JTabbedPane();
			jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.admissionanddischarge"), getAdmissionTab());
			jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.operation"), getOperationTab());
			if (enablePregnancy) {
				jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.delivery"), getDeliveryTab());
				pregnancyTabIndex = jTabbedPaneAdmission.getTabCount() - 1;
				if (!viewingPregnancy) {
					jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, false);
				}
			}
			jTabbedPaneAdmission.addTab("Note", getJPanelNote());
		}
		return jTabbedPaneAdmission;
	}

	private JPanel getAdmissionTab() {
		if (jPanelAdmission == null) {
			jPanelAdmission = new JPanel();

			GroupLayout layout = new GroupLayout(jPanelAdmission);
			jPanelAdmission.setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(LEADING)
					.addComponent(getDiseaseInPanel())
					.addComponent(getDiseaseOutPanel())
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(getWardPanel())
								.addComponent(getAdmissionDatePanel())
								.addComponent(getDischargeDatePanel())
						)
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(getFHUPanel())
								.addComponent(getAdmissionTypePanel())
								.addComponent(getBedDaysPanel())
						)
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(getProgYearPanel())
								.addComponent(getMalnutritionPanel())
								.addComponent(getDischargeTypePanel())
								.addComponent(getJLabelRequiredFields())
						)
					)
				)
			);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getWardPanel())
							.addComponent(getFHUPanel())
							.addComponent(getProgYearPanel())
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getAdmissionDatePanel())
							.addComponent(getAdmissionTypePanel())
							.addComponent(getMalnutritionPanel())
					)
					.addComponent(getDiseaseInPanel())
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getDischargeDatePanel())
							.addComponent(getBedDaysPanel())
							.addComponent(getDischargeTypePanel())
					)
					.addComponent(getDiseaseOutPanel())
					.addComponent(getJLabelRequiredFields())
			);
		}
		return jPanelAdmission;
	}

	private JPanel getOperationTab() {
		if (jPanelOperation == null) {
			jPanelOperation = new JPanel();
			
			GroupLayout layout = new GroupLayout(jPanelOperation);
			jPanelOperation.setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING)
						.addComponent(getOperationDatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
						.addComponent(getOperationPanel(), GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
							.addComponent(getOperationResultPanel())
							.addComponent(getTransfusionPanel())
						)
					)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getOperationDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(getOperationPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getOperationResultPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getTransfusionPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
			);
		}
		return jPanelOperation;
	}

	private JPanel getDeliveryTab() {
		if (jPanelDelivery == null) {
			jPanelDelivery = new JPanel();
			
			GroupLayout layout = new GroupLayout(jPanelDelivery);
			jPanelDelivery.setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(getVisitDatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryDatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(getWeightPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryTypePanel())
					)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(getTreatmentPanel())
							.addComponent(getDeliveryResultTypePanel())
							.addComponent(getControl1DatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
							.addComponent(getControl2DatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
							.addComponent(getAbortDatePanel(), GroupLayout.PREFERRED_SIZE, preferredWidthDates, GroupLayout.PREFERRED_SIZE)
					)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getVisitDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getWeightPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getTreatmentPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getDeliveryDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryTypePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryResultTypePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getControl1DatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getControl2DatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getAbortDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
			);
			
			layout.linkSize(SwingConstants.VERTICAL, getDeliveryDatePanel(), getDeliveryTypePanel(), getDeliveryResultTypePanel());
		}
		return jPanelDelivery;
	}
	
	private JScrollPane getJPanelNote() {

		JScrollPane scrollPane = new JScrollPane(getJTextAreaNote());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50), // external
				new ShadowBorder(5, Color.LIGHT_GRAY))); // internal
		scrollPane.addAncestorListener(new AncestorListener() {

			public void ancestorRemoved(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {
			}

			public void ancestorAdded(AncestorEvent event) {
				textArea.requestFocus();
			}
		});
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		scrollPane.setPreferredSize(new Dimension(screensize.width/2, screensize.height/2));
		return scrollPane;
	}

	private JTextArea getJTextAreaNote() {
		if (textArea == null) {
			textArea = new JTextArea();
			if (editing && admission.getNote() != null) {
				textArea.setText(admission.getNote());
			}
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setMargin(new Insets(10, 10, 10, 10));
		}
		return textArea;
	}

	private JPanel getTreatmentPanel() {
		if (treatmentPanel == null) {
			treatmentPanel = new JPanel();
			
			PregnantTreatmentTypeBrowserManager abm = new PregnantTreatmentTypeBrowserManager();
			treatmTypeBox = new JComboBox();
			treatmTypeBox.addItem("");
			treatmTypeList = abm.getPregnantTreatmentType();
			for (PregnantTreatmentType elem : treatmTypeList) {
				treatmTypeBox.addItem(elem);
				if (editing) {
					if (admission.getPregTreatmentType() != null && admission.getPregTreatmentType().equalsIgnoreCase(elem.getCode())) {
						treatmTypeBox.setSelectedItem(elem);
					}
				}
			}
			
			treatmentPanel.add(treatmTypeBox);
			treatmentPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.treatmenttype")));
		}
		return treatmentPanel;
	}

	private JPanel getWeightPanel() {
		if (weightPanel == null) {
			weightPanel = new JPanel();
			
			weightField = new VoLimitedTextField(5, 5);
			if (editing && admission.getWeight() != null) {
				weight = admission.getWeight().floatValue();
				weightField.setText(String.valueOf(weight));
			}
			
			weightPanel.add(weightField);
			weightPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.weight")));
		}
		return weightPanel;
	}

	private JPanel getVisitDatePanel() {
		if (visitDatePanel == null) {
			visitDatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getVisitDate() != null) {
				visitDate = admission.getVisitDate();
				myDate = visitDate.getTime();
			} else {
				visitDate = new GregorianCalendar();
			}
			visitDateFieldCal = new JDateChooser(myDate, "dd/MM/yy"); // Calendar
			visitDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			visitDateFieldCal.setDateFormatString("dd/MM/yy");
	
			visitDatePanel.add(visitDateFieldCal); // Calendar
			visitDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.visitdate")));
		}
		return visitDatePanel;
	}

	private JPanel getDeliveryResultTypePanel() {
		if (deliveryResultTypePanel == null) {
			deliveryResultTypePanel = new JPanel();
			
			DeliveryResultTypeBrowserManager drtbm = new DeliveryResultTypeBrowserManager();
			deliveryResultTypeBox = new JComboBox();
			deliveryResultTypeBox.addItem("");
			deliveryResultTypeList = drtbm.getDeliveryResultType();
			for (DeliveryResultType elem : deliveryResultTypeList) {
				deliveryResultTypeBox.addItem(elem);
				if (editing) {
					if (admission.getDeliveryResultId() != null && admission.getDeliveryResultId().equalsIgnoreCase(elem.getCode())) {
						deliveryResultTypeBox.setSelectedItem(elem);
					}
				}
			}
			
			deliveryResultTypePanel.add(deliveryResultTypeBox);
			deliveryResultTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliveryresultype")));
		}
		return deliveryResultTypePanel;
	}

	private JPanel getDeliveryTypePanel() {
		if (deliveryTypePanel == null) {
			deliveryTypePanel = new JPanel();
			
			DeliveryTypeBrowserManager dtbm = new DeliveryTypeBrowserManager();
			deliveryTypeBox = new JComboBox();
			deliveryTypeBox.addItem("");
			deliveryTypeList = dtbm.getDeliveryType();
			for (DeliveryType elem : deliveryTypeList) {
				deliveryTypeBox.addItem(elem);
				if (editing) {
					if (admission.getDeliveryTypeId() != null && admission.getDeliveryTypeId().equalsIgnoreCase(elem.getCode())) {
						deliveryTypeBox.setSelectedItem(elem);
					}
				}
			}
			deliveryTypePanel.add(deliveryTypeBox);
			deliveryTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliverytype")));
		}
		return deliveryTypePanel;
	}

	private JPanel getDeliveryDatePanel() {
		if (deliveryDatePanel == null) {
			deliveryDatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getDeliveryDate() != null) {
				deliveryDate = admission.getDeliveryDate();
				myDate = deliveryDate.getTime();
			} 
			deliveryDateFieldCal = new JDateChooser(myDate, "dd/MM/yy"); // Calendar
			deliveryDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			deliveryDateFieldCal.setDateFormatString("dd/MM/yy");
			
			deliveryDatePanel.add(deliveryDateFieldCal);
			deliveryDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliverydate")));
		}
		return deliveryDatePanel;
	}

	private JPanel getAbortDatePanel() {
		if (abortDatePanel == null) {
			abortDatePanel = new JPanel();
			Date myDate = null;
			if (editing && admission.getAbortDate() != null) {
				abortDate = admission.getAbortDate();
				myDate = abortDate.getTime();
			}
			abortDateFieldCal = new JDateChooser(myDate, "dd/MM/yy");
			abortDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			abortDateFieldCal.setDateFormatString("dd/MM/yy");
	
			abortDatePanel.add(abortDateFieldCal);
			abortDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.abortdate")));
		}
		return abortDatePanel;
	}

	private JPanel getControl1DatePanel() {
		if (control1DatePanel == null) {
			control1DatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getCtrlDate1() != null) {
				ctrl1Date = admission.getCtrlDate1();
				myDate = ctrl1Date.getTime();
			} 
			ctrl1DateFieldCal = new JDateChooser(myDate, "dd/MM/yy");
			ctrl1DateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			ctrl1DateFieldCal.setDateFormatString("dd/MM/yy");

			control1DatePanel.add(ctrl1DateFieldCal);
			control1DatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.controln1date")));
		}
		return control1DatePanel;
	}
	
	private JPanel getControl2DatePanel() {
		if (control2DatePanel == null) {
			control2DatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getCtrlDate2() != null) {
				ctrl2Date = admission.getCtrlDate2();
				myDate = ctrl2Date.getTime();
			} 
			ctrl2DateFieldCal = new JDateChooser(myDate, "dd/MM/yy");
			ctrl2DateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			ctrl2DateFieldCal.setDateFormatString("dd/MM/yy");

			control2DatePanel.add(ctrl2DateFieldCal);
			control2DatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.controln2date")));
		}
		return control2DatePanel;
	}

	private JPanel getProgYearPanel() {
		if (yearProgPanel == null) {
			yearProgPanel = new JPanel();
			
			if (saveYProg != null) {
				yProgTextField = new JTextField(saveYProg);
			} else if (editing) {
				yProgTextField = new JTextField("" + admission.getYProg());
			} else {
				yProgTextField = new JTextField("");
			}
			yProgTextField.setColumns(11);
			
			yearProgPanel.add(yProgTextField);
			yearProgPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.progressiveinyear")));
			
		}
		return yearProgPanel;
	}

	private JPanel getFHUPanel() {
		if (fhuPanel == null) {
			fhuPanel = new JPanel();
			
			if (editing) {
				FHUTextField = new JTextField(admission.getFHU());
			} else {
				FHUTextField = new JTextField();
			}
			FHUTextField.setColumns(20);
			
			fhuPanel.add(FHUTextField);
			fhuPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.fromhealthunit")));
			
		}
		return fhuPanel;
	}

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			
			WardBrowserManager wbm = new WardBrowserManager();
			wardBox = new JComboBox();
			wardBox.addItem("");
			wardList = wbm.getWards();
			for (Ward ward : wardList) {
				// if patient is a male you don't see pregnancy case
				if (("" + patient.getSex()).equalsIgnoreCase("F") && !ward.isFemale()) {
					continue;
				} else if (("" + patient.getSex()).equalsIgnoreCase("M") && !ward.isMale()) {
					continue;
				} else {
					if (ward.getBeds() > 0)
						wardBox.addItem(ward);
				}
				if (saveWard != null) {
					if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				} else if (editing) {
					if (admission.getWardId().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				}
			}
			wardBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// set yProg
					if (wardBox.getSelectedIndex() == 0) {
						yProgTextField.setText("");
						return;
					} else {
						String wardId = ((Ward) wardBox.getSelectedItem()).getCode();
						if (wardId.equalsIgnoreCase(admission.getWardId())) {
							yProgTextField.setText("" + admission.getYProg());
						} else {
							AdmissionBrowserManager abm = new AdmissionBrowserManager();
							int nextProg = abm.getNextYProg(wardId);
							yProgTextField.setText("" + nextProg);

							// get default selected warn default beds number
							int nBeds = (((Ward) wardBox.getSelectedItem()).getBeds()).intValue();
							int usedBeds = abm.getUsedWardBed(wardId);
							int freeBeds = nBeds - usedBeds;
							if (freeBeds <= 0)
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.wardwithnobedsavailable"));
						}
					}

					// switch panel
					if (((Ward) wardBox.getSelectedItem()).getCode().equalsIgnoreCase("M")) {
						if (!viewingPregnancy) {
							saveWard = (Ward) wardBox.getSelectedItem();
							saveYProg = yProgTextField.getText();
							viewingPregnancy = true;
							jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, true);
							validate();
							repaint();
						}
					} else {
						if (viewingPregnancy) {
							saveWard = (Ward) wardBox.getSelectedItem();
							saveYProg = yProgTextField.getText();
							viewingPregnancy = false;
							jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, false);
							validate();
							repaint();
						}
					}

				}
			});
			
			wardPanel.add(wardBox);
			wardPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.ward")));
		}
		return wardPanel;
	}

	private JPanel getDiseaseInPanel() {
		if (diseaseInPanel == null) {
			diseaseInPanel = new JPanel();
			diseaseInPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			diseaseInPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.diagnosisinstar")));
			diseaseInPanel.add(Box.createHorizontalStrut(50));
			
			diseaseInBox = new JComboBox();
			diseaseInBox.setPreferredSize(new Dimension(preferredWidthDiagnosis, preferredHeightLine));
			
		}	
		
		Disease found = null;
		String diseaseIn = admission.getDiseaseInId();
		diseaseInBox.removeAllItems();
		diseaseInBox.addItem("");
		for (Disease elem : diseaseInList) {
			diseaseInBox.addItem(elem);
			
			//search for saved DiseaseIn
			if (editing && found == null && diseaseIn != null && diseaseIn.equalsIgnoreCase(elem.getCode())) {
				diseaseInBox.setSelectedItem(elem);
				found = elem;
			}
		}
		
		
		if (editing && found == null && diseaseIn != null) {
			
			//Not found: search among all diseases
			ArrayList<Disease> diseaseAllList = dbm.getDiseaseAll();
			for (Disease elem : diseaseAllList) {
				if (diseaseIn.equalsIgnoreCase(elem.getCode())) {
					diseaseInBox.addItem(elem);
					diseaseInBox.setSelectedItem(elem);
					found = elem;
				}
			}
			
			if (found == null) {
				//Still not found
				diseaseInBox.addItem(MessageBundle.getMessage("angal.admission.no") + admission.getDiseaseOutId1() + " " + MessageBundle.getMessage("angal.admission.notfoundasinpatientdisease"));
				diseaseInBox.setSelectedIndex(diseaseInBox.getItemCount() - 1);
			}
		}
			
		diseaseInPanel.add(diseaseInBox);
		
		return diseaseInPanel;
	}

	/**
	 * @return
	 */
	private JPanel getMalnutritionPanel() {
		if (malnuPanel == null) {
			malnuPanel = new JPanel();
			
			malnuCheck = new JCheckBox();
			if (editing && admission.getType().equalsIgnoreCase("M")) {
				malnuCheck.setSelected(true);
			} else {
				malnuCheck.setSelected(false);
			}
			
			malnuPanel.add(malnuCheck);
			malnuPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.malnutrition")), BorderLayout.CENTER);
			
		}
		return malnuPanel;
	}

	private JPanel getAdmissionTypePanel() {
		if (admissionTypePanel == null) {
			admissionTypePanel = new JPanel();
			
			admTypeBox = new JComboBox();
			admTypeBox.setPreferredSize(new Dimension(preferredWidthTypes, preferredHeightLine));
			admTypeBox.addItem("");
			admTypeList = admMan.getAdmissionType();
			for (AdmissionType elem : admTypeList) {
				admTypeBox.addItem(elem);
				if (editing) {
					if (admission.getAdmType().equalsIgnoreCase(elem.getCode())) {
						admTypeBox.setSelectedItem(elem);
					}
				}
			}
			
			admissionTypePanel.add(admTypeBox);
			admissionTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.admissiontype")));
				
		}
		return admissionTypePanel;
	}

	/**
	 * @return
	 */
	private JPanel getAdmissionDatePanel() {
		if (admissionDatePanel == null) {
			admissionDatePanel = new JPanel();
			
			dateInFieldCal = new JDateChooser(dateIn.getTime());
			dateInFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			dateInFieldCal.setDateFormatString(DATE_TIME_FORMAT);
			dateInFieldCal.addPropertyChangeListener("date", new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					Date newValue = (Date) evt.getNewValue();
					if (newValue.before(patient.getBirthDate())) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.thepatientwasnotyetbornatselecteddate"));
						dateInFieldCal.setDate((Date) evt.getOldValue());
						return;
					}
					dateInFieldCal.setDate(newValue);
					dateIn.setTime(newValue);
					updateBedDays();
					getDiseaseInPanel();
					getDiseaseOut1Panel();
					getDiseaseOut2Panel();
					getDiseaseOut3Panel();
				}
			});
			
			admissionDatePanel.add(dateInFieldCal);
			admissionDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.admissiondate")));
		}
		return admissionDatePanel;
	}

	private JPanel getDiseaseOutPanel() {
		if (diseaseOutPanel == null) {
			diseaseOutPanel = new JPanel();
			diseaseOutPanel.setLayout(new BoxLayout(diseaseOutPanel, BoxLayout.Y_AXIS));
			diseaseOutPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.diagnosisout")));
			diseaseOutPanel.add(getDiseaseOut1Panel());
			diseaseOutPanel.add(getDiseaseOut2Panel());
			diseaseOutPanel.add(getDiseaseOut3Panel());
		}
		return diseaseOutPanel;
	}

	/**
	 * @return
	 */
	private JPanel getDiseaseOut1Panel() {
		if (diseaseOut1Panel == null) {
			diseaseOut1Panel = new JPanel();
			diseaseOut1Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number1"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));
			label.setHorizontalTextPosition(SwingConstants.RIGHT);

			diseaseOut1Panel.add(label);
			
			diseaseOut1Box = new JComboBox();
			diseaseOut1Box.setPreferredSize(new Dimension(preferredWidthDiagnosis, preferredHeightLine));
		}	
		
		Disease found = null;
		String diseaseOut1 = admission.getDiseaseOutId1();
		diseaseOut1Box.removeAllItems();
		diseaseOut1Box.addItem("");
		for (Disease elem : diseaseOutList) {
			diseaseOut1Box.addItem(elem);
		
			//search for saved diseaseOut1
			if (editing && found == null && diseaseOut1 != null && diseaseOut1.equalsIgnoreCase(elem.getCode())) {
					diseaseOut1Box.setSelectedItem(elem);
					found = elem;
				}
			}
		
		if (editing && found == null && diseaseOut1 != null) {
		
			//Not found: search among all diseases
			ArrayList<Disease> diseaseAllList = dbm.getDiseaseAll();
			for (Disease elem : diseaseAllList) {
				if (diseaseOut1.equalsIgnoreCase(elem.getCode())) {
					diseaseOut1Box.addItem(elem);
					diseaseOut1Box.setSelectedItem(elem);
					found = elem;
				}
			}
			
			if (found == null) {
				//Still not found
				diseaseOut1Box.addItem(MessageBundle.getMessage("angal.admission.no") + admission.getDiseaseOutId1() + " " + MessageBundle.getMessage("angal.admission.notfoundasinpatientdisease"));
				diseaseOut1Box.setSelectedIndex(diseaseOut1Box.getItemCount() - 1);
			}
		}
		
		diseaseOut1Panel.add(diseaseOut1Box);
		
		return diseaseOut1Panel;
	}

	private JPanel getDiseaseOut2Panel() {

		if (diseaseOut2Panel == null) {
			diseaseOut2Panel = new JPanel();
			diseaseOut2Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number2"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));
			label.setHorizontalTextPosition(SwingConstants.RIGHT);

			diseaseOut2Panel.add(label);
			
			diseaseOut2Box = new JComboBox();
			diseaseOut2Box.setPreferredSize(new Dimension(preferredWidthDiagnosis, preferredHeightLine));
		}	
		
		Disease found = null;
		String diseaseOut2 = admission.getDiseaseOutId2();
		diseaseOut2Box.removeAllItems();
		diseaseOut2Box.addItem("");
		for (Disease elem : diseaseOutList) {
			diseaseOut1Box.addItem(elem);
		
			//Search for saved disaseOut2
			if (editing && found == null && diseaseOut2 != null&& diseaseOut2.equalsIgnoreCase(elem.getCode())) {
				diseaseOut2Box.setSelectedItem(elem);
				found = elem;
			}
		}
			
		if (editing && found == null && diseaseOut2 != null) {
			
			//Not found: search among all diseases
			ArrayList<Disease> diseaseAllList = dbm.getDiseaseAll();
			for (Disease elem : diseaseAllList) {
				if (diseaseOut2.equalsIgnoreCase(elem.getCode())) {
					diseaseOut2Box.addItem(elem);
					diseaseOut2Box.setSelectedItem(elem);
					found = elem;
				}
			}
			
			if (found == null) {
				//Still not found
				diseaseOut2Box.addItem(MessageBundle.getMessage("angal.admission.no") + admission.getDiseaseOutId1() + " " + MessageBundle.getMessage("angal.admission.notfoundasinpatientdisease"));
				diseaseOut2Box.setSelectedIndex(diseaseOut2Box.getItemCount() - 1);
			}
		}
		
		diseaseOut2Panel.add(diseaseOut2Box);
		
		return diseaseOut2Panel;
	}

	private JPanel getDiseaseOut3Panel() {

		if (diseaseOut3Panel == null) {
			diseaseOut3Panel = new JPanel();
			diseaseOut3Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number3"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));

			diseaseOut3Panel.add(label);
			
			diseaseOut3Box = new JComboBox();
			diseaseOut3Box.setPreferredSize(new Dimension(preferredWidthDiagnosis, preferredHeightLine));
		}	
		
		Disease found = null;
		String diseaseOut3 = admission.getDiseaseOutId3();
		diseaseOut3Box.removeAllItems();
		diseaseOut3Box.addItem("");
		for (Disease elem : diseaseOutList) {
			diseaseOut1Box.addItem(elem);
		
			//Search for saved diseaseOut3
			if (editing && found == null && diseaseOut3 != null && diseaseOut3.equalsIgnoreCase(elem.getCode())) {
				diseaseOut3Box.setSelectedItem(elem);
				found = elem;
			}
		}
			
		if (editing && found == null && diseaseOut3 != null) {
			
			//Not found: search among all diseases
			ArrayList<Disease> diseaseAllList = dbm.getDiseaseAll();
			for (Disease elem : diseaseAllList) {
				if (diseaseOut3.equalsIgnoreCase(elem.getCode())) {
					diseaseOut3Box.addItem(elem);
					diseaseOut3Box.setSelectedItem(elem);
					found = elem;
				}
			}
			
			if (found == null) {
				//Still not found
				diseaseOut3Box.addItem(MessageBundle.getMessage("angal.admission.no") + admission.getDiseaseOutId1() + " " + MessageBundle.getMessage("angal.admission.notfoundasinpatientdisease"));
				diseaseOut3Box.setSelectedIndex(diseaseOut3Box.getItemCount() - 1);
			}
		}

		diseaseOut3Panel.add(diseaseOut3Box);

		return diseaseOut3Panel;
	}

	/*
	 * simply an utility
	 */
	private JRadioButton getRadioButton(String label, char mn, boolean active) {
		JRadioButton rb = new JRadioButton(label);
		rb.setMnemonic(KeyEvent.VK_A + (mn - 'A'));
		rb.setSelected(active);
		rb.setName(label);
		return rb;
	}

	/*
	 * admission sheet: 5th row: insert select operation type and result
	 */
	private JPanel getOperationPanel() {
		if (operationPanel == null) {
			operationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
			OperationBrowserManager obm = new OperationBrowserManager();
			operationBox = new JComboBox();
			operationBox.addItem("");
			operationList = obm.getOperation();
			for (Operation elem : operationList) {
				operationBox.addItem(elem);
				if (editing) {
					if (admission.getOperationId() != null && admission.getOperationId().equalsIgnoreCase(elem.getCode())) {
						operationBox.setSelectedItem(elem);
					}
				}
			}
			operationBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (operationBox.getSelectedIndex() == 0) {
						// operationDateField.setText("");
						operationDateFieldCal.setDate(null);
					} else {
						/*
						 * if (!operationDateField.getText().equals("")){ // leave
						 * old date value }
						 */
						if (operationDateFieldCal.getDate() != null) {
							// leave old date value
						}
	
						else {
							// set today date
							operationDateFieldCal.setDate((new GregorianCalendar()).getTime());
						}
					}
				}
			});

			operationPanel.add(operationBox);
			operationPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.operationtype")));
		}
		return operationPanel;
	}

	/**
	 * @return
	 */
	private JPanel getOperationResultPanel() {
		if (resultPanel == null) {
			resultPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			
			operationResultRadioP = getRadioButton(MessageBundle.getMessage("angal.admission.positive"), 'P', false);
			operationResultRadioN = getRadioButton(MessageBundle.getMessage("angal.admission.negative"), 'N', false);
			operationResultRadioU = getRadioButton(MessageBundle.getMessage("angal.admission.unknown"), 'U', true);
			
			ButtonGroup resultGroup = new ButtonGroup();
			resultGroup.add(operationResultRadioP);
			resultGroup.add(operationResultRadioN);
			resultGroup.add(operationResultRadioU);
			
			if (editing) {
				if (admission.getOpResult() != null) {
					if (admission.getOpResult().equalsIgnoreCase("P"))
						operationResultRadioP.setSelected(true);
					else 
						operationResultRadioN.setSelected(true);
				}
			} 
	
			resultPanel.add(operationResultRadioP);
			resultPanel.add(operationResultRadioN);
			resultPanel.add(operationResultRadioU);
	
			resultPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.operationresult")));
		}
		return resultPanel;
	}

	/*
	 * admission sheet: 6th row: insert operation date and transusional unit
	 */
	private JPanel getOperationDatePanel() {
		if (operationDatePanel == null) {
			operationDatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getOpDate() != null) {
				operationDate = admission.getOpDate();
				myDate = operationDate.getTime();
			}
			operationDateFieldCal = new JDateChooser(myDate, "dd/MM/yy");
			operationDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			operationDateFieldCal.setDateFormatString("dd/MM/yy");
			
			operationDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.operationdate")));
			operationDatePanel.add(operationDateFieldCal);
		}
		return operationDatePanel;
	}
	
	/**
	 * @return
	 */
	private JPanel getTransfusionPanel() {
		if (transfusionPanel == null) {
			transfusionPanel = new JPanel();
			
			float start = 0;
			float min = 0;
			float step = (float) 0.5;
			
			SpinnerModel model = new SpinnerNumberModel(start, min, null, step);
			trsfUnitField = new JSpinner(model);
			trsfUnitField.setPreferredSize(new Dimension(preferredWidthTransfusionSpinner, preferredHeightLine));
			
			if (editing && admission.getTransUnit() != null) {
				trsfUnit = admission.getTransUnit().floatValue();
				trsfUnitField.setValue(trsfUnit);
			}
			
			transfusionPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.transfusionalunit")));
			transfusionPanel.add(trsfUnitField);
		}
		return transfusionPanel;
	}

	private JPanel getDischargeTypePanel() {
		if (dischargeTypePanel == null) {
			dischargeTypePanel = new JPanel();
			
			disTypeBox = new JComboBox();
			disTypeBox.setPreferredSize(new Dimension(preferredWidthTypes, preferredHeightLine));
			disTypeBox.addItem("");
			disTypeList = admMan.getDischargeType();
			for (DischargeType elem : disTypeList) {
				disTypeBox.addItem(elem);
				if (editing) {
					if (admission.getDisType() != null && admission.getDisType().equalsIgnoreCase(elem.getCode())) {
						disTypeBox.setSelectedItem(elem);
					}
				}
			}
			
			dischargeTypePanel.add(disTypeBox);
			dischargeTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.dischargetype")));
		}
		return dischargeTypePanel;
	}
	
	private JPanel getBedDaysPanel() {
		if (bedDaysPanel == null) {
			bedDaysPanel = new JPanel();
			
			bedDaysTextField  = new VoLimitedTextField(10, 10);
			bedDaysTextField.setEditable(false);
			
			bedDaysPanel.add(bedDaysTextField);
			bedDaysPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.beddays")));
		}
		return bedDaysPanel;
	}
	
	private void updateBedDays() {
		try {
			Date admission = dateInFieldCal.getDate();
			Date discharge = dateOutFieldCal.getDate();
			int bedDays = TimeTools.getDaysBetweenDates(admission, discharge, false);
			if (bedDays == 0) bedDays++;
			bedDaysTextField.setText(String.valueOf(bedDays));
		} catch (Exception e) {
			bedDaysTextField.setText("");
		}
	}

	/**
	 * @return
	 */
	private JPanel getDischargeDatePanel() {
		if (dischargeDatePanel == null) {
			dischargeDatePanel = new JPanel();
			
			Date myDate = null;
			if (editing && admission.getDisDate() != null) {
				dateOut = admission.getDisDate();
				myDate = dateOut.getTime();
			}
			dateOutFieldCal = new JDateChooser(myDate, "dd/MM/yy");
			dateOutFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			dateOutFieldCal.setDateFormatString(DATE_TIME_FORMAT);
			dateOutFieldCal.addPropertyChangeListener("date", new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					updateBedDays();
				}
			});
			
			
			dischargeDatePanel.add(dateOutFieldCal);
			dischargeDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.dischargedate")));
			
		}
		return dischargeDatePanel;
	}

	private JComboBox shareWith=null;
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getSaveButton());
			if (MainMenu.checkUserGrants("btnadmadmexamination")) buttonPanel.add(getJButtonExamination());
			buttonPanel.add(getCloseButton());
			
			if(GeneralData.XMPPMODULEENABLED){
			Interaction share= new Interaction();
			Collection<String> contacts = share.getContactOnline();
			contacts.add("-- Share alert with: nobody --");
			shareWith = new JComboBox(contacts.toArray());
			shareWith.setSelectedItem("-- Share alert with: nobody --");
			buttonPanel.add(shareWith);
			}
		}
		return buttonPanel;
	}
	
	private JButton getJButtonExamination() {
		if (jButtonExamination == null) {
			jButtonExamination = new JButton(MessageBundle.getMessage("angal.opd.examination"));
			jButtonExamination.setMnemonic(KeyEvent.VK_E);
			
			jButtonExamination.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					PatientExamination patex;
					ExaminationOperations examOperations = new ExaminationOperations();
					
					PatientExamination lastPatex = examOperations.getLastByPatID(patient.getCode());
					if (lastPatex != null) {
						patex = examOperations.getFromLastPatientExamination(lastPatex);
					} else {
						patex = examOperations.getDefaultPatientExamination(patient);
					}
					
					GenderPatientExamination gpatex = new GenderPatientExamination(patex, patient.getSex() == 'M');
					
					PatientExaminationEdit dialog = new PatientExaminationEdit(AdmissionBrowser.this, gpatex);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
				}
			});
		}
		return jButtonExamination;
	}

	/**
	 * @return
	 */
	private JLabel getJLabelRequiredFields() {
		if (labelRequiredFields == null) {
			labelRequiredFields = new JLabel(MessageBundle.getMessage("angal.admission.indicatesrequiredfields"));
		}
		return labelRequiredFields;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText(MessageBundle.getMessage("angal.common.close"));
			closeButton.setMnemonic(KeyEvent.VK_C);
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return closeButton;
	}
	
	private JButton getSaveButton() {

		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText(MessageBundle.getMessage("angal.common.save"));
			saveButton.setMnemonic(KeyEvent.VK_S);
			saveButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					
					/*
					 * Initialize AdmissionBrowserManager
					 */
					AdmissionBrowserManager abm = new AdmissionBrowserManager();
					ArrayList<Admission> admList = abm.getAdmissions(patient);

					/*
					 * Today GregorianCalendar
					 */
					GregorianCalendar today = new GregorianCalendar();
					
					/*
					 * Date Format for displaying
					 */
					SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
					
					/*
					 * is it an admission update or a discharge? if we have a
					 * valid discharge date isDischarge will be true
					 */
					boolean isDischarge = false;

					/*
					 * set if ward pregnancy is selected
					 */
					boolean isPregnancy = false;

					// get ward id (not null)
					if (wardBox.getSelectedIndex() == 0) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectavalidward"));
						return;
					} else {
						admission.setWardId(((Ward) (wardBox.getSelectedItem())).getCode());
					}

					if (admission.getWardId().equalsIgnoreCase("M")) {
						isPregnancy = true;
					}

					// get disease in id ( it can be null)
					if (diseaseInBox.getSelectedIndex() == 0) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectavaliddiseasein"));
						return;
					} else {
						try {
							Disease diseaseIn = (Disease) diseaseInBox.getSelectedItem();
							admission.setDiseaseInId(diseaseIn.getCode());
						} catch (IndexOutOfBoundsException e1) {
							/*
							 * Workaround in case a fake-disease is selected (ie
							 * when previous disease has been deleted)
							 */
							admission.setDiseaseInId(null);
						}
					}

					// get disease out id ( it can be null)
					int disease1index = diseaseOut1Box.getSelectedIndex();
					if (disease1index == 0) {
						admission.setDiseaseOutId1(null);
					} else {
						Disease diseaseOut1 = (Disease) diseaseOut1Box.getSelectedItem();
						admission.setDiseaseOutId1(diseaseOut1.getCode());
					}

					// get disease out id 2 ( it can be null)
					int disease2index = diseaseOut2Box.getSelectedIndex();
					if (disease2index == 0) {
						admission.setDiseaseOutId2(null);
					} else {
						Disease diseaseOut2 = (Disease) diseaseOut2Box.getSelectedItem();
						admission.setDiseaseOutId2(diseaseOut2.getCode());
					}

					// get disease out id 3 ( it can be null)
					int disease3index = diseaseOut3Box.getSelectedIndex();
					if (disease3index == 0) {
						admission.setDiseaseOutId3(null);
					} else {
						Disease diseaseOut3 = (Disease) diseaseOut3Box.getSelectedItem();
						admission.setDiseaseOutId3(diseaseOut3.getCode());
					}

					// get year prog ( not null)
					try {
						int x = Integer.parseInt(yProgTextField.getText());
						if (x < 0) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertacorrectprogressiveid"));
							return;
						} else {
							admission.setYProg(x);
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertacorrectprogressiveid"));
						return;
					}

					// get FHU (it can be null)
					String s = FHUTextField.getText();
					if (s.equals("")) {
						admission.setFHU(null);
					} else {
						admission.setFHU(FHUTextField.getText());
					}

					// check and get date in (not null)
					try {
						dateIn.setTime(dateInFieldCal.getDate());
						
						if (dateIn.after(today)) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.futuredatenotallowed"));
							dateInFieldCal.setDate(today.getTime());
							return;
						}
						if (dateIn.before(today)) {
							// check for invalid date
							for (Admission ad : admList) {
								if (editing && ad.getId() == admission.getId()) {
									continue;
								}
								if ((ad.getAdmDate().before(dateIn) || ad.getAdmDate().compareTo(dateIn) == 0) 
										&& (ad.getDisDate() != null && ad.getDisDate().after(dateIn))) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.ininserteddatepatientwasalreadyadmitted"));
									dateInFieldCal.setDate(today.getTime());
									return;
								}
							}
						}
						// updateDisplay
						dateInFieldCal.setDate(dateIn.getTime());
						admission.setAdmDate(dateIn);
						RememberDates.setLastAdmInDate(dateIn);

					} catch (IllegalArgumentException iae) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidadmissiondate"));
						return;
					}

					// get admission type (not null)
					if (admTypeBox.getSelectedIndex() == 0) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectavalidadmissiondate"));
						return;
					} else {
						admission.setAdmType(admTypeList.get(admTypeBox.getSelectedIndex() - 1).getCode());
					}

					// check and get date out (it can be null)
					// if set date out, isDischarge is set
					Date date = dateOutFieldCal.getDate();
					if (date != null) {
						dateOut = new GregorianCalendar();
						dateOut.setTime(date);
					} else dateOut = null;
					if (dateOut == null) {
						// only if we are editing the last admission
						// or if it is a new admission
						// no if we are editing an old admission
						Admission last = null;
						if (admList.size() > 0) {
							last = admList.get(admList.size() - 1);
						} else {
							last = admission;
						}
						if (!editing || (editing && admission.getId() == last.getId())) {
							// ok
						} else {
							JOptionPane.showMessageDialog(AdmissionBrowser.this,
									MessageBundle.getMessage("angal.admission.pleaseinsertadischargedate") + ", " + MessageBundle.getMessage("angal.admission.youareeditinganoldadmission"));
							return;

						}

						admission.setDisDate(null);
					} else {
						try {
							// date control
							if (dateOut.before(dateIn)) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.dischargedatemustbeafteradmissiondate"));
								return;
							}
							if (dateOut.after(today)) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.futuredatenotallowed"));
								return;
							} else {
								// check for invalid date
								boolean invalidDate = false;
								Date invalidStart = new Date();
								Date invalidEnd = new Date();
								for (Admission ad : admList) {
									// case current admission : let it be
									if (editing && ad.getId() == admission.getId()) {
										continue;
									}
									// found an open admission
									// only if i close my own first of it
									if (ad.getDisDate() == null) {
										if (!dateOut.after(ad.getAdmDate()))
											;// ok
										else {
											JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.intheselecteddatepatientwasadmittedagain"));
											return;
										}
									}
									// general case
									else {
										// DateIn >= adOut
										if (dateIn.after(ad.getDisDate()) || dateIn.equals(ad.getDisDate())) {
											// ok
										}
										// dateOut <= adIn
										else if (dateOut.before(ad.getAdmDate()) || dateOut.equals(ad.getAdmDate())) {
											// ok
										} else {
											invalidDate = true;
											invalidStart = ad.getAdmDate().getTime();
											invalidEnd = ad.getDisDate().getTime();
											break;
										}
									}
								}
								if (invalidDate) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this,
											MessageBundle.getMessage("angal.admission.invalidadmissionperiod") + MessageBundle.getMessage("angal.admission.theadmissionbetween") + " "
													+ sdf.format(invalidStart) + " " + MessageBundle.getMessage("angal.admission.and") + " " + sdf.format(invalidEnd) + " "
													+ MessageBundle.getMessage("angal.admission.alreadyexists"));
									dateOutFieldCal.setDate(null);
									return;
								}

							}

							// updateDisplay
							admission.setDisDate(dateOut);
							isDischarge = true;
						} catch (IllegalArgumentException iae) {
							System.out.println(iae);
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavaliddischargedate"));
							return;
						}
					}

					// get operation ( it can be null)
					if (operationBox.getSelectedIndex() == 0) {
						admission.setOperationId(null);
					} else {
						admission.setOperationId(operationList.get(operationBox.getSelectedIndex() - 1).getCode());
					}

					// get operation date (may be null)
					date = operationDateFieldCal.getDate();
					if (date != null) {
						operationDate.setTime(date);
					} else operationDate = null;
					
					if (operationDate == null) {
						admission.setOpDate(null);
					} else {
						try {
							GregorianCalendar limit;
							if (admission.getDisDate() == null) {
								limit = today;
							} else {
								limit = admission.getDisDate();
							}

							if (operationDate.before(dateIn) || operationDate.after(limit)) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidvisitdate"));
								return;
							}

							admission.setOpDate(operationDate);
						} catch (IllegalArgumentException iae) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidvisitdate"));
							operationDateField.setText(sdf.format(operationDate.getTime()));
							return;
						}
					}// else

					// get operation result (can be null)
					if (operationResultRadioN.isSelected()) {
						admission.setOpResult("N");
					} else if (operationResultRadioP.isSelected()) {
						admission.setOpResult("P");
					} else {
						admission.setOpResult(null);
					}

					// get discharge type (it can be null)
					// if isDischarge, null value not allowed
					if (disTypeBox.getSelectedIndex() == 0) {
						if (isDischarge) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectavaliddischargetype"));
							return;
						} else {
							admission.setDisType(null);
						}
					} else {
						if (dateOut == null) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertadischargedate"));
							return;
						}
						if (isDischarge) {
							admission.setDisType(disTypeList.get(disTypeBox.getSelectedIndex() - 1).getCode());
						} else {
							admission.setDisType(null);
						}
					}

					// get the disease out n.1 (it can be null)
					// if isDischarge, null value not allowed
					if (admission.getDiseaseOutId1() == null) {
						if (isDischarge) {
							int yes = JOptionPane.showConfirmDialog(null, MessageBundle.getMessage("angal.admission.diagnosisoutsameasdiagnosisin"));
							if (yes == JOptionPane.YES_OPTION) {
								if (diseaseOutList.contains(diseaseInBox.getSelectedItem()))
									admission.setDiseaseOutId1(admission.getDiseaseInId());
								else {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectavaliddiagnosisout"));
									return;
								}
							} else {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseselectatleastfirstdiagnosisout"));
								return;
							}
						}
					} else {
						if (admission.getDisDate() == null) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertadischargedate"));
							return;
						}
					}

					// field notes
					if (textArea.getText().equals("")) {
						admission.setNote(null);
					} else {
						admission.setNote(textArea.getText());
					}

					// get transfusional unit (it can be null)
					try {
							float f = (Float) trsfUnitField.getValue();
							admission.setTransUnit(new Float(f));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidunitvalue"));
						return;
					}

					// fields for pregnancy status
					if (isPregnancy) {

						// get visit date (may be null)
						date = visitDateFieldCal.getDate();
						if (date != null) {
							visitDate.setTime(date);
						} else visitDate = null;
						if (visitDate == null) {
							admission.setVisitDate(null);
						} else {
							try {
								GregorianCalendar limit;
								if (admission.getDisDate() == null) {
									limit = today;
								} else {
									limit = admission.getDisDate();
								}

								if (visitDate.before(dateIn) || visitDate.after(limit)) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidvisitdate"));
									return;
								}

								admission.setVisitDate(visitDate);
							} catch (IllegalArgumentException iae) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidvisitdate"));
								visitDateField.setText(sdf.format(visitDate.getTime()));// CONTROLLARE
								return;
							}
						}// else

						// get weight (it can be null)
						try {
							if (weightField.getText().equals("")) {
								admission.setWeight(null);
							} else {
								float f = Float.parseFloat(weightField.getText());
								if (f < 0.0f) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidweightvalue"));
									return;
								} else {
									admission.setWeight(new Float(f));
								}
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidweightvalue"));
							return;
						}

						// get treatment type(may be null)
						if (treatmTypeBox.getSelectedIndex() == 0) {
							admission.setPregTreatmentType(null);
						} else {
							admission.setPregTreatmentType(treatmTypeList.get(treatmTypeBox.getSelectedIndex() - 1).getCode());

						}

						// get delivery date
						date = deliveryDateFieldCal.getDate();
						if (date != null) {
							deliveryDate.setTime(date);
						} else deliveryDate = null;
						if (deliveryDate == null) {
							admission.setDeliveryDate(null);
						} else {
							try {

								// date control
								GregorianCalendar start;
								if (admission.getVisitDate() == null) {
									start = admission.getAdmDate();
								} else {
									start = admission.getVisitDate();
								}

								GregorianCalendar limit;
								if (admission.getDisDate() == null) {
									limit = today;
								} else {
									limit = admission.getDisDate();
								}

								if (deliveryDate.before(start) || deliveryDate.after(limit)) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavaliddeliverydate"));
									deliveryDateFieldCal.setDate(null);
									return;
								}

								// updateDisplay
								admission.setDeliveryDate(deliveryDate);

							} catch (IllegalArgumentException iae) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavaliddeliverydate"));
								deliveryDateField.setText("");// CONTROLLARE
								return;
							}
						}

						// get delivery type
						if (deliveryTypeBox.getSelectedIndex() == 0) {
							admission.setDeliveryTypeId(null);
						} else {
							admission.setDeliveryTypeId(deliveryTypeList.get(deliveryTypeBox.getSelectedIndex() - 1).getCode());
						}

						// get delivery result type
						if (deliveryResultTypeBox.getSelectedIndex() == 0) {
							admission.setDeliveryResultId(null);
						} else {
							admission.setDeliveryResultId(deliveryResultTypeList.get(deliveryResultTypeBox.getSelectedIndex() - 1).getCode());
						}

						// get ctrl1 date
						date = ctrl1DateFieldCal.getDate();
						if (date != null) {
							ctrl1Date.setTime(date);
						} else ctrl1Date = null;
						if (ctrl1Date == null) {
							admission.setCtrlDate1(null);
						} else {
							try {
								// date control
								if (admission.getDeliveryDate() == null) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.controln1datenodeliverydatefound"));
									return;
								}
								GregorianCalendar limit;
								if (admission.getDisDate() == null) {
									limit = today;
								} else {
									limit = admission.getDisDate();
								}
								if (ctrl1Date.before(deliveryDate) || ctrl1Date.after(limit)) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidcontroln1date"));
									return;
								}

								// updateDisplay
								admission.setCtrlDate1(ctrl1Date);

							} catch (IllegalArgumentException iae) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidcontroln1date"));
								ctrl1DateFieldCal.setDate(null);
								return;
							}
						}

						// get ctrl2 date
						date = ctrl2DateFieldCal.getDate();
						if (date != null) {
							ctrl2Date.setTime(date);
						} else ctrl2Date = null;
						if (ctrl2Date == null) {
							admission.setCtrlDate2(null);
						} else {
							if (admission.getCtrlDate1() == null) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.controldaten2controldaten1notfound"));
								ctrl2DateFieldCal.setDate(null);
								return;
							}
							try {
								// date control
								GregorianCalendar limit;
								if (admission.getDisDate() == null) {
									limit = today;
								} else {
									limit = admission.getDisDate();
								}
								if (ctrl2Date.before(ctrl1Date) || ctrl2Date.after(limit)) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidcontroln2date"));
									return;
								}

								// updateDisplay
								admission.setCtrlDate2(ctrl2Date);

							} catch (IllegalArgumentException iae) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidcontroln2date"));
								ctrl2DateFieldCal.setDate(null);
								return;
							}
						}

						// get abort date
						date = abortDateFieldCal.getDate();
						if (date != null) {
							abortDate.setTime(date);
						} else abortDate = null;
						if (abortDate == null) {
							admission.setAbortDate(null);
						} else {
							try {
								// date control
								GregorianCalendar limit;
								if (admission.getDisDate() == null) {
									limit = today;
								} else {
									limit = admission.getDisDate();
								}
								if (ctrl2Date != null && abortDate.before(ctrl2Date) || ctrl1Date != null && abortDate.before(ctrl1Date) || abortDate.before(visitDate) || abortDate.after(limit)) {
									JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidabortdate"));
									return;
								}

								// updateDisplay
								admission.setAbortDate(abortDate);

							} catch (IllegalArgumentException iae) {
								JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.pleaseinsertavalidabortdate"));
								abortDateFieldCal.setDate(null);
								return;
							}
						}

					}// isPregnancy

					// set not editable fields
					String user = MainMenu.getUser();
					admission.setUserID(user);
					admission.setPatId(patient.getCode());

					if (admission.getDisDate() == null) {
						admission.setAdmitted(1);
					} else {
						admission.setAdmitted(0);
					}

					if (malnuCheck.isSelected()) {
						admission.setType("M");
					} else {
						admission.setType("N");
					}

					admission.setDeleted("N");

					// IOoperation result
					boolean result = false;

					// ready to save...
					if (!editing && !isDischarge) {
						int newKey = admMan.newAdmissionReturnKey(admission);
						if (newKey > 0) {
							result = true;
							admission.setId(newKey);
							fireAdmissionInserted(admission);
							if (GeneralData.XMPPMODULEENABLED) {
								CommunicationFrame frame= (CommunicationFrame)CommunicationFrame.getFrame();
								frame.sendMessage("new patient admission: "+patient.getName()+" in "+((Ward)wardBox.getSelectedItem()).getDescription(), (String)shareWith.getSelectedItem(), false);
							}
							dispose();
						}
					} else if (!editing && isDischarge) {
						result = admMan.newAdmission(admission);
						if (result) {
							fireAdmissionUpdated(admission);
							dispose();
						}
					} else {
						result = admMan.updateAdmission(admission);
						if (result) {
							fireAdmissionUpdated(admission);
							if (GeneralData.XMPPMODULEENABLED) {
								CommunicationFrame frame= (CommunicationFrame)CommunicationFrame.getFrame();
								frame.sendMessage("discharged patient: "+patient.getName()+" for "+((DischargeType)disTypeBox.getSelectedItem()).getDescription() , (String)shareWith.getSelectedItem(), false);
							}
							dispose();
						}
					}

					if (!result) {
						JOptionPane.showMessageDialog(AdmissionBrowser.this, MessageBundle.getMessage("angal.admission.thedatacouldnotbesaved"));
					} else {
						dispose();
					}
				}
			});
		}
		return saveButton;
	}

}// class
