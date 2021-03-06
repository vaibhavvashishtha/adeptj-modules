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

package com.adeptj.modules.webconsole.security;

import com.adeptj.modules.commons.utils.annotation.WebConsolePlugin;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AdeptJ Tools Plugin.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebConsolePlugin(label = ToolsPlugin.LABEL, title = ToolsPlugin.TITLE)
@Component(immediate = true, service = Servlet.class)
public class ToolsPlugin extends SimpleWebConsolePlugin {

    private static final long serialVersionUID = 8041033223220201144L;

    private static final String CATEGORY = "Main";

    private static final String REDIRECT_URI = "/tools/dashboard";

    static final String LABEL = "tools";

    static final String TITLE = "AdeptJ Tools";

    public ToolsPlugin() {
        super(LABEL, TITLE, CATEGORY, null);
    }

    @Override
    protected void renderContent(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect(REDIRECT_URI);
    }

}
