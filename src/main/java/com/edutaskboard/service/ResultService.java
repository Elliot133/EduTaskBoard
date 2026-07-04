package com.edutaskboard.service;

import com.edutaskboard.model.*;
import com.edutaskboard.repository.AssessmentAttemptRepository;
import com.edutaskboard.repository.AssessmentRepository;
import com.edutaskboard.repository.ReportCardRepository;
import com.edutaskboard.repository.SubjectResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 1: Automatic Result System.
 * Teachers never calculate totals — everything below is derived automatically the moment
 * every subject teacher has fully marked CA1, CA2 and Exam for every student in the class.
 */
@Service
public class ResultService {

    @Autowired private AssessmentRepository assessmentRepo;
    @Autowired private AssessmentAttemptRepository attemptRepo;
    @Autowired private SubjectResultRepository subjectResultRepo;
    @Autowired private ReportCardRepository reportCardRepo;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private NotificationService notificationService;

    /** 75–100 = A, 65–74.5 = B, 55–64.5 = C, below 55 = D. */
    public static String grade(double score) {
        if (score >= 75) return "A";
        if (score >= 65) return "B";
        if (score >= 55) return "C";
        return "D";
    }

    /**
     * Checks whether CA1/CA2/Exam are all fully marked for every student in this class/subject,
     * and if so (re)computes every student's SubjectResult automatically. Safe to call after
     * any single attempt is marked — it simply no-ops until the whole class is complete.
     */
    public void checkAndGenerateSubjectResults(Subject subject, SchoolClass schoolClass) {
        List<User> students = schoolClass.getStudents();
        if (students.isEmpty()) return;

        Assessment ca1 = assessmentRepo.findBySubjectAndSchoolClassAndType(subject, schoolClass, Assessment.AssessmentType.CA1).orElse(null);
        Assessment ca2 = assessmentRepo.findBySubjectAndSchoolClassAndType(subject, schoolClass, Assessment.AssessmentType.CA2).orElse(null);
        Assessment exam = assessmentRepo.findBySubjectAndSchoolClassAndType(subject, schoolClass, Assessment.AssessmentType.EXAM).orElse(null);
        if (ca1 == null || ca2 == null || exam == null) return; // not all three assessments exist yet

        Map<Long, int[]> scoresByStudent = new HashMap<>(); // [ca1, ca2, exam]
        User teacher = ca1.getTeacher();
        for (User student : students) {
            AssessmentAttempt a1 = attemptRepo.findByAssessmentAndStudent(ca1, student).orElse(null);
            AssessmentAttempt a2 = attemptRepo.findByAssessmentAndStudent(ca2, student).orElse(null);
            AssessmentAttempt a3 = attemptRepo.findByAssessmentAndStudent(exam, student).orElse(null);
            if (!isComplete(a1) || !isComplete(a2) || !isComplete(a3)) {
                return; // at least one student isn't fully marked yet — wait
            }
            scoresByStudent.put(student.getId(), new int[]{
                    nz(a1.getTotalScore()), nz(a2.getTotalScore()), nz(a3.getTotalScore())
            });
        }

        // Every student is fully marked — compute class-wide stats.
        List<Integer> totals = scoresByStudent.values().stream().map(s -> s[0] + s[1] + s[2]).collect(Collectors.toList());
        double highest = totals.stream().mapToInt(Integer::intValue).max().orElse(0);
        double average = Math.round(totals.stream().mapToInt(Integer::intValue).average().orElse(0) * 10) / 10.0;

        for (User student : students) {
            int[] s = scoresByStudent.get(student.getId());
            int total = s[0] + s[1] + s[2];
            SubjectResult sr = subjectResultRepo.findByStudentAndSubjectAndSchoolClass(student, subject, schoolClass)
                    .orElseGet(SubjectResult::new);
            sr.setStudent(student); sr.setSubject(subject); sr.setSchoolClass(schoolClass); sr.setTeacher(teacher);
            sr.setCa1(s[0]); sr.setCa2(s[1]); sr.setExam(s[2]); sr.setTotal(total);
            sr.setHighest(highest); sr.setAverage(average);
            sr.setGrade(grade(total));
            sr.setSubjectPosition(rank(total, totals));
            subjectResultRepo.save(sr);
        }

        checkAndGenerateReportCards(schoolClass);
    }

