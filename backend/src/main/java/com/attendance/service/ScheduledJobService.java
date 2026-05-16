package com.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobService {
    // Scheduled jobs are implemented in:
    // com.attendance.scheduler.AbsenteeMarkingJob
    // com.attendance.scheduler.DailySummaryJob
    // com.attendance.scheduler.DeviceHealthCheckJob
}
