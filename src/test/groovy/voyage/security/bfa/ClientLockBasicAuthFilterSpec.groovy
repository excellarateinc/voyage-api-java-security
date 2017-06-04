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
package voyage.security.bfa

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class ClientLockBasicAuthFilterSpec extends Specification {
    ClientLockBasicAuthFilter filter
    ClientLockBasicAuthFilterProperties properties
    ClientService clientService

    HttpServletRequest request
    HttpServletResponse response
    HttpSession session
    FilterChain filterChain

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
        session = Mock(HttpSession)

        clientService = Mock(ClientService)

        properties = new ClientLockBasicAuthFilterProperties()
        properties.enabled = true
        properties.resources = '/**'
        properties.maxLoginAttempts = 5

        filter = new ClientLockBasicAuthFilter(clientService, properties)
    }

    def 'doFilterInternal is skipped if disabled'() {
        given:
            properties.enabled = false

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal skips request if the url doesn not match'() {
        given:
            properties.enabled = true
            properties.resources = ['/nomatch']

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal finds a username and the user is authenticated'() {
        given:
            properties.enabled = true
            properties.resources = ['/**']

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> true
            1 * filterChain.doFilter(request, response)

            0 * clientService.findByClientIdentifier('super')
            0 * clientService.save(_ as Client)
    }

    def 'doFilterInternal client failed authentication and increments failure attempts'() {
        given:
            properties.enabled = true
            properties.resources = ['/**']

            Client client = new Client()
            client.isEnabled = true
            client.isAccountLocked = false

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> false
            1 * filterChain.doFilter(request, response)

            1 * clientService.findByClientIdentifier('client-super') >> client
            1 * clientService.save(_ as Client)

            client.failedLoginAttempts == 1
    }

    def 'doFilterInternal client failed authentication and locks the client account'() {
        given:
            properties.enabled = true
            properties.resources = ['/**']

            Client client = new Client()
            client.isEnabled = true
            client.isAccountLocked = false
            client.failedLoginAttempts = 4

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession(true) >> session
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * session.getAttribute(ClientLockBasicAuthFilter.IS_AUTHENTICATED) >> false
            1 * filterChain.doFilter(request, response)

            1 * clientService.findByClientIdentifier('client-super') >> client
            1 * clientService.save(_ as Client)

            client.failedLoginAttempts == 5
            client.isAccountLocked
    }
}
