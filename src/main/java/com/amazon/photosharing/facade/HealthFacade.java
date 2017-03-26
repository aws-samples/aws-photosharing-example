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

import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.listener.Persistence;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.function.Supplier;

public class HealthFacade extends ServiceFacade {

    public HealthFacade() {
        super(Persistence::createEntityManager);
    }

    public HealthFacade(Supplier<EntityManager> p_emFactory) {
        super(p_emFactory);
    }

    public Boolean isHealthy() {

        try {
            Query query = em().createQuery("select count(*) from User");
            query.getSingleResult();

            return true;
        }

        catch (Exception exc) {
            _logger.error(exc.getMessage(), exc);

            return false;
        }

    }
}
