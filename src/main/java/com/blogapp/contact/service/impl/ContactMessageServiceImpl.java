package com.blogapp.contact.service.impl;

import com.blogapp.common.exception.ResourceNotFoundException;
import com.blogapp.contact.dto.request.ContactMessageRequest;
import com.blogapp.contact.dto.response.ContactMessageResponse;
import com.blogapp.contact.entity.ContactMessage;
import com.blogapp.contact.entity.ContactSubject;
import com.blogapp.contact.enums.ContactStatus;
import com.blogapp.contact.mapper.ContactMapper;
import com.blogapp.contact.repository.ContactMessageRepository;
import com.blogapp.contact.repository.ContactSubjectRepository;
import com.blogapp.contact.service.ContactMessageService;
import com.blogapp.otp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository messageRepository;
    private final ContactSubjectRepository subjectRepository;
    private final ContactMapper contactMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public ContactMessageResponse submitMessage(ContactMessageRequest request) {
        ContactSubject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        ContactMessage message = ContactMessage.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .emailAddress(request.getEmailAddress())
                .subjectId(subject.getId())
                .messageText(request.getMessageText())
                .build();

        message = messageRepository.save(message);

        // Notify admin via email
        log.info("Sending Contact Us admin notification for message from: {}", request.getFullName());
        emailService.sendContactUsAdminNotification(
                request.getFullName(),
                request.getEmailAddress(),
                request.getPhoneNumber(),
                subject.getName(),
                request.getMessageText()
        );

        return contactMapper.toContactMessageResponse(message, subject);
    }

    @Override
    public Page<ContactMessageResponse> getMessages(ContactStatus status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ContactMessage> messages;
        if (status != null) {
            messages = messageRepository.findByStatus(status, pageable);
        } else {
            messages = messageRepository.findAll(pageable);
        }

        return messages.map(message -> {
            ContactSubject subject = null;
            if (message.getSubjectId() != null) {
                subject = subjectRepository.findById(message.getSubjectId()).orElse(null);
            }
            return contactMapper.toContactMessageResponse(message, subject);
        });
    }

    @Override
    @Transactional
    public ContactMessageResponse updateMessageStatus(String id, ContactStatus status) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact Message not found"));

        message.setStatus(status);
        message = messageRepository.save(message);

        ContactSubject subject = subjectRepository.findById(message.getSubjectId()).orElse(null);
        return contactMapper.toContactMessageResponse(message, subject);
    }

    @Override
    @Transactional
    public void deleteMessage(String id) {
        ContactMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact Message not found"));
        messageRepository.delete(message);
    }
}
