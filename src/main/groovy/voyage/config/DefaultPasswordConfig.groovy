package voyage.config

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import voyage.security.crypto.CryptoService
import voyage.security.role.Role
import voyage.security.user.User
import voyage.security.user.UserService

@Component
class DefaultPasswordConfig implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPasswordConfig)
    private static final String ENVIRONMENT_TEST = 'test'
    private static final String DEFAULT_PASSWORD = 'password'
    private static final int PASSWORD_LENGTH = 12

    @Autowired
    private UserService userService

    @Autowired
    private CryptoService cryptoService

    @Autowired
    private Environment environment

    @Override
    void afterPropertiesSet() throws Exception {
        updateSuperUsersPassword()
    }

    /**
     * Generates a new password for super users using the default password in other than test environment
     */
    void updateSuperUsersPassword() {
        if (Arrays.asList(environment.activeProfiles).contains(ENVIRONMENT_TEST)) {
            return //skip the change password in the test environment
        }
        Iterable<User> users = userService.findAllByRolesInList([Role.SUPER])
        StringBuilder superUsersInfo = new StringBuilder()
        List<CharacterRule> rules = passwordStrengthRules
        PasswordGenerator generator = new PasswordGenerator()
        users.each { user ->
            if (cryptoService.hashMatches(DEFAULT_PASSWORD, user.password)) {
                user.password = generator.generatePassword(PASSWORD_LENGTH, rules)
                userService.saveDetached(user)
                superUsersInfo.append("User: ${user.username}, Password: ${user.password} \n")
            }
        }
        if (superUsersInfo.length() > 0 ) {
            LOG.info('Restricted Users found with default password. Generating new passwords:')
            LOG.info(superUsersInfo.toString())
        }
    }

    private static List<CharacterRule> getPasswordStrengthRules() {
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, 1)
        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, 1)
        CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit, 1)
        CharacterRule specialCharacterRule = new CharacterRule(EnglishCharacterData.Special, 1)

        return [upperCaseRule, lowerCaseRule, digitRule, specialCharacterRule]
    }
}
