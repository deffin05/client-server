package com.example.processor;

import com.example.Package;

public interface Processor {
    void process(Package pkg) throws InterruptedException;
}
