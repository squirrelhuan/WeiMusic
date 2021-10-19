package cn.demomaster.sporthealth_library;

/**
 * 运动记录点
 */
public class RecordPoint {
    public final double latitude;
    public final double longitude;
    private final long startTime;
    private long stayTime;
    private long endTime;

    public RecordPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        startTime = System.currentTimeMillis();
        endTime = startTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStayTime() {
        return stayTime;
    }

    public void setStayTime(long stayTime) {
        this.stayTime = stayTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.stayTime = endTime - startTime;
    }
}
