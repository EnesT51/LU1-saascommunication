package com.api.RestAPI.presentation.notification.response;

public class NotificationSummaryResponse {

    private String organizationId;
    private String provider;
    private long sent;
    private long failed;
    private long cancelled;
    private long total;

    public NotificationSummaryResponse(String organizationId, String provider,
            long sent, long failed, long cancelled) {
        this.organizationId = organizationId;
        this.provider = provider;
        this.sent = sent;
        this.failed = failed;
        this.cancelled = cancelled;
        this.total = sent + failed + cancelled;
    }

    public String getOrganizationId() { return organizationId; }
    public String getProvider() { return provider; }
    public long getSent() { return sent; }
    public long getFailed() { return failed; }
    public long getCancelled() { return cancelled; }
    public long getTotal() { return total; }
}
