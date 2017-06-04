package voyage.security.verify

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.user-verification')
class VerifyProperties {
    String[] excludeResources
    int verifyCodeExpiresMinutes
}
