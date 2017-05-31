package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.core.error.AppException
import voyage.core.error.ErrorUtils

class WeakPasswordException extends AppException {
    private static final HTTP_STATUS  = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The password does not meet the minimum requirements.'

    WeakPasswordException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    WeakPasswordException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(HTTP_STATUS.value(), 'weak_password')
    }
}
