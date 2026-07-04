package com.edutaskboard.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_classes")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
    private int academicYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private User classTeacher;

    @OneToMany(mappedBy = "schoolClass", fetch = FetchType.EAGER)
    private List<User> students = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }
    public User getClassTeacher() { return classTeacher; }
    public void setClassTeacher(User classTeacher) { this.classTeacher = classTeacher; }
    public List<User> getStudents() { return students; }
    public void setStudents(List<User> students) { this.students = students; }
}
