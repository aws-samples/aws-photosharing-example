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

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "dictionary")        
@Cacheable
@Cache(region="dictionary", usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Dictionary {
	
    private DictionaryPK id;    
    private String text;
    
    public Dictionary() {}
    public Dictionary(String p_key, Locale p_locale, String p_text) {
    	setId(getKey(p_key, p_locale));
    	setText(p_text);
    }
    
    @EmbeddedId
    public DictionaryPK getId() {return id;}
    public void setId(DictionaryPK p_id) {this.id = p_id;}
    
    @Column(columnDefinition="TEXT")
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}
  
    public static DictionaryPK getKey(String p_key, Locale p_locale) {
    	return new DictionaryPK(p_key, p_locale);
    }
}

@Embeddable
class DictionaryPK implements Serializable {
	
	private static final long serialVersionUID = -443136657547347081L;
	
	protected String id;
	protected Locale locale;
		
	public DictionaryPK(){}
	
	public DictionaryPK(String p_key, Locale p_locale) {
		setId(p_key);
		setLocale(p_locale);
	}
	
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}
	
	@Column(length=4)	
	public Locale getLocale() {return locale;}
	public void setLocale(Locale locale) {this.locale = locale;}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DictionaryPK that = (DictionaryPK) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		return locale != null ? locale.equals(that.locale) : that.locale == null;

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (locale != null ? locale.hashCode() : 0);
		return result;
	}
}
