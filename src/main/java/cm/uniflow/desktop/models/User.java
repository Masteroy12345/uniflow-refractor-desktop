package cm.uniflow.desktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    private String name;
    private String email;
    private String role; // lowercase campus role: student, delegate, teacher, admin
    private String status;
    private String matricule;
    private String university;
    private String faculty;
    private String program;
    private String level;
    private String firstName;
    private String lastName;
    private String photoUrl;

    /** Empty constructor for Jackson; intentionally contains no demo identity. */
    public User() {}

    public User(String id, String name, String email, String role, String matricule, String program, String level) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = "active";
        this.matricule = matricule;
        this.university = "";
        this.faculty = "";
        this.program = program;
        this.level = level;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getLevel() { return level; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setLevel(String level) { this.level = level; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
