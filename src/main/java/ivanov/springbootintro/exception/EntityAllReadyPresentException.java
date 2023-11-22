package ivanov.springbootintro.exception;

public class EntityAllReadyPresentException extends RuntimeException {

    public EntityAllReadyPresentException(String message) {
        super(message);
    }
}
