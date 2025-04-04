package com.mpanov.diploma.auth.security.common;

import com.mpanov.diploma.auth.exception.NonCompliantPasswordException;
import lombok.AllArgsConstructor;
import org.passay.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class PasswordService {

    private final PasswordEncoder encoder;

    public String generateCompliantPassword() {
        List<Rule> rules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.SpecialAscii, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new IllegalCharacterRule(new char[]{' ', '\t', '\n', '\r', '\f'})
        );

        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(12, rules);
    }

    public void assertPasswordCompliant(String password) {
        PasswordValidator validator = new PasswordValidator(
                new LengthRule(8, 64),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.SpecialAscii, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new WhitespaceRule()
        );

        PasswordData passwordData = new PasswordData(password);

        RuleResult result = validator.validate(passwordData);
        if (result.isValid()) {
            return;
        }

        throw new NonCompliantPasswordException(result.getDetails());
    }

    public String encryptPassword(String password) {
        return encoder.encode(password);
    }

    public boolean passwordMatches(String password, String encryptedPassword) {
        return encoder.matches(password, encryptedPassword);
    }

}
