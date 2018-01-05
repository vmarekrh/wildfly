/*
Copyright 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.jboss.as.cli.impl.aesh.cmd.security.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.Util;
import static org.jboss.as.cli.Util.isSuccess;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author jdenise@redhat.com
 */
public class HTTPServer {

    public static final String DEFAULT_SERVER = "default-server";

    public static void enableSSL(String serverName, boolean noOverride, CommandContext context, SSLSecurityBuilder builder) throws OperationFormatException {
        if (serverName == null) {
            serverName = DefaultResourceNames.getDefaultServerName(context);
        }
        if (serverName == null) {
            throw new OperationFormatException("No default server name found.");
        }
        if (!noOverride) {
            builder.getSteps().add(writeServerAttribute(serverName, Util.SECURITY_REALM, null));
        }
        builder.getSteps().add(writeServerAttribute(serverName, Util.SSL_CONTEXT, builder.getServerSSLContext().getName()));
    }

    private static ModelNode writeServerAttribute(String serverName, String name, String value) throws OperationFormatException {
        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        builder.setOperationName(Util.WRITE_ATTRIBUTE);
        builder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
        builder.addNode(Util.SERVER, serverName);
        builder.addNode(Util.HTTPS_LISTENER, Util.HTTPS);
        builder.addProperty(Util.NAME, name);
        if (value != null) {
            builder.addProperty(Util.VALUE, value);
        }
        return builder.buildRequest();
    }

    public static String disableSSL(CommandContext context, String serverName, ModelNode steps) throws OperationFormatException {
        if (serverName == null) {
            serverName = DefaultResourceNames.getDefaultServerName(context);
        }
        steps.add(writeServerAttribute(serverName, Util.SSL_CONTEXT, null));
        steps.add(writeServerAttribute(serverName, Util.SECURITY_REALM, DefaultResourceNames.getDefaultApplicationLegacyRealm()));
        return serverName;
    }

    public static void enableHTTPAuthentication(AuthSecurityBuilder builder, String securityDomain, CommandContext ctx) throws Exception {
        final DefaultOperationRequestBuilder reqBuilder = new DefaultOperationRequestBuilder();
        reqBuilder.setOperationName(Util.ADD);
        reqBuilder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
        reqBuilder.addNode(Util.APPLICATION_SECURITY_DOMAIN, securityDomain);
        reqBuilder.addProperty(Util.HTTP_AUTHENTICATION_FACTORY, builder.getAuthFactory().getName());
        builder.getSteps().add(reqBuilder.buildRequest());
    }

    public static ModelNode disableHTTPAuthentication(String securityDomain, CommandContext ctx) throws Exception {
        final DefaultOperationRequestBuilder reqBuilder = new DefaultOperationRequestBuilder();
        reqBuilder.setOperationName(Util.REMOVE);
        reqBuilder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
        reqBuilder.addNode(Util.APPLICATION_SECURITY_DOMAIN, securityDomain);
        return reqBuilder.buildRequest();
    }

    public static String getSecurityDomainFactoryName(String securityDomain, CommandContext ctx) throws IOException, OperationFormatException {
        final DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        final ModelNode request;
        builder.setOperationName(Util.READ_ATTRIBUTE);
        builder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
        builder.addNode(Util.APPLICATION_SECURITY_DOMAIN, securityDomain);
        builder.addProperty(Util.NAME, Util.HTTP_AUTHENTICATION_FACTORY);
        request = builder.buildRequest();

        final ModelNode outcome = ctx.getModelControllerClient().execute(request);
        if (isSuccess(outcome)) {
            if (outcome.hasDefined(Util.RESULT)) {
                return outcome.get(Util.RESULT).asString();
            }
        }

        return null;
    }

    public static String getSSLContextName(String serverName, CommandContext ctx) throws IOException, OperationFormatException {
        final DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        final ModelNode request;
        builder.setOperationName(Util.READ_ATTRIBUTE);
        builder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
        builder.addNode(Util.SERVER, serverName);
        builder.addNode(Util.HTTPS_LISTENER, Util.HTTPS);
        builder.addProperty(Util.NAME, Util.SSL_CONTEXT);
        request = builder.buildRequest();

        final ModelNode outcome = ctx.getModelControllerClient().execute(request);
        if (isSuccess(outcome)) {
            if (outcome.hasDefined(Util.RESULT)) {
                return outcome.get(Util.RESULT).asString();
            }
        }

        return null;
    }

    private static List<String> getNames(ModelControllerClient client, String type) {
        final DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        final ModelNode request;
        try {
            builder.setOperationName(Util.READ_CHILDREN_NAMES);
            builder.addNode(Util.SUBSYSTEM, Util.UNDERTOW);
            builder.addProperty(Util.CHILD_TYPE, type);
            request = builder.buildRequest();
        } catch (OperationFormatException e) {
            throw new IllegalStateException("Failed to build operation", e);
        }

        try {
            final ModelNode outcome = client.execute(request);
            if (Util.isSuccess(outcome)) {
                return Util.getList(outcome);
            }
        } catch (Exception e) {
        }

        return Collections.emptyList();
    }

    public static List<String> getSecurityDomains(CommandContext context) {
        return getNames(context.getModelControllerClient(), Util.APPLICATION_SECURITY_DOMAIN);
    }
}
