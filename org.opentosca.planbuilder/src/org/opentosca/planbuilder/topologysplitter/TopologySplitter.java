package org.opentosca.planbuilder.topologysplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opentosca.planbuilder.model.tosca.AbstractDefinitions;
import org.opentosca.planbuilder.model.tosca.AbstractInterface;
import org.opentosca.planbuilder.model.tosca.AbstractNodeTemplate;
import org.opentosca.planbuilder.model.tosca.AbstractOperation;
import org.opentosca.planbuilder.model.tosca.AbstractParameter;
import org.opentosca.planbuilder.model.tosca.AbstractRelationshipTemplate;
import org.opentosca.planbuilder.model.tosca.AbstractServiceTemplate;
import org.opentosca.planbuilder.model.tosca.AbstractTopologyTemplate;
import org.opentosca.planbuilder.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Anshuman
 *
 */
public class TopologySplitter extends AbstractTopologySplitter {
	final static Logger LOG = LoggerFactory.getLogger(TopologySplitter.class);

	@Override
	public List<TopologySplitDefinition> splitTopology(String csarName, AbstractDefinitions definitions) {
		TopologySplitter.LOG.debug("Elvis has entered the building!");
		List<TopologySplitDefinition> splitDefinitions = null;
		//Get the Service Template from the CSAR Content
		for (AbstractServiceTemplate serviceTemplate : definitions.getServiceTemplates()) {
			
			//If we get even one Split Scaling Plan means we can skip the whole process
			if(this.topologyIsAlreadySplit(serviceTemplate)){
				return null;
			}
			
			//Get the Tags from the Service Template
			Map<String, String> tags = serviceTemplate.getTags();
			
			//Get Splits automatically from the Topology
			splitDefinitions = 
					this.getSplitDefinitions(serviceTemplate.getTopologyTemplate());
			
			//Serious hack. We only consider the first Service Template.
			break;
		}
		
		return splitDefinitions;
		
			/*for(TopologySplitDefinition split: splitDefinitions) {
				if(tags.containsKey("scalingplans")) {
					tags.put(
							"scalingplans", 
							tags.get("scalingplans").concat(","+split.getPlanName())
							);
				}
				else {
					tags.put("scalingplans",split.getPlanName());
				}
				
				//Add the Split Plans Tags
				tags.put(split.getPlanName(), split.getFormattedScalingPlan());
			}
			
			//TODO Put the Tags back into the Service Template
	
		}
		return null;*/
	}
	
	private List<TopologySplitDefinition> getSplitDefinitions(AbstractTopologyTemplate topology) {
		
		List<TopologySplitDefinition> topologySplitDefinitions = 
				new ArrayList<TopologySplitDefinition>();			
		List<AbstractNodeTemplate> orderedNodeTemplateList = 
				new ArrayList<AbstractNodeTemplate>();
		
		//Get Ordered Node Template Sequence from Top to Bottom
		for(AbstractNodeTemplate nodeTemplate:topology.getNodeTemplates()) {			
			//TODO:: Handle multiple parallel branches
			if(nodeTemplate.getIngoingRelations().size() == 0) {
				getOrderedNodeTemplateList(
						nodeTemplate,
						orderedNodeTemplateList
						);
				break;
			}			
		}
		
		//Now that we have the Node Template Sequence we begin 
		//iterating recursively to mark regions for splits.
		int topologyDepth = orderedNodeTemplateList.size();
		for(int i = 0; i < topologyDepth; i++) {
			List<AbstractNodeTemplate> provisioningDependencyList = 
					new ArrayList<AbstractNodeTemplate>();
			
			for(int j = i+1; j < topologyDepth; j++) {
				if(this.checkProvisioningDependency(
						orderedNodeTemplateList.get(i),
						orderedNodeTemplateList.get(j))) {
					provisioningDependencyList.add(orderedNodeTemplateList.get(j));
				}
			}
			
			if(provisioningDependencyList.size() == 0) {
				continue;
			}
			
			//TODO Fix if length of Provisioning Dependency List is > 1
			topologySplitDefinitions.add(
					this.generateTopologySplitDefinition(
							orderedNodeTemplateList.get(i),
							provisioningDependencyList.get(0)
							)
					);
		}
		
		//Handle the Leaf Node
		topologySplitDefinitions.add(
				this.generateTopologySplitDefinition(
						orderedNodeTemplateList.get(topologyDepth-1),
						null
						)
				);
		
		return topologySplitDefinitions;
	}

	private TopologySplitDefinition generateTopologySplitDefinition(AbstractNodeTemplate sourceNodeTemplate,
			AbstractNodeTemplate targetNodeTemplate) {
		//Generate a split Definition
		TopologySplitDefinition splitDef = new TopologySplitDefinition();
		if(targetNodeTemplate != null) {
			String splitPlanName = "split_scaleout_" + sourceNodeTemplate.getName();
			
			splitDef.setPlanName(splitPlanName);
			splitDef.setScaleRegionNodes(sourceNodeTemplate.getName());
			splitDef.setScaleRegionRelations(sourceNodeTemplate.getOutgoingRelations().get(0).getName());
			splitDef.setScaleStratergy("UserProvided["+targetNodeTemplate.getName()+"]");
		}
		else //Leaf Node
		{
			String splitPlanName = "split_scaleout_" + sourceNodeTemplate.getName(); 
			
			splitDef.setPlanName(splitPlanName);
			splitDef.setScaleRegionNodes(sourceNodeTemplate.getName());
			splitDef.setScaleRegionRelations("");
			splitDef.setScaleStratergy("");
		}
		
		
		return splitDef;
	}

