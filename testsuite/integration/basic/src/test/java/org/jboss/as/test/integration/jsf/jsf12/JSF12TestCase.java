/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.test.integration.jsf.jsf12;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.integration.common.HttpRequest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.common.Assert;

/**
 * Test case for https://issues.jboss.org/browse/JBEAP-15074, three modules are created by
 * the ant script build-jsf12-modules.xml that loads the old jsf libraries.
 *
 * Beware that since 7.1 JSF version 1.2 is not supported on multijsf funtionality.
 * @author tmiyar
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class JSF12TestCase {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "multiJsf.war");
        war.addClasses(BackingBean.class);
        war.setWebXML(JSF12TestCase.class.getPackage(), "web.xml");
        war.addAsWebResource(JSF12TestCase.class.getPackage(), "index.jsp", "index.jsp");
        war.addAsWebInfResource(JSF12TestCase.class.getPackage(), "faces-config.xml", "faces-config.xml");
        return war;
    }

    @Test
    public void testJSF12Application() throws Exception {
        final String response = HttpRequest.get(url.toExternalForm() + "index.jsf", 2, TimeUnit.SECONDS);
        Assert.assertTrue(response.contains("com.sun.jsf-impl:mojarra-1.2"));
    }

}
