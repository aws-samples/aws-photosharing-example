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

import java.util.HashMap;
import java.util.Map;

public final class TokenStorage {

    private static final TokenStorage tokenStorage = new TokenStorage();

    private Map<String, String> tokenMap = new HashMap<>();

    private TokenStorage() {

    }

    public static TokenStorage getInstance() {
        return tokenStorage;
    }

    public void storeToken(String username, String token) {
        tokenMap.put(username, token);
    }

    public String getToken(String userName) {
        return tokenMap.get(userName);
    }

    public void removeToken(String userName) {
        tokenMap.remove(userName);
    }
}
