package io.dataease.service.spark;

import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.dto.chart.ChartViewFieldDTO;
import org.apache.commons.collections4.CollectionUtils;
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
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/26 3:49 下午
 */
@Service
public class SparkCalc {
    private static String column_family = "dataease";

    public List<String[]> getData(String hTable, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String tmpTable) throws Exception {
        Scan scan = new Scan();
        scan.addFamily(column_family.getBytes());
        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        String scanToString = new String(Base64.getEncoder().encode(proto.toByteArray()));

        JavaSparkContext sparkContext = CommonBeanFactory.getBean(JavaSparkContext.class);
        Configuration conf = CommonBeanFactory.getBean(Configuration.class);
        conf.set(TableInputFormat.INPUT_TABLE, hTable);
        conf.set(TableInputFormat.SCAN, scanToString);

        JavaPairRDD<ImmutableBytesWritable, Result> pairRDD = sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
        JavaRDD<Row> rdd = pairRDD.map((Function<Tuple2<ImmutableBytesWritable, Result>, Row>) immutableBytesWritableResultTuple2 ->
        {
            Result result = immutableBytesWritableResultTuple2._2;
            List<Object> list = new ArrayList<>();
            xAxis.forEach(x -> {
                if (x.getDeType() == 0 || x.getDeType() == 1) {
                    list.add(Bytes.toString(result.getValue(column_family.getBytes(), x.getOriginName().getBytes())));
                } else if (x.getDeType() == 2) {
                    String l = Bytes.toString(result.getValue(column_family.getBytes(), x.getOriginName().getBytes()));
                    if (StringUtils.isEmpty(l)) {
                        l = "0";
                    }
                    list.add(Long.valueOf(l));
                }
            });
            yAxis.forEach(y -> {
                if (y.getDeType() == 0 || y.getDeType() == 1) {
                    list.add(Bytes.toString(result.getValue(column_family.getBytes(), y.getOriginName().getBytes())));
                } else if (y.getDeType() == 2) {
                    String l = Bytes.toString(result.getValue(column_family.getBytes(), y.getOriginName().getBytes()));
                    if (StringUtils.isEmpty(l)) {
                        l = "0";
                    }
                    list.add(Long.valueOf(l));
                }
            });
            return RowFactory.create(list.toArray());
        });

        List<StructField> structFields = new ArrayList<>();
        // struct顺序要与rdd顺序一致
        xAxis.forEach(x -> {
            if (x.getDeType() == 0 || x.getDeType() == 1) {
                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.StringType, true));
            } else if (x.getDeType() == 2) {
                structFields.add(DataTypes.createStructField(x.getOriginName(), DataTypes.LongType, true));
            }
        });
        yAxis.forEach(y -> {
            if (y.getDeType() == 0 || y.getDeType() == 1) {
                structFields.add(DataTypes.createStructField(y.getOriginName(), DataTypes.StringType, true));
            } else if (y.getDeType() == 2) {
                structFields.add(DataTypes.createStructField(y.getOriginName(), DataTypes.LongType, true));
            }
        });
        StructType structType = DataTypes.createStructType(structFields);

        SQLContext sqlContext = CommonBeanFactory.getBean(SQLContext.class);
        Dataset<Row> dataFrame = sqlContext.createDataFrame(rdd, structType);
        dataFrame.createOrReplaceTempView(tmpTable);

        Dataset<Row> sql = sqlContext.sql(getSQL(xAxis, yAxis, tmpTable));

        List<String[]> data = new ArrayList<>();

        // transform
//        List<Row> list = sql.javaRDD().collect();
        List<Row> list = sql.collectAsList();
        for (Row row : list) {
            String[] r = new String[row.length()];
            for (int i = 0; i < row.length(); i++) {
                r[i] = row.get(i).toString();
            }
            data.add(r);
        }

//        Iterator<Row> rowIterator = sql.toLocalIterator();
//        while (rowIterator.hasNext()){
//            Row row = rowIterator.next();
//            String[] r = new String[row.length()];
//            for (int i = 0; i < row.length(); i++) {
//                r[i] = row.get(i).toString();
//            }
//            data.add(r);
//        }

        return data;
    }

    private String getSQL(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, String table) {
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
