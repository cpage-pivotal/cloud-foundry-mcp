package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CfOrganizationService extends CfBaseService {

    private static final String ORGANIZATION_LIST = "Return the organizations (orgs) in the Cloud Foundry foundation";
    private static final String ORGANIZATION_DETAILS = "Get detailed information about a specific Cloud Foundry organization";
    private static final String ORG_NAME_PARAM = "Name of the Cloud Foundry organization";

    public CfOrganizationService(CloudFoundryOperationsFactory operationsFactory) {
        super(operationsFactory);
    }

    @McpTool(description = ORGANIZATION_LIST)
    public List<OrganizationSummary> organizationsList() {
        return operationsFactory.getDefaultOperations().organizations().list().collectList().block();
    }

    @McpTool(description = ORGANIZATION_DETAILS)
    public OrganizationDetail organizationDetails(@McpToolParam(description = ORG_NAME_PARAM) String organizationName) {
        OrganizationInfoRequest request = OrganizationInfoRequest.builder()
                .name(organizationName)
                .build();
        return operationsFactory.getDefaultOperations().organizations().get(request).block();
    }
}