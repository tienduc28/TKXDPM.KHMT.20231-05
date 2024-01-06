package common.exception.vnPayException;

import common.exception.*;

import java.util.HashMap;
import java.util.Map;

public class PaymentExceptionHolder {
    private static final PaymentExceptionHolder instance = new PaymentExceptionHolder();

    private Map<String, PaymentException> exceptions = new HashMap<>();

    private PaymentExceptionHolder() {
        // Khởi tạo các exception và đưa vào Map
        exceptions.put("01", new TransactionNotDoneException());
        exceptions.put("02", new TransactionFailedException());
        exceptions.put("04", new TransactionReverseException());
        exceptions.put("05", new ProcessingException());
        exceptions.put("09", new RejectedTransactionException());
        exceptions.put("06", new SendToBankException());
        exceptions.put("07", new AnonymousTransactionException());
    }

    public static PaymentExceptionHolder getInstance() {
        return instance;
    }

    public PaymentException getException(String code) {
        return exceptions.get(code);
    }
}
