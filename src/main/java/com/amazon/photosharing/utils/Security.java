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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

public class Security {

    protected final static Logger _logger = LoggerFactory.getLogger(Security.class);

    public static String getPasswordHash(String p_password, byte[] p_seed) {
			
    	byte[] pwd = p_password.getBytes();
    	byte[] result = new byte[pwd.length+p_seed.length];    	
    	
    	for (int i = 0; i<result.length;i++) {
    		if (i%2 == 0 && i/2<pwd.length) {    			
    			result[i] = pwd[i/2];
    		}
    		if ((i+1)%2 == 0 && (i+1)/2<p_seed.length) {    			
    			result[i] = p_seed[(i+1)/2];
    		}    		
    	}    	    
    	MessageDigest md = null;
    	try { md = MessageDigest.getInstance("SHA-256");} 
    	catch (NoSuchAlgorithmException e) {
            _logger.error(e.getMessage(), e);
        }
		
    	return Base64.getEncoder().encodeToString(md.digest(result));
	}
	
	public static String getRandomHash(int p_length) {
		try {
			String result = SHA1(new Date().toString()+(int)(Math.random()*Math.pow(10, 6))); 
			return result.substring(0, p_length<result.length()?p_length:result.length());
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
    public static String SHA1(String p_input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(p_input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
    	