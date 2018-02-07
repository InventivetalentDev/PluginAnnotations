package org.inventivetalent.pluginannotations.utils;

import org.bukkit.ChatColor;

/**
 * Created by shell on 2018/1/15.
 * <p>
 * Github: https://github.com/shellljx
 */
public class ChatColorUtils {

    /**
     * @param altColorChar
     * @param textToEncode
     * @return
     */
    public static String encodeAlternateColorCodes(char altColorChar, String textToEncode) {
        char[] b = textToEncode.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = altColorChar;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
