package org.isf.patvac.model;

/*------------------------------------------
* PatientVaccine - class 
* -----------------------------------------
* modification history
* 25/08/2011 - claudia - first beta version
*------------------------------------------*/

import java.util.GregorianCalendar;

import org.isf.vaccine.model.Vaccine;


public class PatientVaccine {
	private int code;
	private int progr;
	private GregorianCalendar vaccineDate;
	private int patId;
	private Vaccine vaccine;
	private int lock;
	private String patName;
	private int    patAge;
	private String patSex;

	public PatientVaccine(int codeIn, int progIn, GregorianCalendar vacDateIn, 
			               int patIdIn, Vaccine vacIn, int lockIn) {
		code = codeIn;
		progr = progIn;
		vaccineDate = vacDateIn;
		patId = patIdIn;
		vaccine = vacIn;
		lock = lockIn ;
	}
	
	public PatientVaccine(int codeIn, int progIn, GregorianCalendar vacDateIn, 
                          int patIdIn, Vaccine vacIn, int lockIn,
                          String patNameIn, int patAgeIn, String patSexIn) {
		code = codeIn;
		progr = progIn;
		vaccineDate = vacDateIn;
		patId = patIdIn;
		vaccine = vacIn;
		lock = lockIn ;
		patName = patNameIn;
		patAge = patAgeIn;
		patSex = patSexIn;
	}
	
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getProgr() {
		return progr;
	}

	public void setProgr(int progr) {
		this.progr = progr;
	}

	public GregorianCalendar getVaccineDate() {
		return vaccineDate;
	}

	public void setVaccineDate(GregorianCalendar vaccineDate) {
		this.vaccineDate = vaccineDate;
	}

	public int getPatId() {
		return patId;
	}

	public void setPatId(int patId) {
		this.patId = patId;
	}

	public Vaccine getVaccine() {
		return vaccine;
	}

	public void setVaccine(Vaccine vaccine) {
		this.vaccine = vaccine;
	}
	

	public int getLock() {
		return lock;
	}

	public void setLock(int lock) {
		this.lock = lock;
	}

	public String getPatName() {
		return patName;
	}

	public void setPatName(String patName) {
		this.patName = patName;
	}

	public int getPatAge() {
		return patAge;
	}

	public void setPatAge(int patAge) {
		this.patAge = patAge;
	}

	public String getPatSex() {
		return patSex;
	}

	public void setPatSex(String patSex) {
		this.patSex = patSex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PatientVaccine)) {
			return false;
		}
		PatientVaccine other = (PatientVaccine) obj;
		if (code != other.code) {
			return false;
		}
		if (patAge != other.patAge) {
			return false;
		}
		if (patId != other.patId) {
			return false;
		}
		if (patName == null) {
			if (other.patName != null) {
				return false;
			}
		} else if (!patName.equals(other.patName)) {
			return false;
		}
		if (patSex == null) {
			if (other.patSex != null) {
				return false;
			}
		} else if (!patSex.equals(other.patSex)) {
			return false;
		}
		if (progr != other.progr) {
			return false;
		}
		if (vaccine == null) {
			if (other.vaccine != null) {
				return false;
			}
		} else if (!vaccine.equals(other.vaccine)) {
			return false;
		}
		if (vaccineDate == null) {
			if (other.vaccineDate != null) {
				return false;
			}
		} else if (!vaccineDate.equals(other.vaccineDate)) {
			return false;
		}
		return true;
	}
}
