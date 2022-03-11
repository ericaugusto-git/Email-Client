package mail.view;

public enum FontSize {
    SMALL,
    MEDIUM,
    BIG;

    public static String getCssPath(FontSize fontSize){
        switch (fontSize){
            case BIG:
                return "mail/view/css/fontBig.css";
            case MEDIUM:
                return "mail/view/css/fontMedium.css";
            case SMALL:
                return "mail/view/css/fontSmall.css";
            default:
                return null;
        }
    }
}
