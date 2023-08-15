package io.dataease.constant;

public enum DataSourceType {
    mysql(0), oracle(1), sqlServer(2), TiDB(3), hive(4), impala(5), mariadb(6), StarRocks(7), ds_doris(8), pg(9), mongo(10), ck(11), db2(12), redshift(13), es(14), API(15),
    Excel(16), influxdb(17), sls(18), kingbase(19), mongobi(20), maxcompute(21), presto(22),
    dm(23), kylin(24),  folder(25);

    private final Integer flag;

    DataSourceType(Integer flag) {
        this.flag = flag;
    }

    public Integer getFlag() {
        return flag;
    }

    public static DataSourceType fromFlag(int flag) {
        DataSourceType[] values = DataSourceType.values();
        for (DataSourceType value : values) {
            if (value.flag == flag) return value;
        }
        return null;
    }
}
