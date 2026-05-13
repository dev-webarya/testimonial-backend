package com.blogapp.admin.seeder;

import com.blogapp.answer.entity.Answer;
import com.blogapp.answer.enums.AnswerStatus;
import com.blogapp.answer.repository.AnswerRepository;
import com.blogapp.category.entity.Category;
import com.blogapp.category.repository.CategoryRepository;
import com.blogapp.question.entity.Question;
import com.blogapp.question.repository.QuestionRepository;
import com.blogapp.teacher.entity.Teacher;
import com.blogapp.teacher.repository.TeacherRepository;
import com.blogapp.testimonial.entity.Testimonial;
import com.blogapp.testimonial.repository.TestimonialRepository;
import com.blogapp.runningclass.entity.RunningClass;
import com.blogapp.runningclass.entity.Enrollment;
import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
import com.blogapp.runningclass.enums.EnrollmentStatus;
import com.blogapp.runningclass.repository.RunningClassRepository;
import com.blogapp.runningclass.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private final TeacherRepository teacherRepository;
    private final TestimonialRepository testimonialRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final RunningClassRepository runningClassRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void run(String... args) {
        // Only run if the database is completely empty for Teachers
        if (teacherRepository.count() == 0) {
            log.info("No Teachers found... Seeding dummy Ecosystem data.");
            seedEcosystem();
            log.info("Finished seeding dummy data successfully.");
        } else {
            log.info("Database already contains records. Skipping DummyDataSeeder.");
        }
    }

    private void seedEcosystem() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Seed Teacher
        Teacher mathTeacher = Teacher.builder()
                .fullName("Prof. Alan Math")
                .bio("Advanced Calculus & Algebra Specialist with 10 years at MIT.")
                .photoUrl("https://ui-avatars.com/api/?name=Alan+Math&background=random")
                .category("Science")
                .speciality("Mathematics")
                .mainSubject("Calculus")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        mathTeacher = teacherRepository.save(mathTeacher);

        // 2. Seed Testimonials for Teacher
        Testimonial textReview = Testimonial.builder()
                .text("Prof. Math is incredible! He broke down integrals effortlessly.")
                .mediaUrl("")
                .isPrimary(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        Testimonial videoReview = Testimonial.builder()
                .text("This visual representation really helped my understanding!")
                .mediaUrl("https://res.cloudinary.com/demo/video/upload/elephants.mp4")
                .isPrimary(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        testimonialRepository.save(textReview);
        testimonialRepository.save(videoReview);

        // 3. Seed Category
        Category category = Category.builder()
                .name("Calculus")
                .slug("calculus")
                .createdAt(now)
                .updatedAt(now)
                .build();
        category = categoryRepository.save(category);

        // 4. Seed Questions
        Question q1 = Question.builder()
                .title("What is the integral of x^2?")
                .slug("what-is-integral-x2")
                .descriptionHtml("<p>I am struggling with inverse power rules. Please help explain the calculus behind <strong>x<sup>2</sup></strong> integration.</p>")
                .categoryId(category.getId())
                .adminId("seed-admin-123")
                .createdAt(now)
                .updatedAt(now)
                .build();
        q1 = questionRepository.save(q1);

        // 5. Seed Answers
        Answer approvedAnswer = Answer.builder()
                .questionId(q1.getId())
                .userId("seed-user-123")
                .authorName("Charlie Tutor")
                .contentHtml("<p>The integral of x<sup>2</sup> is exactly <strong>x<sup>3</sup> / 3 + C</strong>.</p>")
                .status(AnswerStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Answer pendingAnswer = Answer.builder()
                .questionId(q1.getId())
                .userId("seed-user-456")
                .authorName("Dave Newbie")
                .contentHtml("<p>I think you just divide by 2?</p>")
                .status(AnswerStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        answerRepository.save(approvedAnswer);
        answerRepository.save(pendingAnswer);

        // 6. Seed Running Classes
        RunningClass mathClass = RunningClass.builder()
                .title("Advanced Calculus 101")
                .description("A comprehensive course on multivariable calculus.")
                .category(ClassCategory.UNDERGRADUATE)
                .schedule("Mon, Wed, Fri - 6:00 PM IST")
                .batchSize("12-15")
                .instructorName("Prof. Alan Math")
                .feeInfo("₹5,000 / month")
                .startDate(now.plusDays(10))
                .endDate(now.plusMonths(3))
                .status(ClassStatus.ACTIVE)
                .maxCapacity(20)
                .enrolledCount(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        mathClass = runningClassRepository.save(mathClass);

        // 7. Seed Enrollments
        Enrollment pendingEnrollment = Enrollment.builder()
                .classId(mathClass.getId())
                .userId("seed-student-1")
                .studentName("Alice Smith")
                .parentName("Bob Smith")
                .email("alice@example.com")
                .mobileNumber("9876543210")
                .gradeOrClass("B.Sc 1st Year")
                .schoolOrCollege("MIT")
                .preferredBatch("Evening")
                .message("Looking forward to learning!")
                .status(EnrollmentStatus.PENDING)
                .submittedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Enrollment confirmedEnrollment = Enrollment.builder()
                .classId(mathClass.getId())
                .userId("seed-student-2")
                .studentName("John Doe")
                .parentName("Jane Doe")
                .email("john@example.com")
                .mobileNumber("1234567890")
                .gradeOrClass("B.Sc 2nd Year")
                .schoolOrCollege("Stanford")
                .preferredBatch("Morning")
                .status(EnrollmentStatus.CONFIRMED)
                .submittedAt(now.minusDays(2))
                .confirmedAt(now)
                .confirmedByAdminId("seed-admin-123")
                .createdAt(now)
                .updatedAt(now)
                .build();

        enrollmentRepository.save(pendingEnrollment);
        enrollmentRepository.save(confirmedEnrollment);

        // More Running Classes
        RunningClass dataScienceClass = RunningClass.builder()
                .title("Data Science Bootcamp")
                .description("Intensive 6-month bootcamp covering Python, ML, and Deep Learning.")
                .category(ClassCategory.PROFESSIONAL)
                .schedule("Weekends - 10:00 AM to 2:00 PM IST")
                .batchSize("30-40")
                .instructorName("Dr. Jane Smith")
                .feeInfo("₹15,000 / month")
                .startDate(now.plusDays(20))
                .endDate(now.plusMonths(6))
                .status(ClassStatus.ACTIVE)
                .maxCapacity(40)
                .enrolledCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        dataScienceClass = runningClassRepository.save(dataScienceClass);

        RunningClass physicsClass = RunningClass.builder()
                .title("Quantum Physics Fundamentals")
                .description("Introduction to quantum mechanics and particle physics.")
                .category(ClassCategory.POST_GRADUATE)
                .schedule("Tue, Thu - 5:00 PM IST")
                .batchSize("15-20")
                .instructorName("Prof. Richard Feynman")
                .feeInfo("₹8,000 / month")
                .startDate(now.minusMonths(1))
                .endDate(now.plusMonths(2))
                .status(ClassStatus.ACTIVE)
                .maxCapacity(25)
                .enrolledCount(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        physicsClass = runningClassRepository.save(physicsClass);

        RunningClass legacyClass = RunningClass.builder()
                .title("Legacy Java Programming")
                .description("Old course for Java 8 concepts. Currently not taking enrollments.")
                .category(ClassCategory.UNDERGRADUATE)
                .schedule("N/A")
                .batchSize("20")
                .instructorName("Mr. Gosling")
                .feeInfo("₹3,000 / month")
                .startDate(now.minusYears(1))
                .endDate(now.minusMonths(6))
                .status(ClassStatus.COMPLETED)
                .maxCapacity(20)
                .enrolledCount(20)
                .createdAt(now)
                .updatedAt(now)
                .build();
        runningClassRepository.save(legacyClass);

        // Enrollment for Physics
        Enrollment rejectedEnrollment = Enrollment.builder()
                .classId(physicsClass.getId())
                .userId("seed-student-3")
                .studentName("Tom Hacker")
                .parentName("Jerry Hacker")
                .email("tom@example.com")
                .mobileNumber("1122334455")
                .gradeOrClass("M.Sc 1st Year")
                .schoolOrCollege("Caltech")
                .preferredBatch("Any")
                .status(EnrollmentStatus.REJECTED)
                .rejectionReason("Incomplete prerequisites.")
                .submittedAt(now.minusDays(5))
                .createdAt(now)
                .updatedAt(now)
                .build();
        enrollmentRepository.save(rejectedEnrollment);
    }
}
