package com.codelab.hbase.test;

import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:context/context-hbase.xml"})
public class HBASETest {

    private static Logger logger = LoggerFactory
            .getLogger(HBASETest.class);

    @Ignore
    @Test
    public void testHBASE() throws IOException {

        //使用配置文件创建
        Configuration conf = HBaseConfiguration.create();

        //创建表
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor descriptor = new HTableDescriptor();
        descriptor.addFamily(new HColumnDescriptor("family"));
        descriptor.setName("test3".getBytes());
        admin.createTable(descriptor);

       //获得表实例,方法一,直接根据配置获取
        HTable hTable = new HTable(conf, Bytes.toBytes("test3"));


        //方法二,从HConn中获取



        //单行插入
        Put put = new Put(Bytes.toBytes("row1"), 10L);
        put.addColumn(Bytes.toBytes("family"), Bytes.toBytes("name"), Bytes.toBytes("test1"));
        hTable.put(put);

        //批量插入

        List<Put> puts = new ArrayList<Put>();

        Put put1 = new Put(Bytes.toBytes("row2"), 10L);
        put1.addColumn(Bytes.toBytes("family"), Bytes.toBytes("name"), Bytes.toBytes("test2"));
        puts.add(put1);

        Put put2 = new Put(Bytes.toBytes("row3"), 10L);
        put2.addColumn(Bytes.toBytes("family"), Bytes.toBytes("name"), Bytes.toBytes("test3"));
        puts.add(put2);

        Put put3 = new Put(Bytes.toBytes("row4"), 10L);
        put3.addColumn(Bytes.toBytes("family"), Bytes.toBytes("name"), Bytes.toBytes("test4"));
        puts.add(put3);

        hTable.put(puts);

        //单行读取
        Get get = new Get(Bytes.toBytes("row2"));
        Result result = hTable.get(get);
        byte[] bytes = result.getValue(Bytes.toBytes("family"), Bytes.toBytes("name"));
        String str = Bytes.toString(bytes);
        System.out.print(str);


        //批量读取
        List<Get> gets = new ArrayList<Get>();
        Get get1 = new Get(Bytes.toBytes("row3"));
        gets.add(get1);
        Get get2 = new Get(Bytes.toBytes("row4"));
        gets.add(get2);

        Result[] results = hTable.get(gets);
        for (int i = 0; i < results.length; i++) {
            result = results[i];
            bytes = result.getValue(Bytes.toBytes("family"), Bytes.toBytes("name"));
            str = Bytes.toString(bytes);
            System.out.print(str);
        }

        //批量扫描
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes("family"), Bytes.toBytes("name"));
        //设置开始行和结束行
        scan.setStartRow(Bytes.toBytes("row1"));
        scan.setStopRow(Bytes.toBytes("row3"));
        //Set the maximum number of values to return for each call to next()
        //设置每次取得column size,一次rpc的请求传输多少columns,columns总数要和hbase列一致
        scan.setBatch(1);
        //Set the number of rows for caching that will be passed to scanners.
        //设置每次请求的记录数
        scan.setCaching(1);

        //设置是否缓存
        scan.setCacheBlocks(false);

        //设置过滤器,过滤器在RegionServer上进行执行
//        scan.setFilter(new PageFilter(100));


        ResultScanner scanRes = hTable.getScanner(scan);

        for (Result res : scanRes) {
            bytes = res.getValue(Bytes.toBytes("family"), Bytes.toBytes("name"));
            str = Bytes.toString(bytes);
            System.out.println("res: " + str);
        }


        //连接池操作

        //删除数据
        Delete del = new Delete(Bytes.toBytes("row1"));
        hTable.delete(del);

        //删除表
        admin.disableTable("test3");
        admin.deleteTable("test3");


    }
}
