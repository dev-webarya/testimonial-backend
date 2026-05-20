package com.blogapp.demo.service.impl;

import com.blogapp.common.exception.ResourceNotFoundException;
import com.blogapp.demo.dto.request.CancelDemoRequest;
import com.blogapp.demo.dto.request.ScheduleDemoRequest;
import com.blogapp.demo.dto.request.SendOtpRequest;
import com.blogapp.demo.dto.response.ScheduleDemoResponse;
import com.blogapp.demo.entity.Board;
import com.blogapp.demo.entity.DemoSchedule;
import com.blogapp.demo.entity.Grade;
import com.blogapp.demo.enums.DemoScheduleStatus;
import com.blogapp.demo.mapper.DemoMapper;
import com.blogapp.demo.repository.BoardRepository;
import com.blogapp.demo.repository.DemoScheduleRepository;
import com.blogapp.demo.repository.GradeRepository;
import com.blogapp.demo.service.DemoScheduleService;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.EmailService;
import com.blogapp.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoScheduleServiceImpl implements DemoScheduleService {

    private final DemoScheduleRepository scheduleRepository;
    private final BoardRepository boardRepository;
    private final GradeRepository gradeRepository;
    private final DemoMapper demoMapper;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    public boolean sendOtp(SendOtpRequest request) {
        log.info("Sending schedule demo OTP to email: {}", request.getEmail());
        return otpService.sendOtp(request.getEmail(), OtpPurpose.SCHEDULE_DEMO, request.isResend());
    }

    @Override
    @Transactional
    public ScheduleDemoResponse submitScheduleDemo(ScheduleDemoRequest request) {
        // Verify OTP
        otpService.verifyOtp(request.getEmailId(), request.getOtp(), OtpPurpose.SCHEDULE_DEMO);

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        DemoSchedule schedule = DemoSchedule.builder()
                .studentName(request.getStudentName())
                .parentName(request.getParentName())
                .emailId(request.getEmailId())
                .mobileNumber(request.getMobileNumber())
                .boardId(board.getId())
                .gradeId(grade.getId())
                .preferredDate(request.getPreferredDate())
                .preferredTime(request.getPreferredTime())
                .build();

        schedule = scheduleRepository.save(schedule);

        // Notify admin via email
        log.info("Sending Schedule Demo admin notification for: {}", request.getStudentName());
        emailService.sendScheduleDemoAdminNotification(
                request.getStudentName(),
                request.getParentName(),
                request.getEmailId(),
                request.getMobileNumber(),
                board.getName(),
                grade.getName(),
                request.getPreferredDate() != null ? request.getPreferredDate().toString() : "Not specified",
                request.getPreferredTime()
        );

        return demoMapper.toScheduleDemoResponse(schedule, board, grade);
    }

    @Override
    public Page<ScheduleDemoResponse> getSchedules(LocalDate date, DemoScheduleStatus status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DemoSchedule> schedules;
        if (date != null && status != null) {
            schedules = scheduleRepository.findByStatusAndPreferredDate(status, date, pageable);
        } else if (date != null) {
            schedules = scheduleRepository.findByPreferredDate(date, pageable);
        } else if (status != null) {
            schedules = scheduleRepository.findByStatus(status, pageable);
        } else {
            schedules = scheduleRepository.findAll(pageable);
        }

        return schedules.map(schedule -> {
            Board board = null;
            if (schedule.getBoardId() != null) {
                board = boardRepository.findById(schedule.getBoardId()).orElse(null);
            }
            Grade grade = null;
            if (schedule.getGradeId() != null) {
                grade = gradeRepository.findById(schedule.getGradeId()).orElse(null);
            }
            return demoMapper.toScheduleDemoResponse(schedule, board, grade);
        });
    }

    @Override
    @Transactional
    public ScheduleDemoResponse approveSchedule(String id) {
        DemoSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demo Schedule not found"));
        
        schedule.setStatus(DemoScheduleStatus.APPROVED);
        schedule = scheduleRepository.save(schedule);

        Board board = boardRepository.findById(schedule.getBoardId()).orElse(null);
        Grade grade = gradeRepository.findById(schedule.getGradeId()).orElse(null);

        return demoMapper.toScheduleDemoResponse(schedule, board, grade);
    }

    @Override
    @Transactional
    public ScheduleDemoResponse cancelSchedule(String id, CancelDemoRequest request) {
        DemoSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demo Schedule not found"));

        schedule.setStatus(DemoScheduleStatus.CANCELLED);
        schedule.setCancelReason(request.getCancelReason());
        schedule = scheduleRepository.save(schedule);

        Board board = boardRepository.findById(schedule.getBoardId()).orElse(null);
        Grade grade = gradeRepository.findById(schedule.getGradeId()).orElse(null);

        return demoMapper.toScheduleDemoResponse(schedule, board, grade);
    }
}
