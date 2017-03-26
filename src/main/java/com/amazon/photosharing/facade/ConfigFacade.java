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

package com.amazon.photosharing.facade;

import java.util.function.Supplier;

import javax.persistence.EntityManager;

import com.amazon.photosharing.dao.Config;
import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.listener.Persistence;


public class ConfigFacade extends ServiceFacade {
	
	private static ConfigFacade _this = null;
	
	public static final String get(Configuration p_key) {		
		if (_this == null)
			_this = new ConfigFacade();
		return _this.getConfig(p_key);
	}
	
	public static final String set(Configuration p_key, String p_value) {		
		if (_this == null)
			_this = new ConfigFacade();
		return _this.setConfig(p_key, p_value);
	}
	
	
	public ConfigFacade() {
		super(Persistence::createEntityManager);
	}
	
	public ConfigFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);
	}
	 
	public String getConfig(Configuration p_key) {				
		Config entry = em().find(Config.class, p_key);	
		done();
		if (entry != null) {			
			return entry.getValue();
		} else {
			beginTx();
				em().persist(new Config(p_key, p_key.toString()));
				em().flush();
			commitTx();	
			return p_key.toString();
		}
	}	
	
	public synchronized String setConfig(Configuration p_key, String p_value) {
		beginTx();
		
			Config c = em().find(Config.class, p_key);
			if (c != null)  c.setValue(p_value);				
			else 			c = new Config(p_key, p_value);
						
			em().persist(c);	
			
		commitTx();			
		return p_value;		
	}

}
