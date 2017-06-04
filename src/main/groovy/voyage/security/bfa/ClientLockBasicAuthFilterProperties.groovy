package voyage.security.bfa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.brute-force-attack.client-lock-basic-auth-filter')
class ClientLockBasicAuthFilterProperties {
    boolean enabled
    String[] resources
    int maxLoginAttempts
}
