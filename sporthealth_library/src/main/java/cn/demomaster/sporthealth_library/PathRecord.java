package cn.demomaster.sporthealth_library;


import java.util.ArrayList;
import java.util.List;

/**
 * 用于记录一条轨迹，包括起点、终点、轨迹中间点、距离、耗时、时间
 */
public class PathRecord {

    //主键
    private Long id;
    //运动开始点
    private RecordPoint mStartPoint;
    //运动结束点
    private RecordPoint mEndPoint;
    //运动轨迹
    private List<RecordPoint> mPathLinePoints = new ArrayList<>();
    //运动距离
    private Double mDistance;
    //运动时长
    private Long mDuration;
    //运动开始时间
    private Long mStartTime;
    //运动结束时间
    private Long mEndTime;
    //消耗卡路里
    private Double mCalorie;
    //平均时速(公里/小时)
    private Double mSpeed;
    //平均配速(分钟/公里)
    private Double mDistribution;
    //日期标记
    private String mDateTag;

    public PathRecord() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecordPoint getStartpoint() {
        return mStartPoint;
    }

    public void setStartpoint(RecordPoint startpoint) {
        this.mStartPoint = startpoint;
    }

    public RecordPoint getEndpoint() {
        return mEndPoint;
    }

    public void setEndpoint(RecordPoint endpoint) {
        this.mEndPoint = endpoint;
    }

    public List<RecordPoint> getPathline() {
        return mPathLinePoints;
    }

    public void setPathline(List<RecordPoint> pathline) {
        this.mPathLinePoints = pathline;
    }

    public Double getDistance() {
        return mDistance;
    }

    public void setDistance(Double distance) {
        this.mDistance = distance;
    }

    public Long getDuration() {
        return mDuration;
    }

    public void setDuration(Long duration) {
        this.mDuration = duration;
    }

    public Long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public Long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Long mEndTime) {
        this.mEndTime = mEndTime;
    }

    public List<RecordPoint> getPathLinePoints() {
        return mPathLinePoints;
    }

    public void addpoint(RecordPoint point) {
        if(mPathLinePoints.size()>0) {
            RecordPoint recordPoint = mPathLinePoints.get(mPathLinePoints.size() - 1);
            double betweenDis = AMapUtils.calculateLineDistance(point,
                    recordPoint);
            System.out.println("betweenDis差值：" + betweenDis);
            if(betweenDis!=0){
                mPathLinePoints.add(point);
            }else {
                recordPoint.setEndTime(System.currentTimeMillis());
                mPathLinePoints.set(mPathLinePoints.size() - 1, recordPoint);
            }
        }else {
            mPathLinePoints.add(point);
        }
    }

    public Double getCalorie() {
        return mCalorie;
    }

    public void setCalorie(Double mCalorie) {
        this.mCalorie = mCalorie;
    }

    public Double getSpeed() {
        return mSpeed;
    }

    public void setSpeed(Double mSpeed) {
        this.mSpeed = mSpeed;
    }

    public Double getDistribution() {
        return mDistribution;
    }

    public void setDistribution(Double mDistribution) {
        this.mDistribution = mDistribution;
    }

    public String getDateTag() {
        return mDateTag;
    }

    public void setDateTag(String mDateTag) {
        this.mDateTag = mDateTag;
    }

    @Override
    public String toString() {
        StringBuilder record = new StringBuilder();
        record.append("recordSize:" + getPathline().size() + ", ");
        record.append("distance:" + getDistance() + "m, ");
        record.append("duration:" + getDuration() + "s");
        return record.toString();
    }
    float currentDistance;
    double currentSpeed;
    long currentDuration;

    public float getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(float currentDistance) {
        this.currentDistance = currentDistance;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public long getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(long currentDuration) {
        this.currentDuration = currentDuration;
    }
}