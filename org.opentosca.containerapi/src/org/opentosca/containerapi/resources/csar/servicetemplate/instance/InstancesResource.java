package org.opentosca.containerapi.resources.csar.servicetemplate.instance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.opentosca.containerapi.osgi.servicegetter.CSARInstanceManagementHandler;
import org.opentosca.containerapi.osgi.servicegetter.IOpenToscaControlServiceHandler;
import org.opentosca.containerapi.osgi.servicegetter.ToscaServiceHandler;
import org.opentosca.containerapi.resources.utilities.JSONUtils;
import org.opentosca.containerapi.resources.utilities.ResourceConstants;
import org.opentosca.containerapi.resources.utilities.Utilities;
import org.opentosca.containerapi.resources.xlink.Reference;
import org.opentosca.containerapi.resources.xlink.References;
import org.opentosca.containerapi.resources.xlink.XLinkConstants;
import org.opentosca.core.model.csar.id.CSARID;
import org.opentosca.model.csarinstancemanagement.CSARInstanceID;
import org.opentosca.model.tosca.TBoolean;
import org.opentosca.model.tosca.extension.transportextension.TParameterDTO;
import org.opentosca.model.tosca.extension.transportextension.TPlanDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The representation lists the IDs of the CSAR-Instances for a CSARID.
 * 
 * Copyright 2013 Christian Endres
 * 
 * @author endrescn@fachschaft.informatik.uni-stuttgart.de
 * 
 */
@Path("Instances")
public class InstancesResource {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(InstancesResource.class);
	
	private final CSARID csarID;
	
	UriInfo uriInfo;
	
	
	public InstancesResource(CSARID csarID) {
		this.csarID = csarID;
		if (null == csarID) {
			InstancesResource.LOG.error("{} created: {}", this.getClass(), "but the CSAR does not exist");
		} else {
			InstancesResource.LOG.trace("{} created: {}", this.getClass(), csarID);
			InstancesResource.LOG.trace("CSAR Instance list for requested CSAR: {}", this.csarID.getFileName());
		}
	}
	
	/**
	 * Produces the xml which shows the CSAR instances.
	 * 
	 * @param uriInfo
	 * @return The response with the legal PublicPlanTypes.
	 */
	@GET
	@Produces(ResourceConstants.LINKED_XML)
	public Response getReferencesXML(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return Response.ok(getReferences().getXMLString()).build();
	}
	
	/**
	 * Produces the JSON which shows the CSAR instances.
	 * 
	 * @param uriInfo
	 * @return The response with the legal PublicPlanTypes.
	 */
	@GET
	@Produces(ResourceConstants.LINKED_JSON)
	public Response getReferencesJSON(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return Response.ok(getReferences().getJSONString()).build();
	}
	
	public References getReferences() {
		
		if (csarID == null) {
			InstancesResource.LOG.debug("The CSAR does not exist.");
			return null;
		}
		
		InstancesResource.LOG.debug("Return available instances for CSAR {}.", csarID);
		
		References refs = new References();
		
		if (null != CSARInstanceManagementHandler.csarInstanceManagement.getInstancesOfCSAR(csarID)) {
			for (CSARInstanceID id : CSARInstanceManagementHandler.csarInstanceManagement.getInstancesOfCSAR(csarID)) {
				refs.getReference().add(new Reference(Utilities.buildURI(uriInfo.getAbsolutePath().toString(), Integer.toString(id.getInstanceID())), XLinkConstants.SIMPLE, Integer.toString(id.getInstanceID())));
			}
		}
		
		InstancesResource.LOG.debug("Number of References in Root: {}", refs.getReference().size());
		
		// selflink
		refs.getReference().add(new Reference(uriInfo.getAbsolutePath().toString(), XLinkConstants.SIMPLE, XLinkConstants.SELF));
		return refs;
	}
	
	/**
	 * Returns the CSAR-Instance representation for the given ID.
	 * 
	 * @param instanceID
	 * @return the representation object
	 */
	@Path("{instanceID}")
	@Produces(ResourceConstants.LINKED_XML)
	public Object getInstance(@PathParam("instanceID") String instanceID) {
		return new InstanceResource(csarID, instanceID);
	}
	
