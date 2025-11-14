package org.tanzu.cfpulse.clone;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.tanzu.cfpulse.cf.CfBaseService;
import org.tanzu.cfpulse.cf.CloudFoundryOperationsFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Application cloning service that creates buildpack-specific placeholders to ensure consistent deployments.
 * 
 * This approach ensures buildpack consistency by:
 * 1. Detecting source app's buildpack and configuration
 * 2. Creating buildpack-specific placeholder content
 * 3. Deploying placeholder with source app's exact buildpack
 * 4. Copying source over placeholder to preserve buildpack
 * 5. Scaling and starting with verified buildpack matching
 */
@Service
public class CfApplicationCloner extends CfBaseService {

    private final ApplicationConfigService configService;
    private final BuildpackPlaceholderGenerator placeholderGenerator;
    private final ApplicationDeploymentService deploymentService;

    public CfApplicationCloner(CloudFoundryOperationsFactory operationsFactory,
                              ApplicationConfigService configService,
                              BuildpackPlaceholderGenerator placeholderGenerator,
                              ApplicationDeploymentService deploymentService) {
        super(operationsFactory);
        this.configService = configService;
        this.placeholderGenerator = placeholderGenerator;
        this.deploymentService = deploymentService;
    }

    /**
     * Clone an existing Cloud Foundry application by creating a buildpack-specific placeholder
     */
    @McpTool(description = "Clone an existing Cloud Foundry application to create a copy with a new name. Uses buildpack-specific placeholders to ensure consistent deployments.")
    public void cloneApp(
            @McpToolParam(description = "Source application name") String sourceApp,
            @McpToolParam(description = "Target application name") String targetApp,
            @McpToolParam(description = "Organization name (optional)", required = false) String organization,
            @McpToolParam(description = "Space name (optional)", required = false) String space) {
        
        System.out.println("Starting clone operation: " + sourceApp + " -> " + targetApp);
        
        try {
            // First get both source app config AND buildpack info
            Mono.zip(
                configService.getSourceAppConfig(sourceApp, organization, space),
                configService.getBuildpackInfo(sourceApp, organization, space)
            )
                    .flatMap(tuple -> {
                        ApplicationConfigService.AppConfig config = tuple.getT1();
                        String sourceBuildpack = tuple.getT2();
                        
                        System.out.println("Retrieved source app info: memory=" + config.memoryLimit() + 
                                         ", disk=" + config.diskQuota() + ", instances=" + config.instances() +
                                         ", buildpack=" + sourceBuildpack + 
                                         ", env vars=" + config.environmentVariables().size());
                        
                        return Mono.fromCallable(() -> placeholderGenerator.createPlaceholder(targetApp, sourceBuildpack))
                                .flatMap(placeholderPath -> {
                                    System.out.println("Created buildpack placeholder for: " + sourceBuildpack);
                                    
                                    return deploymentService.deployPlaceholderWithSourceBuildpack(targetApp, placeholderPath, sourceBuildpack, organization, space, config)
                                            .doOnSuccess(v -> System.out.println("Placeholder deployed with matching buildpack: " + sourceBuildpack))
                                            .then(
                                                    // Step 2: Copy application source (buildpack already matches)
                                                    deploymentService.copySourceWithBuildpackVerification(sourceApp, targetApp, sourceBuildpack, organization, space, config)
                                                            .doOnSuccess(v -> System.out.println("Source copy completed with buildpack verification"))
                                            )
                                            .doFinally(signal -> {
                                                System.out.println("Cleaning up temporary files...");
                                                deploymentService.cleanup(placeholderPath);
                                            });
                                });
                    })
                    .timeout(Duration.ofMinutes(10)) // 10 minute timeout for the entire operation
                    .block(); // Block to make it synchronous for MCP
            
            System.out.println("Clone operation completed successfully: " + sourceApp + " -> " + targetApp);
            
        } catch (Exception e) {
            System.err.println("Clone operation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to clone application: " + sourceApp + " -> " + targetApp, e);
        }
    }
}