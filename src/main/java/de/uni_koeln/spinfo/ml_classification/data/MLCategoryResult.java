package de.uni_koeln.spinfo.ml_classification.data;

import de.uni_koeln.spinfo.classification.zoneAnalysis.data.CategoryResult;

public class MLCategoryResult extends CategoryResult{

	private static final long serialVersionUID = 1L;
	private String label;
	
	public MLCategoryResult(int categoryID) {
		super(categoryID);
	}
	
	public MLCategoryResult(int categoryID, String label){
		super(categoryID);
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("result of " + label +": \n   recall: "+ getRecall()+"\n   precision: " + getPrecision()+ "\n   accuracy: "+ getAccuracy()+"\n   F1Score: "+ getF1Score()+ "\n\n");
		return sb.toString();
	}

}
