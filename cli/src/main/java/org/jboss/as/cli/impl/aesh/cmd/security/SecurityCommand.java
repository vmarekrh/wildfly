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
package org.jboss.as.cli.impl.aesh.cmd.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.GroupCommandDefinition;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.aesh.cmd.AbstractCommaCompleter;
import org.jboss.as.cli.impl.aesh.cmd.AbstractCompleter;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.http.ManagementDisableHTTPCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.http.ManagementEnableHTTPCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.http.HTTPServerDisableAuthCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.http.HTTPServerEnableAuthCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.sasl.AbstractReorderSASLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.sasl.ManagementDisableSASLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.sasl.ManagementEnableSASLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.auth.sasl.ManagementReorderSASLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.model.AuthFactory;
import org.jboss.as.cli.impl.aesh.cmd.security.model.AuthFactorySpec;
import org.jboss.as.cli.impl.aesh.cmd.security.model.AuthMechanism;
import org.jboss.as.cli.impl.aesh.cmd.security.model.ElytronUtil;
import org.jboss.as.cli.impl.aesh.cmd.security.model.ServerSSLContext;
import org.jboss.as.cli.impl.aesh.cmd.security.ssl.HTTPServerDisableSSLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.ssl.ManagementDisableSSLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.ssl.HTTPServerEnableSSLCommand;
import org.jboss.as.cli.impl.aesh.cmd.security.ssl.ManagementEnableSSLCommand;
import org.jboss.dmr.ModelNode;
import org.wildfly.core.cli.command.aesh.CLICommandInvocation;
import org.wildfly.core.cli.command.aesh.CLICompleterInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(name = "security", description = "", groupCommands
        = {ManagementEnableSSLCommand.class, ManagementDisableSSLCommand.class,
            HTTPServerEnableSSLCommand.class, HTTPServerDisableSSLCommand.class,
            ManagementEnableSASLCommand.class, ManagementDisableSASLCommand.class,
            ManagementReorderSASLCommand.class, ManagementInfoCommand.class,
            ManagementDisableHTTPCommand.class, ManagementEnableHTTPCommand.class,
            HTTPServerEnableAuthCommand.class, HTTPServerInfoCommand.class,
            HTTPServerDisableAuthCommand.class})
public class SecurityCommand implements Command<CLICommandInvocation> {

    public static class OptionCompleters {