	/**
	 * PUT for BUILD plans which have no CSAR-Instance-ID yet.
	 * 
	 * @param planElement the BUILD PublicPlan
	 * @return Response
	 */
	@POST
	@Consumes(ResourceConstants.TOSCA_XML)
	public Response postManagementPlan(JAXBElement<TPlanDTO> planElement) {
		
		InstancesResource.LOG.debug("Received a build plan for CSAR " + csarID);
		
		TPlanDTO plan = planElement.getValue();
		
		if (null == plan) {
			LOG.error("The given PublicPlan is null!");
			return Response.status(Status.CONFLICT).build();
		}
		
		if (null == plan.getId()) {
			LOG.error("The given PublicPlan has no ID!");
			return Response.status(Status.CONFLICT).build();
		}
		
		// if (null == plan.getId() || plan.getId().isEmpty()) {
		//
		// String id = plan.getId();
		// QName qname = new QName(id.substring(1, id.indexOf("}")),
		// id.substring(id.indexOf("}") + 1, id.length()));
		// plan.setPlanID(qname);
		// }
		
		String namespace = ToscaServiceHandler.getToscaEngineService().getToscaReferenceMapper().getNamespaceOfPlan(csarID, plan.getId().getLocalPart());
		plan.setId(new QName(namespace, plan.getId().getLocalPart()));
		
		LOG.debug("PublicPlan to invoke: " + plan.getId());
		
		InstancesResource.LOG.debug("Post of the PublicPlan " + plan.getId());
		
		// TODO return correlation ID
		String correlationID = IOpenToscaControlServiceHandler.getOpenToscaControlService().invokePlanInvocation(csarID, -1, plan);
		
		return Response.ok(correlationID).build();
		
	}
	
	/**
	 * PUT for BUILD plans which have no CSAR-Instance-ID yet.
	 * 
	 * @param planElement the BUILD PublicPlan
	 * @return Response
	 * @throws URISyntaxException 
	 */
	@POST
	@Consumes(ResourceConstants.TEXT_PLAIN)
	@Produces(ResourceConstants.APPLICATION_JSON)
	public Response postBUILDJSONReturnJSON(@Context UriInfo uriInfo, String json) throws URISyntaxException {
		String planURL = postManagementPlanJSON(uriInfo, json);
		JsonObject ret = new JsonObject();
		ret.addProperty("PlanURL", planURL);
		return Response.created(new URI(planURL)).entity(ret.toString()).build();
	}
	
	/**
	 * PUT for BUILD plans which have no CSAR-Instance-ID yet.
	 * 
	 * @param planElement the BUILD PublicPlan
	 * @return Response
	 * @throws URISyntaxException 
	 */
	@POST
	@Consumes(ResourceConstants.TEXT_PLAIN)
	@Produces(ResourceConstants.TOSCA_XML)
	public Response postBUILDJSONReturnXML(@Context UriInfo uriInfo, String json) throws URISyntaxException {
		
		String url = postManagementPlanJSON(uriInfo, json);
		
		return Response.created(new URI(url)).build();
	}
	
