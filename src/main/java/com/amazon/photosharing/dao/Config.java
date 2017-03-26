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

package com.amazon.photosharing.dao;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.amazon.photosharing.enums.Configuration;

@Entity
@Table(name = "config")        
@Cacheable
@Cache(region="config", usage=CacheConcurrencyStrategy.READ_WRITE)
public class Config {
	
	@Id
	@Enumerated(EnumType.STRING)
    private Configuration config_key;    
	
	@Column(columnDefinition="TEXT")
	private String value;
    
    public Config() {}
    public Config(Configuration p_key, String p_value) {    
    	setKey(p_key);
    	setValue(p_value);
    }
    
    public Configuration getKey() {return config_key;}
    public void setKey(Configuration p_key) {this.config_key = p_key;}    
  	
	public String getValue() {return value;}
	public void setValue(String p_value) {this.value = p_value;}
   
}
