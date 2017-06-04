package voyage.security.bfa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.brute-force-attack.sleep-after-failure')
class SleepAfterFailureProperties {
    boolean enabled
    int[] httpStatusFailureList
    int minSleepSeconds
    int maxSleepSeconds
}
