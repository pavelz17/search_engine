package searchengine.exceptions;

public class QueryParamError extends RuntimeException {
    public QueryParamError(String message) {
        super(message);
    }
}
