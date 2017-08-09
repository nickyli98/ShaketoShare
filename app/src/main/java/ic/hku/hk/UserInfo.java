package ic.hku.hk;

public class UserInfo {

    private final String name;
    private final String email;
    private final String phone;
    private final String company;

    public UserInfo(String name, String email, String phone, String company) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompany() {
        return company;
    }
}
