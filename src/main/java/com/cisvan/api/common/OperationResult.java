package com.cisvan.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResult {

    private List<ErrorDetail> errors;

    public static OperationResult withErrors(List<ErrorDetail> errorDetails) {
        return OperationResult.builder().errors(errorDetails).build();
    }

    public static OperationResult error(String message) {
        ErrorDetail error = ErrorDetail.builder()
                .field("global")  // puedes cambiar a null si prefieres sin campo específico
                .message(message)
                .build();
        return OperationResult.builder()
                .errors(List.of(error))
                .build();
    }


    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public void addError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(ErrorDetail.builder().field(field).message(message).build());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String field;
        private String message;
    }
}
