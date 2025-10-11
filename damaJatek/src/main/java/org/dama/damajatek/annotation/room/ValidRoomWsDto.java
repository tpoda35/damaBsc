//package org.dama.damajatek.annotation.room;
//
//import jakarta.validation.Constraint;
//import jakarta.validation.Payload;
//
//import java.lang.annotation.*;
//
//@Target({ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Constraint(validatedBy = RoomWsDtoValidator.class)
//@Documented
//public @interface ValidRoomWsDto {
//
//    String message() default "Invalid RoomWsDto: either roomCreateDto or roomId must be provided depending on type";
//
//    Class<?>[] groups() default {};
//
//    Class<? extends Payload>[] payload() default {};
//}
