package battleship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private AtomicInteger counter = new AtomicInteger(0);

    public int getValue() {
        return counter.get();
    }

    public int increment() {
        return counter.getAndIncrement();
    }
}