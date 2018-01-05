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

import org.aesh.command.CommandException;
import org.jboss.as.cli.CommandContext;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author jdenise@redhat.com
 */
public class KeyStoreNameSecurityBuilder extends SSLSecurityBuilder {

    private final String name;

    public KeyStoreNameSecurityBuilder(String name) throws CommandException {
        this.name = name;
    }

    @Override
    protected KeyStore buildKeyStore(CommandContext ctx, ModelNode step) throws Exception {
        String password;
        password = ElytronUtil.retrieveKeyStorePassword(ctx, name);

        if (password == null) {
            throw new CommandException("Can't retrieve password for " + name);
        }
        return new KeyStore(name, password, true);
    }

}
