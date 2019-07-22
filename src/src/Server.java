package src;
import java.io.*;
import java.util.Scanner;

public class Server {
    private static final int Q_LIMIT = 100;
    private static final int BUSY = 1;
    private static final  int IDLE = 0;
    private static int next_event_type;
    private static int num_custs_delayed;
    private static int num_events;
    private static int num_in_q;
    private static int server_status;
    private static double area_num_in_q;
    private static double area_server_status;
    private static double mean_interarrival;
    private static double mean_service;
    private static double time;
    private static double[] time_next_event = new double[3];
    private static double time_last_event;
    private static double[] time_arrival = new double[Q_LIMIT + 1];
    private static double total_of_delays;
    private static File infile = new File("C:\\Users\\Daviance\\IdeaProjects\\serversimulation\\src\\src\\file.in");

    public static void main(String []args) throws  IOException {
        num_events = 2;
        int num_delays_required;
        try (Scanner sc = new Scanner(infile)) {
            mean_interarrival = sc.nextDouble();
            mean_service = sc.nextDouble();
            num_delays_required = sc.nextInt();
        }
        PrintWriter outfile = new PrintWriter("file.out");
        outfile.printf("Single-server queuing system\n\n");
        outfile.printf("Mean interarrival %11.3f  minutes\n\n", mean_interarrival);
        outfile.printf("Number of customers %8d \n\n ", num_delays_required);
        outfile.close();
        initialise();
        while (num_custs_delayed < num_delays_required) {
            timing();
            update_time_avg_stats();
            switch (next_event_type) {
                case 1:
                    arrive();
                    break;
                case 2:
                    depart();
                    break;
            }

        }
        report();
    }

    private static void initialise() {
        time = 0.0;
        server_status = IDLE;
        num_in_q = 0;
        time_last_event = 0.0;
        num_custs_delayed = 0;
        total_of_delays = 0.0;
        area_num_in_q = 0.0;
        area_server_status = 0.0;
        time_next_event[1] = time + expo(mean_interarrival);
        time_next_event[2] = 1.0e+30;
    }

    private static void timing() throws  IOException {
        int i;
        double min_time_next_event = 1.0e+29;
        next_event_type = 0;
        FileWriter file = new FileWriter("file.out", true);
        PrintWriter outfile = new PrintWriter(file);
        for (i = 1; i <= num_events; ++i) {
            if (time_next_event[i] < min_time_next_event) {
                min_time_next_event = time_next_event[i];
                next_event_type = i;
            }
        }
        if (next_event_type == 0) {
            outfile.printf("\n Event list is empty at time %f", time);
            outfile.close();
            System.exit(1);
        }
        time = min_time_next_event;
    }

    private static void arrive() throws  IOException {
        double delay;
        time_next_event[1] = time + expo(mean_interarrival);
        FileWriter file = new FileWriter("file.out", true);
        PrintWriter outfile = new PrintWriter(file);
        if (server_status == BUSY) {
            num_in_q++;
            if (num_in_q > Q_LIMIT) {
                outfile.printf("\n Overflow of the array time_arrival at time %f", time);
                outfile.close();
                System.exit(2);
            }
            time_arrival[num_in_q] = time;
        } else {
            delay = 0.0;
            total_of_delays += delay;
            ++num_custs_delayed;
            server_status = BUSY;
            time_next_event[2] = time + expo(mean_service);
        }
    }

    private static void depart() {
        int i;
        double delay;
        if (num_in_q == 0) {
            server_status = IDLE;
            time_next_event[2] = 1.0e+30;
        } else {
            server_status = BUSY;
            --num_in_q;
            delay = time - time_arrival[1];
            total_of_delays += delay;
            ++num_custs_delayed;
            time_next_event[2] = time + expo(mean_service);
            for (i = 1; i <= num_in_q; ++i) {
                time_arrival[i] = time_arrival[i + 1];
            }
        }
    }

    private static void report() throws  IOException {
        FileWriter file = new FileWriter("file.out", true);
        PrintWriter outfile = new PrintWriter(file);
        outfile.printf("\n\n Average delay in queue %11.3f minutes \n", total_of_delays / num_custs_delayed);
        outfile.printf("Average number in queue %10.3f \n", area_num_in_q / time);
        outfile.printf("Server utilization %15.3f \n\n", area_server_status / time);
        outfile.printf("Time simulation ended %12.3f", time);
        outfile.close();
    }

    private static void update_time_avg_stats() {
        double time_since_last_event;
        time_since_last_event = time - time_last_event;
        time_last_event = time;
        area_num_in_q += num_in_q * time_since_last_event;
        area_server_status += server_status * time_since_last_event;
    }

    private static double expo(double mean) {
        return -mean * Math.log(Math.random());
    }
}
