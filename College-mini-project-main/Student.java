public class Student {
    private final String name;
    private final String department;
    private final int semester;
    private final String phone;
    private final String email;

    public Student(String name, String department, int semester, String phone, String email) {
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.phone = phone;
        this.email = email;
    }

    public String getName() { return name; }
    public String getDepartment() { return department; }
    public int getSemester() { return semester; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}