package org.dama.damajatek.annotation.room;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoomPasswordValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoomPassword {
    String message() default "Password is required when room is locked.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