        public static class FileSystemRealmCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return ElytronUtil.getFileSystemRealmNames(completerInvocation.getCommandContext().
                        getModelControllerClient());
            }
        }

        public static class SimpleDecoderCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return ElytronUtil.getSimpleDecoderNames(completerInvocation.getCommandContext().
                        getModelControllerClient());
            }
        }

        public static class MechanismsCompleter extends AbstractCommaCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                AbstractReorderSASLCommand cmd = (AbstractReorderSASLCommand) completerInvocation.getCommand();
                try {
                    return ElytronUtil.getMechanisms(completerInvocation.getCommandContext(),
                            cmd.getSASLFactoryName(completerInvocation.getCommandContext()),
                            AuthFactorySpec.SASL);
                } catch (Exception ex) {
                    return Collections.emptyList();
                }
            }
        }

        public static class SecurityDomainCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return Util.getUndertowSecurityDomains(completerInvocation.getCommandContext().getModelControllerClient());
            }
        }

        public static class KeyStoreNameCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return ElytronUtil.getKeyStoreNames(completerInvocation.getCommandContext().
                        getModelControllerClient());
            }
        }

        public static class SSLContextNameCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return ElytronUtil.getSSLContextNames(completerInvocation.getCommandContext().
                        getModelControllerClient());
            }
        }

        public static class KeyStoreTypeCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return Arrays.asList(ElytronUtil.JKS);
            }
        }

        public static class ProtocolsCompleter extends AbstractCommaCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return ElytronUtil.getAllowedProtocols(completerInvocation.getCommandContext());
            }
        }

        public static class ServerNameCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return Util.getUndertowServerNames(completerInvocation.getCommandContext().getModelControllerClient());
            }
        }

        public static class ManagementInterfaceCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return Util.getManagementInterfaces(completerInvocation.getCommandContext().getModelControllerClient());
            }
        }

        public static class SecureSocketBindingCompleter extends AbstractCompleter {

            @Override
            protected List<String> getItems(CLICompleterInvocation completerInvocation) {
                return Util.getStandardSocketBindings(completerInvocation.getCommandContext().getModelControllerClient());
            }
        }
    }

    public static final String OPT_FILE_SYSTEM_REALM = "file-system-realm";
    public static final String OPT_USER_ROLE_DECODER = "user-role-decoder";
    public static final String OPT_USER_PROPERTIES_FILE = "user-properties-file";
    public static final String OPT_GROUP_PROPERTIES_FILE = "group-properties-file";
    public static final String OPT_RELATIVE_TO = "relative-to";
    public static final String OPT_NO_RELOAD = "no-reload";
    public static final String OPT_USER_PROPERTIES_REALM_NAME = "user-properties-realm-name";

    public static final String OPT_NEW_SECURITY_DOMAIN_NAME = "new-security-domain-name";
    public static final String OPT_NEW_AUTH_FACTORY_NAME = "new-auth-factory-name";
    public static final String OPT_NEW_REALM_NAME = "new-realm-name";

    public static final String OPT_LOCAL_USER = "jboss-local-user";
    public static final String OPT_LOCAL_SUPER_USER = "jboss-local-super-user";

    public static final String OPT_DIGEST = "digest";
    public static final String OPT_BASIC = "basic";
    public static final String OPT_FORM = "form";
    public static final String OPT_CLIENT_CERT_TRUST_STORE = "client-cert-trust-store";

    public static final String OPT_DIGEST_MD5 = "digest-md5";
    public static final String OPT_PLAIN = "plain";
    public static final String OPT_EXTERNAL_TRUST_STORE = "external-trust-store";

    public static final String OPT_KEY_STORE_NAME = "key-store-name";
    public static final String OPT_KEY_STORE_PATH = "key-store-path";
    public static final String OPT_KEY_STORE_PATH_RELATIVE_TO = "key-store-path-relative-to";
    public static final String OPT_KEY_STORE_PASSWORD = "key-store-password";
    public static final String OPT_KEY_STORE_TYPE = "key-store-type";

    public static final String OPT_MANAGEMENT_INTERFACE = "management-interface";

    public static final String OPT_HTTP_SECURE_SOCKET_BINDING = "http-secure-socket-binding";

    public static final String OPT_NEW_KEY_MANAGER_NAME = "new-key-manager-name";
    public static final String OPT_NEW_SSL_CONTEXT_NAME = "new-ssl-context-name";
    public static final String OPT_NEW_KEY_STORE_NAME = "new-key-store-name";

    public static final String OPT_PROTOCOLS = "protocols";

    public static final String OPT_SERVER_NAME = "server-name";
    public static final String OPT_NO_OVERRIDE_SECURITY_REALM = "no-override-security-realm";
    public static final String OPT_SECURITY_DOMAIN = "security-domain";

    @Override
    public CommandResult execute(CLICommandInvocation commandInvocation)
            throws CommandException, InterruptedException {
        throw new CommandException("Command action is missing.");
    }

    public static String formatOption(String name) {
        return "--" + name;
    }

    public static void execute(CommandContext ctx, ModelNode request) throws CommandException {
        ModelNode response;
        try {
            response = ctx.getModelControllerClient().execute(request);
        } catch (IOException ex) {
            throw new CommandException(ex);
        }
        if (!Util.isSuccess(response)) {
            throw new CommandException(Util.getFailureDescription(response));
        }
    }

    public static void execute(CommandContext ctx, ModelNode request, boolean noReload) throws CommandException {
        execute(ctx, request);
        if (!noReload) {
            try {
                ctx.handle("reload");
                ctx.printLine("Server reloaded.");
            } catch (CommandLineException ex) {
                throw new CommandException(ex.getLocalizedMessage(), ex);
            }
        } else {
            ctx.printLine("Warning: server has not been reloaded. Call 'reload' to apply changes.");
        }
    }

    public static void displayAuthSecurityInfo(CommandContext context, String factoryName, String itf, AuthFactorySpec spec) {
        context.printLine(spec.getName() + " authentication enabled: " + (factoryName != null));
        if (factoryName != null) {
            AuthFactory factory = ElytronUtil.getAuthFactory(factoryName, spec, context);
            context.printLine("Authentication Factory: " + factory.getName());
            context.printLine("Security Domain: " + factory.getSecurityDomain().getName());
            context.printLine("Mechanisms:");
            for (AuthMechanism m : factory.getMechanisms()) {
                context.printLine(m.getType()
                        + (m.getConfig().getRealmMapper() != null ? " realm-mapper=" + m.getConfig().getRealmMapper() : "")
                        + (m.getConfig().getRealmName() != null ? " realm-name=" + m.getConfig().getRealmName() : ""));
            }
        }
    }

    public static void displaySSLSecurityInfo(CommandContext context, String sslContextName) {
        context.printLine("SSL enabled: " + (sslContextName != null));
        if (sslContextName != null) {
            ServerSSLContext sslContext = ElytronUtil.getServerSSLContext(context, sslContextName);
            context.printLine("Server SSLContext: " + sslContext.getName());
            context.printLine("Key Manager: " + sslContext.getKeyManager().getName());
            context.printLine("Key Store: " + sslContext.getKeyManager().getKeyStore().getName());
        }
    }
}
