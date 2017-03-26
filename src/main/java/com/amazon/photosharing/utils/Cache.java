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

package com.amazon.photosharing.utils;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.enums.Configuration;

import net.spy.memcached.MemcachedClient;
 
public class Cache {
		
    private static final String 		NAMESPACE= "PHOTOSHARING:LndphKcyHf";
    private static Cache 				instance = null;
    private static MemcachedClient[] 	m = new MemcachedClient[1];
        
    private static final Integer clusterPort = 11211;
    
    private static final Logger _logger = LoggerFactory.getLogger(Cache.class);
     
    static {
    	 for (int i = 0; i < m.length; i++) {
    		 try {
// 				local configuration
//				m[i] = new MemcachedClient(
//							new BinaryConnectionFactory(),AddrUtil.getAddresses("127.0.0.1:11211"));
				
				m[i] = new MemcachedClient(new InetSocketAddress(Configuration.ELASTICACHE_CONFIG_ENDPOINT.toString(), clusterPort));
				
			} catch (IOException e) {
				e.printStackTrace();
			}		    	
    	 }
    }
    
    private Cache() {}
     
    public static synchronized Cache getInstance() {        
        if(instance == null) {
            _logger.debug("Creating a new instance");
            instance = new Cache();
         }
         return instance;
    }
     
    public void set(String key, int ttl, final Object o) {    	
    	try {
    		MemcachedClient c = getCache();
    		if (c != null)
    			getCache().set(NAMESPACE + key, ttl, o);
    		
    	} catch (IllegalStateException ex) {
    		_logger.warn("Clients not properly initialized. Disabling Cache.");
    		clearClients();    		
    	}    	       
    }
     
    public Object get(String key) {
    	try {
    		MemcachedClient c = getCache();
    		if (c != null)
    			return getCache().get(NAMESPACE + key);
    		else
    			return null;
    	} catch (IllegalStateException ex) {
    		_logger.warn("Clients not properly initialized. Disabling Cache.");
    		clearClients();
    		return null;
    	}
    }
     
    public Object delete(String key) {
        return getCache().delete(NAMESPACE + key);  
    }
     
    public void clearClients() {
    	for (int i = 0; i < m.length; i++) {
    		if (m[i] != null)
    			m[i].shutdown();
			m[i] = null;
		}
    }
    
    public MemcachedClient getCache() {       
    	int i = (int) (Math.random()* m.length-1);
        return m[i];        
    }
}
