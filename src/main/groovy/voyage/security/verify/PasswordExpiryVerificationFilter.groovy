/*
 * Copyright (c) 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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
package voyage.security.verify

import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import java.security.Principal

import voyage.security.user.User
import voyage.security.user.UserService

@Component
@Order(2)
class PasswordExpiryVerificationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordExpiryVerificationFilter)

    private final UserService userService
    private final VerifyProperties verifyProperties
    private final PasswordVerifyProperties passwordVerifyProperties

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
    }

    @Autowired
    PasswordExpiryVerificationFilter(UserService userService, VerifyProperties verifyProperties, PasswordVerifyProperties passwordVerifyProperties) {
        this.userService = userService
        this.verifyProperties = verifyProperties
        this.passwordVerifyProperties = passwordVerifyProperties
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request
        if (isRequestFilterable(httpRequest)) {
            if (isUserCredentialsExpired(httpRequest)) {
                writePasswordVerificationResponse(response)
                return
            }
        } else {
            LOG.debug("PASSWORD EXPIRY VERIFICATION FILTER: Request path ${getRequestPath(httpRequest)} is excluded from " +
                    'this filter. Skipping password expiry verification.')
        }
        chain.doFilter(request, response)
    }

    private boolean isUserCredentialsExpired(HttpServletRequest httpRequest) {
        Principal authenticatedUser = httpRequest.userPrincipal
        if (!authenticatedUser) {
            LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: User is not authenticated. Skipping password expiry verification.')
            return false
        }
        if (authenticatedUser instanceof Authentication) {
            String username
            Authentication authenticationToken = (Authentication)authenticatedUser
            if (authenticationToken.principal instanceof UserDetails) {
                username = ((UserDetails)authenticationToken.principal).username

            } else if (authenticationToken.principal instanceof String) {
                username = authenticationToken.principal
            }
            if (username) {
                User user = userService.findByUsername(username)
                if (user) {
                    if (user.isCredentialsExpired || isPasswordExpired(user)) {
                        LOG.info('PASSWORD EXPIRY VERIFICATION FILTER: User password expired. Returning error response.')
                        return true
                    }
                } else {
                    LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: User was not found in the database. Skipping password expiry verification.')
                }
            } else {
                LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: Authenticated principal is not a recognized object. ' +
                        'Skipping password expiry verification.')
            }
        } else {
            LOG.debug('PASSWORD EXPIRY VERIFICATION FILTER: Authenticated user is not a recognized Authorization object.' +
                    ' Skipping password expiry verification.')
        }
        return false
    }

    private static void writePasswordVerificationResponse(ServletResponse response) {
        Map errorResponse = [
                error:'403_password_expired',
                errorDescription:'Password is expired. Please change the password to access the application',
        ]
        JsonBuilder json = new JsonBuilder([errorResponse])
        response.contentType = 'application/json'
        Writer responseWriter = response.writer
        json.writeTo(responseWriter)
        responseWriter.close()
        responseWriter.flush()
    }

    private boolean isRequestFilterable(HttpServletRequest request) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        for (String antPattern : verifyProperties.excludeResources) {
            if (antPathMatcher.match(antPattern, path)) {
                return false
            }
        }
        for (String antPattern : passwordVerifyProperties.excludeResources) {
            if (antPathMatcher.match(antPattern, path)) {
                return false
            }
        }
        return true
    }

    private static String getRequestPath(HttpServletRequest request) {
        String url = request.servletPath
        if (request.pathInfo) {
            url += request?.pathInfo
        }
        return url
    }

    private boolean isPasswordExpired(User user) {
        Integer diffInDays = new Date() - user.passwordCreatedDate
        if (passwordVerifyProperties.passwordResetDays == 0) {
            return false
        }
        return  diffInDays > passwordVerifyProperties.passwordResetDays
    }

    @Override
    void destroy() {

    }
}
