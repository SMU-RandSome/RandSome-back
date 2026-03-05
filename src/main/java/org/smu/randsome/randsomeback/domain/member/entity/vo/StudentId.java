package org.smu.randsome.randsomeback.domain.member.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Embeddable
public record StudentId(
        @Column(name = "student_id", nullable = false, unique = true)
        String number
) {

    private static final String STUDENT_ID_REGEX = "^\\d{9}$";
    private static final String DELIMITER = "@";
    private static final int MIN_YEAR = 2021;
    private static final int MAX_YEAR = LocalDate.now().getYear();

    public StudentId {
        if (number == null || !number.matches(STUDENT_ID_REGEX)) {
            throw new CoreException(ErrorType.INVALID_STUDENT_ID_FORMAT);
        }
        validateYear(number);
    }

    public static StudentId create(String email) {
        String number = extractFromEmail(email);
        return new StudentId(number);
    }

    private static String extractFromEmail(String email) {
        if (email == null || !email.contains(DELIMITER)) {
            throw new CoreException(ErrorType.INVALID_STUDENT_ID_FORMAT);
        }

        return email.substring(0, email.indexOf(DELIMITER));
    }

    private static void validateYear(String number) {
        int year = Integer.parseInt(number.substring(0, 4));

        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new CoreException(ErrorType.INVALID_STUDENT_ID_YEAR);
        }
    }

}