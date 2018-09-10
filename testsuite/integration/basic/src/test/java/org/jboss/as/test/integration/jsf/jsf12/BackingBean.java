/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.as.test.integration.jsf.jsf12;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.jboss.logging.Logger;

public class BackingBean {
    private static final Logger log = Logger.getLogger(BackingBean.class.getName());
    private String name = "JBoss";
    private String response;

    @PostConstruct
    public void postConstruct() {
        log.info("PostConstruct: JSF Classloader: " + getJsfClassLoader());
    }

    public String hello() {
        this.response = ("Hello " + this.name);
        return "";
    }

    public String getResponse() {
        return this.response;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsfClassLoader() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getClass().getName() + " from " + context.getClass().getClassLoader();
    }
}
