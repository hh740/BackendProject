package com.codelab.hdfs;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class HDFSService {

	private Logger logger = LoggerFactory.getLogger(HDFSService.class);

	public String readFileFromHDFS(String path, int desLineNum) {

		try {

			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(new Path(path))) {

				logger.debug("filepath:{}", path);
				FSDataInputStream fis = fs.open(new Path(path));
				BufferedReader bis = new BufferedReader(new InputStreamReader(fis, "utf-8"));

				String line = null;
                int lineNum = 0;
				while (StringUtils.isNotBlank((line = bis.readLine()))) {
					logger.debug("line:{}", line);
					if (lineNum == desLineNum)
						return line;
					lineNum++;
				}
				logger.debug("line num is out of max length");
				return null;

			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("error happen ");
			return null;
		}
		logger.debug("file does not exist");
		return null;
	}
}
