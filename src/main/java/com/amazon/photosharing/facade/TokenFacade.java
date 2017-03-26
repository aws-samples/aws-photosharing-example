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

import com.amazon.photosharing.dao.Token;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.utils.TokenGenerator;
import com.amazon.photosharing.utils.TokenStorage;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.function.Supplier;

public class TokenFacade extends ServiceFacade {

    public TokenFacade() {
        super();
    }

    public TokenFacade(Supplier<EntityManager> p_emFactory) {
        super(p_emFactory);
    }

    public void storeToken(String p_username, String p_token) {
        _logger.info("Stored token " + p_token + " for user " + p_username);
        TokenStorage.getInstance().storeToken(p_username, p_token);

        Token token = this.findToken(p_username);

        if (token == null) {
            token = new Token();
            token.setUserName(p_username);
        }

        token.setToken(p_token);

        beginTx();
        em().merge(token);
        commitTx();
        em().close();
    }

    public Token findToken(String p_username) {
        CriteriaBuilder builder = em().getCriteriaBuilder();
        CriteriaQuery<Token> criteria = builder.createQuery(Token.class );
        Root<Token> token_root = criteria.from(Token.class);
        criteria.select(token_root);
        criteria.where(builder.equal(token_root.get("userName"), p_username));
        try {
            Token t =  em().createQuery(criteria).getSingleResult();
            return t;
        } catch (NoResultException ex) {
            return null;
        }
    }

    public boolean validateToken(String p_token) {
        String username = TokenGenerator.getInstance().getUsernameFromToken(p_token);

        _logger.debug("Token: " + p_token);
        _logger.debug("Username: " + username);

        String token = TokenStorage.getInstance().getToken(username);

        if (token == null) {
            _logger.debug("Token is null, trying to get it from DB");
            Token tmpToken = findToken(username);
            if (tmpToken == null)
                return false;

            TokenStorage.getInstance().storeToken(username, tmpToken.getToken());
            token = tmpToken.getToken();
        }

        if (token.equals(p_token)) {
            _logger.debug("Tokens are the same");
            boolean isValid = TokenGenerator.getInstance().validateToken(token);
            _logger.debug("Is token valid: " + isValid);

            if (!isValid) {
                TokenStorage.getInstance().removeToken(username);
                Token tmpToken = findToken(username);

                if (tmpToken != null) {
                    beginTx();
                    em().remove(tmpToken);
                    commitTx();
                }
            }

            return isValid;
        }
        else {
            _logger.info("Tokens not the same");
            return false;
        }
    }
}
