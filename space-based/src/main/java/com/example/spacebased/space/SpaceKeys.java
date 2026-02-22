package com.example.spacebased.space;

/**
 * Key names for the Redis "space". Partitioning is by nodeId for prices and by alertId for alerts.
 */
public final class SpaceKeys {

    private static final String PREFIX = "energy:";

    /** Latest price per node: price:{nodeId} -> value (e.g. "150.50") */
    public static String latestPrice(String nodeId) {
        return PREFIX + "price:" + nodeId;
    }

    /** Rolling window of prices per node (sorted set, score = timestamp ms): window:{nodeId} */
    public static String priceWindow(String nodeId) {
        return PREFIX + "window:" + nodeId;
    }

    /** Active alert (string or hash), with TTL: alert:{alertId} */
    public static String alert(String alertId) {
        return PREFIX + "alert:" + alertId;
    }

    /** Set of active alert IDs for a node (for listing): node_alerts:{nodeId} */
    public static String nodeAlerts(String nodeId) {
        return PREFIX + "node_alerts:" + nodeId;
    }

    /** Global set of all active alert IDs: alerts:active */
    public static String activeAlertIds() {
        return PREFIX + "alerts:active";
    }

    /** Queue for persistence worker: persisted alert records */
    public static String alertHistoryQueue() {
        return PREFIX + "alert_history:queue";
    }

    private SpaceKeys() {}
}