	private String postManagementPlanJSON(UriInfo uriInfo, String json) {
		
		InstancesResource.LOG.debug("Received a build plan for CSAR " + csarID + "\npassed entity:\n   " + json);
		
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(json).getAsJsonObject();
		
		LOG.trace(JSONUtils.withoutQuotationMarks(object.get("ID").toString()));
		
		// Example of JSON:
		// {
		// "ID":"BuildPlanNoImpl.war",
		// "Name":"BuildPlanNoImpl.war",
		// "PlanType":"http://docs.oasis-open.org/tosca/ns/2011/12/PlanTypes/BuildPlan",
		// "PlanLanguage":"http://www.omg.org/spec/BPMN/2.0/",
		// "InputParameters":[
		// {"InputParameter":{"Name":"HypervisorEndpoint","Type":"String","Value":"HypervisorEndpoint","Required":"yes"}},
		// {"InputParameter":{"Name":"HypervisorTenantID","Type":"String","Value":"HypervisorTenantID","Required":"yes"}}
		// ],
		// "OutputParameters":[
		// {"OutputParameter":{"Name":"CorrelationID","Type":"correlation","Required":"yes"}}
		// ],
		// "PlanModelReference":{"Reference":"../servicetemplates/http%253A%252F%252Fopentosca.org%252FBPMN/BPMNLAMPStack/plans/BPMNLAMPStack_buildPlan/BuildPlanNoImpl.war"}
		// }
		
		TPlanDTO plan = new TPlanDTO();
		
		plan.setId(new QName(JSONUtils.withoutQuotationMarks(object.get("ID").toString())));
		plan.setName(JSONUtils.withoutQuotationMarks(object.get("Name").toString()));
		plan.setPlanType(JSONUtils.withoutQuotationMarks(object.get("PlanType").toString()));
		plan.setPlanLanguage(JSONUtils.withoutQuotationMarks(object.get("PlanLanguage").toString()));
		
		JsonArray array = object.get("InputParameters").getAsJsonArray();
		Iterator<JsonElement> iterator = array.iterator();
		while (iterator.hasNext()) {
			TParameterDTO para = new TParameterDTO();
			JsonObject tmp = iterator.next().getAsJsonObject();
			para.setName(JSONUtils.withoutQuotationMarks(tmp.get("InputParameter").getAsJsonObject().get("Name").toString()));
			para.setRequired(TBoolean.fromValue(JSONUtils.withoutQuotationMarks(tmp.get("InputParameter").getAsJsonObject().get("Required").toString())));
			para.setType(JSONUtils.withoutQuotationMarks(tmp.get("InputParameter").getAsJsonObject().get("Type").toString()));
			// if a parameter value is not set, just add "" as value
			if (null != tmp.get("InputParameter").getAsJsonObject().get("Value")) {
				para.setValue(JSONUtils.withoutQuotationMarks(tmp.get("InputParameter").getAsJsonObject().get("Value").toString()));
			} else {
				para.setValue("");
			}
			plan.getInputParameters().getInputParameter().add(para);
		}
		array = object.get("OutputParameters").getAsJsonArray();
		iterator = array.iterator();
		while (iterator.hasNext()) {
			TParameterDTO para = new TParameterDTO();
			JsonObject tmp = iterator.next().getAsJsonObject();
			para.setName(JSONUtils.withoutQuotationMarks(tmp.get("OutputParameter").getAsJsonObject().get("Name").toString()));
			para.setRequired(TBoolean.fromValue(JSONUtils.withoutQuotationMarks(tmp.get("OutputParameter").getAsJsonObject().get("Required").toString())));
			para.setType(JSONUtils.withoutQuotationMarks(tmp.get("OutputParameter").getAsJsonObject().get("Type").toString()));
			plan.getOutputParameters().getOutputParameter().add(para);
		}
		
		String namespace = ToscaServiceHandler.getToscaEngineService().getToscaReferenceMapper().getNamespaceOfPlan(csarID, plan.getId().getLocalPart());
		plan.setId(new QName(namespace, plan.getId().getLocalPart()));
		
		LOG.debug("Plan to invoke: " + plan.getId());
		
		InstancesResource.LOG.debug("Post of the PublicPlan " + plan.getId());
		
		String correlationID = IOpenToscaControlServiceHandler.getOpenToscaControlService().invokePlanInvocation(csarID, -1, plan);
		int csarInstanceID = IOpenToscaControlServiceHandler.getOpenToscaControlService().getCSARInstanceIDForCorrelationID(correlationID);
		LOG.debug("Return correlation ID of running plan: " + correlationID + " for csar instance " + csarInstanceID);
		
		String url = uriInfo.getBaseUri().toString() + "CSARs/" + csarID.getFileName() + "/Instances/" + csarInstanceID + "/PlanInstances/" + correlationID;
		
		return url;
		
	}
}