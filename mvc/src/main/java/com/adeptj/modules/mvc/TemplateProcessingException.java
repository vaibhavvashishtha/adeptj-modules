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

package com.adeptj.modules.mvc;

/**
 * The TemplateProcessingException thrown from {@link TemplateEngine#render} method execution.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class TemplateProcessingException extends RuntimeException {

    private static final long serialVersionUID = -8756067154883255519L;

    public TemplateProcessingException(Throwable cause) {
        super(cause);
    }
}
