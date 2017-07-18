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

import com.adeptj.modules.security.jwt.JwtService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * JaxRSAuthenticator.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Path("/auth")
@Component(immediate = true, service = JaxRSAuthenticator.class, property = "osgi.jaxrs.resource.base=auth")
public class JaxRSAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxRSAuthenticator.class);

    private static final String BIND_JWT_SERVICE = "bindJwtService";

    private static final String UNBIND_JWT_SERVICE = "unbindJwtService";

    @Reference
    private JaxRSAuthenticationRepository authenticationRepository;

    @Reference(
            bind = BIND_JWT_SERVICE,
            unbind = UNBIND_JWT_SERVICE,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC
    )
    private JwtService jwtService;

    @POST
    @Path("jwt/issue")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response issueToken(@NotNull @FormParam("subject") String subject, @NotNull @FormParam("password") String password) {
        if (this.jwtService == null) {
            LOGGER.warn("Can't issue token as JwtService unavailable!");
            return Response.status(UNAUTHORIZED).entity("JwtService unavailable!!").build();
        }
        Response response;
        try {
            // First authenticate the user using the credentials provided.
            JaxRSAuthenticationInfo authenticationInfo = this.authenticationRepository.getAuthenticationInfo(subject);
            if (authenticationInfo == null) {
                response = Response.status(UNAUTHORIZED).entity("Unknown credentials!!").build();
            } else {
                if (authenticationInfo.getSubject().equals(subject)
                        && Arrays.equals(authenticationInfo.getPassword(), password.toCharArray())) {
                    // All well here, now issue a token for the Subject
                    Map<String, String> payload = new HashMap<>();
                    payload.put("subject", subject);
                    response = Response.ok().header(AUTHORIZATION, this.jwtService.issueToken(payload)).build();
                } else {
                    response = Response.status(UNAUTHORIZED).entity("Invalid credentials!!").build();
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return Response.status(UNAUTHORIZED).build();
        }
        return response;
    }

    @GET
    @Path("jwt/check")
    @RequiresJwtCheck
    public Response checkJwt() {
        return Response.ok("JWT is valid!!").build();
    }

    // LifeCycle Methods

    protected void bindJwtService(JwtService jwtService) {
        LOGGER.info("Binding JwtService: [{}]", jwtService);
        this.jwtService = jwtService;
    }

    protected void unbindJwtService(JwtService jwtService) {
        LOGGER.info("Unbinding JwtService: [{}]", jwtService);
        this.jwtService = null;
    }

}
