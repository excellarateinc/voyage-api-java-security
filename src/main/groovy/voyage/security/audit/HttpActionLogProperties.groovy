package voyage.security.audit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.http-audit-log')
class HttpActionLogProperties {
    String[] excludeResources
    String[] formUsernameFields
    String[] maskFields
    boolean storeRequestBody
    boolean storeResponseBody
}
