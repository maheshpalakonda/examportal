//package com.exam.portal.service;
//
//import com.exam.portal.model.Job;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.LinkedBlockingQueue;
//
//@Service
//public class TaskQueue {
//    private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<>();
//
//    public void enqueue(Job job) {
//        queue.offer(job);
//    }
//
//    public Job dequeue() throws InterruptedException {
//        return queue.take();
//    }
//
//    public boolean isEmpty() {
//        return queue.isEmpty();
//    }
//
//    public int size() {
//        return queue.size();
//    }
//}
