package searchengine.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data()
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends BaseResponse {
    private final String error;
}
