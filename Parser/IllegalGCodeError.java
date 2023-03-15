package Parser;

public class IllegalGCodeError extends Error{
    public IllegalGCodeError(String errorMessage){
        super(errorMessage);
    }
}
