/*
 * Copyright 2015 Bud Byrd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.budjb.jaxrs.security

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.access.SecurityConfig
import org.springframework.security.web.FilterInvocation
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource

@CompileStatic
@Slf4j
class ObjectDefinitionSourceRegistry implements FilterInvocationSecurityMetadataSource {

    /**
     * DENY config rule.
     *
     * This is brought over from {@see AbstractFilterInvocationDefintion}.
     */
    protected static final Collection<ConfigAttribute> DENY = Collections.singletonList((ConfigAttribute)new SecurityConfig("_DENY_"));

    /**
     * Whether to reject the request if no rule is found.
     */
    boolean rejectIfNoRule

    /**
     * Contains all registered object definition sources.
     */
    List<FilterInvocationSecurityMetadataSource> sources = []

    Collection<ConfigAttribute> getAttributes(o) {
        for (FilterInvocationSecurityMetadataSource source : sources) {
            log.debug('retrieving security attributes using object definition source {}', source.getClass().simpleName)

            Collection<ConfigAttribute> configAttributes = source.getAttributes(o)
            if (!configAttributes) {
                continue
            }

            if (configAttributes.size() == 1 && configAttributes[0].attribute == '_DENY_') {
                continue
            }

            return configAttributes
        }

        if (rejectIfNoRule) {
            return DENY
        }
    }

    Collection<ConfigAttribute> getAllConfigAttributes() {
        return sources.collect { it.allConfigAttributes }
    }

    boolean supports(Class<?> clazz) {
        return FilterInvocation.isAssignableFrom(clazz)
    }

    /**
     * Registers an object definition source.
     */
    void register(FilterInvocationSecurityMetadataSource source) {
        log.debug('registering object definition source {}', source.getClass().simpleName)
        sources << source
    }

    /**
     * Un-registers an object definition source.
     */
    void unregister(FilterInvocationSecurityMetadataSource source) {
        log.debug('un-registering object definition source {}', source.getClass().simpleName)
        sources.remove source
    }
}
