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

import org.springframework.util.StopWatch
import spock.lang.Specification

class SleepAfterFailureEventListenerSpec extends Specification {
    long minSleepMillis = 3000
    long maxSleepMillis = 5000
    SleepAfterFailureEventListener listener
    SleepAfterFailureProperties properties

    def setup() {
        properties = new SleepAfterFailureProperties()
        properties.minSleepSeconds = minSleepMillis / 1000
        properties.maxSleepSeconds = maxSleepMillis / 1000

        listener = new SleepAfterFailureEventListener(properties)
    }

    def 'authenticationFailed is skipped if disabled'() {
        given:
            properties.enabled = false
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            listener.authenticationFailed(null)
            stopWatch.stop()

        then:
            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'authenticationFailed sleeps between the min and max amount'() {
        given:
        properties.enabled = true
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            listener.authenticationFailed(null)
            stopWatch.stop()

        then:
            withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    private boolean withinSleepRange(long milliseconds) {
        return milliseconds >= minSleepMillis && milliseconds <= maxSleepMillis
    }
}
