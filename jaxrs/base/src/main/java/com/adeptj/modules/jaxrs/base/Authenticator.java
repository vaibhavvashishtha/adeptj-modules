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
package com.adeptj.modules.jaxrs.base;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Authenticator.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Path("/auth")
@Component(immediate = true, service = Authenticator.class, property = "osgi.jaxrs.resource.base=auth")
public class Authenticator {

    @POST
    @Path("login")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response authenticate(@FormParam("username") String username, @FormParam("pwd") String pwd) {
        try {
            // First authenticate the user using the credentials provided but from where?
            // Now issue a token for the user
            return Response.ok().header(AUTHORIZATION, this.issueToken(username)).build();
        } catch (Exception e) {
            return Response.status(UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("jwt-check")
    @ValidateJWT
    public Response withAuth() {
        return Response.ok().build();
    }

    private String issueToken(String subject) {
        return "Bearer " + Jwts.builder().setSubject(subject).setIssuer("AdeptJ Runtime REST API").setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now().plusMinutes(15l).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS256, JaxRSAuthConfigProvider.INSTANCE.getJaxRSAuthConfig(subject).getSigningKey())
                .compact();
    }
}