package voyage.security.bfa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.brute-force-attack.user-lock-event-listener')
class UserLockEventListenerProperties {
    boolean enabled
    int maxLoginAttempts
}
