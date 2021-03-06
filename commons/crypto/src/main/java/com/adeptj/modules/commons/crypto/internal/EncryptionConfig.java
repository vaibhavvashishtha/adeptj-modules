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

package com.adeptj.modules.commons.crypto.internal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

/**
 * OSGi Configuration for {@link com.adeptj.modules.commons.crypto.EncryptionService}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@ObjectClassDefinition(
        name = "AdeptJ Crypto Encryption Configuration",
        description = "Configuration for the AdeptJ EncryptionService"
)
public @interface EncryptionConfig {

    @AttributeDefinition(
            name = "Key Size",
            description = "The key length in bits for encryption key."
    )
    int keySize() default 128;

    @AttributeDefinition(
            name = "Initialization Vector",
            description = "The Initialization Vector."
    )
    String iv();

    @AttributeDefinition(
            name = "Encryption Key",
            description = "The encryption key, which will be automatically generated and populate here."
    )
    String key();

    @AttributeDefinition(
            name = "Charset to encode",
            description = "Charset to encode the salt and plain text",
            options = {
                    @Option(label = "US ASCII", value = "US-ASCII"),
                    @Option(label = "UTF 8", value = "UTF-8"),
            }
    )
    String charsetToEncode() default "US-ASCII";
}