	private boolean checkProvisioningDependency(AbstractNodeTemplate sourceNodeTemplate,
			AbstractNodeTemplate targetNodeTemplate) {
		
		if(this.checkIaDaDependency(sourceNodeTemplate,targetNodeTemplate)) {
			return true;
		}
		
		return false;
	}

	private boolean checkIaDaDependency(AbstractNodeTemplate sourceNodeTemplate,
			AbstractNodeTemplate targetNodeTemplate) {
		//Get the Winery Properties of the Source Node Template that 
		//need to be dynamically assigned for Provisioning
		List<String> sourceNodeProperties = 
				this.getWineryProperties(sourceNodeTemplate);
		
		//If the current Node has Lifecycle Interface then, 
		//first check input params with Output of Lifecycle Operations of Source Node 
		for(AbstractInterface lifeCycleInterface:sourceNodeTemplate.getType().getInterfaces()) {
			if(lifeCycleInterface.getName().equals("http://www.example.com/interfaces/lifecycle")) {
				for(AbstractOperation op:lifeCycleInterface.getOperations()) {
					List<AbstractParameter> outParams = op.getOutputParameters();
					if(this.hasPropertyDependency(sourceNodeProperties,outParams)) {
						return true;
					}
				}
			}
		}
		
		//If Current Node does not have Lifecycle Interface then,
		//Check with output params of non-lifecycle Interfaces of Target Node
		for(AbstractInterface nonLifecycleInterface:targetNodeTemplate.getType().getInterfaces()) {
			if(!nonLifecycleInterface.getName().equals("http://www.example.com/interfaces/lifecycle")) {
				for(AbstractOperation op:nonLifecycleInterface.getOperations()) {
					List<AbstractParameter> outParams = op.getOutputParameters();
					if(this.hasPropertyDependency(sourceNodeProperties,outParams)) {
						return true;
					}
				}
			}
		}
				
		return false;
	}

	private boolean hasPropertyDependency(List<String> inputNodeProps, List<AbstractParameter> outParams) {
		Iterator<AbstractParameter> iter = outParams.iterator();
		while(iter.hasNext()) {
			AbstractParameter param = (AbstractParameter)iter.next();
			if(inputNodeProps.contains(param.getName())) {
				return true;
			}
		}
		return false;
	}

	private List<String> getWineryProperties(AbstractNodeTemplate nodeTemplate) {

		List<String> nodePropertiesList = new ArrayList<String>();
		NodeList wineryPropertiesList = 
				nodeTemplate.getProperties().getDOMElement().getChildNodes();
		
		for(int i = 0; i < wineryPropertiesList.getLength(); i++) {
			Node wineryProperty = wineryPropertiesList.item(i);
			if(wineryProperty.getTextContent().isEmpty()) {
				nodePropertiesList.add(wineryProperty.getLocalName());
			}
		}
		
		return nodePropertiesList;
	}

	private void getOrderedNodeTemplateList(final AbstractNodeTemplate nodeTemplate,
			final List<AbstractNodeTemplate> orderedNodeTemplateList) {
		
		//Add the nodes to the list recursively
		orderedNodeTemplateList.add(nodeTemplate);
		
		//Terminate recursion if we reach the leaf node
		if(nodeTemplate.getOutgoingRelations().size() == 0) {
			return;
		}
		
		for (final AbstractRelationshipTemplate relation : nodeTemplate.getOutgoingRelations()) {
			TopologySplitter.LOG.debug("Checking if relation is infrastructure edge, relation: " + relation.getId());
			if (Utils.getRelationshipBaseType(relation).equals(Utils.TOSCABASETYPE_DEPENDSON)
					|| Utils.getRelationshipBaseType(relation).equals(Utils.TOSCABASETYPE_HOSTEDON)
					|| Utils.getRelationshipBaseType(relation).equals(Utils.TOSCABASETYPE_DEPLOYEDON)) {
				this.getOrderedNodeTemplateList(relation.getTarget(),orderedNodeTemplateList);
			}
		}
	}

	private boolean topologyIsAlreadySplit(AbstractServiceTemplate serviceTemplate) {
		
		Map<String, String> tags = serviceTemplate.getTags();
		
		// fetch scaling plan names
		if(tags.size() != 0 && tags.containsKey("scalingplans")) {
			String scalingPlanNamesRawValue = tags.get("scalingplans").trim();
			
			List<String> scalingPlanNamesRaw = this.getElementsFromCSV(scalingPlanNamesRawValue);
			
			for (String scalingPlanName : scalingPlanNamesRaw) {
				if(scalingPlanName.toLowerCase().contains("split_"))
					return true;
			}
		}
		
		return false;
	}
	
	private List<String> getElementsFromCSV(String csvString) {
		csvString = this.cleanCSVString(csvString);
		String[] scalingPlanRelationNamesRawSplit = csvString.split(",");
		return Arrays.asList(scalingPlanRelationNamesRawSplit);
	}
	
	private String cleanCSVString(String commaSeperatedList) {
		while (commaSeperatedList.endsWith(";") | commaSeperatedList.endsWith(",")) {
			commaSeperatedList = commaSeperatedList.substring(0, commaSeperatedList.length() - 2);
		}
		return commaSeperatedList;
	}

}
