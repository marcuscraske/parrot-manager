package com.limpygnome.parrot.component.sendKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Layout of keys specific to a type of keyboard, based on locale and operating system.
 */
public class KeyboardLayout
{
    private static final Logger LOG = LoggerFactory.getLogger(KeyboardLayout.class);

    private String name;
    private String locale;
    private String os;

    private Map<Character, int[]> keys;

    private KeyboardLayout()
    {
        keys = new HashMap<>();
    }

    public int[] convert(char character)
    {
        // attempt to find in map first
        int[] result = keys.get(character);

        if (result == null)
        {
            // fall back to jre
            int key = KeyEvent.getExtendedKeyCodeForChar(character);
            if (Character.isUpperCase(character))
            {
                result = new int[] { KeyEvent.VK_SHIFT, key };
            }
            else
            {
                result = new int[] { KeyEvent.VK_SHIFT };
            }
        }

        return result;
    }

    boolean isBetterMatch(KeyboardLayout layout, String currentOs, String currentLocale)
    {
        boolean localeMatch = Objects.equals(locale, currentLocale);
        boolean otherLocaleMatch = Objects.equals(layout.locale, currentLocale);

        if (localeMatch && otherLocaleMatch)
        {
            // os will determine better layout
            boolean osMatch = Objects.equals(os, currentOs);
            boolean otherOsMatch = Objects.equals(layout.os, currentOs);

            if (osMatch && otherOsMatch)
            {
                // just go with this one, but lets output a warning
                LOG.warn("multiple keyboard layouts conflicting - '{}' and '{}'", name, layout.name);
                return false;
            }
            else if (!osMatch && !otherLocaleMatch)
            {
                // no os is always better
                return os == null;
            }
            else
            {
                // this is a better match if the os is matching
                return osMatch;
            }
        }
        else
        {
            // this is a better match as locale is matching (and other layout is not)
            return localeMatch;
        }
    }

    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

    String getLocale()
    {
        return locale;
    }

    void setLocale(String locale)
    {
        this.locale = (locale != null ? locale.toLowerCase() : null);
    }

    String getOs()
    {
        return os;
    }

    void setOs(String os)
    {
        this.os = (os != null ? os : null);
    }

    static KeyboardLayout parse(String config, List<String> messages, String nameForLogging)
    {
        KeyboardLayout layout = new KeyboardLayout();

        // read line-by-line; ignore //, # or ; at start
        String[] lines = config.split("\n");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
        {
            String line = lines[lineIndex].trim();
            boolean ignore = line.isEmpty() || line.startsWith("//") || line.startsWith("#") || line.startsWith(";");

            if (!ignore)
            {
                parseLine(layout, line, messages, nameForLogging, lineIndex + 1);
            }
        }

        // enforce mandatory options
        if (layout.name == null)
        {
            messages.add("mandatory meta-data 'name' missing");
            throw new IllegalStateException("name must be defined - keyboard layout: " + nameForLogging);
        }
        else if (layout.locale == null)
        {
            messages.add("mandatory meta-data 'locale' missing");
            throw new IllegalStateException("locale must be defined - keyboard layout: " + nameForLogging);
        }

        return layout;
    }

    private static void parseLine(KeyboardLayout layout, String line, List<String> messages, String nameForLogging, int lineNumber)
    {
        // split by white-space
        String[] parts = splitIntoColumns(line);

        boolean handled = false;

        if (parts.length >= 2)
        {
            String key = parts[0];

            // handle layout settings
            switch (key.toLowerCase())
            {
                case "name":
                    String name = Arrays.stream(parts).skip(1).collect(Collectors.joining(" "));
                    layout.setName(name);
                    handled = true;
                    LOG.debug("layout: {}, line: {} - name parsed", nameForLogging, lineNumber);
                    break;
                case "locale":
                    layout.setLocale(parts[1]);
                    handled = true;
                    LOG.debug("layout: {}, line: {} - locale parsed", nameForLogging, lineNumber);
                    break;
                case "os":
                    String os = parts[1];
                    if (!"*".equals(os))
                    {
                        layout.setOs(os);
                    }
                    handled = true;
                    LOG.debug("layout: {}, line: {} - os parsed", nameForLogging, lineNumber);
                    break;
            }

            // handle as key
            if (!handled)
            {
                Character character = parseCharacter(key);

                if (character != null)
                {
                    int[] nativeKeys = parseNativeKeys(parts);

                    if (nativeKeys.length == 0)
                    {
                        LOG.warn("layout: {}, line: {} - ignored key '{}' as unable to map native keys", nameForLogging, lineNumber, key);
                        messages.add("layout: " + nameForLogging + ", line: " + lineNumber + " - unable to map key '" + key + "'");
                    }
                    else
                    {
                        layout.keys.put(character, nativeKeys);
                        handled = true;

                        LOG.debug("layout: {}, line: {} - key '{}' mapped with {} native keys", nameForLogging, lineNumber, key, nativeKeys.length);
                    }
                }
                else
                {
                    LOG.warn("layout: {}, line: {} - unknown character '{}'", nameForLogging, lineNumber, key);
                    messages.add("layout: " + nameForLogging + ", line: " + lineNumber + " - unknown character '" + key + "'");
                }
            }
        }

        if (!handled)
        {
            LOG.warn("layout: {}, line: {} - invalid line", nameForLogging, lineNumber);
            messages.add("layout: " + nameForLogging + ", line: " + lineNumber + " - malformed line, ignored");
        }
    }

    private static String[] splitIntoColumns(String line)
    {
        return Arrays.stream(line.split("\\s"))
                .map(s -> s.trim())
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);
    }

    private static Character parseCharacter(String part)
    {
        Character result;

        if (part.length() == 1)
        {
            result = part.charAt(0);
        }
        else
        {
            switch (part)
            {
                case "\\n":
                    result = '\n';
                    break;
                case "\\t":
                    result = '\t';
                    break;
                case "\\r":
                    result = '\r';
                    break;
                default:
                    result = null;
                    break;
            }
        }

        return result;
    }

    private static int[] parseNativeKeys(String[] parts)
    {
        return Arrays.stream(parts)
                .skip(1)
                .map(KeyboardLayout::parseNativeKey)
                .filter(integer -> integer > 0)
                .mapToInt(i -> i)
                .toArray();
    }

    private static int parseNativeKey(String part)
    {
        int result;

        // parse constant
        if (part.startsWith("VK_"))
        {
            try
            {
                // use reflection to pull constant
                Field field = KeyEvent.class.getDeclaredField(part);
                result = field.getInt(null);
            }
            catch (Exception e)
            {
                result = 0;
                LOG.warn("unable to extract native key from constant '{}'", part);
            }
        }
        // literal key
        else if (part.length() == 1)
        {
            result = KeyEvent.getExtendedKeyCodeForChar(part.charAt(0));
        }
        else
        {
            // attempt to parse as number
            try
            {
                result = Integer.parseInt(part);
            }
            catch (NumberFormatException e)
            {
                result = 0;
                LOG.warn("unable to parse as integer for '{}'", part);
            }
        }

        return result;
    }

}
