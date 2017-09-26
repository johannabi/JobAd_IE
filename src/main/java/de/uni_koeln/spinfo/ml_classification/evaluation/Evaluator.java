package de.uni_koeln.spinfo.ml_classification.evaluation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

public class Evaluator {
	
	
//	public static void evaluate(List<ClassifyUnit> classified){
//		
//		int truePosKey = 0;
//		int trueNegKey = 0;
//		int falsePosKey = 0;
//		int falseNegKey = 0;
//		
//		int truePosClassifier = 0;
//		int trueNegClassifier = 0;
//		int falsePosClassifier = 0;
//		int falseNegClassifier = 0;
//		
//		int truePosCombined = 0;
//		int trueNegCombined = 0;
//		int falsePosCombined = 0;
//		int falseNegCombined = 0;
//		
//		for (ClassifyUnit classifyUnit : classified) {
//			FocusClassifyUnit fcu = (FocusClassifyUnit) classifyUnit;
//			
//			Map<String, Boolean> goldStandard = fcu.getInFocus();
//			Map<String, Boolean> keyFocus = fcu.getKeyWordFocus();
//			Map<String, Boolean> classifierFocus = fcu.getClassifiedFocus();
//			Map<String, Boolean> combinedFocus = fcu.getCombinedFocus();
//			
//			//iterates over gold standard
//			for(Map.Entry<String, Boolean> gs : goldStandard.entrySet()){
//				Boolean gsBoolean = gs.getValue();
//				Boolean kBoolean = keyFocus.get(gs.getKey());
//				Boolean cBoolean = classifierFocus.get(gs.getKey());
//				Boolean combiBoolean = combinedFocus.get(gs.getKey());
//				
//				
//				// if Focus is tagged in gold standard
//				if(gsBoolean){
//					if(kBoolean)
//						truePosKey++;  // gs && key
//					else
//						falseNegKey++; // gs && !key
//					if(cBoolean)
//						truePosClassifier++; // gs && classifier
//					else
//						falseNegClassifier++; // gs && !classifier
//					if(combiBoolean)
//						truePosCombined++; // gs && combined
//					else
//						falseNegCombined++; // gs && !combined
//				} 
//				// if Focus isn't tagged in gold standard
//				else{
//					if(kBoolean) 
//						falsePosKey++;  //!gs && key
//					else
//						trueNegKey++; // !gs && !key
//					if(cBoolean)
//						falsePosClassifier++; // !gs && classifier
//					else
//						trueNegClassifier++; // !gs && !classifier
//					if(combiBoolean)
//						falsePosCombined++; // !gs && combined
//					else
//						trueNegCombined++; // !gs && !combined
//					
//				}
//			}
//		}
//		
//		System.out.println("EVALUATION.... ");
//		
//		System.out.println("Keyword Classifier");
//		System.out.println("True Positives: " + truePosKey);
//		System.out.println("True Negatives: " + trueNegKey);
//		System.out.println("False Positives: " + falsePosKey);
//		System.out.println("False Negatives: " + falseNegKey);
//		System.out.println("Precision: " + (double)(truePosKey/(double)(truePosKey + falsePosKey)));
//		System.out.println("Recall: " + (double)(truePosKey/(double)(truePosKey + falseNegKey)));
//		System.out.println("----------------");
//		System.out.println("Classifier");
//		System.out.println("True Positives: " + truePosClassifier);
//		System.out.println("True Negatives: " + trueNegClassifier);
//		System.out.println("False Positives: " + falsePosClassifier);
//		System.out.println("False Negatives: " + falseNegClassifier);
//		System.out.println("Precision: " + (double)(truePosClassifier/(double)(truePosClassifier + falsePosClassifier)));
//		System.out.println("Recall: " + (double)(truePosClassifier/(double)(truePosClassifier + falseNegClassifier)));
//		System.out.println("----------------");
//		System.out.println("Combined");
//		System.out.println("True Positives: " + truePosCombined);
//		System.out.println("True Negatives: " + trueNegCombined);
//		System.out.println("False Positives: " + falsePosCombined);
//		System.out.println("False Negatives: " + falseNegCombined);
//		System.out.println("Precision: " + (double)(truePosCombined/(double)(truePosCombined + falsePosCombined)));
//		System.out.println("Recall: " + (double)(truePosCombined/(double)(truePosCombined + falseNegCombined)));
//	}

	public static Map<Map<String, Boolean>, Integer> analyzeCombinations(List<ClassifyUnit> annotatedData, int i) throws IOException {

		Map<Map<String, Boolean>, Integer> combiCount = new HashMap<Map<String,Boolean>, Integer>();
		for (ClassifyUnit cu : annotatedData){

			Map<String, Boolean> focuses = ((FocusClassifyUnit) cu).getInFocus();

			// count this specific set
			Integer count = 0;
			if(combiCount.containsKey(focuses))
				count = combiCount.get(focuses);
			combiCount.put(focuses, (count + 1));
		}
		
		FileOutputStream fos = new FileOutputStream("focus_combination_run" + i + ".xlsx");
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("combinations");
		
		int row = 0;
		int cell = 0;
		XSSFRow r = sheet.createRow(row);
		XSSFCell c;
		List<Map<String,Boolean>> focuses = new ArrayList<Map<String,Boolean>>(combiCount.keySet());
		Set<String> focusSet = focuses.get(0).keySet();
		for (String string : focusSet) {
			c = r.createCell(cell);
			c.setCellValue(string);
			cell++;
		}
		
		row++;
		
		for(Map.Entry<Map<String,Boolean>, Integer> e : combiCount.entrySet()){
			r = sheet.createRow(row);
			
			cell = 0;
			String bool;
			for(Map.Entry<String, Boolean> f : e.getKey().entrySet()){
				if(f.getValue())
					bool = "1";
				else
					bool = "0";
				c = r.createCell(cell);
				c.setCellValue(bool);
				cell++;
			}	
			c = r.createCell(cell);
			c.setCellValue(e.getValue());
			row++;
		}
			
		wb.write(fos);
		fos.close();
		wb.close();
		
		
		return combiCount;
	}

}