    private boolean isComplete(AssessmentAttempt a) {
        return a != null && a.isFullyMarked() && a.getTotalScore() != null;
    }

    private int nz(Integer i) { return i == null ? 0 : i; }

    /** Standard competition ranking (ties share a rank; next rank skips accordingly), descending by score. */
    private int rank(int value, List<Integer> all) {
        long higherCount = all.stream().filter(v -> v > value).count();
        return (int) higherCount + 1;
    }

    /**
     * Once every subject taught in the class has a SubjectResult for every student,
     * automatically assembles the class's report cards.
     */
    public void checkAndGenerateReportCards(SchoolClass schoolClass) {
        List<User> students = schoolClass.getStudents();
        if (students.isEmpty()) return;

        Set<Subject> subjectsTaught = assignmentService.byClass(schoolClass).stream()
                .map(TeacherAssignment::getSubject).collect(Collectors.toSet());
        if (subjectsTaught.isEmpty()) return;

        Map<Long, List<SubjectResult>> resultsByStudent = new HashMap<>();
        for (User student : students) {
            List<SubjectResult> results = subjectResultRepo.findByStudentAndSchoolClass(student, schoolClass);
            Set<Long> coveredSubjectIds = results.stream().map(r -> r.getSubject().getId()).collect(Collectors.toSet());
            boolean allCovered = subjectsTaught.stream().allMatch(sub -> coveredSubjectIds.contains(sub.getId()));
            if (!allCovered) return; // this student isn't complete yet — whole class waits
            resultsByStudent.put(student.getId(), results);
        }

        List<Integer> overallTotals = resultsByStudent.values().stream()
                .map(list -> list.stream().mapToInt(SubjectResult::getTotal).sum())
                .collect(Collectors.toList());

        for (User student : students) {
            List<SubjectResult> results = resultsByStudent.get(student.getId());
            int overallTotal = results.stream().mapToInt(SubjectResult::getTotal).sum();
            int numSubjects = results.size();
            double overallAverage = Math.round((overallTotal * 10.0) / numSubjects) / 10.0;

            ReportCard rc = reportCardRepo.findByStudentAndSchoolClass(student, schoolClass).orElseGet(ReportCard::new);
            boolean isNew = rc.getId() == null;
            rc.setStudent(student); rc.setSchoolClass(schoolClass);
            rc.setOverallTotal(overallTotal);
            rc.setOverallAverage(overallAverage);
            rc.setOverallGrade(grade(overallAverage));
            rc.setNumberOfSubjects(numSubjects);
            rc.setOverallPosition(rank(overallTotal, overallTotals));
            rc.setAttendancePercent(attendanceService.attendancePercentage(student));
            reportCardRepo.save(rc);

            if (isNew) {
                notificationService.notify(student, Notification.NotificationType.RESULT,
                        "Report Card Ready", "Your report card for " + schoolClass.getName() + " has been generated.",
                        "/student/results");
            }
        }
    }

    public Optional<ReportCard> reportCardFor(User student, SchoolClass schoolClass) {
        return reportCardRepo.findByStudentAndSchoolClass(student, schoolClass);
    }

    public List<SubjectResult> subjectResultsFor(User student, SchoolClass schoolClass) {
        return subjectResultRepo.findByStudentAndSchoolClass(student, schoolClass);
    }

    public List<ReportCard> reportCardsFor(SchoolClass schoolClass) { return reportCardRepo.findBySchoolClass(schoolClass); }

    public List<SubjectResult> subjectResultsForTeacher(Subject subject, SchoolClass schoolClass) {
        return subjectResultRepo.findBySubjectAndSchoolClass(subject, schoolClass);
    }

    public void saveTeacherRemark(Long subjectResultId, String remark) {
        subjectResultRepo.findById(subjectResultId).ifPresent(sr -> { sr.setTeacherRemark(remark); subjectResultRepo.save(sr); });
    }

    public void saveClassTeacherRemark(Long reportCardId, String remark) {
        reportCardRepo.findById(reportCardId).ifPresent(rc -> { rc.setClassTeacherRemark(remark); reportCardRepo.save(rc); });
    }

    public void savePrincipalRemark(Long reportCardId, String remark) {
        reportCardRepo.findById(reportCardId).ifPresent(rc -> { rc.setPrincipalRemark(remark); reportCardRepo.save(rc); });
    }
}
