package com.sb.savingsgoal.controller;

import javax.servlet.http.HttpServletRequest;

import com.sb.savingsgoal.service.CustomerApiClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackageClasses = SavingsGoalController.class)
public class SavingsGoalControllerAdvice {

    @ExceptionHandler(CustomerApiClientException.class)
    @ResponseBody
    ResponseEntity<Error> handleClientException(HttpServletRequest request, Throwable ex) {
        return new ResponseEntity<>(new Error(ex.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    ResponseEntity<Error> handleControllerException(HttpServletRequest request, Throwable ex) {
        return new ResponseEntity<>(new Error(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
    }
}
