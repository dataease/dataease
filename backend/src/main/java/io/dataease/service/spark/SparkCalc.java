//package io.dataease.service.spark;
//
//import io.dataease.base.domain.DatasetTableField;
//import io.dataease.commons.utils.CommonBeanFactory;
//import io.dataease.controller.request.chart.ChartExtFilterRequest;
//import io.dataease.dto.chart.ChartViewFieldDTO;
//import org.antlr.analysis.MachineProbe;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.Scan;
//import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
//import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
//import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
//import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
//import org.apache.hadoop.hbase.util.Bytes;
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.api.java.function.FlatMapFunction;
//import org.apache.spark.api.java.function.Function;
//import org.apache.spark.sql.*;
//import org.apache.spark.sql.types.DataTypes;
//import org.apache.spark.sql.types.StructField;
//import org.apache.spark.sql.types.StructType;
//import org.apache.spark.storage.StorageLevel;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Service;
//import scala.Tuple2;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.text.MessageFormat;
//import java.util.*;
//
///**
// * @Author gin
// * @Date 2021/3/26 3:49 下午
// */
//@Service
//public class SparkCalc {
//    private static String column_family = "dataease";
//    private static String data_path = "/opt/dataease/data/db/";
//    @Resource
//    private Environment env; // 保存了配置文件的信息
//
//    public List<String[]> getData(String hTable, List<DatasetTableField> fields, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String tmpTable, List<ChartExtFilterRequest> requestList) throws Exception {
//        // Spark Context
//        SparkSession spark = CommonBeanFactory.getBean(SparkSession.class);
//        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());
//
//        // Spark SQL Context
//        SQLContext sqlContext = new SQLContext(sparkContext);
//        sqlContext.setConf("spark.sql.shuffle.partitions", env.getProperty("spark.sql.shuffle.partitions", "1"));
//        sqlContext.setConf("spark.default.parallelism", env.getProperty("spark.default.parallelism", "1"));
//
//        /*Map<String, BigDecimal> dataFrame = getData(sparkContext, sqlContext, hTable, fields);
//        List<String[]> data = new ArrayList<>();
//        Iterator<Map.Entry<String, BigDecimal>> iterator = dataFrame.entrySet().iterator();
//        while (iterator.hasNext()) {
//            String[] r = new String[2];
//            Map.Entry<String, BigDecimal> next = iterator.next();
//            String key = next.getKey();
//            BigDecimal value = next.getValue();
//            r[0] = key;
//            r[1] = value.toString();
//            data.add(r);
//        }*/
//
////        Dataset<Row> dataFrame = getData(sparkContext, sqlContext, hTable, fields);
//        Dataset<Row> dataFrame = CacheUtil.getInstance().getCacheData(hTable);
//        if (ObjectUtils.isEmpty(dataFrame)) {
//            dataFrame = getHBaseDataAndCache(sparkContext, sqlContext, hTable, fields);
//        }
//
//        dataFrame.createOrReplaceTempView(tmpTable);
//        Dataset<Row> sql = sqlContext.sql(getSQL(xAxis, yAxis, tmpTable, requestList));
//        // transform
//        List<String[]> data = new ArrayList<>();
//        List<Row> list = sql.collectAsList();
//        for (Row row : list) {
//            String[] r = new String[row.length()];
//            for (int i = 0; i < row.length(); i++) {
//                r[i] = row.get(i) == null ? "null" : row.get(i).toString();
//            }
//            data.add(r);
//        }
//        return data;
//    }
//
//    public Dataset<Row> getHBaseDataAndCache(String hTable, List<DatasetTableField> fields) throws Exception {
//        // Spark Context
//        SparkSession spark = CommonBeanFactory.getBean(SparkSession.class);
//        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());
//
//        // Spark SQL Context
//        SQLContext sqlContext = new SQLContext(sparkContext);
//        sqlContext.setConf("spark.sql.shuffle.partitions", env.getProperty("spark.sql.shuffle.partitions", "1"));
//        sqlContext.setConf("spark.default.parallelism", env.getProperty("spark.default.parallelism", "1"));
//        return getHBaseDataAndCache(sparkContext, sqlContext, hTable, fields);
//    }
//
//    public Map<String, BigDecimal> getData(JavaSparkContext sparkContext, SQLContext sqlContext, String tableId, List<DatasetTableField> fields) throws Exception {
//        fields.sort((o1, o2) -> {
//            if (o1.getOriginName() == null) {
//                return -1;
//            }
//            if (o2.getOriginName() == null) {
//                return 1;
//            }
//            return o1.getOriginName().compareTo(o2.getOriginName());
//        });
//
//        JavaRDD<String> pairRDD = sparkContext.textFile(data_path + tableId + ".txt");
////        System.out.println(pairRDD.count());
//
////        JavaRDD<Map.Entry<String, BigDecimal>> rdd = pairRDD.map((Function<String, Map.Entry<String, BigDecimal>>) v1 -> {
////            Map<String, BigDecimal> map = new HashMap<>();
////            String[] items = v1.split(";");
////            String day = null;
////            BigDecimal res = new BigDecimal(0);
////            for (int i = 0; i < items.length; i++) {
////                String l = items[i];
////                DatasetTableField x = fields.get(i);
////                if (x.getOriginName().equalsIgnoreCase("sync_day")) {
////                    day = l;
////                }
////                if (x.getOriginName().equalsIgnoreCase("usage_cost")) {
////                    res = new BigDecimal(l);
////                }
////            }
////            BigDecimal bigDecimal = map.get(day);
////            if (bigDecimal == null) {
////                map.put(day, res);
////            } else {
////                map.put(day, bigDecimal.add(res));
////            }
////            return map.entrySet().iterator().next();
////        });
//
//        JavaRDD<Map.Entry<String, BigDecimal>> rdd = pairRDD.mapPartitions((FlatMapFunction<java.util.Iterator<String>, Map.Entry<String, BigDecimal>>) tuple2Iterator -> {
//            Map<String, BigDecimal> map = new HashMap<>();
//            while (tuple2Iterator.hasNext()) {
//                String[] items = tuple2Iterator.next().split(";");
//                String day = null;
//                BigDecimal res = new BigDecimal(0);
//                for (int i = 0; i < items.length; i++) {
//                    String l = items[i];
//                    DatasetTableField x = fields.get(i);
//                    if (x.getOriginName().equalsIgnoreCase("sync_day")) {
//                        day = l;
//                    }
//                    if (x.getOriginName().equalsIgnoreCase("usage_cost")) {
//                        res = new BigDecimal(l);
//                    }
//                }
//                BigDecimal bigDecimal = map.get(day);
//                if (bigDecimal == null) {
//                    map.put(day, res);
//                } else {
//                    map.put(day, bigDecimal.add(res));
//                }
//            }
//            return map.entrySet().iterator();
//        });
//
//
////        System.out.println(rdd.count());
//
//        Map<String, BigDecimal> map = new HashMap<>();
//        List<Map.Entry<String, BigDecimal>> collect = rdd.collect();
////        System.out.println(collect.size());
//
//        collect.forEach(stringBigDecimalEntry -> {
//            String key = stringBigDecimalEntry.getKey();
//            BigDecimal value = stringBigDecimalEntry.getValue();
//
//            BigDecimal bigDecimal = map.get(key);
//            if (bigDecimal == null) {
//                map.put(key, value);
//            } else {
//                map.put(key, bigDecimal.add(value));
//            }
//        });
//
//        return map;
//    }
//
////    public Dataset<Row> getData(JavaSparkContext sparkContext, SQLContext sqlContext, String tableId, List<DatasetTableField> fields) throws Exception {
////        fields.sort((o1, o2) -> {
////            if (o1.getOriginName() == null) {
////                return -1;
////            }
////            if (o2.getOriginName() == null) {
////                return 1;
////            }
////            return o1.getOriginName().compareTo(o2.getOriginName());
////        });
////
////        JavaRDD<String> pairRDD = sparkContext.textFile(data_path + tableId + ".txt");
////
////        JavaRDD<Row> rdd = pairRDD.mapPartitions((FlatMapFunction<java.util.Iterator<String>, Row>) tuple2Iterator -> {
////            List<Row> iterator = new ArrayList<>();
////            while (tuple2Iterator.hasNext()) {
////                String[] items = tuple2Iterator.next().split(";");
////                List<Object> list = new ArrayList<>();
////                for (int i = 0; i < items.length; i++) {
////                    String l = items[i];
////                    DatasetTableField x = fields.get(i);
////                    if (x.getDeType() == 0 || x.getDeType() == 1) {
////                        list.add(l);
////                    } else if (x.getDeType() == 2) {
////                        if (StringUtils.isEmpty(l)) {
////                            l = "0";
////                        }
////                        if (StringUtils.equalsIgnoreCase(l, "Y")) {
////                            l = "1";
////                        }
////                        if (StringUtils.equalsIgnoreCase(l, "N")) {
////                            l = "0";
////                        }
////                        list.add(Long.valueOf(l));
////                    } else if (x.getDeType() == 3) {
////                        if (StringUtils.isEmpty(l)) {
////                            l = "0.0";
////                        }
////                        list.add(Double.valueOf(l));
////                    }
////                }
////                iterator.add(RowFactory.create(list.toArray()));
////            }
////            return iterator.iterator();
////        });
////
////        List<StructField> structFields = new ArrayList<>();
////        // struct顺序要与rdd顺序一致
////        fields.forEach(x -> {
////            if (x.getDeType() == 0 || x.getDeType() == 1) {
////                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.StringType, true));
////            } else if (x.getDeType() == 2) {
////                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.LongType, true));
////            } else if (x.getDeType() == 3) {
////                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.DoubleType, true));
////            }
////        });
////        StructType structType = DataTypes.createStructType(structFields);
////
////        Dataset<Row> dataFrame = sqlContext.createDataFrame(rdd, structType);
////        return dataFrame;
////    }
//
//    public Dataset<Row> getHBaseDataAndCache(JavaSparkContext sparkContext, SQLContext sqlContext, String hTable, List<DatasetTableField> fields) throws Exception {
//        Scan scan = new Scan();
//        scan.addFamily(Bytes.toBytes(column_family));
//        for (DatasetTableField field : fields) {
//            scan.addColumn(Bytes.toBytes(column_family), Bytes.toBytes(field.getOriginName()));
//        }
//        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
//        String scanToString = new String(Base64.getEncoder().encode(proto.toByteArray()));
//
//        // HBase config
//        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
//        conf.set("hbase.zookeeper.quorum", env.getProperty("hbase.zookeeper.quorum"));
//        conf.set("hbase.zookeeper.property.clientPort", env.getProperty("hbase.zookeeper.property.clientPort"));
//        conf.set("hbase.client.retries.number", env.getProperty("hbase.client.retries.number", "1"));
//        conf.set(TableInputFormat.INPUT_TABLE, hTable);
//        conf.set(TableInputFormat.SCAN, scanToString);
//
//        JavaPairRDD<ImmutableBytesWritable, Result> pairRDD = sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
//
//        JavaRDD<Row> rdd = pairRDD.mapPartitions((FlatMapFunction<Iterator<Tuple2<ImmutableBytesWritable, Result>>, Row>) tuple2Iterator -> {
//            List<Row> iterator = new ArrayList<>();
//            while (tuple2Iterator.hasNext()) {
//                Result result = tuple2Iterator.next()._2;
//                List<Object> list = new ArrayList<>();
//                fields.forEach(x -> {
//                    String l = Bytes.toString(result.getValue(column_family.getBytes(), x.getOriginName().getBytes()));
//                    if (x.getDeType() == 0 || x.getDeType() == 1) {
//                        list.add(l);
//                    } else if (x.getDeType() == 2) {
//                        if (StringUtils.isEmpty(l)) {
//                            l = "0";
//                        }
//                        list.add(Long.valueOf(l));
//                    } else if (x.getDeType() == 3) {
//                        if (StringUtils.isEmpty(l)) {
//                            l = "0.0";
//                        }
//                        list.add(Double.valueOf(l));
//                    }
//                });
//                iterator.add(RowFactory.create(list.toArray()));
//            }
//            return iterator.iterator();
//        });
//
//        List<StructField> structFields = new ArrayList<>();
//        // struct顺序要与rdd顺序一致
//        fields.forEach(x -> {
//            if (x.getDeType() == 0 || x.getDeType() == 1) {
//                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.StringType, true));
//            } else if (x.getDeType() == 2) {
//                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.LongType, true));
//            } else if (x.getDeType() == 3) {
//                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.DoubleType, true));
//            }
//        });
//        StructType structType = DataTypes.createStructType(structFields);
//
//        Dataset<Row> dataFrame = sqlContext.createDataFrame(rdd, structType).persist(StorageLevel.MEMORY_AND_DISK_SER());
//        CacheUtil.getInstance().addCacheData(hTable, dataFrame);
//        dataFrame.count();
//        return dataFrame;
//    }
//
//    public String getSQL(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String table, List<ChartExtFilterRequest> extFilterRequestList) {
//        // 字段汇总 排序等
//        String[] field = yAxis.stream().map(y -> "CAST(" + y.getSummary() + "(" + y.getOriginName() + ") AS DECIMAL(20,2)) AS _" + y.getSummary() + "_" + y.getOriginName()).toArray(String[]::new);
//        String[] group = xAxis.stream().map(ChartViewFieldDTO::getOriginName).toArray(String[]::new);
//        String[] order = yAxis.stream().filter(y -> StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none"))
//                .map(y -> "_" + y.getSummary() + "_" + y.getOriginName() + " " + y.getSort()).toArray(String[]::new);
//
//        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
//                StringUtils.join(group, ","),
//                StringUtils.join(field, ","),
//                table,
//                transExtFilter(extFilterRequestList),// origin field filter and panel field filter,
//                StringUtils.join(group, ","),
//                StringUtils.join(order, ","));
//        if (sql.endsWith(",")) {
//            sql = sql.substring(0, sql.length() - 1);
//        }
//        // 如果是对结果字段过滤，则再包裹一层sql
//        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
//                .map(y -> {
//                    String[] s = y.getFilter().stream().map(f -> "AND _" + y.getSummary() + "_" + y.getOriginName() + transFilterTerm(f.getTerm()) + f.getValue()).toArray(String[]::new);
//                    return StringUtils.join(s, " ");
//                }).toArray(String[]::new);
//        if (resultFilter.length == 0) {
//            return sql;
//        } else {
//            String filterSql = MessageFormat.format("SELECT * FROM {0} WHERE 1=1 {1}",
//                    "(" + sql + ") AS tmp",
//                    StringUtils.join(resultFilter, " "));
//            return filterSql;
//        }
//    }
//
//    public String transFilterTerm(String term) {
//        switch (term) {
//            case "eq":
//                return " = ";
//            case "not_eq":
//                return " <> ";
//            case "lt":
//                return " < ";
//            case "le":
//                return " <= ";
//            case "gt":
//                return " > ";
//            case "ge":
//                return " >= ";
//            case "in":
//                return " IN ";
//            case "not in":
//                return " NOT IN ";
//            case "like":
//                return " LIKE ";
//            case "not like":
//                return " NOT LIKE ";
//            case "null":
//                return " IS NULL ";
//            case "not_null":
//                return " IS NOT NULL ";
//            default:
//                return "";
//        }
//    }
//
//    public String transExtFilter(List<ChartExtFilterRequest> requestList) {
//        if (CollectionUtils.isEmpty(requestList)) {
//            return "";
//        }
//        StringBuilder filter = new StringBuilder();
//        for (ChartExtFilterRequest request : requestList) {
//            List<String> value = request.getValue();
//            if (CollectionUtils.isEmpty(value)) {
//                continue;
//            }
//            DatasetTableField field = request.getDatasetTableField();
//            filter.append(" AND ")
//                    .append(field.getOriginName())
//                    .append(" ")
//                    .append(transFilterTerm(request.getOperator()))
//                    .append(" ");
//            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
//                filter.append("('").append(StringUtils.join(value, "','")).append("')");
//            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
//                filter.append("'%").append(value.get(0)).append("%'");
//            } else {
//                filter.append("'").append(value.get(0)).append("'");
//            }
//        }
//        return filter.toString();
//    }
//}
