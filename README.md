# Cloud Foundry MCP Server

This MCP Server provides an LLM interface for interacting with your Cloud Foundry foundation. It was built with the [Spring AI MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html).

![Sample](images/sample.png)

## Building the Server

```bash
./mvnw clean package
```

### Using with MCP Client

By default, this MCP server runs with the SSE transport. Configure your MCP client like this:

```json
{
  "mcpServers": {
    "cloud-foundry": {
      "url": "https://[URL for deployed CF MCP server]/sse",
      "env": {
        "CF_APIHOST": "[Your CF API Endpoint e.g. api.sys.mycf.com]",
        "CF_USERNAME": "[Your CF User]",
        "CF_PASSWORD": "[Your CF Password]",
        "CF_ORG": "[Your Org]",
        "CF_SPACE": "[Your Space]"
      }
    }
  }
}
```

### Deploying to Cloud Foundry with User-Provided Service

When deploying the MCP server to Cloud Foundry, you can use a user-provided service to inject credentials instead of using environment variables. This approach keeps sensitive credentials out of your manifest files.

#### Create the User-Provided Service

Create a user-provided service named `cf-client` with the required Cloud Foundry credentials:

```bash
cf create-user-provided-service cf-client -p '{
  "apihost": "api.sys.mycf.com",
  "username": "your-cf-username",
  "password": "your-cf-password",
  "org": "your-org",
  "space": "your-space"
}'
```

Or create it interactively by providing credentials one at a time:

```bash
cf create-user-provided-service cf-client -p "apihost, username, password, org, space"
```

#### Bind the Service to Your Application

After deploying the application, bind the service:

```bash
cf bind-service cloud-foundry-mcp cf-client
cf restage cloud-foundry-mcp
```

Or include the service binding in your `manifest.yml`:

```yaml
applications:
- name: cloud-foundry-mcp
  memory: 1G
  instances: 1
  path: target/cloud-foundry-mcp-0.0.1-SNAPSHOT.jar
  services:
  - cf-client
```

The application will automatically read credentials from the `cf-client` service binding via the VCAP_SERVICES environment variable, as configured in `src/main/resources/application.yaml`.

#### Update the Service Credentials

If you need to update the credentials:

```bash
cf update-user-provided-service cf-client -p '{
  "apihost": "api.sys.mycf.com",
  "username": "new-username",
  "password": "new-password",
  "org": "new-org",
  "space": "new-space"
}'
cf restage cloud-foundry-mcp
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
