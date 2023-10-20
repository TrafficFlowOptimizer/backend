package app.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = StartBeforeEndTimeValidator.class)
public @interface StartBeforeEndTime {
    String message() default "start time not before end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
