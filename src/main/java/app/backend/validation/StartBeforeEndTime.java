package app.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = StartBeforeEndTimeValidator.class)
public @interface StartBeforeEndTime {
    public String message() default "start time not before end time";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
