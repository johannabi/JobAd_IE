package de.uni_koeln.spinfo.ml_classification.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

/**
 * represents a job ad
 * @author Johanna
 *
 */
public class FocusClassifyUnit extends ClassifyUnit implements Serializable{

	private static final long serialVersionUID = 1L;

	private Map<String, Boolean> inFocus = new HashMap<String, Boolean>();
	
	private Map<String, Boolean> studySubjects = new HashMap<String, Boolean>();
	
	private Map<String, Boolean> degrees = new HashMap<String, Boolean>();
	
	private Map<String, Boolean> extractedStudies = new HashMap<String, Boolean>();
	
	private Map<String, Boolean> extractedDegrees = new HashMap<String, Boolean>();
	
	private String title;
	
	private String contentHTML;
	
//	private Set<String> degrees;
	
	private List<FocusClassifyUnit> neighbors;
	
	private Map<String, Integer> neighborFocusCount;

	private Map<String, Double> ranking;

	

	public FocusClassifyUnit(String content, UUID id) {
		super(content, id);
	}
	
	public FocusClassifyUnit(String content, UUID id, String contentHTML) {
		super(content, id);
		this.contentHTML = contentHTML;
	}
	
	public FocusClassifyUnit(String content){
		super(content);
	}

	public FocusClassifyUnit(String title, String content, String contentHTML) {
		super(content);
		this.title = title;
		this.contentHTML = contentHTML;
	}

	public String getContentHTML(){
		return contentHTML;
	}
	
	/**
	 * contains focus names and Boolean if Unit belongs to focus
	 * @return
	 */
	public Map<String, Boolean> getInFocus() {
		return inFocus;
	}

	/**
	 * contains focus names and Boolean if Unit belongs to focus
	 * @param inFocus
	 */
	public void setInFocus(Map<String, Boolean> inFocus) {
		this.inFocus = inFocus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * List of k nearest Neighbors
	 * @return
	 */
	public List<FocusClassifyUnit> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<FocusClassifyUnit> neighbors) {
		this.neighbors = neighbors;
	}

	/**
	 * returns the number a focus occurs in the
	 * nearest neighbors
	 * @return
	 */
	public Map<String, Integer> getNeighborFocusCount() {
		return neighborFocusCount;
	}

	public void setNeighborFocusCount(Map<String, Integer> neighborFocusCount) {
		this.neighborFocusCount = neighborFocusCount;
	}

	public void setRanking(Map<String, Double> ranking) {
		this.ranking = ranking;
		
	}
	
	public Map<String, Double> getRanking(){
		return ranking;
	}

	public Map<String, Boolean> getStudySubjects() {
		return studySubjects;
	}

	public void setStudySubjects(Map<String, Boolean> studySubjects) {
		this.studySubjects = studySubjects;
	}

	public Map<String, Boolean> getDegrees() {
		return degrees;
	}

	public void setDegrees(Map<String, Boolean> degrees) {
		this.degrees = degrees;
	}

	public Map<String, Boolean> getExtractedStudies() {
		return extractedStudies;
	}

	public void setExtractedStudies(Map<String, Boolean> extractedStudies) {
		this.extractedStudies = extractedStudies;
	}
	
	public Map<String, Boolean> getExtractedDegrees() {
		return extractedDegrees;
	}

	public void setExtractedDegrees(Map<String, Boolean> extractedDegrees) {
		this.extractedDegrees = extractedDegrees;
	}
	
	

}
