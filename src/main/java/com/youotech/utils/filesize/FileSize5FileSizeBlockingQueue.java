package com.youotech.utils.filesize;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 使用BlockingQueue和AtomicLong的实现
 */
public class FileSize5FileSizeBlockingQueue {
	private ExecutorService service;
    final private BlockingQueue<Long> fileSizes = new ArrayBlockingQueue<Long>(
            500);
    final AtomicLong pendingFileVisits = new AtomicLong();
    private void startExploreDir(final File file) {
        pendingFileVisits.incrementAndGet();
        service.execute(new Runnable() {
            public void run() {
                exploreDir(file);
            }
        });
    }
    private void exploreDir(final File file) {
        long fileSize = 0;
        if (file.isFile())
            fileSize = file.length();
        else {
            final File[] children = file.listFiles();
            if (children != null)
                for (final File child : children) {
                    if (child.isFile())
                        fileSize += child.length();
                    else {
                        startExploreDir(child);
                    }
                }
        }
        try {
            fileSizes.put(fileSize);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        pendingFileVisits.decrementAndGet();
    }
    private long getTotalSizeOfFile(final String fileName)
            throws InterruptedException {
        service = Executors.newFixedThreadPool(100);
        try {
            startExploreDir(new File(fileName));
            long totalSize = 0;
            while (pendingFileVisits.get() > 0 || fileSizes.size() > 0) {
                final Long size = fileSizes.poll(10, TimeUnit.SECONDS);
                totalSize += size;
            }
            return totalSize;
        } finally {
            service.shutdown();
        }
    }

	public static void main(final String[] args) throws InterruptedException  {
		String dirts = "E://winning//工作总结//2020三季度";
		dirts = "E://winning//";
		final long start = System.nanoTime();
		final long total = new FileSize5FileSizeBlockingQueue()
                .getTotalSizeOfFile(dirts);
		final long end = System.nanoTime();
		System.out.println("Total Size: " + total);
		System.out.println("Time taken: " + (end - start) / 1.0e9);
	}
}
