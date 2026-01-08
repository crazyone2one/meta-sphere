package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.*;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class CarRealInfo extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final FileHelper fileHelper;
    private final StringRedisTemplate redisTemplate;

    private CarRealInfo(SensorService sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper, StringRedisTemplate redisTemplate) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.CARBASEINFO.getKey(), WkkSensorEnum.CARBASEINFO.getTableName());
        List<Row> list = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String fileName = projectNum + "_" + WkkSensorEnum.CARBASEINFO.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        if (CollectionUtils.isNotEmpty(list)) {
            String content = DateFormatUtil.localDateTime2StringStyle2(now) + ";" + list.size() + "~" +
                    // 文件体
                    bodyContent(list) +
                    END_FLAG;
            String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "clry", fileName);
            fileHelper.generateFile(filePath, content, "车辆设备实时数据[" + fileName + "]");
            // sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
            fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "clry");
        }
    }

    private String bodyContent(List<Row> list) {
        List<Row> persons = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.LTPERSON.getKey(), WkkSensorEnum.LTPERSON.getTableName());
        StringBuilder sb = new StringBuilder();
        String carStatus = Optional.ofNullable(config.getField("carStatus", String.class)).orElse("1");
        String carLocationNo = config.getField("car_location_no", String.class);
        TrajectoryGeneratorUtil.TrajectoryPoint trajectoryPoint = generateCircularTrajectory();
        for (Row row : list) {
            sb.append(row.getString("car_location_no")).append(";");
            sb.append(row.getString("car_card_no")).append(";");
            List<Row> rows = RandomUtil.getRandomSubList(persons, 1);
            sb.append(rows.getFirst().getString("person_code")).append(";");
            sb.append(trajectoryPoint.getLongitude()).append(";");
            sb.append(trajectoryPoint.getLatitude()).append(";");
            sb.append(trajectoryPoint.getAltitude()).append(";");
            sb.append("采区").append(row.getString("car_card_no")).append(";");
            sb.append(row.getString("car_location_no").equals(carLocationNo) ? carStatus : "1").append("~");
        }
        return sb.toString();
    }

    private TrajectoryGeneratorUtil.TrajectoryPoint generateCircularTrajectory() {
        String trajectory = redisTemplate.opsForValue().get("trajectory:" + projectNum);
        if (StringUtils.isEmpty(trajectory)) {
            // 创建中心点
            TrajectoryGeneratorUtil.TrajectoryPoint centerPoint = new TrajectoryGeneratorUtil.TrajectoryPoint(39.9042, 116.4074, 50.0);
            // 生成圆形轨迹
            List<TrajectoryGeneratorUtil.TrajectoryPoint> circularTrajectory = TrajectoryGeneratorUtil.generateCircularTrajectory(
                    centerPoint, 100.0, 50, 2); // 半径100米，50个点，转2圈
            TrajectoryGeneratorUtil.TrajectoryPoint first = circularTrajectory.getFirst();
            circularTrajectory.remove(first);
            redisTemplate.opsForValue().set("trajectory:" + projectNum, JSON.toJSONString(circularTrajectory));
            return first;
        }
        List<TrajectoryGeneratorUtil.TrajectoryPoint> trajectoryPoints = JSON.parseArray(trajectory, TrajectoryGeneratorUtil.TrajectoryPoint.class);
        TrajectoryGeneratorUtil.TrajectoryPoint first = trajectoryPoints.getFirst();
        trajectoryPoints.remove(first);
        redisTemplate.opsForValue().set("trajectory:" + projectNum, JSON.toJSONString(trajectoryPoints));
        String vireo = "";
        return first;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CarRealInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CarRealInfo.class.getName());
    }
}
