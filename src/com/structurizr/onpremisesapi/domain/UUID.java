package com.structurizr.onpremisesapi.domain;

public final class UUID {

    public static boolean isUUID(String uuid) {
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

}
