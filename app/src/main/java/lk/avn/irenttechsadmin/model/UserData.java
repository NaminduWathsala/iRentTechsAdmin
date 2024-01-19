package lk.avn.irenttechsadmin.model;

public class UserData {
    private String id;
    private String name;
    private String email;
    private String contact;
    private String active;

    public UserData() {
    }

    public UserData(String id, String name, String email, String contact) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contact = contact;
    }

    public UserData(String id, String name, String email, String contact, String active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.active = active;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
