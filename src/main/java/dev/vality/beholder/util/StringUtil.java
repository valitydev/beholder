package dev.vality.beholder.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.ObjectUtils;

@UtilityClass
public class StringUtil {

    public static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();
        if (!ObjectUtils.isEmpty(str)) {
            char firstLetter = str.charAt(0);
            result.append(Character.toLowerCase(firstLetter));
            for (int i = 1; i < str.length(); i++) {
                char letter = str.charAt(i);
                if (Character.isUpperCase(letter)) {
                    result.append('_');
                    result.append(Character.toLowerCase(letter));
                } else {
                    result.append(letter);
                }
            }
        }
        return result.toString();
    }
}
