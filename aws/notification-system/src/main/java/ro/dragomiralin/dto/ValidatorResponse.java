package ro.dragomiralin.dto;

public record ValidatorResponse(boolean hasError, String errorMessage) {
}
