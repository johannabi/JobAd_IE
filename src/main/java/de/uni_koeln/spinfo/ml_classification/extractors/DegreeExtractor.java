package de.uni_koeln.spinfo.ml_classification.extractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;

public class DegreeExtractor extends AbstractExtractor{
	
	private Map<String, List<String>> degrees;
	
	public DegreeExtractor(Map<String, List<String>> degrees){
		this.degrees = degrees;
	}

	/**
	 * extracts the given degrees from the content of the given JobAd
	 */
	@Override
	public Map<String, Boolean> extract(FocusClassifyUnit toExtract) {
		
		Map<String, Boolean> toReturn = new HashMap<String, Boolean>();
		String content = toExtract.getContent();

		for (Map.Entry<String, List<String>> e : degrees.entrySet()){
			
			// degree
			String regex = e.getKey();
			Pattern pattern = Pattern.compile(regex.toLowerCase());
			Matcher matcher = pattern.matcher(content.toLowerCase());
			Boolean found = matcher.find();
			
			// keywords for degrees
			if(!found){
				for (String key : e.getValue()) {
					Pattern keyPat = Pattern.compile(key);
					Matcher keyMatch = keyPat.matcher(content.toLowerCase());
					if(keyMatch.find())
						found = true;
				}
			}
//			System.out.println(regex + found);
			toReturn.put(regex, found);
		}
		
		if(!toReturn.containsValue(true)){
			toReturn.put("Bachelor", true);
			toReturn.put("Master/Diplom", true);
			toReturn.put("Ausbildung", true);
		}
			
		
		return toReturn;
	}

}
