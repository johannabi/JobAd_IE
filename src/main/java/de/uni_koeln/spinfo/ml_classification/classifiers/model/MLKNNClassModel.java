package de.uni_koeln.spinfo.ml_classification.classifiers.model;

import java.io.Serializable;

public class MLKNNClassModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String focusName;
	private double[] probKWhenFocus;
	private double[] probKWhenNoFocus;
	
	
	public double[] getProbKWhenFocus() {
		return probKWhenFocus;
	}

	public void setProbKWhenFocus(double[] probKWhenFocus) {
		this.probKWhenFocus = probKWhenFocus;
	}

	public double[] getProbKWhenNoFocus() {
		return probKWhenNoFocus;
	}

	public void setProbKWhenNoFocus(double[] probKWhenNoFocus) {
		this.probKWhenNoFocus = probKWhenNoFocus;
	}

	public String getFocusName() {
		return focusName;
	}
	
	public void setFocusName(String focusName) {
		this.focusName = focusName;
	}
	


}
