package io.dataease.license.bo;

public class F2CLicResult {

    private Status status;
    private F2CLicense license;
    private String message;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public F2CLicense getLicense() {
        return license;
    }

    public void setLicense(F2CLicense license) {
        this.license = license;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Status {
        no_record,
        valid,
        invalid,
        expired
    }

    public static F2CLicResult invalid(String a) {
        F2CLicResult f2CLicResult = new F2CLicResult();
        f2CLicResult.setStatus(Status.invalid);
        f2CLicResult.setLicense(null);
        f2CLicResult.setMessage(a);
        return f2CLicResult;
    }

    public static F2CLicResult noRecord() {
        F2CLicResult f2CLicResult = new F2CLicResult();
        f2CLicResult.setStatus(Status.no_record);
        f2CLicResult.setLicense(null);
        f2CLicResult.setMessage("No license record");
        return f2CLicResult;
    }
}
