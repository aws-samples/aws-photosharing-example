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

import java.util.Locale;
import java.util.function.Supplier;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.dao.Dictionary;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.utils.Cache;

public class DictionaryFacade extends ServiceFacade {
	
	protected static final Logger _logger = LoggerFactory.getLogger(DictionaryFacade.class);
		
	public DictionaryFacade() {
		super();
	}
	
	public DictionaryFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);		
	}
	
	public static final String get(String p_key, Locale p_locale) {		
		Object value = Cache.getInstance().get(p_key+":"+p_locale);;		
		if (value == null) {
			value = new DictionaryFacade().getEntry(p_key, p_locale);	
			if (value != null) {
				Cache.getInstance().set(p_key+":"+p_locale, 60, value);
				return value.toString();
			} else {
				value = "???"+p_key+":"+p_locale+"???";
				Cache.getInstance().set(p_key+":"+p_locale, 60, value);
			}
		}
		return value.toString();
	}
	
	
	
	private String getEntry(String p_key, Locale p_locale) {
		Dictionary entry = em().find(Dictionary.class, Dictionary.getKey(p_key, p_locale));
		done();
		if (entry != null) {			
			return entry.getText();
		} else {
			beginTx();
				em().persist(new Dictionary(p_key, p_locale, null));
			commitTx();		
			done();
			return null;
		}		
	}

}
