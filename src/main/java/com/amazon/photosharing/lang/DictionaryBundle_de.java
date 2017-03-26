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

package com.amazon.photosharing.lang;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import com.amazon.photosharing.facade.DictionaryFacade;

public class DictionaryBundle_de extends ResourceBundle {
		
	@Override
	protected Object handleGetObject(String key) {
		return DictionaryFacade.get(key, Locale.GERMAN);		
	}

	@Override
	public Enumeration<String> getKeys() {return null;}

}
