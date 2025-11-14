package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.services.*;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CfServiceInstanceService extends CfBaseService {

    private static final String SERVICE_INSTANCE_LIST = "Return the service instances (SIs) in a Cloud Foundry space. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    private static final String SERVICE_INSTANCE_DETAIL = "Get detailed information about a service instance in a Cloud Foundry space. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    private static final String SERVICE_OFFERINGS_LIST = "Return the service offerings available in the Cloud Foundry marketplace. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    private static final String BIND_SERVICE_INSTANCE = "Bind a service instance to a Cloud Foundry application. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    private static final String UNBIND_SERVICE_INSTANCE = "Unbind a service instance from a Cloud Foundry application. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    private static final String DELETE_SERVICE_INSTANCE = "Delete a Cloud Foundry service instance. Organization and space parameters are optional - if not provided or null, the configured default org/space will be used automatically.";
    
    private static final String SI_NAME_PARAM = "Name of the Cloud Foundry service instance";

    public CfServiceInstanceService(CloudFoundryOperationsFactory operationsFactory) {
        super(operationsFactory);
    }

    @McpTool(description = SERVICE_INSTANCE_LIST)
    public List<ServiceInstanceSummary> serviceInstancesList(
            @McpToolParam(description = ORG_PARAM, required = false) String organization,
            @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        return getOperations(organization, space).services().listInstances().collectList().block();
    }

    @McpTool(description = SERVICE_INSTANCE_DETAIL)
    public ServiceInstance serviceInstanceDetails(
            @McpToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
            @McpToolParam(description = ORG_PARAM, required = false) String organization,
            @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        GetServiceInstanceRequest request = GetServiceInstanceRequest.builder().name(serviceInstanceName).build();
        return getOperations(organization, space).services().getInstance(request).block();
    }

    @McpTool(description = SERVICE_OFFERINGS_LIST)
    public List<ServiceOffering> serviceOfferingsList(
            @McpToolParam(description = ORG_PARAM, required = false) String organization,
            @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        ListServiceOfferingsRequest request = ListServiceOfferingsRequest.builder().build();
        return getOperations(organization, space).services().listServiceOfferings(request).collectList().block();
    }

    @McpTool(description = BIND_SERVICE_INSTANCE)
    public void bindServiceInstance(@McpToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                    @McpToolParam(description = NAME_PARAM) String applicationName,
                                    @McpToolParam(description = ORG_PARAM, required = false) String organization,
                                    @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        BindServiceInstanceRequest request = BindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        getOperations(organization, space).services().bind(request).block();
    }

    @McpTool(description = UNBIND_SERVICE_INSTANCE)
    public void unbindServiceInstance(@McpToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                      @McpToolParam(description = NAME_PARAM) String applicationName,
                                      @McpToolParam(description = ORG_PARAM, required = false) String organization,
                                      @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        UnbindServiceInstanceRequest request = UnbindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        getOperations(organization, space).services().unbind(request).block();
    }

    @McpTool(description = DELETE_SERVICE_INSTANCE)
    public void deleteServiceInstance(@McpToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                     @McpToolParam(description = ORG_PARAM, required = false) String organization,
                                     @McpToolParam(description = SPACE_PARAM, required = false) String space) {
        DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder().
                name(serviceInstanceName).
                build();
        getOperations(organization, space).services().deleteInstance(request).block();
    }
}