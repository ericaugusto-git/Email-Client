package mail.controller.persistence;

import java.io.Serializable;

public class AccountRememberPreference implements Serializable {

    private String address;
    private boolean remember;

    public AccountRememberPreference(String address, boolean remember) {
        this.address = address;
        this.remember = remember;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }
}
