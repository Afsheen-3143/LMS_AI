package com.afsheen.aiassistant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.afsheen.aiassistant.entity.AssistantFaq;
import com.afsheen.aiassistant.repository.AssistantFaqRepository;

/** Seeds a couple of demo FAQ rows on first run so reindex has FAQ content too. */
@Component
public class DemoFaqSeeder implements CommandLineRunner {

    private final AssistantFaqRepository faqRepository;

    public DemoFaqSeeder(AssistantFaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Override
    public void run(String... args) {
        if (faqRepository.count() > 0) {
            return;
        }

        faqRepository.save(faq("Payments",
                "What payment methods are accepted?",
                "We accept major credit/debit cards and UPI. Fees can be paid in full or via the "
                        + "installment plan shown at checkout for that course/program."));

        faqRepository.save(faq("Enrollment",
                "Can I switch batches after enrolling?",
                "Yes - contact support within the first week of a batch starting and we'll move you "
                        + "to another open batch for the same course at no extra charge."));

        faqRepository.save(faq("Certificates",
                "Do I get a certificate after completing a course?",
                "Yes, a completion certificate is issued automatically once you finish all scheduled "
                        + "classes and any required assessments for that course."));
    }

    private AssistantFaq faq(String category, String question, String answer) {
        AssistantFaq f = new AssistantFaq();
        f.setCategory(category);
        f.setQuestion(question);
        f.setAnswer(answer);
        f.setActive(true);
        return f;
    }
}
