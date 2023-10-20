package app.backend.validation;

import app.backend.document.TimeInterval;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartBeforeEndTimeValidator implements ConstraintValidator<StartBeforeEndTime, TimeInterval> {
    @Override
    public void initialize(StartBeforeEndTime annotation) {
    }

    @Override
    public boolean isValid(TimeInterval timeInterval, ConstraintValidatorContext context) {
        return timeInterval.getStartTime().isBefore(timeInterval.getEndTime());
    }
}
