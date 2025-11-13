package com.hlayiseko.AlumniMentoring.rest;

class UnreadCountDTO {
    private long count;

    public UnreadCountDTO(long count) {
        this.count = count;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
