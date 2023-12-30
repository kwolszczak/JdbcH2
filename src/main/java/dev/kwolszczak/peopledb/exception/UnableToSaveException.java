package dev.kwolszczak.peopledb.exception;

public class UnableToSaveException extends RuntimeException{
    public UnableToSaveException(String message) {
        super(message);
    }
}
