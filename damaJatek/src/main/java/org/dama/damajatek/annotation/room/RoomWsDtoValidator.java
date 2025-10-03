package org.dama.damajatek.annotation.room;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dama.damajatek.dto.room.websocket.RoomWsDto;
import org.dama.damajatek.enums.room.RoomWsType;

public class RoomWsDtoValidator implements ConstraintValidator<ValidRoomWsDto, RoomWsDto> {

    @Override
    public boolean isValid(RoomWsDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // @NotNull should handle null object itself
        }

        boolean valid;

        if (dto.getType() == RoomWsType.CREATE) {
            valid = dto.getRoomCreateDto() != null;
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("roomCreateDto is required when type is CREATE")
                        .addPropertyNode("roomCreateDto")
                        .addConstraintViolation();
            }
        } else {
            valid = dto.getRoomId() != null;
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("roomId is required when type is not CREATE")
                        .addPropertyNode("roomId")
                        .addConstraintViolation();
            }
        }
        return valid;
    }
}

