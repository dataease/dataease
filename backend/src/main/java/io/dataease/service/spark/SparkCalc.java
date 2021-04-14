package io.dataease.service.spark;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.dto.chart.ChartViewFieldDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/26 3:49 下午
 */
@Service
public class SparkCalc {
    private static String column_family = "dataease";
    @Resource
    private Environment env; // 保存了配置文件的信息

    public List<String[]> getData(String hTable, List<DatasetTableField> fields, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String tmpTable) throws Exception {
        // Spark Context
        SparkSession spark = CommonBeanFactory.getBean(SparkSession.class);
        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());

        // Spark SQL Context
//        SQLContext sqlContext = CommonBeanFactory.getBean(SQLContext.class);
        SQLContext sqlContext = new SQLContext(sparkContext);
        sqlContext.setConf("spark.sql.shuffle.partitions", env.getProperty("spark.sql.shuffle.partitions", "1"));
        sqlContext.setConf("spark.default.parallelism", env.getProperty("spark.default.parallelism", "1"));

        Dataset<Row> dataFrame = CacheUtil.getInstance().getCacheData(hTable);
        if (ObjectUtils.isEmpty(dataFrame)) {
            dataFrame = getHBaseDataAndCache(sparkContext, sqlContext, hTable, fields);
        }

        dataFrame.createOrReplaceTempView(tmpTable);
        Dataset<Row> sql = sqlContext.sql(getSQL(xAxis, yAxis, tmpTable));
        // transform
        List<String[]> data = new ArrayList<>();
        List<Row> list = sql.collectAsList();
        for (Row row : list) {
            String[] r = new String[row.length()];
            for (int i = 0; i < row.length(); i++) {
                r[i] = row.get(i) == null ? "null" : row.get(i).toString();
            }
            data.add(r);
        }
        return data;
    }

    public Dataset<Row> getHBaseDataAndCache(String hTable, List<DatasetTableField> fields) throws Exception {
        // Spark Context
        SparkSession spark = CommonBeanFactory.getBean(SparkSession.class);
        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());

        // Spark SQL Context
//        SQLContext sqlContext = CommonBeanFactory.getBean(SQLContext.class);
        SQLContext sqlContext = new SQLContext(sparkContext);
        sqlContext.setConf("spark.sql.shuffle.partitions", env.getProperty("spark.sql.shuffle.partitions", "1"));
        sqlContext.setConf("spark.default.parallelism", env.getProperty("spark.default.parallelism", "1"));
        return getHBaseDataAndCache(sparkContext, sqlContext, hTable, fields);
    }

    public Dataset<Row> getHBaseDataAndCache(JavaSparkContext sparkContext, SQLContext sqlContext, String hTable, List<DatasetTableField> fields) throws Exception {
        Scan scan = new Scan();
        scan.addFamily(column_family.getBytes());
        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        String scanToString = new String(Base64.getEncoder().encode(proto.toByteArray()));

        // HBase config
//        Configuration conf = CommonBeanFactory.getBean(Configuration.class);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("hbase.zookeeper.quorum", env.getProperty("hbase.zookeeper.quorum"));
        conf.set("hbase.zookeeper.property.clientPort", env.getProperty("hbase.zookeeper.property.clientPort"));
        conf.set("hbase.client.retries.number", env.getProperty("hbase.client.retries.number", "1"));
        conf.set(TableInputFormat.INPUT_TABLE, hTable);
        conf.set(TableInputFormat.SCAN, scanToString);

        JavaPairRDD<ImmutableBytesWritable, Result> pairRDD = sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

        JavaRDD<Row> rdd = pairRDD.mapPartitions((FlatMapFunction<Iterator<Tuple2<ImmutableBytesWritable, Result>>, Row>) tuple2Iterator -> {
            List<Row> iterator = new ArrayList<>();
            while (tuple2Iterator.hasNext()) {
                Result result = tuple2Iterator.next()._2;
                List<Object> list = new ArrayList<>();
                fields.forEach(x -> {
                    String l = Bytes.toString(result.getValue(column_family.getBytes(), x.getOriginName().getBytes()));
                    if (x.getDeType() == 0 || x.getDeType() == 1) {
                        list.add(l);
                    } else if (x.getDeType() == 2) {
                        if (StringUtils.isEmpty(l)) {
                            l = "0";
                        }
                        list.add(Long.valueOf(l));
                    } else if (x.getDeType() == 3) {
                        if (StringUtils.isEmpty(l)) {
                            l = "0.0";
                        }
                        list.add(Double.valueOf(l));
                    }
                });
                iterator.add(RowFactory.create(list.toArray()));
            }
            return iterator.iterator();
        });

        List<StructField> structFields = new ArrayList<>();
        // struct顺序要与rdd顺序一致
        fields.forEach(x -> {
            if (x.getDeType() == 0 || x.getDeType() == 1) {
                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.StringType, true));
            } else if (x.getDeType() == 2) {
                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.LongType, true));
            } else if (x.getDeType() == 3) {
                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.DoubleType, true));
            }
        });
        StructType structType = DataTypes.createStructType(structFields);

        Dataset<Row> dataFrame = sqlContext.createDataFrame(rdd, structType).persist(StorageLevel.MEMORY_AND_DISK_SER());
        CacheUtil.getInstance().addCacheData(hTable, dataFrame);
        dataFrame.count();
        return dataFrame;
    }

    public String getSQL(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String table) {
        // 字段汇总 排序等
        String[] field = yAxis.stream().map(y -> "CAST(" + y.getSummary() + "(" + y.getOriginName() + ") AS DECIMAL(20,2)) AS _" + y.getSummary() + "_" + y.getOriginName()).toArray(String[]::new);
        String[] group = xAxis.stream().map(ChartViewFieldDTO::getOriginName).toArray(String[]::new);
        String[] order = yAxis.stream().filter(y -> StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none"))
                .map(y -> "_" + y.getSummary() + "_" + y.getOriginName() + " " + y.getSort()).toArray(String[]::new);

        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
                StringUtils.join(group, ","),
                StringUtils.join(field, ","),
                table,
                "",
                StringUtils.join(group, ","),
                StringUtils.join(order, ","));
        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        // 如果是对结果字段过滤，则再包裹一层sql
        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
                .map(y -> {
                    String[] s = y.getFilter().stream().map(f -> "AND _" + y.getSummary() + "_" + y.getOriginName() + transFilterTerm(f.getTerm()) + f.getValue()).toArray(String[]::new);
                    return StringUtils.join(s, " ");
                }).toArray(String[]::new);
        if (resultFilter.length == 0) {
            return sql;
        } else {
            String filterSql = MessageFormat.format("SELECT * FROM {0} WHERE 1=1 {1}",
                    "(" + sql + ") AS tmp",
                    StringUtils.join(resultFilter, " "));
            return filterSql;
        }
    }

    public String transFilterTerm(String term) {
        switch (term) {
            case "eq":
                return " = ";
            case "not_eq":
                return " <> ";
            case "lt":
                return " < ";
            case "le":
                return " <= ";
            case "gt":
                return " > ";
            case "ge":
                return " >= ";
            case "null":
                return " IS NULL ";
            case "not_null":
                return " IS NOT NULL ";
            default:
                return "";
        }
    }
}
