package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.*;
import org.cloudfoundry.operations.spaceadmin.GetSpaceQuotaRequest;
import org.cloudfoundry.operations.spaceadmin.SpaceQuota;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;

@Service
public class CfService {

    private final DefaultCloudFoundryOperations cloudFoundryOperations;

    public CfService(DefaultCloudFoundryOperations defaultCloudFoundryOperations) {
        this.cloudFoundryOperations = defaultCloudFoundryOperations;
    }

    /*
        Applications
    */
    private static final String APPLICATION_LIST = "Return the applications (apps) in my Cloud Foundry space";

    @Tool(description = APPLICATION_LIST)
    public List<ApplicationSummary> applicationsList() {
        return cloudFoundryOperations.applications().list().collectList().block();
    }

    private static final String APPLICATION_DETAILS = "Gets detailed information about a Cloud Foundry application";

    @Tool(description = APPLICATION_DETAILS)
    public ApplicationDetail applicationDetails(@ToolParam(description = NAME_PARAM) String applicationName) {
        GetApplicationRequest request = GetApplicationRequest.builder().name(applicationName).build();
        return cloudFoundryOperations.applications().get(request).block();
    }


    private static final String PUSH_APPLICATION = "Push an application JAR file to the Cloud Foundry space.";
    private static final String NAME_PARAM = "Name of the Cloud Foundry application";
    private static final String PATH_PARAM = "Fully qualified directory pathname to the compiled JAR file for the application";
    private static final String NO_START_PARAM = "Set this flag to true if you want to explicitly prevent the app from starting after being pushed.";

