# Cloud Foundry MCP Server

This MCP Server provides an LLM interface for interacting with your Cloud Foundry foundation. It was built with the [Spring AI MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html).

![Sample](images/sample.png)

### IMPORTANT
This MCP Server now uses the Streamable HTTP Transport, instead of SSE. If you are connecting to this server with Tanzu Platform Chat, be sure to consult the [README](https://github.com/cpage-pivotal/cf-mcp-client) for instructions on configuring the service binding for Streamable transport.

## Building the Server

```bash
./mvnw clean package
```

### Deploying to Cloud Foundry with Variables File

When deploying the MCP server to Cloud Foundry, use a variables file to inject credentials. This approach keeps sensitive credentials out of your manifest files and version control.

#### Create a Variables File

Create a file named `vars.yaml` with your Cloud Foundry credentials:

```yaml
CF_APIHOST: api.sys.mycf.com
CF_USERNAME: your-cf-username
CF_PASSWORD: your-cf-password
CF_ORG: your-org
CF_SPACE: your-space
```

> **IMPORTANT:** The `vars.yaml` file contains sensitive credentials and should **never** be committed to Git. Add it to your `.gitignore` file:
> ```bash
> echo "vars.yaml" >> .gitignore
> ```

#### Deploy the Application

Deploy using the `--vars-file` flag:

```bash
cf push --vars-file=vars.yaml
```

The `manifest.yml` references these variables using the `((variable-name))` syntax, which injects them as environment variables at deploy time.

### Publishing to Tanzu Service Marketplace

You can publish this MCP server as a service in the Tanzu Platform marketplace using [Tanzu Service Publisher](https://techdocs.broadcom.com/us/en/vmware-tanzu/platform/service-publisher/10-3/srvc-pub/app-publishing.html). This allows other applications to bind to the MCP server and consume it as a service.

#### Prerequisites

Before publishing, ensure:
- You have space developer privileges in the app's space
- The MCP server application is running
- A route is mapped to the app on the `apps.internal` domain using HTTP protocol

#### Service Definition

A `service.yaml` file is included in the root of this repository with the service configuration for publishing.

#### Publish the Service

Use the Tanzu cf CLI to publish the app:

```bash
cf publish-service cloud-foundry-mcp-server -f service.yaml
```

Check the publishing status:

```bash
cf published-service cloud-foundry-mcp-server
```

Wait until the status shows `successful`.

#### Enable Service Access

By default, new service offerings are disabled. An admin must enable access for the service to appear in the marketplace:

```bash
cf enable-service-access cloud-foundry-mcp
```

#### Create Service Instances

Once enabled, developers can create service instances:

```bash
cf create-service cloud-foundry-mcp default my-mcp-service
```

And bind them to applications:

```bash
cf bind-service my-app my-mcp-service
```

## Capabilities

This MCP server exposes the following Cloud Foundry operations as tools:

### Application Management (8 tools)
- **applicationsList** - List all applications in a space
- **applicationDetails** - Get detailed information about a specific application
- **cloneApplication** - Clone an existing application
- **scaleApplication** - Scale application instances, memory, or disk quota
- **startApplication** - Start a stopped application
- **stopApplication** - Stop a running application
- **restartApplication** - Restart an application
- **deleteApplication** - Delete an application

### Organization & Space Management (7 tools)
- **organizationsList** - List all organizations
- **organizationDetails** - Get details about a specific organization
- **spacesList** - List all spaces in an organization
- **getSpaceQuota** - Get quota information for a space
- **createSpace** - Create a new space
- **deleteSpace** - Delete a space
- **renameSpace** - Rename an existing space

### Service Management (6 tools)
- **serviceInstancesList** - List all service instances in a space
- **serviceInstanceDetails** - Get details about a specific service instance
- **serviceOfferingsList** - List available service offerings
- **bindServiceInstance** - Bind a service instance to an application
- **unbindServiceInstance** - Unbind a service instance from an application
- **deleteServiceInstance** - Delete a service instance

### Route Management (6 tools)
- **routesList** - List all routes in a space
- **createRoute** - Create a new route
- **deleteRoute** - Delete a specific route
- **deleteOrphanedRoutes** - Delete all unmapped routes
- **mapRoute** - Map a route to an application
- **unmapRoute** - Unmap a route from an application

### Network Policy Management (3 tools)
- **addNetworkPolicy** - Create network policy between applications
- **listNetworkPolicies** - List all network policies
- **removeNetworkPolicy** - Remove network policy between applications

### Application Cloning (1 tool)

All tools support multi-context operations with optional `organization` and `space` parameters to target different environments.
