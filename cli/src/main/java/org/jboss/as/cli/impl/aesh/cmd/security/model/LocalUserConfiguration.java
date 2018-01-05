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

import org.jboss.as.cli.Util;

/**
 *
 * @author jdenise@redhat.com
 */
public class LocalUserConfiguration implements MechanismConfiguration {

    private final boolean superUser;

    public LocalUserConfiguration(boolean superUser) {
        this.superUser = superUser;
    }
    @Override
    public String getRealmName() {
        return Util.LOCAL;
    }

    @Override
    public String getRoleDecoder() {
        return null;
    }

    @Override
    public String getRoleMapper() {
        return superUser ? Util.SUPER_USER_MAPPER : null;
    }

    @Override
    public String getRealmMapper() {
        return Util.LOCAL;
    }

}
