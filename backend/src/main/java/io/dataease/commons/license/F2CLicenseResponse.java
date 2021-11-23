package io.dataease.commons.license;

public class F2CLicenseResponse {

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

    public static F2CLicenseResponse invalid(String a) {
        F2CLicenseResponse f2CLicenseResponse = new F2CLicenseResponse();
        f2CLicenseResponse.setStatus(Status.invalid);
        f2CLicenseResponse.setLicense(null);
        f2CLicenseResponse.setMessage(a);
        return f2CLicenseResponse;
    }

    public static F2CLicenseResponse noRecord() {
        F2CLicenseResponse f2CLicenseResponse = new F2CLicenseResponse();
        f2CLicenseResponse.setStatus(Status.no_record);
        f2CLicenseResponse.setLicense(null);
        f2CLicenseResponse.setMessage("No license record");
        return f2CLicenseResponse;
    }
}
