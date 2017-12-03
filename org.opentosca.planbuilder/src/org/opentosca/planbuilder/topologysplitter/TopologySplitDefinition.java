package org.opentosca.planbuilder.topologysplitter;

public class TopologySplitDefinition {
	private String planName;
	private String scaleRegionNodes;
	private String scaleRegionRelations;
	private String scaleStratergy ;
	
	
	public String getPlanName() {
		return planName;
	}
	public void setPlanName(String planName) {
		this.planName = planName;
	}
	public String getScaleRegionNodes() {
		return scaleRegionNodes;
	}
	public void setScaleRegionNodes(String scaleRegion) {
		this.scaleRegionNodes = scaleRegion;
	}
	public String getScaleStratergy() {
		return scaleStratergy;
	}
	public void setScaleStratergy(String scaleStratergy) {
		this.scaleStratergy = scaleStratergy;
	}
	
	public String getScaleRegionRelations() {
		return scaleRegionRelations;
	}
	public void setScaleRegionRelations(String scaleRegionRelations) {
		this.scaleRegionRelations = scaleRegionRelations;
	}
	
	public String getFormattedScalingPlan() {
		return this.scaleRegionNodes+";"+this.scaleRegionRelations+";"+this.scaleStratergy;
	}
	

}
