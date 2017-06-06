package voyage.security.crypto

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix='voyage.security.crypto')
class CryptoProperties {
    String privateKeyName
    String privateKeyPassword
    String keyStoreFileName
    String keyStorePassword
}
