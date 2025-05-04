package org.example.utils;

import org.example.model.UserDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    public static boolean validateUserAttributes(UserDto userDto) {
        if(userDto.getPassword().length() < 8 || userDto.getPassword().length() > 16){
            return false;
        }
        final Pattern PATTERN=Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if(!PATTERN.matcher(userDto.getEmail()).matches()){
            return false;
        }
        return true;
    }
}
