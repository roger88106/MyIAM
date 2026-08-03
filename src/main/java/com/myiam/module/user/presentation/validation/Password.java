package com.myiam.module.user.presentation.validation;

import com.myiam.module.user.domain.user.vo.RawPassword;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * パスワード（明文）のフォーマット検証
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Password.Validator.class)
public @interface Password {
    /**
     * デフォルトメッセージ：フォーマットが不正です。
     */
    String message() default "{myiam.validation.invalid_format}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // ============================== バリデータ ==============================

    /**
     * パスワード（明文）用のバリデータ
     */
    class Validator implements ConstraintValidator<Password, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) return true;

            // VO の検証に委ねる
            return RawPassword.isValidFormat(value);
        }
    }
}
