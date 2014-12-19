/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.clustering.web.infinispan.session;

import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.ee.infinispan.TransactionBatch;
import org.wildfly.clustering.infinispan.spi.service.CacheBuilder;
import org.wildfly.clustering.infinispan.spi.service.CacheContainerServiceName;
import org.wildfly.clustering.infinispan.spi.service.CacheServiceNameFactory;
import org.wildfly.clustering.infinispan.spi.service.TemplateConfigurationBuilder;
import org.wildfly.clustering.web.session.SessionManagerConfiguration;
import org.wildfly.clustering.web.session.SessionManagerFactory;
import org.wildfly.clustering.web.session.SessionManagerFactoryBuilder;

/**
 * Service building strategy the Infinispan session manager factory.
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerFactoryBuilder implements SessionManagerFactoryBuilder<TransactionBatch> {
    public static final String DEFAULT_CACHE_CONTAINER = "web";

    @Override
    public ServiceBuilder<SessionManagerFactory<TransactionBatch>> buildDeploymentDependency(ServiceTarget target, ServiceName name, SessionManagerConfiguration config) {
        ServiceName templateCacheServiceName = getCacheServiceName(config.getCacheName());
        String templateCacheName = templateCacheServiceName.getSimpleName();
        ServiceName containerServiceName = templateCacheServiceName.getParent();
        String containerName = containerServiceName.getSimpleName();
        String cacheName = config.getDeploymentName();

        new TemplateConfigurationBuilder(containerName, cacheName, templateCacheName).build(target).install();
        new CacheBuilder<>(containerName, cacheName).build(target)
                .addAliases(InfinispanRouteLocatorService.getCacheServiceAlias(cacheName))
                .install();

        return InfinispanSessionManagerFactory.build(target, name, containerName, cacheName, config);
    }

    private static ServiceName getCacheServiceName(String cacheName) {
        ServiceName baseServiceName = CacheContainerServiceName.CACHE_CONTAINER.getServiceName(CacheServiceNameFactory.DEFAULT_CACHE).getParent();
        ServiceName serviceName = ServiceName.parse((cacheName != null) ? cacheName : DEFAULT_CACHE_CONTAINER);
        if (!baseServiceName.isParentOf(serviceName)) {
            serviceName = baseServiceName.append(serviceName);
        }
        return (serviceName.length() < 4) ? serviceName.append(CacheServiceNameFactory.DEFAULT_CACHE) : serviceName;
    }
}
