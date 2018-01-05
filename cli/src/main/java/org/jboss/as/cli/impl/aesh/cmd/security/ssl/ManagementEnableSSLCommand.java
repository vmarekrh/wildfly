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
package org.jboss.as.cli.impl.aesh.cmd.security.ssl;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.option.Option;
import org.jboss.as.cli.CommandContext;
import static org.jboss.as.cli.impl.aesh.cmd.security.SecurityCommand.OPT_HTTP_SECURE_SOCKET_BINDING;
import static org.jboss.as.cli.impl.aesh.cmd.security.SecurityCommand.OPT_MANAGEMENT_INTERFACE;
import org.jboss.as.cli.impl.aesh.cmd.security.SecurityCommand.OptionCompleters;
import org.jboss.as.cli.impl.aesh.cmd.security.model.DefaultResourceNames;
import org.jboss.as.cli.impl.aesh.cmd.security.model.ManagementInterfaces;
import org.jboss.as.cli.impl.aesh.cmd.security.model.SSLSecurityBuilder;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "management-enable-ssl", description = "")
public class ManagementEnableSSLCommand extends AbstractEnableSSLCommand {

    private static final String DEFAULT_FILE_NAME = "management.keystore";

    @Option(name = OPT_MANAGEMENT_INTERFACE, completer = OptionCompleters.ManagementInterfaceCompleter.class)
    String managementInterface;

    @Option(name = OPT_HTTP_SECURE_SOCKET_BINDING, activator = OptionActivators.SecureSocketBindingActivator.class,
            completer = OptionCompleters.SecureSocketBindingCompleter.class)
    String secureSocketBinding;

    @Override
    protected void secure(CommandContext ctx, SSLSecurityBuilder builder) throws CommandException {
        try {
            ManagementInterfaces.enableSSL(managementInterface, secureSocketBinding, ctx, builder);
        } catch (Exception ex) {
            throw new CommandException(ex);
        }
    }

    @Override
    String getDefaultKeyStoreFileName() {
        return DEFAULT_FILE_NAME;
    }

    @Override
    protected boolean isSSLEnabled(CommandContext ctx) throws Exception {
        String target = managementInterface;
        if (target == null) {
            target = DefaultResourceNames.getDefaultManagementInterfaceName(ctx);
        }
        return ManagementInterfaces.getManagementInterfaceSSLContextName(ctx, target) != null;
    }

    @Override
    protected String getTarget(CommandContext ctx) {
        String target = managementInterface;
        if (target == null) {
            target = DefaultResourceNames.getDefaultManagementInterfaceName(ctx);
        }
        return target;
    }

}
