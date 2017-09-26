package de.uni_koeln.spinfo.ml_classification.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

public class FocusClassifyUnit extends ClassifyUnit implements Serializable{

	private static final long serialVersionUID = 1L;

	private Map<String, Boolean> inFocus, combinedFocus, keyWordFocus, classifiedFocus = new HashMap<String, Boolean>();
	
	private String title;
	
	private String contentHTML;
	
	private Set<String> studySubjects, degrees;
	
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

	public Map<String, Boolean> getKeyWordFocus() {
		return keyWordFocus;
	}

	public void setKeyWordFocus(Map<String, Boolean> keyWordFocus) {
		this.keyWordFocus = keyWordFocus;
	}

	public Map<String, Boolean> getClassifiedFocus() {
		return classifiedFocus;
	}

	public void setClassifiedFocus(Map<String, Boolean> classifiedFocus) {
		this.classifiedFocus = classifiedFocus;
	}

	public Map<String, Boolean> getCombinedFocus() {
		return combinedFocus;
	}

	public void setCombinedFocus(Map<String, Boolean> combinedFocus) {
		this.combinedFocus = combinedFocus;
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

	public Set<String> getStudySubjects() {
		return studySubjects;
	}

	public void setStudySubjects(Set<String> studySubjects) {
		this.studySubjects = studySubjects;
	}

	public Set<String> getDegrees() {
		return degrees;
	}

	public void setDegrees(Set<String> degrees) {
		this.degrees = degrees;
	}
	
	

}
