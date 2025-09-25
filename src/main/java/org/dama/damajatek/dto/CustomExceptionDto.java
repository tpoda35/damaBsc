package org.dama.damajatek.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data transfer object for exceptions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomExceptionDto{
    private Date date;
    private Integer statusCode;
    private String message;
}
