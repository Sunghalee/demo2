package com.app.srNsys;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class GoldenSoftWork {

    /** 员工id */
    private String employeeId;

    /** 员工姓名 */
    private String employeeName;

    /** 员工部门 */
    private String department;

    /** 考勤日期 */
    private LocalDate attendanceDate;

    /** 签到时间 */
    private String checkInTime;

    /** 签退时间 */
    private String checkOutTime;

    /** 休息时间 */
    private String lunchBreakTime;

    /** 出勤时长 */
    private String attendanceDuration;
}
