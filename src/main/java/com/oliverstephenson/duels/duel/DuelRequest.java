package com.oliverstephenson.duels.duel;

import java.util.UUID;

public class DuelRequest {

    private final UUID challengerUUID;
    private final UUID challengedUUID;
    private final long createdAt;
    private final int  timeoutSeconds;
    private int expiryTaskId = -1;

    public DuelRequest(UUID challengerUUID, UUID challengedUUID, int timeoutSeconds) {
        this.challengerUUID = challengerUUID;
        this.challengedUUID = challengedUUID;
        this.createdAt = System.currentTimeMillis();
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > (long) timeoutSeconds * 1000;
    }

    public UUID getChallengerUUID() {return challengerUUID;}
    public UUID getChallengedUUID() {return challengedUUID;}
    public int getExpiryTaskId() {return expiryTaskId;}
    public void setExpiryTaskId(int id) {this.expiryTaskId = id;}
}