/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package voyage.config

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.LengthRule
import org.passay.PasswordValidator
import org.passay.WhitespaceRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PasswordPolicyConfig {

    @Bean
    PasswordValidator passwordValidator() {
        return new PasswordValidator(passwordPolicyRules)
    }

    private static List<CharacterRule> getPasswordPolicyRules() {
        CharacterRule upperCaseCharacterRule = new CharacterRule(EnglishCharacterData.UpperCase, 1)
        CharacterRule lowerCaseCharacterRule = new CharacterRule(EnglishCharacterData.LowerCase, 1)
        CharacterRule numericCharacterRule = new CharacterRule(EnglishCharacterData.Digit, 1)
        CharacterRule specialCharacterRule = new CharacterRule(EnglishCharacterData.Special, 1)
        LengthRule lengthRule = new LengthRule(8, 30)
        WhitespaceRule whitespaceRule = new WhitespaceRule()
        [upperCaseCharacterRule, lowerCaseCharacterRule, numericCharacterRule, specialCharacterRule, lengthRule, whitespaceRule]
    }
}
