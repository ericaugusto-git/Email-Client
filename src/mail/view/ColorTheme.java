package mail.view;

public enum ColorTheme {
    LIGHT,
    DEFAULT,
    DARK;

    public static String getCssPath(ColorTheme colorTheme){
        switch (colorTheme){
            case LIGHT:
                return "mail/view/css/themeLight.css";
            case DARK:
                return "mail/view/css/themeDark.css";
            case DEFAULT:
                return "mail/view/css/themeDefault.css";
            default:
                return null;
        }
    }
}
