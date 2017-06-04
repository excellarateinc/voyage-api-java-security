package voyage.security.filter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.cors')
class CorsProperties {
    String accessControlAllowHeaders
}
