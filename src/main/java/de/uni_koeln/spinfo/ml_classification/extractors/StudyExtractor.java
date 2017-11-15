package de.uni_koeln.spinfo.ml_classification.extractors;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;

public class StudyExtractor extends AbstractExtractor {

	private Map<String, List<String>> studySubjects;

	public StudyExtractor(Map<String, List<String>> studyKeywords) throws IOException {

		studySubjects = studyKeywords;
	}

	/**
	 * extracts the given study subjects from the content of the given JobAd
	 */
	@Override
	public Map<String, Boolean> extract(FocusClassifyUnit toExtract) {

		Map<String, Boolean> toReturn = new HashMap<String, Boolean>();

		String content = toExtract.getContent();
		String relevantContent = getStudyContent(content);
		

//		System.out.println(studySubjects);
		for (Map.Entry<String, List<String>> e : studySubjects.entrySet()) {

			// study subject
			String regex = e.getKey();
			Pattern pattern = Pattern.compile(regex.toLowerCase());
			Matcher matcher = pattern.matcher(content.toLowerCase());
			Boolean found = matcher.find();

			// keywords for study subject
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

		return toReturn;
	}

	private String getStudyContent(String content) {
		StringBuilder toReturn = new StringBuilder();
		String[] sentences = content.split("\n");
		for (int i = 0; i < sentences.length; i++) {
			//TODO regex
			if(sentences[i].toLowerCase().contains("studi")
					|| sentences[i].toLowerCase().contains("ausbildung"))
				toReturn.append(sentences[i]);
		}
		
		return toReturn.toString();
	}

}
