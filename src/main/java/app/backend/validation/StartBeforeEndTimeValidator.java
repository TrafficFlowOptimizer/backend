package app.backend.validation;

import app.backend.document.CarFlow;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartBeforeEndTimeValidator implements ConstraintValidator<StartBeforeEndTime, CarFlow> {
    @Override
    public void initialize(StartBeforeEndTime annotation){}

    @Override
    public boolean isValid(CarFlow carFlow, ConstraintValidatorContext context) {
        return carFlow.getStartTime().isBefore(carFlow.getEndTime());
    }
}
