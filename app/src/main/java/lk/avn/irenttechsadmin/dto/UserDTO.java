package lk.avn.irenttechsadmin.dto;

public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String contact;
    private Boolean active;

    public UserDTO() {
    }

    public UserDTO(String email, Boolean active) {
        this.email = email;
        this.active = active;
    }

    public UserDTO(String id, String name, String email, String contact, Boolean active) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
