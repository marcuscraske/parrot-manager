# Keyboard Layouts
In order to send passwords to other applications, physical keys are emulated.

This can be problematic as different locales and/or keyboard layouts use different key-codes for the same characters.
A good example is " and @ between British and US keyboards. This can be further complicated by operating systems, as
the default British keyboard layout for Mac is different to Windows/Linux.


## Automatic Detection
The keyboard layout used automatically is determined based on finding the layout with the best match.

This first starts by filtering layouts by the locale used with Parrot, as set by the operating system (or parameters
passed to the Java virtual machine). If multiple layouts exist for a locale, this is further filtered by matching
the operating system. If the operating system is not an exact match, a layout wildcarded for all operating systems
is preferred.

If automatic detection is incorrect, you can override the layout in the settings page.


## Custom Layouts
You can create a custom layout file.

By default all key codes use the US keyboard layout, but this has issues with special characters between different
locales. So you may only need to specify special characters to be mapped differently.

These layouts are read from three places with the following priority, as a layout with the same name will replace
the prior loaded layout:

* Internally stored in the application's binary (or rather JAR / class-path).
* Local user's parrot manager preferences/settings directory. This is usually `.settings/parrot-manager` or
  `%APPDATA\parrot-manager`.
* Working directory of the parrot-manager process.

The latter two directories can be opened from the settings page.

### Format
The file-name is irrelevant, but the contents are important.

Each line should either be meta data or key-mapping.

Blank lines, or lines starting with: `//`, `#` or `;`; will be ignored.

### Format: Meta Data
The following items are mandatory:
* `name` - the name of the keyboard layout.
* `locale` - the locale e.g. en-GB or en-US; used for automatic detection.
* `os` - the os filter, either: * (for all), windows, mac or linux.

The line should start with the above, followed by a space, and then the value. The value can contain spaces for names.

### Format: Key Mappings
The line should start with the text character to be mapped, followed by the keyboard key-code separated by a space.

An example to map "$" to hold keys SHIFT+4:

````
$    VK_SHIFT    VK_4
````

The key codes can be numeric, or as in the above case, constants from the Java `KeyEvent` class:
<https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html>

### Example for en-GB

````
name    en-GB
locale  en-GB
os      *

`    VK_BACK_QUOTE
-    VK_MINUS
=    VK_EQUALS
~    VK_SHIFT    VK_NUMBER_SIGN
!    VK_SHIFT    VK_1
@    VK_SHIFT    VK_QUOTE
#    VK_NUMBER_SIGN
$    VK_SHIFT    VK_4
%    VK_SHIFT    VK_5
^    VK_SHIFT    VK_6
&    VK_SHIFT    VK_7
*    VK_SHIFT    VK_8
(    VK_LEFT_PARENTHESIS
)    VK_RIGHT_PARENTHESIS
_    VK_SHIFT    VK_MINUS
+    VK_SHIFT    VK_EQUALS
\t    VK_TAB
\n    VK_ENTER
[    VK_OPEN_BRACKET
]    VK_CLOSE_BRACKET
\    VK_BACK_SLASH
{    VK_SHIFT    VK_OPEN_BRACKET
}    VK_SHIFT    VK_CLOSE_BRACKET
|    VK_SHIFT    VK_BACK_SLASH
;    VK_SEMICOLON
:    VK_SHIFT    VK_SEMICOLON
'    VK_QUOTE
"    VK_SHIFT    VK_2
,    VK_COMMA
<    VK_SHIFT    VK_COMMA
>    VK_SHIFT    VK_PERIOD
.    VK_PERIOD
/    VK_SLASH
?    VK_SHIFT    VK_SLASH
````
