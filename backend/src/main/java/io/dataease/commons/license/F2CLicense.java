package io.dataease.commons.license;

public class F2CLicense {

    private String corporation;
    private String expired;
    private String licenseVersion;
    private String product;
    private Long generateTime;
    private String edition;
    private Long count;

    public String getCorporation() {
        return corporation;
    }

    public void setCorporation(String corporation) {
        this.corporation = corporation;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getLicenseVersion() {
        return licenseVersion;
    }

    public void setLicenseVersion(String licenseVersion) {
        this.licenseVersion = licenseVersion;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Long getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Long generateTime) {
        this.generateTime = generateTime;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }


}