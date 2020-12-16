package com.youotech.utils.filesize;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class FileSize6ForkJoin {
	private final static ForkJoinPool forkJoinPool = new ForkJoinPool();
  
 
    private static class FileSizeFinder extends RecursiveTask<Long> {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final File file;
        public FileSizeFinder(final File theFile) {
            file = theFile;
        }
        @Override
        public Long compute() {
            long size = 0;
            if (file.isFile()) {
                size = file.length();
            } else {
                final File[] children = file.listFiles();
                if (children != null) {
                    List<ForkJoinTask<Long>> tasks = new ArrayList<ForkJoinTask<Long>>();
                    for (final File child : children) {
                        if (child.isFile()) {
                            size += child.length();
                        } else {
                            tasks.add(new FileSizeFinder(child));
                        }
                    }
                    for (final ForkJoinTask<Long> task : invokeAll(tasks)) {
                        size += task.join();
                    }
                }
            }
            return size;
        }
    }
    public static void main(final String[] args) {
    	String dirts = "E://winning//工作总结//2020三季度";
		dirts = "E://winning//";        
		final long start = System.nanoTime();
        final long total = forkJoinPool.invoke(new FileSizeFinder(new File(dirts)));
        final long end = System.nanoTime();
        System.out.println("Total Size: " + total);
        System.out.println("Time taken: " + (end - start) / 1.0e9);
    }

}
