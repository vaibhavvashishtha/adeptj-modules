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

package com.adeptj.modules.jaxrs.resteasy.internal;

/**
 * Constants for RESTEasy modules.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class ResteasyConstants {

    private ResteasyConstants() {
    }

    static final String METHOD_GET_CTX_RESOLVERS = "getContextResolvers";

    static final String FIELD_PROVIDER_INSTANCES = "providerInstances";

    static final String FIELD_PROVIDER_CLASSES = "providerClasses";

    static final boolean FORCE_ACCESS = true;

    static final String RESTEASY_PROXY_SERVLET_NAME = "AdeptJ RESTEasy ProxyServlet";

    static final String SERVLET_URL_PATTERN = "/";
}
