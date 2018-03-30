package com.seal.contracts.generator.csv.exception;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.Export;

/**
 * Created by root on 12.08.15..
 */
public class ValidationException extends Exception implements CSVParsableException {

    private final Severity severity;
    private final Optional<String> id;
    private final String description;


    public ValidationException(String description, Severity severity, Optional<String> id, Throwable throwable) {
        super(description, throwable);
        this.severity = severity;
        this.id = id;
        this.description = description == null ? "null" : description;
    }

    public ValidationException(String description, Severity severity, Optional<String> id) {
        this(description, severity, id, null);

    }

    public ValidationException(String description, Severity severity, Export export) {
        this(description, severity, export != null ? Optional.fromNullable(export.getContractId()) : Optional.of("N/A"), null);
    }

    public ValidationException(String description, Severity severity, Export export, Throwable throwable) {
        this(description, severity, Optional.fromNullable(export.getContractId()), throwable);
    }

    public Severity getSeverity() {
        return severity;
    }

    public Optional<String> getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "message='" + super.getMessage() + '\'' +
                ", severity=" + severity +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public String toCSV() {
        return Joiner.on(",").join(description, severity, id.isPresent() ? id.get() : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationException exception = (ValidationException) o;

        if (severity != exception.severity) return false;
        if (!id.equals(exception.id)) return false;
        return description.equals(exception.description);

    }

    @Override
    public int hashCode() {
        int result = severity.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
