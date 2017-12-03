package org.opentosca.planbuilder.topologysplitter;

import java.util.List;

import org.opentosca.planbuilder.model.tosca.AbstractDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Anshuman Dash
 *
 */
public abstract class AbstractTopologySplitter {
	private final static Logger LOG = LoggerFactory.getLogger(AbstractTopologySplitter.class);
	
	/**
	 * 
	 * @param csarName
	 * @param definitions
	 * @return
	 */
	abstract public List<TopologySplitDefinition> splitTopology(String csarName, AbstractDefinitions definitions);

}

