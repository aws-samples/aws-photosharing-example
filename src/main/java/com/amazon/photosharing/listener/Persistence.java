/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.photosharing.listener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Persistence implements ServletContextListener {
	
    private static EntityManagerFactory emf;

    public void contextInitialized(ServletContextEvent event) {
    	if (emf == null)
    		emf = javax.persistence.Persistence.createEntityManagerFactory("dao");
    }

    public void contextDestroyed(ServletContextEvent event) {    	
    	if (emf != null)
    		emf.close();
    	emf = null;    	
    }
         
    public static final EntityManager createEntityManager() {
        if (emf == null) {        	
        	emf = javax.persistence.Persistence.createEntityManagerFactory("dao");
        }
        return emf.createEntityManager();
    }

}