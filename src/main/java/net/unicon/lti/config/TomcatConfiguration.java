/**
 * Copyright 2021 Unicon (R)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.unicon.lti.config;

import net.unicon.lti.utils.SameSiteCookieValve;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is needed because often a LTI tool will run in an iframe and with the recent stricter cookie rules
 * browsers are rejecting cookies set in an iframe from the LTI redirect unless they have a SameSite cookie policy
 * of "None".
 */
@Configuration
public class TomcatConfiguration {
    @Value("${application.url}")
    String sessionDomain;

    static final Logger log = LoggerFactory.getLogger(TomcatConfiguration.class);

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return tomcatServletWebServerFactory -> {
            tomcatServletWebServerFactory.addContextValves(new SameSiteCookieValve());
            tomcatServletWebServerFactory.addContextCustomizers(context -> {
                Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();
                processor.setSameSiteCookies("None");
                processor.setPartitioned(true);
                context.setUsePartitioned(true);
                try {
                    URI uri = new URI(sessionDomain);
                    context.setSessionCookieDomain(uri.getHost());
                } catch (URISyntaxException e) {
                    log.error("Missing session.domain property or it is wrong", e);
                }

                context.setCookieProcessor(processor);
            });
        };
    }
}
