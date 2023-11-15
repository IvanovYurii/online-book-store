package ivanov.springbootintro.validation.impl;

import ivanov.springbootintro.exception.PasswordMismatchException;
import ivanov.springbootintro.validation.FieldMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(final FieldMatch constraintAnnotation) {
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        try {
            final Object firstObj = org.apache.commons.beanutils.BeanUtils
                    .getProperty(value, firstFieldName);
            final Object secondObj = org.apache.commons.beanutils.BeanUtils
                    .getProperty(value, secondFieldName);

            return firstObj == null && secondObj == null || firstObj != null
                    && firstObj.equals(secondObj);
        } catch (final Exception ex) {
            throw new PasswordMismatchException("Password validation failed");
        }
    }
}
