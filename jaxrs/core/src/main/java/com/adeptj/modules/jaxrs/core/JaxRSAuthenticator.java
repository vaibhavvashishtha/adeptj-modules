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

package com.adeptj.modules.jaxrs.core;

/**
 * Provides {@link JaxRSAuthenticationInfo} by querying all the registered JaxRSAuthenticationRealm.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public interface JaxRSAuthenticator {

    /**
     * Validates the provided credentials by querying all the registered JaxRSAuthenticationRealm.
     * If any of the realm return a non null JaxRSAuthenticationInfo then no further realms are queried.
     * <p>
     * Note: Just the presence of non null JaxRSAuthenticationInfo will be treated a valid auth info by
     * {@link com.adeptj.modules.jaxrs.core.JaxRSAuthenticator} as it has no way to further validate the
     * information returned by the implementations.
     *
     * @param subject  the userId
     * @param password password of the supplied subject
     * @return a validated non null JaxRSAuthenticationInfo instance.
     */
    JaxRSAuthenticationInfo handleSecurity(String subject, String password);
}