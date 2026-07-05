package com.edutaskboard.config;

import com.edutaskboard.model.*;
import com.edutaskboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepo;
    @Autowired private SchoolClassRepository classRepo;
    @Autowired private TaskRepository taskRepo;
    @Autowired private AnnouncementRepository annRepo;
    @Autowired private TaskSubmissionRepository subRepo;
    @Autowired private PasswordEncoder enc;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private TeacherAssignmentRepository assignmentRepo;
    @Autowired private TimetableSlotRepository timetableSlotRepo;
    @Autowired private SchoolSettingsRepository schoolSettingsRepo;

    @Override
    public void run(String... args) {

        // ===========================
        // IMPORTANT FIX
        // ===========================
        if (userRepo.count() > 0) {
            System.out.println("Database already initialized. Skipping sample data.");
            return;
        }

        // ===========================
        // Admin
        // ===========================

        User admin = user(
                "admin",
                "admin123",
                "Dr. Samuel Okafor",
                "admin@edu.ng",
                User.Role.ADMIN
        );
        userRepo.save(admin);

        // ===========================
        // Teachers
        // ===========================

        User t1 = user(
                "teacher1",
                "teacher123",
                "Mrs. Amaka Nwosu",
                "amaka@edu.ng",
                User.Role.TEACHER
        );
        t1.setSubject("Mathematics");
        t1.setQualification("B.Sc Maths Education");

        User t2 = user(
                "teacher2",
                "teacher123",
                "Mr. Emeka Chukwu",
                "emeka@edu.ng",
                User.Role.TEACHER
        );
        t2.setSubject("English Language");
        t2.setQualification("B.A English");

        User t3 = user(
                "teacher3",
                "teacher123",
                "Ms. Fatima Bello",
                "fatima@edu.ng",
                User.Role.TEACHER
        );
        t3.setSubject("Basic Science");
        t3.setQualification("B.Sc Science Education");

        userRepo.save(t1);
        userRepo.save(t2);
        userRepo.save(t3);

        // ===========================
        // Classes
        // ===========================

        SchoolClass cA = cls(
                "JSS 1A",
                "Junior Secondary 1 Class A",
                2025,
                t1
        );

        SchoolClass cB = cls(
                "JSS 2B",
                "Junior Secondary 2 Class B",
                2025,
                t2
        );

        SchoolClass cC = cls(
                "SSS 3A",
                "Senior Secondary 3 Class A",
                2025,
                t3
        );

        classRepo.save(cA);
        classRepo.save(cB);
        classRepo.save(cC);

        // ===========================
        // Students
        // ===========================

        String[] names = {
                "Chidi Obi",
                "Ngozi Eze",
                "Tunde Adeyemi",
                "Blessing Uchenna",
                "Kemi Afolabi"
        };

        SchoolClass[] classes = {
                cA,
                cA,
                cB,
                cB,
                cC
        };

        for (int i = 0; i < names.length; i++) {

            User s = user(
                    "student" + (i + 1),
                    "student123",
                    names[i],
                    "student" + (i + 1) + "@edu.ng",
                    User.Role.STUDENT
            );

            s.setStudentId("STU00" + (i + 1));
            s.setSchoolClass(classes[i]);
            s.setParentName("Parent of " + names[i]);
            s.setParentPhone("080" + (10000000 + i));

            userRepo.save(s);
        }

        // ===========================
        // Tasks
        // ===========================

        Task tk1 = task(
                "Algebra Equations – Ch.5",
                "Solve quadratic equations pg 120-125. Show working.",
                LocalDate.now().plusDays(5),
                Task.Priority.HIGH,
                t1,
                cA,
                "Mathematics",
                100
        );

        Task tk2 = task(
                "Essay: My Future Career",
                "Write 500 words about your dream career and how to achieve it.",
                LocalDate.now().plusDays(3),
                Task.Priority.MEDIUM,
                t2,
                cB,
                "English Language",
                50
        );

        Task tk3 = task(
                "Photosynthesis Diagram",
                "Draw and label a photosynthesis diagram.",
                LocalDate.now().plusDays(7),
                Task.Priority.LOW,
                t3,
                cC,
                "Basic Science",
                80
        );

        Task tk4 = task(
                "Times Table Quiz",
                "Practice 1-15 tables.",
                LocalDate.now().plusDays(2),
                Task.Priority.HIGH,
                t1,
                cA,
                "Mathematics",
                30
        );

        taskRepo.save(tk1);
        taskRepo.save(tk2);
        taskRepo.save(tk3);
        taskRepo.save(tk4);

        // ===========================
        // Sample submission
        // ===========================

        User s1 = userRepo.findByUsername("student1").orElseThrow();

        TaskSubmission sub = new TaskSubmission();
        sub.setTask(tk1);
        sub.setStudent(s1);
        sub.setContent("Q1 solved.");
        sub.setStatus(TaskSubmission.SubmissionStatus.GRADED);
        sub.setScore(87);
        sub.setTeacherFeedback("Well done!");

        subRepo.save(sub);

        // ===========================
        // Announcements
        // ===========================

        ann(
                "Welcome to EduTaskBoard",
                "Welcome to the new academic session.",
                Announcement.AudienceType.ALL,
                admin,
                true
        );

        ann(
                "Mid-Term Exams",
                "Exam timetable has been released.",
                Announcement.AudienceType.STUDENTS,
                admin,
                false
        );

        ann(
                "Staff Meeting",
                "Friday 3PM.",
                Announcement.AudienceType.TEACHERS,
                admin,
                false
        );

        // ===========================
        // Subjects
        // ===========================

        Subject math = subject("Mathematics", "MTH", "Math");
        Subject eng = subject("English Language", "ENG", "English");
        Subject sci = subject("Basic Science", "SCI", "Science");

        subjectRepo.save(math);
        subjectRepo.save(eng);
        subjectRepo.save(sci);

        assignmentRepo.save(assignment(t1, math, cA));
        assignmentRepo.save(assignment(t2, eng, cB));
        assignmentRepo.save(assignment(t3, sci, cC));

        SchoolSettings settings = new SchoolSettings();
        settings.setId(1L);
        settings.setDailyStartTime(java.time.LocalTime.of(8, 0));

        schoolSettingsRepo.save(settings);

        timetableSlotRepo.save(slot(cA, math, t1, TimetableSlot.SchoolDay.MONDAY, 1));
        timetableSlotRepo.save(slot(cA, math, t1, TimetableSlot.SchoolDay.MONDAY, 2));
        timetableSlotRepo.save(slot(cB, eng, t2, TimetableSlot.SchoolDay.MONDAY, 1));
        timetableSlotRepo.save(slot(cC, sci, t3, TimetableSlot.SchoolDay.TUESDAY, 1));

        System.out.println("EduTaskBoard initialized successfully.");
    }

    private User user(String username, String pass, String name, String email, User.Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(enc.encode(pass));
        u.setFullName(name);
        u.setEmail(email);
        u.setRole(role);
        u.setEnabled(true);
        u.setApprovalStatus(User.ApprovalStatus.APPROVED);
        return u;
    }

    private SchoolClass cls(String name, String desc, int year, User teacher) {
        SchoolClass c = new SchoolClass();
        c.setName(name);
        c.setDescription(desc);
        c.setAcademicYear(year);
        c.setClassTeacher(teacher);
        return c;
    }

    private Task task(String title, String desc, LocalDate due, Task.Priority priority,
                      User teacher, SchoolClass cls, String subject, int maxScore) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(desc);
        t.setDueDate(due);
        t.setPriority(priority);
        t.setTeacher(teacher);
        t.setSchoolClass(cls);
        t.setSubject(subject);
        t.setMaxScore(maxScore);
        t.setStatus(Task.TaskStatus.ACTIVE);
        return t;
    }

    private Subject subject(String name, String code, String desc) {
        Subject s = new Subject();
        s.setName(name);
        s.setCode(code);
        s.setDescription(desc);
        return s;
    }

    private TeacherAssignment assignment(User teacher, Subject subject, SchoolClass cls) {
        TeacherAssignment t = new TeacherAssignment();
        t.setTeacher(teacher);
        t.setSubject(subject);
        t.setSchoolClass(cls);
        return t;
    }

    private TimetableSlot slot(SchoolClass cls, Subject subject, User teacher,
                               TimetableSlot.SchoolDay day, int period) {
        TimetableSlot s = new TimetableSlot();
        s.setSchoolClass(cls);
        s.setSubject(subject);
        s.setTeacher(teacher);
        s.setDay(day);
        s.setPeriodNumber(period);
        return s;
    }

    private void ann(String title, String content,
                     Announcement.AudienceType audience,
                     User author,
                     boolean pinned) {

        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setAudience(audience);
        a.setAuthor(author);
        a.setPinned(pinned);

        annRepo.save(a);
    }
}