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

package com.adeptj.modules.security.jwt.internal;

import com.adeptj.modules.security.jwt.JwtClaimsValidator;
import com.adeptj.modules.security.jwt.JwtConfig;
import com.adeptj.modules.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static io.jsonwebtoken.SignatureAlgorithm.RS256;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

/**
 * Service for signing and parsing JWT
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@Designate(ocd = JwtConfig.class)
@Component(configurationPolicy = REQUIRE)
public class JwtServiceImpl implements JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    private static final String AUTH_SCHEME_BEARER = "Bearer";

    private static final String UTF8 = "UTF-8";

    private static final String KEY_HEADER = "-----BEGIN PRIVATE KEY-----";

    private static final String KEY_FOOTER = "-----END PRIVATE KEY-----";

    private static final String REGEX_SPACE = "\\s";

    private static final String ALGO_RSA = "RSA";

    private static final String SYS_PROP_CURRENT_DIR = "user.dir";

    private static final String DEFAULT_KEY_FILE = "/default.pem";

    private static final String KEY_INIT_FAIL_MSG = "Couldn't initialize the SigningKey!!";

    private static final String BIND_JWT_CLAIMS_VALIDATOR_SERVICE = "bindJwtClaimsValidator";

    private static final String UNBIND_JWT_CLAIMS_VALIDATOR_SERVICE = "unbindJwtClaimsValidator";

    private JwtConfig jwtConfig;

    private String base64EncodedSigningKey;

    private SignatureAlgorithm signatureAlgo;

    private Key signingKey;

    // As per Felix SCR, dynamic references should be declared as volatile.
    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            bind = BIND_JWT_CLAIMS_VALIDATOR_SERVICE,
            unbind = UNBIND_JWT_CLAIMS_VALIDATOR_SERVICE
    )
    private volatile JwtClaimsValidator jwtClaimsValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public String issue(String subject, Map<String, Object> claims) {
        Assert.hasText(subject, "Subject can't be null or empty!!");
        // Lets first set the claims, we don't want callers to act smart and pass the default claims parameters
        // such as "iss", "sub", "iat" etc. Since its a map and existing keys will be replaced when the same ones
        // provided in the payload which is not the intended behaviour.
        // Default claims parameters should come from JwtConfig and others can be generated at execution time
        // such as "iat", "exp", "jti" etc.
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setHeaderParam(TYPE, JWT_TYPE)
                .setSubject(subject)
                .setIssuer(this.jwtConfig.issuer())
                .setIssuedAt(Date.from(Instant.now()))
                .setId(UUID.randomUUID().toString())
                .setExpiration(Date.from(LocalDateTime.now()
                        .plusMinutes(this.jwtConfig.expirationTime())
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));
        this.signWith(jwtBuilder);
        return AUTH_SCHEME_BEARER + SPACE + jwtBuilder.compact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verify(String subject, String jwt) {
        boolean verified = false;
        try {
            Assert.hasText(subject, "Subject can't be null or empty!!");
            Assert.hasText(subject, "JWT can't be null or empty!!");
            JwtParser jwtParser = Jwts.parser()
                    .requireIssuer(this.jwtConfig.issuer())
                    .requireSubject(subject);
            this.setSigningKey(jwtParser);
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
            verified = !this.jwtConfig.validateClaims() ||
                    this.jwtClaimsValidator == null ||
                    this.jwtClaimsValidator.validate(claimsJws.getBody());
        } catch (RuntimeException ex) {
            if (this.jwtConfig.printJwtExceptionTrace()) {
                LOGGER.error("Invalid JWT!!", ex);
            } else {
                LOGGER.error(ex.getMessage());
            }
        }
        return verified;
    }

    // Component Lifecycle Methods

    protected void bindJwtClaimsValidator(JwtClaimsValidator jwtClaimsValidator) {
        this.jwtClaimsValidator = jwtClaimsValidator;
    }

    protected void unbindJwtClaimsValidator(JwtClaimsValidator jwtClaimsValidator) {
        this.jwtClaimsValidator = null;
    }

    @Activate
    protected void start(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        try {
            this.signatureAlgo = SignatureAlgorithm.forName(jwtConfig.signatureAlgo());
            String signKey = jwtConfig.signingKey();
            if (StringUtils.isNotEmpty(signKey) && this.signatureAlgo.isHmac()) {
                this.base64EncodedSigningKey = new String(Base64.getEncoder().encode(signKey.getBytes(UTF8)));
            } else if (StringUtils.isEmpty(signKey) && this.signatureAlgo.isHmac()) {
                this.signatureAlgo = RS256;
            } else {
                // This is to safeguard from situation where there was no signingKey provided
                // and HMAC is chosen from  signatureAlgo dropdown. Configuring RS256 as default.
                if (this.signatureAlgo.isHmac()) {
                    this.signatureAlgo = RS256;
                }
            }
            this.initAndValidateSigningKey();
        } catch (Exception ex) { // NOSONAR
            LOGGER.error(ex.getMessage(), ex);
            // Let the exception be rethrown so that SCR would not create a service object of this component.
            throw new RuntimeException(ex); // NOSONAR
        }
    }

    private void initAndValidateSigningKey() throws Exception {
        if (this.signatureAlgo.isRsa() && (this.signingKey = this.resolveSigningKey()) == null) {
            throw new IllegalStateException(KEY_INIT_FAIL_MSG);
        }
    }

    private Key resolveSigningKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGO_RSA);
        // 1. try the jwtConfig provided keyFileLocation
        String keyFileLocation = System.getProperty(SYS_PROP_CURRENT_DIR) +
                File.separator +
                this.jwtConfig.keyFileLocation();
        LOGGER.info("Loading Key file from location: [{}]", keyFileLocation);
        Key key = this.loadKeyFromLocation(keyFactory, keyFileLocation);
        if (key == null && this.jwtConfig.useDefaultKey()) {
            LOGGER.warn("Couldn't load Key file from location [{}], using the default one!!", keyFileLocation);
            // 2. Use the default one that is embedded with this module.
            key = this.loadDefaultKey(keyFactory);
        }
        return key;
    }

    private Key loadDefaultKey(KeyFactory keyFactory) {
        Key key = null;
        try (InputStream inputStream = JwtServiceImpl.class.getResourceAsStream(DEFAULT_KEY_FILE)) {
            key = this.generatePrivateKey(keyFactory, inputStream);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return key;
    }

    private Key loadKeyFromLocation(KeyFactory keyFactory, String keyFileLocation) throws Exception {
        Key key = null;
        try (FileInputStream inputStream = new FileInputStream(keyFileLocation)) {
            key = this.generatePrivateKey(keyFactory, inputStream);
        } catch (FileNotFoundException ex) {
            // Gulp it. Try to load the embedded one next.
        }
        return key;
    }

    private Key generatePrivateKey(KeyFactory keyFactory, InputStream stream) throws Exception {
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(this.encodedKey(stream)));
    }

    private byte[] encodedKey(InputStream stream) throws IOException {
        return Base64.getDecoder().decode(IOUtils.toString(stream, UTF8)
                .replace(KEY_HEADER, EMPTY)
                .replace(KEY_FOOTER, EMPTY)
                .replaceAll(REGEX_SPACE, EMPTY)
                .getBytes(UTF8));
    }

    private void signWith(JwtBuilder jwtBuilder) {
        if (this.signingKey == null) {
            jwtBuilder.signWith(this.signatureAlgo, this.base64EncodedSigningKey);
        } else {
            jwtBuilder.signWith(this.signatureAlgo, this.signingKey);
        }
    }

    private void setSigningKey(JwtParser parser) {
        if (this.signingKey == null) {
            parser.setSigningKey(this.base64EncodedSigningKey);
        } else {
            parser.setSigningKey(this.signingKey);
        }
    }
}