    @Tool(description = PUSH_APPLICATION)
    public void pushApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                @ToolParam(description = PATH_PARAM) String path,
                                @ToolParam(description = NO_START_PARAM, required = false) Boolean noStart,
                                @ToolParam(description = MEMORY_PARAM, required = false) Integer memory,
                                @ToolParam(description = DISK_PARAM, required = false) Integer disk) {
        PushApplicationRequest request = PushApplicationRequest.builder().
                name(applicationName).
                path(Paths.get(path)).
                noStart(true).
                buildpack("java_buildpack_offline").
                memory(memory).
                diskQuota(disk).
                build();
        cloudFoundryOperations.applications().push(request).block();

        SetEnvironmentVariableApplicationRequest envRequest = SetEnvironmentVariableApplicationRequest.builder().
                name(applicationName).variableName("JBP_CONFIG_OPEN_JDK_JRE").variableValue("{ jre: { version: 17.+ } }").
                build();
        cloudFoundryOperations.applications().setEnvironmentVariable(envRequest).block();

        if (noStart == null || !noStart) {
            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                    name(applicationName).
                    build();
            cloudFoundryOperations.applications().start(startApplicationRequest).block();
        }
    }

    private static final String SCALE_APPLICATION = "Scale the number of instances, memory, or disk size of an application. ";
    private static final String INSTANCES_PARAM = "The new number of instances of the Cloud Foundry application";
    private static final String MEMORY_PARAM = "The memory limit, in megabytes, of the Cloud Foundry application";
    private static final String DISK_PARAM = "The disk size, in megabytes, of the Cloud Foundry application";

    @Tool(description = SCALE_APPLICATION)
    public void scaleApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                 @ToolParam(description = INSTANCES_PARAM, required = false) Integer instances,
                                 @ToolParam(description = MEMORY_PARAM, required = false) Integer memory,
                                 @ToolParam(description = DISK_PARAM, required = false) Integer disk) {
        ScaleApplicationRequest scaleApplicationRequest = ScaleApplicationRequest.builder().
                name(applicationName).
                instances(instances).
                diskLimit(disk).
                memoryLimit(memory).
                build();
        cloudFoundryOperations.applications().scale(scaleApplicationRequest).block();
    }

    private static final String START_APPLICATION = "Start a Cloud Foundry application";

    @Tool(description = START_APPLICATION)
    public void startApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().start(startApplicationRequest).block();
    }

    private static final String STOP_APPLICATION = "Stop a running Cloud Foundry application";

    @Tool(description = STOP_APPLICATION)
    public void stopApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().stop(stopApplicationRequest).block();
    }

    private static final String RESTART_APPLICATION = "Restart a running Cloud Foundry application";

    @Tool(description = RESTART_APPLICATION)
    public void restartApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        RestartApplicationRequest request = RestartApplicationRequest.builder().name(applicationName).build();
        cloudFoundryOperations.applications().restart(request).block();
    }

    private static final String DELETE_APPLICATION = "Delete a Cloud Foundry application";

    @Tool(description = DELETE_APPLICATION)
    public void deleteApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        DeleteApplicationRequest deleteApplicationRequest = DeleteApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().delete(deleteApplicationRequest).block();
    }

    /*
        Organizations
     */
    private static final String ORGANIZATION_LIST = "Return the organizations (orgs) in my Cloud Foundry foundation";

    @Tool(description = ORGANIZATION_LIST)
    public List<OrganizationSummary> organizationsList() {
        return cloudFoundryOperations.organizations().list().collectList().block();
    }

    /*
        Services
     */
    private static final String SERVICE_INSTANCE_LIST = "Return the service instances (SIs) in my Cloud Foundry space";

    @Tool(description = SERVICE_INSTANCE_LIST)
    public List<ServiceInstanceSummary> serviceInstancesList() {
        return cloudFoundryOperations.services().listInstances().collectList().block();
    }

    private static final String SERVICE_INSTANCE_DETAIL = "Get detailed information about a service instance in my Cloud Foundry space";

    @Tool(description = SERVICE_INSTANCE_DETAIL)
    public ServiceInstance serviceInstanceDetails(@ToolParam(description = NAME_PARAM) String serviceInstanceName) {
        GetServiceInstanceRequest request = GetServiceInstanceRequest.builder().name(serviceInstanceName).build();
        return cloudFoundryOperations.services().getInstance(request).block();
    }

    private static final String SERVICE_OFFERINGS_LIST = "Return the service offerings available to me in the Cloud Foundry marketplace";

    @Tool(description = SERVICE_OFFERINGS_LIST)
    public List<ServiceOffering> serviceOfferingsList() {
        ListServiceOfferingsRequest request = ListServiceOfferingsRequest.builder().build();
        return cloudFoundryOperations.services().listServiceOfferings(request).collectList().block();
    }

    private static final String BIND_SERVICE_INSTANCE = "Bind a service instance to a Cloud Foundry application";
    private static final String SI_NAME_PARAM = "Name of the Cloud Foundry service instance";

    @Tool(description = BIND_SERVICE_INSTANCE)
    public void bindServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                    @ToolParam(description = NAME_PARAM) String applicationName) {
        BindServiceInstanceRequest request = BindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        cloudFoundryOperations.services().bind(request).block();
    }

    private static final String UNBIND_SERVICE_INSTANCE = "Unbind a service instance from a Cloud Foundry application";

    @Tool(description = UNBIND_SERVICE_INSTANCE)
    public void unbindServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                      @ToolParam(description = NAME_PARAM) String applicationName) {
        UnbindServiceInstanceRequest request = UnbindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        cloudFoundryOperations.services().unbind(request).block();
    }

    private static final String DELETE_SERVICE_INSTANCE = "Delete a Cloud Foundry service instance";

    @Tool(description = DELETE_SERVICE_INSTANCE)
    public void deleteServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName) {
        DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder().
                name(serviceInstanceName).
                build();
        cloudFoundryOperations.services().deleteInstance(request).block();
    }

    /*
        Spaces
     */
    private static final String SPACE_LIST = "Returns the spaces in my Cloud Foundry organization (org)";

    @Tool(description = SPACE_LIST)
    public List<SpaceSummary> spacesList() {
        return cloudFoundryOperations.spaces().list().collectList().block();
    }

    private static final String GET_SPACE_QUOTA = "Returns a quota (set of resource limits) scoped to a Cloud Foundry space";
    private static final String SPACE_QUOTA_NAME_PARAM = "Name of the Cloud Foundry space quota";

    @Tool(description = GET_SPACE_QUOTA)
    public SpaceQuota getSpaceQuota(@ToolParam(description = SPACE_QUOTA_NAME_PARAM) String spaceName) {
        GetSpaceQuotaRequest request = GetSpaceQuotaRequest.builder().name(spaceName).build();
        return cloudFoundryOperations.spaceAdmin().get(request).block();
    }
}
