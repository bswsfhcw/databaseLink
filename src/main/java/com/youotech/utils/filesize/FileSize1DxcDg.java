package com.youotech.utils.filesize;
import java.io.File;

/**
 * 单线程递归
 */
public class FileSize1DxcDg {
	    // 递归方式 计算文件的大小
	    long getTotalSizeOfFilesInDir(final File file) {
	        if (file.isFile())
	            return file.length();
	        final File[] children = file.listFiles();
	        long total = 0;
	        if (children != null)
	            for (final File child : children)
	                total += getTotalSizeOfFilesInDir(child);
	        return total;
	    }
	    public static void main(final String[] args) {
	    	String dirts = "E://winning//工作总结//2020三季度";
	    	dirts = "C://Users//bswsfhcw//Documents//Visual Studio 2013";
	        final long start = System.nanoTime();
	        final long total = new FileSize1DxcDg()
	                .getTotalSizeOfFilesInDir(new File(dirts));
	        final long end = System.nanoTime();
	        System.out.println("Total Size: " + total);
	        System.out.println("Time taken: " + (end - start) / 1.0e9);
	    }

}
