package com.edutaskboard.service;

import com.edutaskboard.dto.ContactDto;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.TeacherAssignment;
import com.edutaskboard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds the messaging contact list for a user and enforces who is allowed to
 * privately message whom, per the School Management System's messaging rules:
 *
 *  - Admin        -> every Teacher, every Student, General Chat.
 *  - Class Teacher-> Admin, General Chat, teachers assigned to the same class(es),
 *                    every student in their class(es) plus every student they teach.
 *  - Non-Class     -> Admin, General Chat, teachers assigned to the same class(es),
 *    Teacher          only the students they personally teach.
 *  - Student      -> Admin, General Chat, their Class Teacher(s), other teachers
 *                    teaching their class, students in their own class.
 *
 * Contact list order is always: General (pinned) -> Admin (pinned, except for Admin
 * viewing their own list) -> Teachers (alphabetical) -> Students (alphabetical).
 */
@Service
public class MessagingAccessService {

    @Autowired private UserService userService;
    @Autowired private SchoolClassService classService;
    @Autowired private TeacherAssignmentService assignmentService;

    /** Returns the ordered contact list to render in the sidebar for this user. */
    public List<ContactDto> buildContactList(User user) {
        List<ContactDto> result = new ArrayList<>();
        result.add(new ContactDto("general", "GENERAL", null, "General", null));

        if (user.getRole() != User.Role.ADMIN) {
            userService.byRole(User.Role.ADMIN).stream().findFirst().ifPresent(admin ->
                    result.add(contact(admin)));
        }

        List<User> teachers = relevantTeachers(user);
        List<User> students = relevantStudents(user);

        teachers.stream()
                .filter(t -> !t.getId().equals(user.getId()))
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .forEach(t -> result.add(contact(t)));

        students.stream()
                .filter(s -> !s.getId().equals(user.getId()))
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .forEach(s -> result.add(contact(s)));

        return result;
    }

    /** Whether `sender` may privately message `receiver`. General Chat is always allowed for any authenticated user. */
    public boolean canMessagePrivately(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) return false;
        if (sender.getRole() == User.Role.ADMIN) return true; // admin can message anyone
        if (receiver.getRole() == User.Role.ADMIN) return true; // everyone can message admin

        return relevantTeachers(sender).stream().anyMatch(t -> t.getId().equals(receiver.getId()))
                || relevantStudents(sender).stream().anyMatch(s -> s.getId().equals(receiver.getId()));
    }

    private ContactDto contact(User u) {
        return new ContactDto("u-" + u.getId(), "PRIVATE", u.getId(), u.getFullName(), u.getRole().name());
    }

    /** Teachers relevant to this user, per the rules above. Empty for STUDENT-role callers is handled separately. */
    private List<User> relevantTeachers(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return new ArrayList<>(userService.byRole(User.Role.TEACHER));
            case TEACHER: {
                List<SchoolClass> classTeacherOf = classService.getAll().stream()
                        .filter(c -> c.getClassTeacher() != null && c.getClassTeacher().getId().equals(user.getId()))
                        .toList();
                Set<User> set = new LinkedHashSet<>();
                if (!classTeacherOf.isEmpty()) {
                    // Class Teacher: every teacher assigned to the same class(es).
                    for (SchoolClass c : classTeacherOf) {
                        for (TeacherAssignment ta : assignmentService.byClass(c)) set.add(ta.getTeacher());
                    }
                } else {
                    // Non-Class Teacher: teachers assigned to the same class(es) they also teach.
                    Set<SchoolClass> myClasses = assignmentService.byTeacher(user).stream()
                            .map(TeacherAssignment::getSchoolClass)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    for (SchoolClass c : myClasses) {
                        for (TeacherAssignment ta : assignmentService.byClass(c)) set.add(ta.getTeacher());
                        if (c.getClassTeacher() != null) set.add(c.getClassTeacher());
                    }
                }
                set.remove(user);
                return new ArrayList<>(set);
            }
            case STUDENT: {
                Set<User> set = new LinkedHashSet<>();
                if (user.getSchoolClass() != null) {
                    SchoolClass sc = user.getSchoolClass();
                    if (sc.getClassTeacher() != null) set.add(sc.getClassTeacher());
                    for (TeacherAssignment ta : assignmentService.byClass(sc)) set.add(ta.getTeacher());
                }
                return new ArrayList<>(set);
            }
            default:
                return List.of();
        }
    }

    /** Students relevant to this user, per the rules above. Empty for STUDENT-role callers other than classmates. */
    private List<User> relevantStudents(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return new ArrayList<>(userService.byRole(User.Role.STUDENT));
            case TEACHER: {
                List<SchoolClass> classTeacherOf = classService.getAll().stream()
                        .filter(c -> c.getClassTeacher() != null && c.getClassTeacher().getId().equals(user.getId()))
                        .toList();
                Set<User> set = new LinkedHashSet<>();
                if (!classTeacherOf.isEmpty()) {
                    // Class Teacher: every student in their class(es) plus every student they teach.
                    for (SchoolClass c : classTeacherOf) set.addAll(c.getStudents());
                    for (TeacherAssignment ta : assignmentService.byTeacher(user)) set.addAll(ta.getSchoolClass().getStudents());
                } else {
                    // Non-Class Teacher: only students they personally teach.
                    for (TeacherAssignment ta : assignmentService.byTeacher(user)) set.addAll(ta.getSchoolClass().getStudents());
                }
                return new ArrayList<>(set);
            }
            case STUDENT: {
                if (user.getSchoolClass() == null) return List.of();
                Set<User> set = new LinkedHashSet<>(user.getSchoolClass().getStudents());
                return new ArrayList<>(set);
            }
            default:
                return List.of();
        }
    }
}
