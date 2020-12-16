package com.youotech.utils.filesize;

import java.io.File;

/**
 */
public class FileSizeMain {

	public static void main(final String[] args) {
		String dirts = "E://winning//工作总结//2020三季度";
		dirts = "E://winning//";
		final long start = System.nanoTime();
		final long total = new FileSize1DxcDg().getTotalSizeOfFilesInDir(new File(dirts));
		final long end = System.nanoTime();
		System.out.println("Total Size: " + total);
		System.out.println("Time taken: " + (end - start) / 1.0e9);
	}

}
