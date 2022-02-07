package com.example.restControllerAdvice;

import com.example.exceptions.BadRequestException;
import com.example.exceptions.NotFoundException;
import com.example.restControllerAdvice.pojo.ResponseMsg;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomRestControllerAdvice {

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseMsg notFoundException(NotFoundException notFoundException) {

        return new ResponseMsg(notFoundException.getMessage());
    }

    @ExceptionHandler(value = BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseMsg notModifiedException(BadRequestException badRequestException) {
        return new ResponseMsg(badRequestException.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseMsg illegalArgEx(IllegalArgumentException illegalArgumentException) {
        return new ResponseMsg(illegalArgumentException.getMessage());
    }

    @ExceptionHandler(value = PropertyValueException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseMsg illegalArgEx(PropertyValueException propertyValueException) {
        return new ResponseMsg("Error! Not-null property references a null or transient value : " + propertyValueException.getPropertyName());
    }
}
