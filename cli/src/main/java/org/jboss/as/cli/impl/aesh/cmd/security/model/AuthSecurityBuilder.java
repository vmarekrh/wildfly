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
import java.util.List;
import java.util.Objects;
import org.aesh.command.CommandException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author jdenise@redhat.com
 */
public class AuthSecurityBuilder {

    private String newSecurityDomain;
    private String newFactoryName;
    private String realmName;
    private String activeFactoryName;
    private final ModelNode composite = new ModelNode();
    private final AuthMechanism mechanism;
    private AuthFactory authFactory;
    private final AuthFactory ootbFactory;
    private final List<String> order;
    private final AuthFactorySpec spec;

    public AuthSecurityBuilder(AuthMechanism mechanism, AuthFactorySpec spec) throws CommandException {
        Objects.requireNonNull(mechanism);
        Objects.requireNonNull(spec);
        this.mechanism = mechanism;
        ootbFactory = null;
        order = null;
        this.spec = spec;
        init();
    }

    public AuthSecurityBuilder(AuthFactory ootbFactory) throws CommandException {
        Objects.requireNonNull(ootbFactory);
        mechanism = null;
        this.ootbFactory = ootbFactory;
        order = null;
        spec = ootbFactory.getSpec();
        init();
    }

    public AuthSecurityBuilder(List<String> order) {
        Objects.requireNonNull(order);
        this.order = order;
        mechanism = null;
        this.ootbFactory = null;
        init();
        spec = AuthFactorySpec.SASL;
    }

    private void init() {
        composite.get(Util.OPERATION).set(Util.COMPOSITE);
        composite.get(Util.ADDRESS).setEmptyList();
    }

    public ModelNode getRequest() {
        return composite;
    }

    public ModelNode getSteps() {
        return composite.get(Util.STEPS);
    }

    public AuthFactory getAuthFactory() {
        return ootbFactory == null ? authFactory : ootbFactory;
    }

    public AuthSecurityBuilder setRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }

    public AuthSecurityBuilder setSecurityDomainName(String securityDomain) {
        this.newSecurityDomain = securityDomain;
        return this;
    }

    public AuthSecurityBuilder setAuthFactoryName(String newFactoryName) {
        this.newFactoryName = newFactoryName;
        return this;
    }

    public AuthSecurityBuilder setActiveFactoryName(String activeFactoryName) {
        this.activeFactoryName = activeFactoryName;
        return this;
    }

    public boolean isFactoryAlreadySet() {
        return activeFactoryName != null;
    }

    public void buildRequest(CommandContext ctx) throws Exception {
        // rely on existing resources, no request.
        if (ootbFactory != null) {
            return;
        }

        if (order != null) {
            if (activeFactoryName == null) {
                throw new Exception("No SASL factory to re-order");
            }
            ModelNode request = ElytronUtil.reorderSASLFactory(ctx, order, activeFactoryName);
            getSteps().add(request);
            return;
        }

        Realm realm;
        SecurityDomain securityDomain;
        // First build the realm.
        realm = buildRealm(ctx);

        // If we have an active Factory, the securityDomain is already present.
        if (activeFactoryName == null) {
            // Lookup for a factory that would contain only this exact same mechanism/realm
            // and reuse it.
            authFactory = ElytronUtil.findMatchingAuthFactory(mechanism, spec, ctx);
            if (authFactory == null) {
                // Add a new security domain (realm added to the new securityDomain)
                securityDomain = buildSecurityDomain(ctx, realm);
                // Finally the Factory;
                authFactory = buildAuthFactory(ctx, securityDomain);
            }
        } else {
            authFactory = ElytronUtil.getAuthFactory(activeFactoryName, spec, ctx);
            if (authFactory == null) {
                throw new Exception("Impossible to create factory");
            }
            // Add the realm to the existing security domain.
            addRealm(ctx, authFactory.getSecurityDomain(), realm);
        }
        if (authFactory == null) {
            throw new Exception("Impossible to create factory");
        }

        //Add the mechanism to the factory
        addAuthMechanism(ctx, authFactory, mechanism);
    }

    private Realm buildRealm(CommandContext ctx) throws Exception {
        Realm realm = null;
        if (mechanism.getConfig() instanceof PropertiesRealmConfiguration) {
            PropertiesRealmConfiguration config = (PropertiesRealmConfiguration) mechanism.getConfig();
            String rName = ElytronUtil.findMatchingUsersPropertiesRealm(ctx, config);
            boolean existing = false;
            String name = null;
            if (rName == null) {
                if (realmName == null) {
                    realmName = DefaultResourceNames.buildUserPropertiesDefaultRealmName(ctx, config);
                }
                ModelNode request = ElytronUtil.addUsersPropertiesRealm(ctx, realmName, config);
                getSteps().add(request);
                name = realmName;
            } else {
                existing = true;
                name = rName;
            }
            realm = new Realm(name, mechanism.getConfig(), existing);
        } else if (mechanism.getConfig() instanceof TrustStoreConfiguration) {
            // We must first retrieve the realm-name from the keyStore.
            TrustStoreConfiguration tsConfig = (TrustStoreConfiguration) mechanism.getConfig();
            String ksRealmName = ElytronUtil.findKeyStoreRealm(ctx, tsConfig.getTrustStore());
            if (ksRealmName == null) {
                ksRealmName = "ks-realm-" + tsConfig.getTrustStore();
                ModelNode request = ElytronUtil.addKeyStoreRealm(ctx, ksRealmName, tsConfig.getTrustStore());
                getSteps().add(request);
            }
            tsConfig.setRealmName(ksRealmName);
            realm = new Realm(mechanism.getConfig().getRealmName(),
                    mechanism.getConfig(), true);

        } else {
            realm = new Realm(mechanism.getConfig().getRealmName(),
                    mechanism.getConfig(), true);
        }
        return realm;
    }

    private SecurityDomain buildSecurityDomain(CommandContext ctx, Realm realm) throws OperationFormatException, IOException {
        if (newSecurityDomain == null) {
            newSecurityDomain = DefaultResourceNames.buildDefaultSecurityDomainName(realm, ctx);
        }
        ModelNode request = ElytronUtil.addSecurityDomain(ctx, realm, newSecurityDomain);
        getSteps().add(request);
        SecurityDomain domain = new SecurityDomain(newSecurityDomain);
        domain.addRealm(realm);
        return domain;
    }

    private AuthFactory buildAuthFactory(CommandContext ctx, SecurityDomain securityDomain) throws OperationFormatException, IOException {
        if (newFactoryName == null) {
            newFactoryName = DefaultResourceNames.buildDefaultAuthFactoryName(mechanism, spec, ctx);
        }
        ModelNode request = ElytronUtil.addAuthFactory(ctx, securityDomain, newFactoryName, spec);
        getSteps().add(request);
        AuthFactory factory = new AuthFactory(newFactoryName, securityDomain, spec);
        return factory;
    }

    private void addAuthMechanism(CommandContext ctx, AuthFactory authFactory, AuthMechanism mechanism) throws OperationFormatException {
        ElytronUtil.addAuthMechanism(ctx, authFactory, mechanism, getSteps());
    }

    private void addRealm(CommandContext ctx, SecurityDomain securityDomain, Realm realm) throws OperationFormatException {
        ElytronUtil.addRealm(ctx, securityDomain, realm, getSteps());
    }

    public boolean isEmpty() {
        return !getSteps().isDefined();
    }
}
