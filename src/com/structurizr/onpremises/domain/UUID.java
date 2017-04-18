package com.structurizr.onpremises.domain;

public final class UUID {

    public static boolean isUUID(String uuid) {
        try {
            if (uuid != null) {
                java.util.UUID.fromString(uuid);
                return true;
            }
        } catch (IllegalArgumentException iae) {
        }

        return false;
    }

}
