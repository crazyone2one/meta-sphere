package com.master.meta.utils;

import com.master.meta.constants.SensorMNType;
import com.mybatisflex.core.row.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * @author Created by 11's papa on 2025/10/15
 */
public class RandomUtil {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    public static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }

        return builder.toString();
    }

    public static List<Row> getRandomSubList(List<Row> originalList, int count) {
        List<Row> result = new ArrayList<>(originalList);
        Collections.shuffle(result);
        // 返回前count个元素
        return result.subList(0, Math.min(count, result.size()));
    }

    /**
     * 生成指定范围内的随机双精度浮点数字符串，保留两位小数
     *
     * @param min 随机数范围的最小整数值
     * @param max 随机数范围的最大整数值
     * @return 格式化后的随机双精度浮点数字符串，保留两位小数并换行
     */
    public static String doubleTypeString(int min, int max) {
        return String.format("%.2f%n", min + ((max - min) * random.nextDouble()));
    }

    /**
     * 生成指定范围内的随机双精度浮点数字符串
     *
     * @param min 随机数的最小值
     * @param max 随机数的最大值
     * @return 格式化为两位小数的随机双精度浮点数字符串
     */
    public static String generateRandomDoubleString(double min, double max) {
        return String.format("%.2f", min + ((max - min) * random.nextDouble()));
    }

    /**
     * 生成指定传感器类型范围内的随机双精度浮点数字符串
     *
     * @param sensorMNType 传感器类型，用于确定随机数的最小值和最大值范围
     * @return 在传感器类型最小值和最大值之间的随机双精度浮点数字符串，保留两位小数
     */
    public static String generateRandomDoubleString(SensorMNType sensorMNType) {
        return String.format("%.2f", sensorMNType.getMinValue() + ((sensorMNType.getMaxValue() - sensorMNType.getMinValue()) * new Random().nextDouble()));
    }
    /**
     * 生成指定位数的随机正整数
     *
     * @param length 正整数的位数
     * @return 指定位数的随机正整数
     */
    public static int generateRandomIntegerByLength(int length) {
        if (length <= 0 || length > 9) {
            throw new IllegalArgumentException("长度必须在1到9之间");
        }

        if (length == 1) {
            return random.nextInt(9) + 1; // 1-9
        } else {
            int min = (int) Math.pow(10, length - 1); // 最小值，如长度为4则是1000
            int max = (int) Math.pow(10, length) - 1; // 最大值，如长度为4则是9999
            return min + random.nextInt(max - min + 1);
        }
    }

    /**
     * 生成人员信息
     * 包含姓名、身份证号、工种或职位、部门、是否本单位人员（是/否）、性别、学历、婚姻状况、手机号码、家庭住址、工种证件及编号（名称和编码之间用/分割）、工种证件有效日期（YYYY_MM_DD）
     *
     * @return 人员信息字符串，各字段用分号分隔
     */
    public static String generatePersonInfo(String idNumber) {
        StringBuilder personInfo = new StringBuilder();

        // 姓名 - 随机生成中文姓名
        String name = generateChineseName();
        personInfo.append(name).append(";");

        // 身份证号 - 生成18位身份证号
        // String idNumber = generateIdNumber();
        personInfo.append(idNumber).append(";");

        // 工种或职位 - 随机选择
        String jobTitle = generateRandomJobTitle();
        personInfo.append(jobTitle).append(";");

        // 部门 - 随机选择
        String department = generateRandomDepartment();
        personInfo.append(department).append(";");

        // 是否本单位人员 - 随机选择是/否
        String isInternal = random.nextBoolean() ? "是" : "否";
        personInfo.append(isInternal).append(";");

        // 性别 - 根据身份证号判断
        String gender = idNumber.charAt(16) % 2 == 0 ? "女" : "男";
        personInfo.append(gender).append(";");

        // 学历 - 随机选择
        String education = generateRandomEducation();
        personInfo.append(education).append(";");

        // 婚姻状况 - 随机选择
        String maritalStatus = generateRandomMaritalStatus();
        personInfo.append(maritalStatus).append(";");

        // 手机号码 - 生成11位手机号
        String phoneNumber = generatePhoneNumber();
        personInfo.append(phoneNumber).append(";");

        // 家庭住址 - 随机生成地址
        String address = generateRandomAddress();
        personInfo.append(address).append(";");

        // 工种证件及编号（名称和编码之间用/分割）
        String certificate = generateRandomCertificate();
        personInfo.append(certificate).append(";");

        // 工种证件有效日期（YYYY-MM-DD）
        String certificateDate = generateRandomCertificateDate();
        personInfo.append(certificateDate);

        return personInfo.toString();
    }

    /**
     * 生成随机中文姓名
     */
    private static String generateChineseName() {
        String[] surnames = {"赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐", "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹", "姚", "邵", "湛", "汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧", "计", "伏", "成", "戴", "谈", "宋", "茅", "庞", "熊", "纪", "舒", "屈", "项", "祝", "董", "梁", "杜", "阮", "蓝", "闵", "席", "季", "麻", "强", "贾", "路", "娄", "危", "江", "童", "颜", "郭", "梅", "盛", "林", "刁", "钟", "徐", "邱", "骆", "高", "夏", "蔡", "田", "樊", "胡", "凌", "霍", "虞", "万", "支", "柯", "昝", "管", "卢", "莫", "经", "房", "裘", "缪", "干", "解", "应", "宗", "丁", "宣", "贲", "邓", "郁", "单", "杭", "洪", "包", "诸", "左", "石", "崔", "吉", "钮", "龚", "程", "嵇", "邢", "滑", "裴", "陆", "荣", "翁", "荀", "羊", "於", "惠", "甄", "曲", "家", "封", "芮", "羿", "储", "靳", "汲", "邴", "糜", "松", "井", "段", "富", "巫", "乌", "焦", "巴", "弓", "牧", "隗", "山", "谷", "车", "侯", "宓", "蓬", "全", "郗", "班", "仰", "秋", "仲", "伊", "宫", "宁", "仇", "栾", "暴", "甘", "钭", "厉", "戎", "祖", "武", "符", "刘", "景", "詹", "束", "龙", "叶", "幸", "司", "韶", "郜", "黎", "蓟", "薄", "印", "宿", "白", "怀", "蒲", "邰", "从", "鄂", "索", "咸", "籍", "赖", "卓", "蔺", "屠", "蒙", "池", "乔", "阴", "郁", "胥", "能", "苍", "双", "闻", "莘", "党", "翟", "谭", "贡", "劳", "逄", "姬", "申", "扶", "堵", "冉", "宰", "郦", "雍", "郤", "璩", "桑", "桂", "濮", "牛", "寿", "通", "边", "扈", "燕", "冀", "郏", "浦", "尚", "农", "温", "别", "庄", "晏", "柴", "瞿", "阎", "充", "慕", "连", "茹", "习", "宦", "艾", "鱼", "容", "向", "古", "易", "慎", "戈", "廖", "庾", "终", "暨", "居", "衡", "步", "都", "耿", "满", "弘", "匡", "国", "文", "寇", "广", "禄", "阙", "东", "欧", "殳", "沃", "利", "蔚", "越", "夔", "隆", "师", "巩", "厍", "聂", "晁", "勾", "敖", "融", "冷", "訾", "辛", "阚", "那", "简", "饶", "空", "曾", "母", "沙", "乜", "养", "鞠", "须", "丰", "巢", "关", "蒯", "相", "查", "后", "荆", "红", "游", "竺", "权", "逯", "盖", "益", "桓", "公", "万俟", "司马", "上官", "欧阳", "夏侯", "诸葛", "闻人", "东方", "赫连", "皇甫", "尉迟", "公羊", "澹台", "公冶", "宗政", "濮阳", "淳于", "单于", "太叔", "申屠", "公孙", "仲孙", "轩辕", "令狐", "钟离", "宇文", "长孙", "慕容", "鲜于", "闾丘", "司徒", "司空", "亓官", "司寇", "仉", "督", "子车", "颛孙", "端木", "巫马", "公西", "漆雕", "乐正", "壤驷", "公良", "拓跋", "夹谷", "宰父", "谷梁", "晋", "楚", "闫", "法", "汝", "鄢", "涂", "钦", "段干", "百里", "东郭", "南门", "呼延", "归", "海", "羊舌", "微生", "岳", "帅", "缑", "亢", "况", "后", "有", "琴", "梁丘", "左丘", "东门", "西门", "商", "牟", "佘", "佴", "伯", "赏", "南宫", "墨", "哈", "谯", "笪", "年", "爱", "阳", "佟"};
        String[] givenNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "建华", "文", "华", "金凤", "志", "桂兰", "秀珍", "建国", "建军", "春梅", "春兰", "淑珍", "学军", "丽娟", "永刚", "玉兰", "建军", "建平", "丽华", "秀芳", "丽萍", "秀梅", "秀珍", "丽", "强", "敏", "静", "伟", "芳", "娜", "丽", "勇", "涛", "超", "刚", "明", "华", "军", "洋", "磊", "杰", "娟", "文", "志", "丽娟", "永刚", "玉兰", "桂兰", "秀珍", "建国", "建军", "春梅", "春兰", "学军", "淑珍", "建平", "丽华", "秀芳", "丽萍", "秀梅", "艳", "霞", "平", "桂英", "建华", "金凤"};
        
        String surname = surnames[random.nextInt(surnames.length)];
        String givenName = givenNames[random.nextInt(givenNames.length)];
        
        // 有时候名字是两个字，有时候是三个字
        if (random.nextBoolean()) {
            givenName += givenNames[random.nextInt(givenNames.length)];
        }
        
        return surname + givenName;
    }

    /**
     * 生成18位身份证号
     */
    public static String generateIdNumber() {
        // 生成前17位
        StringBuilder idBuilder = new StringBuilder();
        
        // 地区码（前6位）- 使用常见地区码
        String[] areaCodes = {"110101", "120101", "130101", "140101", "150101", "210101", "220101", "230101", "310101", "320101", "330101", "340101", "350101", "360101", "370101", "410101", "420101", "430101", "440101", "450101", "460101", "500101", "510101", "520101", "530101", "610101", "620101", "630101", "640101", "650101"};
        idBuilder.append(areaCodes[random.nextInt(areaCodes.length)]);
        
        // 出生日期（第7-14位）- 1970-2000年之间
        int birthYear = random.nextInt(31) + 1970; // 1970-2000
        int birthMonth = random.nextInt(12) + 1;
        int birthDay = random.nextInt(28) + 1; // 简化处理，避免闰年等复杂情况
        idBuilder.append(String.format("%04d%02d%02d", birthYear, birthMonth, birthDay));
        
        // 顺序码（第15-17位）- 随机生成
        idBuilder.append(String.format("%03d", random.nextInt(1000)));
        
        // 校验码（第18位）
        String[] checkCodes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "X"};
        idBuilder.append(checkCodes[random.nextInt(checkCodes.length)]);
        
        return idBuilder.toString();
    }

    /**
     * 生成随机工种或职位
     */
    private static String generateRandomJobTitle() {
        String[] jobTitles = {"工程师", "技术员", "项目经理", "安全员", "质量员", "施工员", "资料员", "材料员", "机械员", "劳务员", "标准员", "测量员", "试验员", "特种作业人员", "电工", "焊工", "起重工", "架子工", "钢筋工", "木工", "混凝土工", "普工", "财务", "人事", "行政", "销售", "采购", "设计", "研发", "运维", "测试", "产品经理", "运营", "市场", "客服"};
        return jobTitles[random.nextInt(jobTitles.length)];
    }

    /**
     * 生成随机部门
     */
    private static String generateRandomDepartment() {
        String[] departments = {"工程部", "技术部", "安全部", "质量部", "财务部", "人事部", "行政部", "销售部", "采购部", "项目部", "设计部", "研发部", "运维部", "测试部", "市场部", "客服部", "商务部", "法务部", "审计部", "生产部", "设备部", "后勤部", "环保部", "商务部", "信息部", "监察部", "综合部", "党群部", "工会", "团委"};
        return departments[random.nextInt(departments.length)];
    }

    /**
     * 生成随机学历
     */
    private static String generateRandomEducation() {
        String[] educations = {"博士", "硕士", "本科", "大专", "高中", "中专", "初中", "小学"};
        return educations[random.nextInt(educations.length)];
    }

    /**
     * 生成随机婚姻状况
     */
    private static String generateRandomMaritalStatus() {
        String[] maritalStatuses = {"已婚", "未婚", "离异", "丧偶"};
        return maritalStatuses[random.nextInt(maritalStatuses.length)];
    }

    /**
     * 生成11位手机号
     */
    private static String generatePhoneNumber() {
        // 生成手机号，以1开头，第二位为3-9
        StringBuilder phoneBuilder = new StringBuilder();
        phoneBuilder.append("1");
        phoneBuilder.append(random.nextInt(7) + 3); // 3-9
        
        for (int i = 0; i < 9; i++) {
            phoneBuilder.append(random.nextInt(10));
        }
        
        return phoneBuilder.toString();
    }

    /**
     * 生成随机地址
     */
    private static String generateRandomAddress() {
        String[] provinces = {"北京市", "上海市", "天津市", "重庆市", "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "海南省", "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省", "台湾省", "内蒙古", "广西", "西藏", "宁夏", "新疆", "香港", "澳门"};
        String[] cities = {"北京市", "上海市", "天津市", "重庆市", "石家庄市", "太原市", "呼和浩特市", "沈阳市", "长春市", "哈尔滨市", "南京市", "杭州市", "合肥市", "福州市", "南昌市", "济南市", "郑州市", "武汉市", "长沙市", "广州市", "南宁市", "海口市", "成都市", "贵阳市", "昆明市", "拉萨市", "西安市", "兰州市", "西宁市", "银川市", "乌鲁木齐市", "香港市", "澳门市", "台北市"};
        String[] districts = {"朝阳区", "海淀区", "浦东新区", "天河区", "越秀区", "黄浦区", "渝中区", "锦江区", "碑林区", "莲湖区", "新城区", "雁塔区", "未央区", "灞桥区", "长安区", "高新区", "经济技术开发区", "工业园区", "软件园", "科技城", "大学城", "商业区", "住宅区", "工业区", "文化区", "行政区"};
        
        String province = provinces[random.nextInt(provinces.length)];
        String city = cities[random.nextInt(cities.length)];
        String district = districts[random.nextInt(districts.length)];
        
        // 生成街道和门牌号
        int streetNum = random.nextInt(200) + 1;
        int houseNum = random.nextInt(1000) + 1;
        
        return province + city + district + "第" + random.nextInt(10) + "街道" + streetNum + "号" + "小区" + houseNum + "栋";
    }

    /**
     * 生成随机工种证件及编号
     */
    private static String generateRandomCertificate() {
        String[] certificateNames = {"建造师证", "安全员证", "质量员证", "施工员证", "资料员证", "材料员证", "机械员证", "劳务员证", "标准员证", "测量员证", "试验员证", "电工证", "焊工证", "起重工证", "架子工证", "钢筋工证", "木工证", "混凝土工证", "特种作业证", "注册工程师证", "监理工程师证", "造价工程师证", "安全工程师证", "消防工程师证", "结构工程师证", "电气工程师证", "机械工程师证", "土木工程师证", "公路工程师证", "桥梁工程师证"};
        
        String certName = certificateNames[random.nextInt(certificateNames.length)];
        String certNumber = "Z" + String.format("%03d", random.nextInt(1000)) + "-" + String.format("%06d", random.nextInt(1000000));
        
        return certName + "/" + certNumber;
    }

    /**
     * 生成随机工种证件有效日期
     */
    private static String generateRandomCertificateDate() {
        // 生成未来3-10年的有效日期
        int year = LocalDate.now().getYear() + random.nextInt(8) + 3; // 3-10年后的年份
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1; // 简化处理
        
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
