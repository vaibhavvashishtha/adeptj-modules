/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.modules.data.jpa;

import com.adeptj.modules.commons.utils.Loggers;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Common JPA Utilities.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class JpaUtil {

    public static final int LEN_ZERO = 0;

    private JpaUtil() {
    }

    public static void closeEntityManager(EntityManager em) {
        try {
            if (em != null && em.isOpen()) {
                em.close();
            }
        } catch (RuntimeException ex) {
            Loggers.get(JpaUtil.class).error("Exception while closing EntityManager!!", ex);
        }
    }

    public static EntityTransaction getTransaction(EntityManager em) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        return transaction;
    }

    public static void setRollbackOnly(EntityTransaction txn) {
        if (txn != null && txn.isActive() && !txn.getRollbackOnly()) {
            txn.setRollbackOnly();
        }
    }

    public static void rollbackTransaction(EntityTransaction txn) {
        try {
            if (txn != null && txn.isActive() && txn.getRollbackOnly()) {
                Loggers.get(JpaUtil.class).warn("Rolling back transaction!!");
                txn.rollback();
            }
        } catch (RuntimeException ex) {
            Loggers.get(JpaUtil.class).error("Exception while rolling back transaction!!", ex);
        }
    }

    public static <T> Predicate[] getPredicates(Map<String, Object> attributes, CriteriaBuilder cb, Root<T> root) {
        return attributes
                .entrySet()
                .stream()
                .map(entry -> cb.equal(root.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList())
                .toArray(new Predicate[LEN_ZERO]);
    }

    /**
     * This method sets the the positional parameters to the given JPA {@link Query}.
     *
     * @param query     the JPA {@link Query}
     * @param posParams positional parameters
     */
    public static Query queryWithParams(Query query, List<Object> posParams) {
        setQueryParams(query, posParams);
        return query;
    }

    /**
     * This method sets the the positional parameters to the given JPA {@link TypedQuery}.
     *
     * @param query     the JPA {@link TypedQuery}
     * @param posParams positional parameters
     */
    public static <T> TypedQuery<T> typedQueryWithParams(TypedQuery<T> query, List<Object> posParams) {
        setQueryParams(query, posParams);
        return query;
    }


    private static void setQueryParams(Query query, List<Object> posParams) {
        Objects.requireNonNull(posParams, "Positional Parameters cannot be null!!");
        AtomicInteger posParamCounter = new AtomicInteger();
        posParams.forEach(param -> query.setParameter(posParamCounter.incrementAndGet(), param));
    }
}