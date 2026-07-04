package com.edutaskboard.dto;

import java.time.LocalTime;

/** A single computed row of a day's timetable: either a teaching period or a fixed break. */
public class TimetableRow {
    public enum RowType { PERIOD, SHORT_BREAK, LONG_BREAK }

    private RowType rowType;
    private Integer periodNumber; // null for breaks
    private LocalTime startTime;
    private LocalTime endTime;
    private String subjectName;   // null/"Free Period" when unassigned
    private String teacherName;
    private Long slotId;
    private boolean freePeriod;
    private boolean substituted;
    private String substituteTeacherName;

    public RowType getRowType() { return rowType; }
    public void setRowType(RowType rowType) { this.rowType = rowType; }
    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public boolean isFreePeriod() { return freePeriod; }
    public void setFreePeriod(boolean freePeriod) { this.freePeriod = freePeriod; }
    public boolean isSubstituted() { return substituted; }
    public void setSubstituted(boolean substituted) { this.substituted = substituted; }
    public String getSubstituteTeacherName() { return substituteTeacherName; }
    public void setSubstituteTeacherName(String substituteTeacherName) { this.substituteTeacherName = substituteTeacherName; }
}
